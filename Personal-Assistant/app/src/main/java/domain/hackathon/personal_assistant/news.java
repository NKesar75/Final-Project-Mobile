package domain.hackathon.personal_assistant;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class news extends AppCompatActivity {
    ArrayList<HashMap<String, String>> newshash;
    private newsRecAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView recyclerView;
    String tag = news.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        recyclerView = (RecyclerView) findViewById(R.id.news_rec);
        newshash = new ArrayList<>();
        Jsonparserweather handle = new Jsonparserweather();
        String result = handle.makeServiceCall("https://personalassistant-ec554.appspot.com/recognize/news/FL/orlando/");
        getjson(result);


        mAdapter = new newsRecAdapter(newshash, Glide.with(this));
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(news.this, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }
                @Override
                public void onLongPress(MotionEvent motionEvent)
                {

                }
            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View ChildView ;

                ChildView = rv.findChildViewUnder(e.getX(), e.getY());

                if(ChildView != null && gestureDetector.onTouchEvent(e)) {
                    int pos;
                    //get the right information passed on the where the user clicked
                    pos = rv.getChildAdapterPosition(ChildView);
                    //open up the link in a browser
                    if (newshash.get(pos).get("url") != null && !newshash.get(pos).get("url").isEmpty() && !newshash.get(pos).get("url").equals("null"))
                    {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(newshash.get(pos).get("url")));
                        startActivity(browserIntent);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "No url found", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    public void getjson(String result){
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray jsonArray = jsonObj.getJSONArray("articles");

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    HashMap<String, String> temp = new HashMap<>();

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    JSONArray source = jsonObj.getJSONArray("source");
//                    for (int j = 0; i < source.length(); i++){
//                        JSONObject sourceObject = source.getJSONObject(j);
//                        temp.put("name", sourceObject.getString("name"));
//                    }
                    JSONObject source = jsonObject.getJSONObject("source");
                    temp.put("name", source.getString("name"));


                    temp.put("title", jsonObject.getString("title"));
                    temp.put("description", jsonObject.getString("description"));
                    temp.put("url", jsonObject.getString("url"));
                    temp.put("urlToImage", jsonObject.getString("urlToImage"));


                    newshash.add(temp);
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
