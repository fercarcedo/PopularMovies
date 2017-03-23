package fergaral.popularmovies;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Created by Fer on 19/03/2017.
 */

public class UIUtils {
    public static int calculateNoOfColumns(View view) {
        Log.d("hola", String.valueOf(view.getWidth()));
        float dpWidth = pxToDp(view.getContext(), view.getWidth());
        int scalingFactor = 180;
        return (int) (dpWidth / scalingFactor);
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
