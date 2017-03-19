package fergaral.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fer on 18/03/2017.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> mReviews;
    private final Context mContext;

    public ReviewsAdapter(Context context) {
        this(null, context);
    }

    public ReviewsAdapter(List<Review> reviews, Context context) {
        mReviews = (reviews != null) ? reviews : new ArrayList<Review>();
        mContext = context;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = mReviews.get(position);
        holder.bind(review.getAuthor(), review.getContent());
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.size();
    }

    public void addReviews(List<Review> reviews) {
        mReviews.addAll(reviews);
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor;
        TextView tvContent;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_review_author);
            tvContent = (TextView) itemView.findViewById(R.id.tv_review_content);
        }

        public void bind(String author, String content) {
            tvAuthor.setText(mContext.getString(R.string.review_author_title, author));
            tvContent.setText(content);
        }
    }
}
