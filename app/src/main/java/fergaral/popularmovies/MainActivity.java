package fergaral.popularmovies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MovieClickListener {

    private static final String SORT_ORDER_KEY = "sort_order";
    private static final String PAGE_KEY = "current_page";

    private RecyclerView mMoviesRecyclerView;
    private View mNoInternetView;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarPage;
    private MoviesAdapter mMoviesAdapter;
    private int numPages = Integer.MAX_VALUE;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PAGE_KEY))
                page = savedInstanceState.getInt(PAGE_KEY);
        }

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mNoInternetView = findViewById(R.id.ll_no_internet);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
        mProgressBarPage = (ProgressBar) findViewById(R.id.pb_load_page);

        int numColumns = getResources().getInteger(R.integer.movies_grid_num_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numColumns);

        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);

        mMoviesRecyclerView.setHasFixedSize(true);

        mMoviesAdapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);

        mMoviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0){ // only when scrolling up

                    final int visibleThreshold = 2;

                    GridLayoutManager layoutManager = (GridLayoutManager)mMoviesRecyclerView.getLayoutManager();
                    int lastItem  = layoutManager.findLastCompletelyVisibleItemPosition();
                    int currentTotalCount = layoutManager.getItemCount();

                    if(currentTotalCount <= lastItem + visibleThreshold){
                        if (page < numPages) {
                            URL url = NetworkUtils.getMoviesURL(
                                    PopularMoviesPreferences.getBoolean(MainActivity.this, SORT_ORDER_KEY),
                                    ++page
                            );
                            new FetchMoviesTask(true).execute(url);
                        }
                    }
                }
            }
        });

        boolean byPopularity = PopularMoviesPreferences.getBoolean(MainActivity.this,
                SORT_ORDER_KEY);
        new FetchMoviesTask(false).execute(NetworkUtils.getMoviesURL(byPopularity));
    }

    private void showMoviesView() {
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
        mNoInternetView.setVisibility(View.INVISIBLE);
    }

    private void showErrorView() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mNoInternetView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie, View view) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra(DetailActivity.EXTRA_MOVIE, movie);

        String transitionString = getString(R.string.movie_transition_string);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                view,
                transitionString
        );

        ActivityCompat.startActivity(this, detailIntent, options.toBundle());
    }

    public class FetchMoviesTask extends AsyncTask<URL, Void, List<Movie>> {
        private boolean mIsForLoadingNextPage;

        public FetchMoviesTask(boolean isForLoadingNextPage) {
            mIsForLoadingNextPage = isForLoadingNextPage;
        }

        @Override
        protected void onPreExecute() {
            if (!mIsForLoadingNextPage) {
                mProgressBar.setVisibility(View.VISIBLE);
                mMoviesRecyclerView.setVisibility(View.INVISIBLE);
            } else {
                mProgressBarPage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Movie> doInBackground(URL... urls) {
            URL url = urls[0];

            try {
                String jsonStr = NetworkUtils.getMoviesAsString(url);
                numPages = NetworkUtils.getMoviesNumPages(jsonStr);
                return NetworkUtils.parseMoviesJSON(jsonStr);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressBarPage.setVisibility(View.GONE);

            if (movies != null) {
                showMoviesView();
                mMoviesAdapter.addMovies(movies);
            } else {
                showErrorView();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort_order) {
            showSortOrderDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortOrderDialog() {
        String[] items = getResources().getStringArray(R.array.sort_order_names);
        boolean byPopularity = PopularMoviesPreferences.getBoolean(MainActivity.this,
                SORT_ORDER_KEY);

        int selectedItem = byPopularity ? 0 : 1;

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, selectedItem, null)
                .setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        PopularMoviesPreferences.putBoolean(MainActivity.this,
                                SORT_ORDER_KEY,
                                selectedPosition == 0);

                        mMoviesAdapter.clear();

                        //Refresh list
                        new FetchMoviesTask(false).execute(NetworkUtils.getMoviesURL(selectedPosition == 0));
                    }
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_KEY, page);
    }
}
