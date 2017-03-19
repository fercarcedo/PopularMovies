package fergaral.popularmovies;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Fer on 17/03/2017.
 */

public class MovieDBAPIClient {
    private static final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/";
    public static final String IMAGE_REPOSITORY_URL = "http://image.tmdb.org/t/p/w185";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(MOVIES_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
