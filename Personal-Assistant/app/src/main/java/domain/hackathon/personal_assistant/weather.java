package domain.hackathon.personal_assistant;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class weather extends AppCompatActivity {
    ArrayList<HashMap<String, String>> weatherhash;
    private weatherRecAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView reclist;
    TextView citystate;
    EditText weathertosearch;
    String searchurl = "https://personalassistant-ec554.appspot.com/recognize/";
    String finishedurlstring;
    private String TAG = weather.class.getSimpleName();
    String weathersearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        weatherhash = new ArrayList<>();
        reclist = (RecyclerView) findViewById(R.id.wReclist);
        citystate = (TextView) findViewById(R.id.scitystate);


        Intent intent = getIntent();
        if (intent.hasExtra("wsearch")) {
            //store the information from the intent
            weatherhash = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("wsearch");
            citystate.setText(weatherhash.get(0).get("city") + ", " + weatherhash.get(0).get("state"));
        }
        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "Action: " + actionId);
                if (actionId == 6) {
                    weathersearch = weathertosearch.getText().toString();
                    finishedurlstring = "";
                    // ex http://personalassistant-ec554.appspot.com/recognize/weather/CA/San_Francisco/
                    if (weathersearch.contains(" ")) {
                        weathersearch = weathersearch.replace(" ", "_");
                        //String[] parts = weathersearch.split(" ");
                        finishedurlstring = searchurl + "weather_in_" + weathersearch + "/";
                        Log.d(TAG, finishedurlstring);
                    } else {
                        Intent intent = getIntent();
                        String state = intent.getStringExtra("state");

                        finishedurlstring = searchurl + "weather/" + state + "/" + weathersearch + "/";
                        Log.d(TAG, finishedurlstring);

                    }
                    getJson();
                    mAdapter.notifyDataSetChanged();

                }
                return true;
            }
        };


        weathertosearch = (EditText) findViewById(R.id.txtweathertosearch);
        weathertosearch.setOnEditorActionListener(exampleListener);

        mAdapter = new weatherRecAdapter(weatherhash, Glide.with(this));
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        //reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mAdapter);


    }

    private void getJson() {
        Jsonparserweather hand = new Jsonparserweather();

        // Making a request to url and getting response
        String jsonStr = hand.makeServiceCall(finishedurlstring);

//        hand.makeServiceCall(finishedurlstring);
//        while (Jsonparserweather.isdoneconn != true) ;
//
//        try {
//            Thread.sleep(1000);
//        } catch (Exception C) {
//            C.printStackTrace();
//        }
//        String jsonStr = Jsonparserweather.response;


        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String key = jsonObj.getString("key");


                weatherhash.clear();
                // Getting JSON Array node
                JSONArray jsonArray = jsonObj.getJSONArray("results");
                String city = jsonObj.getString("city");
                String state = jsonObj.getString("state");


                // looping through All Contacts
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject inside = jsonArray.getJSONObject(i);
                    String temp_highf = inside.getString("temp_highf");
                    String temp_highc = inside.getString("temp_highc");
                    String humidity = inside.getString("humidity");
                    String precip = inside.getString("precip");
                    String condition = inside.getString("condition");
                    String picurl = inside.getString("url");
                    String temp_lowf = inside.getString("temp_lowf");
                    //converting stringdate into a date
                    String stringdate = inside.getString("day") + ":" + inside.getString("month") + ":" + inside.getString("year");
                    SimpleDateFormat regularDateFormat = new SimpleDateFormat("dd:MM:yyyy");
                    String days = "";

                    try {
                        Date date = regularDateFormat.parse(stringdate);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        int day = calendar.get(Calendar.DAY_OF_WEEK);
                        if (day == Calendar.MONDAY) {
                            days = "Monday";

                        } else if (day == Calendar.TUESDAY) {
                            days = "Tuesday";

                        } else if (day == Calendar.WEDNESDAY) {
                            days = "Wednesday";

                        } else if (day == Calendar.THURSDAY) {
                            days = "Thursday";

                        } else if (day == Calendar.FRIDAY) {
                            days = "Friday";

                        } else if (day == Calendar.SATURDAY) {
                            days = "Saturday";

                        } else if (day == Calendar.SUNDAY) {
                            days = "Sunday";

                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e);
                    }

                    //temp hash map to store in the arraylist of hash maps
                    HashMap<String, String> weather = new HashMap<>();
                    weather.put("day", days);
                    weather.put("key", key);
                    weather.put("temp_highf", temp_highf);
                    weather.put("temp_highc", temp_highc);
                    weather.put("temp_lowf", temp_lowf);
                    weather.put("city", city);
                    weather.put("state", state);
                    weather.put("humidity", humidity);
                    weather.put("precip", precip);
                    weather.put("condition", condition);
                    weather.put("picurl", picurl);
                    //adding the hash map to the arraylist
                    weatherhash.add(weather);
                }
                citystate.setText(weatherhash.get(0).get("city") + ", " + weatherhash.get(0).get("state"));


            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

        }
    }


}
