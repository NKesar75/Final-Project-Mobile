package domain.hackathon.personal_assistant;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by darkness7245 on 4/30/2018.
 */

public class weatherRecAdapter extends RecyclerView.Adapter<weatherRecAdapter.ViewHolder> {
    //It is easier to look up if store this way
    private ArrayList<HashMap<String, String>> mDataset;
    private RequestManager glide;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView temph, templ, condition, humidity, precip, day;
        public ImageView image;
        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.sweathericon);
            temph = (TextView) v.findViewById(R.id.stemph);
            templ = (TextView) v.findViewById(R.id.stempl);
            condition = (TextView) v.findViewById(R.id.scondition);
            humidity = (TextView) v.findViewById(R.id.shumidity);
            precip = (TextView) v.findViewById(R.id.sprecip);
            day = (TextView) v.findViewById(R.id.sday);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public weatherRecAdapter(ArrayList<HashMap<String, String>> myDataset, RequestManager glide) {
        mDataset = myDataset;
        this.glide = glide;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public weatherRecAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_rec_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        HashMap<String, String> info;

        info = mDataset.get(position);
//        if (position == 0){
            glide.load(info.get("picurl")).into(holder.image);
            holder.temph.setText("High: " + info.get("temp_highf") + (char) 0x00B0 + "F");
            holder.templ.setText("Low: " + info.get("temp_lowf") + (char) 0x00B0 + "F");
            holder.condition.setText(info.get("condition"));
            holder.humidity.setText("Humidity: " + info.get("humidity"));
            holder.precip.setText("Precipitation: " + info.get("precip") + "%");
            holder.day.setText(info.get("day"));
//        }
//        else {
//            glide.load(info.get("picurl")).into(holder.image);
//            holder.temph.setText("High: " + info.get("temp_highf") + (char) 0x00B0 + "F");
//            holder.templ.setText("Low: " + info.get("temp_lowf") + (char) 0x00B0 + "F");
//            holder.day.setText(info.get("day"));
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.condition.getLayoutParams();
//            params.topMargin = 0;
//            params.alignWithParent = true;
//            holder.condition.setLayoutParams(params);
//            holder.condition.setVisibility(View.INVISIBLE);
//            holder.humidity.setLayoutParams(params);
//            holder.humidity.setVisibility(View.INVISIBLE);
//            holder.precip.setLayoutParams(params);
//            holder.precip.setVisibility(View.INVISIBLE);
//            holder.image.setLayoutParams(new RelativeLayout.LayoutParams(200, 200));
//            RelativeLayout.LayoutParams imageparams = (RelativeLayout.LayoutParams) holder.image.getLayoutParams();
//            imageparams.addRule(RelativeLayout.ALIGN_TOP, holder.day.getId());
//            imageparams.topMargin = 100;
//            imageparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//            holder.image.setLayoutParams(imageparams);
//            RelativeLayout.LayoutParams temph = (RelativeLayout.LayoutParams) holder.temph.getLayoutParams();
//            temph.addRule(RelativeLayout.ALIGN_LEFT, holder.image.getId());
//            holder.temph.setLayoutParams(temph);
//            RelativeLayout.LayoutParams tmpl = (RelativeLayout.LayoutParams) holder.templ.getLayoutParams();
//            tmpl.addRule(RelativeLayout.ALIGN_LEFT, holder.image.getId());
//            holder.templ.setLayoutParams(tmpl);
//        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset.size();
    }
}

