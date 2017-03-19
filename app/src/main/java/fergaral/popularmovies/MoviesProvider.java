package fergaral.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Fer on 18/03/2017.
 */

public class MoviesProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbOpenHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Uri for the entire movies repository
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, CODE_MOVIES);
        //Uri for a single movie, given its unique ID
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", CODE_MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriMatch = sUriMatcher.match(uri);
        Cursor cursor;

        switch(uriMatch) {
            case CODE_MOVIES:
                cursor = mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIES_WITH_ID:
                String idStr = uri.getLastPathSegment();

                String[] selectionArguments = new String[] { idStr };

                cursor = mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.MoviesEntry.ID_COLUMN + "=?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            default: throw new IllegalArgumentException("Invalid uri for querying movies");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case CODE_MOVIES:
                long id = mOpenHelper.getWritableDatabase().insert(MoviesContract.MoviesEntry.TABLE_NAME,
                        null,
                        values);

                return ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI, id);
            default:
                throw new IllegalArgumentException("Invalid uri for inserting movies");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case CODE_MOVIES:
                return mOpenHelper.getWritableDatabase().delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
            case CODE_MOVIES_WITH_ID:
                String idStr = uri.getLastPathSegment();

                return mOpenHelper.getWritableDatabase().delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        MoviesContract.MoviesEntry.ID_COLUMN + "=?",
                        new String[] { idStr });
            default:
                throw new IllegalArgumentException("Invalid uri for deleting movies");
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
