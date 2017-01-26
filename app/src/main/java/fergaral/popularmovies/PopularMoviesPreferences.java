package fergaral.popularmovies;

import android.content.Context;

/**
 * Created by fer on 22/01/17.
 */

public final class PopularMoviesPreferences {

    private static final String PREFS_NAME = "popular_movies_prefs";

    public static boolean getBoolean(Context context, String key) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getBoolean(key, false);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, value)
                .apply();
    }
}
