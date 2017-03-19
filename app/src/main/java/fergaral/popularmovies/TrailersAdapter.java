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

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private List<Video> mVideos;
    private final Context mContext;
    private final TrailerClickListener mListener;

    public interface TrailerClickListener {
        void onClick(Video video, View view);
    }

    public TrailersAdapter(Context context, TrailerClickListener listener) {
        this(null, context, listener);
    }

    public TrailersAdapter(List<Video> videos, Context context, TrailerClickListener listener) {
        mVideos = (videos != null) ? videos : new ArrayList<Video>();
        mContext = context;
        mListener = listener;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bind(position + 1);
    }

    @Override
    public int getItemCount() {
        if (mVideos == null) return 0;
        return mVideos.size();
    }

    public void addTrailers(List<Video> trailers) {
        mVideos.addAll(trailers);
        notifyDataSetChanged();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTrailer;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            tvTrailer = (TextView) itemView.findViewById(R.id.tv_trailer);
            itemView.setOnClickListener(this);
        }

        public void bind(int number) {
            tvTrailer.setText(mContext.getString(R.string.trailer_name, number));
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onClick(mVideos.get(getAdapterPosition()), v);
        }
    }
}
