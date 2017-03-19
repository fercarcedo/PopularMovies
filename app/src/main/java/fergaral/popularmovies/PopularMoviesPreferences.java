package fergaral.popularmovies;

import android.content.Context;

/**
 * Created by fer on 22/01/17.
 */

public final class PopularMoviesPreferences {

    private static final String PREFS_NAME = "popular_movies_prefs";

    public static int getInt(Context context, String key) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getInt(key, 0);
    }

    public static void putInt(Context context, String key, int value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(key, value)
                .apply();
    }
}
