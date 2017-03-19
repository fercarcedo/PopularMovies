package fergaral.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Fer on 17/03/2017.
 */

public class VideosResponse {
    @SerializedName("id")
    private long id;
    @SerializedName("results")
    private List<Video> results;

    public VideosResponse(long id, List<Video> results) {
        this.id = id;
        this.results = results;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setResults(List<Video> results) {
        this.results = results;
    }

    public List<Video> getResults() {
        return results;
    }
}
