package domain.hackathon.personal_assistant;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by darkness7245 on 3/14/2018.
 */
public class recAdapter extends RecyclerView.Adapter<recAdapter.ViewHolder> {
    private List<String> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView, tittle, snippet, url;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.txtlistinfo);
            tittle = (TextView) v.findViewById(R.id.txttittle);
            snippet = (TextView) v.findViewById(R.id.txtsnippet);
            url = (TextView) v.findViewById(R.id.txturl);
            //uses this recaadapter for two avtivties
            if (Homescreen_nav.whichlayout == "notes"){
                // if it is the notes activity use the ui needed for the notes activity
                mTextView.setVisibility(View.VISIBLE);
                tittle.setVisibility(View.INVISIBLE);
                snippet.setVisibility(View.INVISIBLE);
                url.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) snippet.getLayoutParams();
                params.topMargin = 0;
                params.alignWithParent = true;
                snippet.setLayoutParams(params);
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) url.getLayoutParams();
                params2.topMargin = 0;
                params2.alignWithParent = true;
                snippet.setLayoutParams(params2);
            }
            else if (Homescreen_nav.whichlayout == "google")
            {
                // if it is the google activity use the ui needed for the notes activity
                mTextView.setVisibility(View.INVISIBLE);
                tittle.setVisibility(View.VISIBLE);
                snippet.setVisibility(View.VISIBLE);
                url.setVisibility(View.VISIBLE);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public recAdapter(List<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public recAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reclayout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        String info = mDataset.get(position);
        if (Homescreen_nav.whichlayout == "notes")
            holder.mTextView.setText(info);
        else if (Homescreen_nav.whichlayout == "google")
        {
            position = position * 3;
            holder.tittle.setText(mDataset.get(position));
            holder.snippet.setText(mDataset.get(position + 1));
            holder.url.setText(mDataset.get(position + 2));
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (Homescreen_nav.whichlayout == "google")
        {
            return mDataset.size() / 3;
        }
        return mDataset.size();
    }
}
