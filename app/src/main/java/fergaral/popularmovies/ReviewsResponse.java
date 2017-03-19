package fergaral.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Fer on 17/03/2017.
 */

public class ReviewsResponse {
    @SerializedName("id")
    private long id;
    @SerializedName("page")
    private int page;
    @SerializedName("total_pages")
    private int numPages;
    @SerializedName("results")
    private List<Review> results;

    public ReviewsResponse(long id, int page, int numPages, List<Review> results) {
        this.id = id;
        this.page = page;
        this.numPages = numPages;
        this.results = results;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }

    public List<Review> getResults() {
        return results;
    }
}
