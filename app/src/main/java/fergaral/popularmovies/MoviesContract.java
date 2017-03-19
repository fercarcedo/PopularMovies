package fergaral.popularmovies;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Fer on 18/03/2017.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "fergaral.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    public static class MoviesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                .appendPath(PATH_MOVIES)
                                                .build();
        public static final String TABLE_NAME = "movies";
        public static final String ID_COLUMN = "id";
        public static final String TITLE_COLUMN = "title";
        public static final String POSTER_COLUMN = "poster";
        public static final String SYNOPSIS_COLUMN = "synopsis";
        public static final String RATING_COLUMN = "rating";
        public static final String RELEASE_DATE_COLUMN = "release_date";
    }
}
