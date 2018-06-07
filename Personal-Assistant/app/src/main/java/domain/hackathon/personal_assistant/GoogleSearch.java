package domain.hackathon.personal_assistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleSearch extends AppCompatActivity {
    private FirebaseAuth auth;
    String search;
    String searchurl = "https://personalassistant-ec554.appspot.com/recognize/";
    private String TAG = GoogleSearch.class.getSimpleName();
    String finishedurlstring;
    HashMap<String, String> searchhash;
    private recAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView reclist;
    List<String> searchlist;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.gtoolbar);
        setSupportActionBar(toolbar);
        Homescreen_nav.whichlayout = "google";
        reclist = (RecyclerView) findViewById(R.id.gReclist);
        searchlist = new ArrayList<String>();


        searchhash = new HashMap<>();

        final Intent intent = getIntent();
        if (intent.hasExtra("gsearch"))
            //search = intent.getStringExtra("gsearch");
            searchlist = (ArrayList<String>) getIntent().getSerializableExtra("gsearch");
        else
        {
            search = "search_for_people";
            finishedurlstring = searchurl + search + "/key" + "/Google/yes";
            getGoogleJson();
        }


        mAdapter = new recAdapter(searchlist);
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mAdapter);
        reclist.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(GoogleSearch.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }
                @Override
                public void onLongPress(MotionEvent motionEvent)
                {
                    View child  = reclist.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    int pos = reclist.getChildAdapterPosition(child);
                    final int finalpos = pos + (pos + 1) * 2;
                    AlertDialog.Builder builder = new AlertDialog.Builder(GoogleSearch.this);
                    builder.setTitle("Remember");
                    builder.setMessage("Would you like to remember this link " + searchlist.get(finalpos));
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            String characterstorid = ".$[]#/";
                            String key = searchlist.get(finalpos - 2);

                            for (int j = 0; j < characterstorid.length(); j++)
                            {
                                if (key.contains(String.valueOf(characterstorid.charAt(j))))
                                {
                                    key = key.replace(String.valueOf(characterstorid.charAt(j)), "");
                                }
                            }
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("remeb").child(key).setValue("Google," + searchlist.get(finalpos));
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View ChildView ;

                ChildView = rv.findChildViewUnder(e.getX(), e.getY());

                if(ChildView != null && gestureDetector.onTouchEvent(e)) {
                    int pos;

                    pos = rv.getChildAdapterPosition(ChildView);
                    pos = pos + (pos + 1) * 2;

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(searchlist.get(pos)));
                    startActivity(browserIntent);
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



        auth = FirebaseAuth.getInstance();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homescreen_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            auth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getGoogleJson()
    {
        Jsonparserweather hand = new Jsonparserweather();

        // Making a request to url and getting response
        hand.makeServiceCall(finishedurlstring);
        while (Jsonparserweather.isdoneconn != true) ;

        try {
            Thread.sleep(1000);
        } catch (Exception C) {
            C.printStackTrace();
        }
        String jsonStr = Jsonparserweather.response;


        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                JSONArray jsonArray = jsonObj.getJSONArray("results");
                searchhash.clear();

                for(int i=0; i< jsonArray.length(); i++)
                {
                    JSONObject inside = jsonArray.getJSONObject(i);
                    String title = inside.getString("title");
                    String snippet = inside.getString("snippet");
                    String url = inside.getString("url");
                    searchlist.add(title);
                    searchlist.add(snippet);
                    searchlist.add(url);
                }


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
