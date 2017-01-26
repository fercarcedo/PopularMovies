package fergaral.popularmovies;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by fer on 21/01/17.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/";
    private static final String MOVIES_PATH = "movie";
    private static final String POPULAR_MOVIES = "popular";
    private static final String TOP_RATED_MOVIES = "top_rated";
    private static final String API_KEY_KEY = "api_key";
    private static final String API_KEY_VALUE = "a85b53b10b81feed4c91c1eab93dd402";
    private static final String PAGE_KEY = "page";

    public static URL getMoviesURL(boolean byPopularity) {
        return getMoviesURL(byPopularity, 1);
    }

    public static URL getMoviesURL(boolean byPopularity, int numPage) {
        Uri uri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendPath(MOVIES_PATH)
                .appendPath(byPopularity ? POPULAR_MOVIES : TOP_RATED_MOVIES)
                .appendQueryParameter(API_KEY_KEY, API_KEY_VALUE)
                .appendQueryParameter(PAGE_KEY, String.valueOf(numPage))
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to build movies URL");
        }

        return url;
    }

    public static String getMoviesAsString(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream is = urlConnection.getInputStream();

            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext())
                return scanner.next();
        } finally {
            urlConnection.disconnect();
        }

        return null;
    }

    public static List<Movie> parseMoviesJSON(String moviesJsonStr) {
        try {
            JSONObject moviesObject = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesObject.getJSONArray("results");

            List<Movie> movies = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject movieObj = resultsArray.getJSONObject(i);

                String posterPath = "http://image.tmdb.org/t/p/w185" + movieObj.getString("poster_path");
                String originalTitle = movieObj.getString("original_title");
                String synopsis = movieObj.getString("overview");
                double rating = movieObj.getDouble("vote_average");
                Date releaseDate = dateFormat.parse(movieObj.getString("release_date"));
                movies.add(new Movie(originalTitle, posterPath, synopsis, rating, releaseDate));
            }

            return movies;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error parsing movies JSON string");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "Error parsing release date");
        }
        return null;
    }

    public static int getMoviesNumPages(String moviesJsonStr) {
        try {
            JSONObject moviesObject = new JSONObject(moviesJsonStr);
            return moviesObject.getInt("total_pages");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
