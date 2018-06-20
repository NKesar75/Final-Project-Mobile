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

public class stockAdapter extends RecyclerView.Adapter<stockAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> mDataset;
    private RequestManager glide;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title, price, size;
        public ImageView image;
        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.stocklogo);
            title = (TextView) v.findViewById(R.id.stocktitle);
            price = (TextView) v.findViewById(R.id.stockprice);
            size = (TextView) v.findViewById(R.id.stocksize);

        }
    }

    public stockAdapter(ArrayList<HashMap<String, String>> myDataset, RequestManager glide) {
        mDataset = myDataset;
        this.glide = glide;
    }

    @Override
    public stockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stocks_rec_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(stockAdapter.ViewHolder holder, int position) {
        HashMap<String, String> info;

        info = mDataset.get(position);
        holder.price.setText("Price: " + info.get("price"));
        holder.size.setText("Size: " + info.get("size"));
        switch (info.get("symbol").toLowerCase()){
            case "aapl":
                holder.image.setImageResource(R.mipmap.ic_aapl);
                holder.title.setText("Apple");
                break;
            case "bac":
                holder.image.setImageResource(R.mipmap.ic_bac);
                holder.title.setText("Bank of America");
                break;
            case "ccf":
                holder.image.setImageResource(R.mipmap.ic_ccf);
                holder.title.setText("Chase");
                break;
            case "cvx":
                holder.image.setImageResource(R.mipmap.ic_cvx);
                holder.title.setText("Chevy");
                break;
            case "dis":
                holder.image.setImageResource(R.mipmap.ic_dis);
                holder.title.setText("Disney");
                break;
            case "f":
                holder.image.setImageResource(R.mipmap.ic_f);
                holder.title.setText("Ford");
                break;
            case "fb":
                holder.image.setImageResource(R.mipmap.ic_fb);
                holder.title.setText("Facebook");
                break;
            case "frsh":
                holder.image.setImageResource(R.mipmap.ic_frsh);
                holder.title.setText("Papa Murphy's");
                break;
            case "hmc":
                holder.image.setImageResource(R.mipmap.ic_hmc);
                holder.title.setText("Honda");
                break;
            case "mcd":
                holder.image.setImageResource(R.mipmap.ic_mcd);
                holder.title.setText("McDonald's");
                break;
            case "msft":
                holder.image.setImageResource(R.mipmap.ic_msft);
                holder.title.setText("Microsoft");
                break;
            case "pep":
                holder.image.setImageResource(R.mipmap.ic_pep);
                holder.title.setText("Pepsi");
                break;
            case "s":
                holder.image.setImageResource(R.mipmap.ic_s);
                holder.title.setText("Sprint");
                break;
            case "sne":
                holder.image.setImageResource(R.mipmap.ic_sne);
                holder.title.setText("Sony");
                break;
            case "sonc":
                holder.image.setImageResource(R.mipmap.ic_sonc);
                holder.title.setText("Sonic");
                break;
            case "tgt":
                holder.image.setImageResource(R.mipmap.ic_tgt);
                holder.title.setText("Target");
                break;
            case "tm":
                holder.image.setImageResource(R.mipmap.ic_tm);
                holder.title.setText("Toyota");
                break;
            case "vz":
                holder.image.setImageResource(R.mipmap.ic_vz);
                holder.title.setText("Verizon");
                break;
            case "wen":
                holder.image.setImageResource(R.mipmap.ic_wen);
                holder.title.setText("Wendy's");
                break;
            case "wmt":
                holder.image.setImageResource(R.mipmap.ic_wmt);
                holder.title.setText("Walmart");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
