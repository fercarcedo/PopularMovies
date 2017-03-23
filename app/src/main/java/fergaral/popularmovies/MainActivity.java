package fergaral.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static fergaral.popularmovies.MainFragment.SORT_ORDER_KEY;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MovieClickListener {

    private static final String MAIN_FRAGMENT_KEY = "main_fragment";
    private static final String DETAIL_FRAGMENT_TAG = "detailFragment";
    private MainFragment mMainFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mMainFragment = (MainFragment) getSupportFragmentManager().getFragment(savedInstanceState, MAIN_FRAGMENT_KEY);
        } else {
            mMainFragment = new MainFragment();
        }

        if (findViewById(R.id.detail_fragment_container) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, mMainFragment)
                    .replace(R.id.detail_fragment_container, new DetailFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, mMainFragment)
                    .commit();
        }
    }

    @Override
    public void onClick(Movie movie, View view) {

        if (findViewById(R.id.detail_fragment_container) == null) {
            //Single pane
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailActivity.EXTRA_MOVIE, movie);

            String transitionString = getString(R.string.movie_transition_string);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    view,
                    transitionString
            );

            ActivityCompat.startActivity(this, detailIntent, options.toBundle());
        } else {
            //Side by side
            Bundle detailArgs = new Bundle();
            detailArgs.putParcelable(DetailActivity.EXTRA_MOVIE, movie);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(detailArgs);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        //Tint white sort order icon
        Drawable sortDrawable = menu.findItem(R.id.action_sort_order).getIcon();
        sortDrawable = DrawableCompat.wrap(sortDrawable);
        DrawableCompat.setTint(sortDrawable, ContextCompat.getColor(this, R.color.white));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_most_popular:
                PopularMoviesPreferences.putInt(MainActivity.this, SORT_ORDER_KEY, 0);
                mMainFragment.clearMovies();
                mMainFragment.loadMovies();
                clearDetail();
                break;
            case R.id.menu_sort_highest_rated:
                PopularMoviesPreferences.putInt(MainActivity.this, SORT_ORDER_KEY, 1);
                mMainFragment.clearMovies();
                mMainFragment.loadMovies();
                clearDetail();
                break;
            case R.id.menu_sort_favoritess:
                PopularMoviesPreferences.putInt(MainActivity.this, SORT_ORDER_KEY, 2);
                mMainFragment.clearMovies();
                mMainFragment.loadMovies();
                clearDetail();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearDetail() {
        if (findViewById(R.id.detail_fragment_container) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT_KEY, mMainFragment);
    }
}
