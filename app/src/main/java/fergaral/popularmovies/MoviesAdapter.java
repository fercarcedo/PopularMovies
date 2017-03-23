package fergaral.popularmovies;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fer on 21/01/17.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {
    private ArrayList<Movie> mMovies;
    private final MovieClickListener mListener;

    public interface MovieClickListener {
        void onClick(Movie movie, View view);
    }

    public MoviesAdapter(MovieClickListener listener) {
        this(null, listener);
    }

    public MoviesAdapter(ArrayList<Movie> movies, MovieClickListener listener) {
        mMovies = (movies != null) ? movies : new ArrayList<Movie>();
        mListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_item, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(mMovies.get(position));
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public void addMovies(List<Movie> movies) {
        mMovies.addAll(movies);
        notifyDataSetChanged();
        /*if (mListener != null && !movies.isEmpty()) {
            Log.d("TAG", String.valueOf(movies.size()));
            mListener.onClick(movies.get(0), null);
        }*/
    }

    public void setMovies(List<Movie> movies) {
        mMovies = new ArrayList<>(movies);
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    public void clear() {
        mMovies.clear();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mMoviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mMoviePosterImageView = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        public void bind(Movie movie) {
            Picasso.with(mMoviePosterImageView.getContext())
                    .load(MovieDBAPIClient.IMAGE_REPOSITORY_URL + movie.getImageThumbnail())
                    .placeholder(new ColorDrawable(
                                    ContextCompat.getColor(mMoviePosterImageView.getContext(),
                                            R.color.white)))
                    .into(mMoviePosterImageView);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null)
                mListener.onClick(mMovies.get(getAdapterPosition()), mMoviePosterImageView);
        }
    }
}
