package fergaral.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fer on 18/03/2017.
 */

public class MoviesDbOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    public MoviesDbOpenHelper(Context context) {
        super(context, MoviesContract.MoviesEntry.TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE = "CREATE TABLE " + MoviesContract.MoviesEntry.TABLE_NAME + "(" +
                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.MoviesEntry.ID_COLUMN + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.TITLE_COLUMN + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.POSTER_COLUMN + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.RATING_COLUMN + " REAL NOT NULL, " +
                MoviesContract.MoviesEntry.RELEASE_DATE_COLUMN + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.SYNOPSIS_COLUMN + " TEXT NOT NULL, " +
                "UNIQUE (" + MoviesContract.MoviesEntry.ID_COLUMN + ") ON CONFLICT REPLACE);";
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DROP = "DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME + ";";
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}
