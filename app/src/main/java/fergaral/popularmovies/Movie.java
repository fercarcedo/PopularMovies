package fergaral.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by fer on 21/01/17.
 */

public class Movie implements Parcelable {
    @SerializedName("id")
    private long id;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("poster_path")
    private String imageThumbnail;
    @SerializedName("overview")
    private String plotSynopsis;
    @SerializedName("vote_average")
    private double rating;
    @SerializedName("release_date")
    private Date releaseDate;

    public Movie(long id, String originalTitle, String imageThumbnail, String plotSynopsis,
                                                        double rating, Date releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.imageThumbnail = imageThumbnail;
        this.plotSynopsis = plotSynopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public Movie(Parcel in) {
        id = in.readLong();
        originalTitle = in.readString();
        imageThumbnail = in.readString();
        plotSynopsis = in.readString();
        rating = in.readDouble();
        releaseDate = (Date) in.readSerializable();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public double getRating() {
        return rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(originalTitle);
        parcel.writeString(imageThumbnail);
        parcel.writeString(plotSynopsis);
        parcel.writeDouble(rating);
        parcel.writeSerializable(releaseDate);
    }
}
