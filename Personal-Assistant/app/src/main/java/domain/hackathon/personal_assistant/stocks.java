package domain.hackathon.personal_assistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class stocks extends AppCompatActivity {
    String tag = stocks.class.getSimpleName();
    String apiurl = "https://api.iextrading.com/1.0/tops/last?symbols=aapl,bac,ccf,cvx,fb,f,hmc,mcd,msft,frsh,pep,sonc,sne,s,tgt,tm,vz,wmt,dis,wen";
    ArrayList<HashMap<String, String>> apiresults;
    RecyclerView recyclerView;
    private stockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks);

        apiresults = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.stockreclist);

        Jsonparserweather handle = new Jsonparserweather();
        String result = handle.makeServiceCall(apiurl);
        getjsoninfo(result);

        mAdapter = new stockAdapter(apiresults, Glide.with(this));
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }
    public void getjsoninfo(String result){
        if (result != null) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    HashMap<String, String> temp = new HashMap<>();

                    temp.put("symbol", jsonObject.getString("symbol"));
                    temp.put("price", jsonObject.getString("price"));
                    temp.put("size", jsonObject.getString("size"));

                    apiresults.add(temp);
                }

            } catch (Exception e) {
                Log.d(tag, "json parsing error: " + e);
                Toast.makeText(getApplicationContext(), "Error when parsing json", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(tag, "Couldn't get json from server");
            Toast.makeText(getApplicationContext(), "Couldn't get json from server", Toast.LENGTH_LONG).show();
        }
    }
}
