package fergaral.popularmovies;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Fer on 17/03/2017.
 */

public class Video {
    @SerializedName("key")
    private String key;

    public Video(String key) {
        this.key = key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
