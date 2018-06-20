package domain.hackathon.personal_assistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;

public class newsRecAdapter extends RecyclerView.Adapter<newsRecAdapter.ViewHolder> {
    private ArrayList<HashMap<String, String>> mDataset;
    private RequestManager glide;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, title;
        public ImageView image;
        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.news_image);
            name = (TextView) v.findViewById(R.id.news_name);
            title = (TextView) v.findViewById(R.id.news_title);
        }
    }

    public newsRecAdapter(ArrayList<HashMap<String, String>> mDataset, RequestManager glide){
        this.mDataset = mDataset;
        this.glide = glide;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_rec_layout, parent, false);

        newsRecAdapter.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, String> info;

        info = mDataset.get(position);
        if (info.get("urlToImage") != null && !info.get("urlToImage").isEmpty() && !info.get("urlToImage").equals("null"))
            glide.load(info.get("urlToImage")).into(holder.image);
        if (info.get("name") != null && !info.get("name").isEmpty() && !info.get("name").equals("null"))
            holder.name.setText(info.get("name"));
        else
            holder.name.setText("name not available");
        if (info.get("title") != null && !info.get("title").isEmpty() && !info.get("title").equals("null"))
            holder.title.setText(info.get("title"));
        else
            holder.title.setText("title not available");
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
