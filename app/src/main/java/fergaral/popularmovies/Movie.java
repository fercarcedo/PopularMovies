package fergaral.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by fer on 21/01/17.
 */

public class Movie implements Parcelable {
    private String originalTitle;
    private String imageThumbnail;
    private String plotSynopsis;
    private double rating;
    private Date releaseDate;

    public Movie(String originalTitle, String imageThumbnail, String plotSynopsis,
                                                        double rating, Date releaseDate) {
        this.originalTitle = originalTitle;
        this.imageThumbnail = imageThumbnail;
        this.plotSynopsis = plotSynopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    protected Movie(Parcel in) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(imageThumbnail);
        parcel.writeString(plotSynopsis);
        parcel.writeDouble(rating);
        parcel.writeSerializable(releaseDate);
    }
}
