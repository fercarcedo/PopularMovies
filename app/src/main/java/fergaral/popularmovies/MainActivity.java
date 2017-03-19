package fergaral.popularmovies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MovieClickListener,
                                                                    LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SORT_ORDER_KEY = "sort_order";
    private static final String PAGE_KEY = "current_page";
    private static final int MOVIES_LOADER_ID = 20;

    private RecyclerView mMoviesRecyclerView;
    private View mNoInternetView;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarPage;
    private MoviesAdapter mMoviesAdapter;
    private TextView mNoMoviesTextView;
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
        mNoMoviesTextView = (TextView) findViewById(R.id.tv_no_movies);

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
                            int selectedItem = PopularMoviesPreferences.getInt(MainActivity.this, SORT_ORDER_KEY);

                            if (selectedItem != 2) {
                                fetchMoviesAsync(selectedItem == 0,
                                        ++page);
                            }
                        }
                    }
                }
            }
        });

        loadMovies();
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
        final int selectedItem = PopularMoviesPreferences.getInt(MainActivity.this,
                SORT_ORDER_KEY);

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, selectedItem, null)
                .setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        PopularMoviesPreferences.putInt(MainActivity.this,
                                SORT_ORDER_KEY,
                                selectedPosition);

                        mMoviesAdapter.clear();

                        loadMovies();
                    }
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_KEY, page);
    }

    private void fetchMoviesAsync(boolean byPopularity) {
        fetchMoviesAsync(byPopularity, 1);
    }

    private void fetchMoviesAsync(boolean byPopularity, int page) {
        onPreFetchMovies(page > 1);
        MovieDBService service = MovieDBAPIClient.getClient().create(MovieDBService.class);

        Call<MoviesResponse> moviesResponse;
        if (byPopularity)
            moviesResponse = service.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_KEY, page);
        else
            moviesResponse = service.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_KEY, page);

        moviesResponse.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBarPage.setVisibility(View.GONE);

                if (movies != null) {
                    showMoviesView();
                    numPages = response.body().getTotalPages();
                    mMoviesAdapter.addMovies(movies);

                    if (movies.isEmpty())
                        showNoMoviesView();
                } else {
                    showErrorView();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBarPage.setVisibility(View.GONE);
                showErrorView();
            }
        });
    }

    private void onPreFetchMovies(boolean isForLoadingNextPage) {
        mNoMoviesTextView.setVisibility(View.INVISIBLE);
        if (!isForLoadingNextPage) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBarPage.setVisibility(View.VISIBLE);
        }
    }

    public void retry(View view) {
        findViewById(R.id.ll_no_internet).setVisibility(View.INVISIBLE);
        loadMovies();
    }

    private void loadMovies() {
        int selectedPosition = PopularMoviesPreferences.getInt(this, SORT_ORDER_KEY);

        if (selectedPosition == 2) {
            //Favorites, so no need to query API, so start CursorLoader
            onPreFetchMovies(false);
            getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            fetchMoviesAsync(selectedPosition == 0);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                MoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBarPage.setVisibility(View.GONE);
        if (data != null) {
            List<Movie> movies = new ArrayList<>(data.getCount());
            while (data.moveToNext()) {
                long id = data.getLong(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.ID_COLUMN));
                String title = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.TITLE_COLUMN));
                String imageThumbnail = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.POSTER_COLUMN));
                String synopsis = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.SYNOPSIS_COLUMN));
                double rating = data.getDouble(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.RATING_COLUMN));
                long releaseDate = data.getLong(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.RELEASE_DATE_COLUMN));
                movies.add(new Movie(id, title, imageThumbnail, synopsis, rating, new Date(releaseDate)));
            }

            if (movies.isEmpty()) {
                numPages = 0;
                showNoMoviesView();
            } else {
                showMoviesView();
                numPages = 1; //Only one page, since there is no API request
                mMoviesAdapter.addMovies(movies);
            }
        } else {
            numPages = 0;
            showNoMoviesView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showNoMoviesView() {
        mNoMoviesTextView.setVisibility(View.VISIBLE);
    }
}
