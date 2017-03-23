package fergaral.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment implements MoviesAdapter.MovieClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String SORT_ORDER_KEY = "sort_order";
    private static final String PAGE_KEY = "current_page";
    private static final int MOVIES_LOADER_ID = 20;
    private static final String RV_POSITION_KEY_INSTANCE = "rv_position_instance" ;
    private static final String MOVIES_LIST_KEY = "movies_list";

    private RecyclerView mMoviesRecyclerView;
    private View mNoInternetView;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarPage;
    private MoviesAdapter mMoviesAdapter;
    private TextView mNoMoviesTextView;
    private int numPages = Integer.MAX_VALUE;
    private int page = 1;
    private RecyclerView.LayoutManager mLayoutManager;
    private Parcelable mLayoutManagerSavedState;
    private ArrayList<Movie> mMovies;
    private MoviesAdapter.MovieClickListener mMovieClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviesRecyclerView = (RecyclerView) view.findViewById(R.id.rv_movies);
        mNoInternetView = view.findViewById(R.id.ll_no_internet);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        mProgressBarPage = (ProgressBar) view.findViewById(R.id.pb_load_page);
        mNoMoviesTextView = (TextView) view.findViewById(R.id.tv_no_movies);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().post(new Runnable() {
            @Override
            public void run() {
                int numColumns = UIUtils.calculateNoOfColumns(getView());
                mLayoutManager = new GridLayoutManager(getActivity(), numColumns);
                mMoviesRecyclerView.setLayoutManager(mLayoutManager);
                mMoviesRecyclerView.setHasFixedSize(true);
                mMoviesAdapter = new MoviesAdapter(MainFragment.this);
                mMoviesRecyclerView.setAdapter(mMoviesAdapter);

                if (mMovies == null || mMovies.isEmpty())
                    loadMovies();
                else {
                    addMovies(mMovies);
                    if (mLayoutManagerSavedState != null)
                        mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
                }
            }
        });


        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PAGE_KEY))
                page = savedInstanceState.getInt(PAGE_KEY);
            if (savedInstanceState.containsKey(RV_POSITION_KEY_INSTANCE))
                mLayoutManagerSavedState = savedInstanceState.getParcelable(RV_POSITION_KEY_INSTANCE);
            if (savedInstanceState.containsKey(MOVIES_LIST_KEY))
                mMovies = savedInstanceState.getParcelableArrayList(MOVIES_LIST_KEY);
        }


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
                            int selectedItem = PopularMoviesPreferences.getInt(getActivity(), SORT_ORDER_KEY);

                            if (selectedItem != 2) {
                                fetchMoviesAsync(selectedItem == 0,
                                        ++page);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMovieClickListener = (MoviesAdapter.MovieClickListener) context;
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
        if (mMovieClickListener != null) //Delegate control to Activity
            mMovieClickListener.onClick(movie, view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RV_POSITION_KEY_INSTANCE, mLayoutManager.onSaveInstanceState());
        outState.putInt(PAGE_KEY, page);
        outState.putParcelableArrayList(MOVIES_LIST_KEY, mMovies);
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
                if (response.body().getResults() == null)
                    mMovies = null;
                else
                    mMovies = new ArrayList<>(response.body().getResults());

                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBarPage.setVisibility(View.GONE);

                if (mMovies != null) {
                    showMoviesView();
                    numPages = response.body().getTotalPages();
                    addMovies(mMovies);

                    if (mMovies.isEmpty())
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
        getView().findViewById(R.id.ll_no_internet).setVisibility(View.INVISIBLE);
        loadMovies();
    }

    public void loadMovies() {
        int selectedPosition = PopularMoviesPreferences.getInt(getActivity(), SORT_ORDER_KEY);

        if (selectedPosition == 2) {
            //Favorites, so no need to query API, so start CursorLoader
            onPreFetchMovies(false);
            getActivity().getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            fetchMoviesAsync(selectedPosition == 0);
        }
    }

    public void clearMovies() {
        mMoviesAdapter.clear();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
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
            mMovies = new ArrayList<>(data.getCount());
            while (data.moveToNext()) {
                long id = data.getLong(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.ID_COLUMN));
                String title = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.TITLE_COLUMN));
                String imageThumbnail = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.POSTER_COLUMN));
                String synopsis = data.getString(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.SYNOPSIS_COLUMN));
                double rating = data.getDouble(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.RATING_COLUMN));
                long releaseDate = data.getLong(data.getColumnIndexOrThrow(MoviesContract.MoviesEntry.RELEASE_DATE_COLUMN));
                mMovies.add(new Movie(id, title, imageThumbnail, synopsis, rating, new Date(releaseDate)));
            }

            if (mMovies.isEmpty()) {
                numPages = 0;
                showNoMoviesView();
            } else {
                showMoviesView();
                numPages = 1; //Only one page, since there is no API request
            }

            if (PopularMoviesPreferences.getInt(getActivity(), SORT_ORDER_KEY) == 2)
                mMoviesAdapter.setMovies(mMovies);
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

    private void addMovies(List<Movie> movies) {
        mMoviesAdapter.addMovies(movies);
        mMovies = mMoviesAdapter.getMovies();
    }
}
