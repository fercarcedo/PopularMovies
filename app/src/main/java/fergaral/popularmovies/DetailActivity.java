package fergaral.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements TrailersAdapter.TrailerClickListener {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private ImageView mIvMoviePoster;
    private TextView mTvReleaseDate;
    private TextView mTvPlotSynopsis;
    private AppCompatRatingBar mRbRating;

    private RecyclerView mRvTrailers;
    private RecyclerView mRvReviews;

    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;

    private ProgressBar mTrailersProgBar;
    private ProgressBar mReviewsProgBar;

    private TextView mTvErrorReviews;
    private TextView mTvErrorTrailers;
    private TextView mTvNoReviews;
    private TextView mTvNoTrailers;

    private Movie mMovie;
    private Menu menu;
    private String mFirstTrailer;

    private int reviewsPage = 1;
    private int reviewsNumPages = Integer.MAX_VALUE;
    private ProgressBar mLoadReviewsPageProgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mIvMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        mTvReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mTvPlotSynopsis = (TextView) findViewById(R.id.tv_plot_synopsis);
        mRbRating = (AppCompatRatingBar) findViewById(R.id.rb_rating);
        mRvTrailers = (RecyclerView) findViewById(R.id.rv_trailers);
        mRvReviews = (RecyclerView) findViewById(R.id.rv_reviews);
        mTrailersProgBar = (ProgressBar) findViewById(R.id.pb_load_trailers);
        mReviewsProgBar = (ProgressBar) findViewById(R.id.pb_load_reviews);
        mTvErrorReviews = (TextView) findViewById(R.id.tv_error_reviews);
        mTvErrorTrailers = (TextView) findViewById(R.id.tv_error_trailers);
        mTvNoReviews = (TextView) findViewById(R.id.tv_no_reviews);
        mTvNoTrailers = (TextView) findViewById(R.id.tv_no_trailers);
        mLoadReviewsPageProgBar = (ProgressBar) findViewById(R.id.pb_load_reviews_page);

        mRvTrailers.setLayoutManager(new LinearLayoutManager(this));
        mRvReviews.setLayoutManager(new LinearLayoutManager(this));
        mTrailersAdapter = new TrailersAdapter(this, this);
        mReviewsAdapter = new ReviewsAdapter(this);
        mRvTrailers.setAdapter(mTrailersAdapter);
        mRvReviews.setAdapter(mReviewsAdapter);

        mRvReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0){ // only when scrolling up

                    final int visibleThreshold = 2;

                    GridLayoutManager layoutManager = (GridLayoutManager)mRvReviews.getLayoutManager();
                    int lastItem  = layoutManager.findLastCompletelyVisibleItemPosition();
                    int currentTotalCount = layoutManager.getItemCount();

                    if(currentTotalCount <= lastItem + visibleThreshold){
                        if (reviewsPage < reviewsNumPages) {
                            fetchReviewsAsync(++reviewsPage);
                        }
                    }
                }
            }
        });

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MOVIE)) {
            Movie movie = intent.getParcelableExtra(EXTRA_MOVIE);
            mMovie = movie;

            showMovie(movie);

            MovieDBService movieService = MovieDBAPIClient.getClient().create(MovieDBService.class);

            onPrefetchTrailers();
            Call<VideosResponse> trailersResponse = movieService.getTrailers(movie.getId(), BuildConfig.THE_MOVIE_DB_API_KEY);
            trailersResponse.enqueue(new Callback<VideosResponse>() {
                @Override
                public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                    mTrailersProgBar.setVisibility(View.INVISIBLE);
                    mTvErrorTrailers.setVisibility(View.INVISIBLE);

                    if (!response.body().getResults().isEmpty()) {
                        menu.findItem(R.id.action_share).setVisible(true);
                        mFirstTrailer = getString(R.string.base_trailer_url, response.body().getResults().get(0).getKey());
                        mRvTrailers.setVisibility(View.VISIBLE);
                        mTrailersAdapter.addTrailers(response.body().getResults());
                    } else {
                        mTvNoTrailers.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<VideosResponse> call, Throwable t) {
                    mTrailersProgBar.setVisibility(View.INVISIBLE);
                    mTvErrorTrailers.setVisibility(View.VISIBLE);
                }
            });

            onPrefetchReviews();
            Call<ReviewsResponse> reviewsResponse = movieService.getReviews(movie.getId(), BuildConfig.THE_MOVIE_DB_API_KEY);
            reviewsResponse.enqueue(new Callback<ReviewsResponse>() {
                @Override
                public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                    mReviewsProgBar.setVisibility(View.INVISIBLE);
                    mTvErrorReviews.setVisibility(View.INVISIBLE);

                    if (!response.body().getResults().isEmpty()) {
                        reviewsNumPages = response.body().getNumPages();
                        mRvReviews.setVisibility(View.VISIBLE);
                        mReviewsAdapter.addReviews(response.body().getResults());
                    } else {
                        reviewsNumPages = 0;
                        mTvNoReviews.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                    reviewsNumPages = 0;
                    mReviewsProgBar.setVisibility(View.INVISIBLE);
                    mTvErrorReviews.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void showMovie(Movie movie) {
        Picasso.with(this)
                .load(MovieDBAPIClient.IMAGE_REPOSITORY_URL + movie.getImageThumbnail())
                .into(mIvMoviePoster);

        getSupportActionBar().setTitle(movie.getOriginalTitle());
        mTvReleaseDate.setText(DateFormat.getDateInstance().format(movie.getReleaseDate()));
        mTvPlotSynopsis.setText(movie.getPlotSynopsis());
        mRbRating.setRating((float) (movie.getRating() / 2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem favItem = menu.findItem(R.id.action_fav);
        setWhiteDrawable(favItem, getFavStarResource());
        MenuItem shareItem = menu.findItem(R.id.action_share);
        setWhiteDrawable(shareItem, R.drawable.ic_share_black_24dp);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fav:
                toggleFavIcon(item);
                break;
            case R.id.action_share:
                shareFirstTrailer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavIcon(MenuItem item) {
        int favIconRes = getFavStarResource();

        int newFavIconRes;
        if (favIconRes == R.drawable.ic_star_black_24dp) {
            //Delete from provider
            Uri uri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI, mMovie.getId());
            getContentResolver().delete(uri, null, null);
            newFavIconRes = R.drawable.ic_star_border_black_24dp;
        } else {
            //Insert into provider
            ContentValues values = new ContentValues();
            values.put(MoviesContract.MoviesEntry.ID_COLUMN, mMovie.getId());
            values.put(MoviesContract.MoviesEntry.TITLE_COLUMN, mMovie.getOriginalTitle());
            values.put(MoviesContract.MoviesEntry.POSTER_COLUMN, mMovie.getImageThumbnail());
            values.put(MoviesContract.MoviesEntry.RATING_COLUMN, mMovie.getRating());
            values.put(MoviesContract.MoviesEntry.RELEASE_DATE_COLUMN, mMovie.getReleaseDate().getTime());
            values.put(MoviesContract.MoviesEntry.SYNOPSIS_COLUMN, mMovie.getPlotSynopsis());

            getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, values);
            newFavIconRes = R.drawable.ic_star_black_24dp;
        }
        Drawable favIconFull = ContextCompat.getDrawable(this, newFavIconRes);
        favIconFull = DrawableCompat.wrap(favIconFull);
        DrawableCompat.setTint(favIconFull, ContextCompat.getColor(this, R.color.white));
        item.setIcon(favIconFull);
    }

    @Override
    public void onClick(Video video, View view) {
        Intent trailerIntent = new Intent(Intent.ACTION_VIEW);
        trailerIntent.setData(Uri.parse(getString(R.string.base_trailer_url, video.getKey())));
        startActivity(trailerIntent);
    }

    private void onPrefetchTrailers() {
        mTvErrorTrailers.setVisibility(View.INVISIBLE);
        mTvNoTrailers.setVisibility(View.INVISIBLE);
        mTrailersProgBar.setVisibility(View.VISIBLE);
        mRvTrailers.setVisibility(View.INVISIBLE);
    }

    private void onPrefetchReviews() {
        mTvErrorReviews.setVisibility(View.INVISIBLE);
        mTvNoReviews.setVisibility(View.INVISIBLE);
        mReviewsProgBar.setVisibility(View.VISIBLE);
        mRvReviews.setVisibility(View.INVISIBLE);
    }

    private int getFavStarResource() {
        //If there is a record in the favs database, is a filled star.
        //Otherwise, it's simply a star with only the border
        Uri uri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI, mMovie.getId());
        Cursor cursor = getContentResolver().query(uri,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return R.drawable.ic_star_black_24dp;
        }

        if (cursor != null)
            cursor.close();

        return R.drawable.ic_star_border_black_24dp;
    }

    private void setWhiteDrawable(MenuItem menuItem, int drawableRes) {
        Drawable favDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, drawableRes));
        DrawableCompat.setTint(favDrawable, ContextCompat.getColor(this, R.color.white));
        menuItem.setIcon(favDrawable);
    }

    private void shareFirstTrailer() {
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mFirstTrailer)
                .startChooser();
    }

    private void fetchReviewsAsync(int numPage) {
        mLoadReviewsPageProgBar.setVisibility(View.VISIBLE);
        MovieDBService movieService = MovieDBAPIClient.getClient().create(MovieDBService.class);
        Call<ReviewsResponse> reviewsResponse = movieService.getReviews(mMovie.getId(), numPage, BuildConfig.THE_MOVIE_DB_API_KEY);
        reviewsResponse.enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                mLoadReviewsPageProgBar.setVisibility(View.GONE);
                mReviewsProgBar.setVisibility(View.INVISIBLE);
                mTvErrorReviews.setVisibility(View.INVISIBLE);

                if (!response.body().getResults().isEmpty()) {
                    mRvReviews.setVisibility(View.VISIBLE);
                    mReviewsAdapter.addReviews(response.body().getResults());
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                mLoadReviewsPageProgBar.setVisibility(View.GONE);
                mReviewsProgBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
