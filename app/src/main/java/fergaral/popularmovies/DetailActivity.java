package fergaral.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private ImageView mIvMoviePoster;
    private TextView mTvReleaseDate;
    private TextView mTvPlotSynopsis;
    private AppCompatRatingBar mRbRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mIvMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        mTvReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mTvPlotSynopsis = (TextView) findViewById(R.id.tv_plot_synopsis);
        mRbRating = (AppCompatRatingBar) findViewById(R.id.rb_rating);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MOVIE)) {
            Movie movie = intent.getParcelableExtra(EXTRA_MOVIE);

            showMovie(movie);
        }
    }

    private void showMovie(Movie movie) {
        Picasso.with(this)
                .load(movie.getImageThumbnail())
                .into(mIvMoviePoster);

        getSupportActionBar().setTitle(movie.getOriginalTitle());
        mTvReleaseDate.setText(DateFormat.getDateInstance().format(movie.getReleaseDate()));
        mTvPlotSynopsis.setText(movie.getPlotSynopsis());
        mRbRating.setRating((float) (movie.getRating() / 2));
    }
}
