package domain.hackathon.personal_assistant;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Geocoder;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Homescreen_nav extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private boolean isRecording = false;
    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private static String audioFilePath;
    private boolean recstop = false;
    private String PythonApiUrl = "https://personalassistant-ec554.appspot.com/recognize";

    LocationManager locationManager;
    String city;
    String state;
    String Zipcode;
    Geocoder geocoder;
    List<Address> addressList;
    static final int REQUEST_LOCATION = 1;
    private ProgressDialog pDialog;
    private String TAG = MainActivity.class.getSimpleName();
    HashMap<String, String> weatherhash;
    private StorageReference mStorage;
    TextView temp, loca;
    ImageView iweather;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO};
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                //new SetupTask(this).execute();
            } else {
                //finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1);
        }
        weatherhash = new HashMap<>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        mStorage = FirebaseStorage.getInstance().getReference();

        temp = (TextView) findViewById(R.id.temp);
        loca = (TextView) findViewById(R.id.location);
        iweather = (ImageView) findViewById(R.id.weather);
        temp.setVisibility(View.INVISIBLE);
        loca.setVisibility(View.INVISIBLE);
        iweather.setVisibility(View.INVISIBLE);

        FloatingActionButton myFab = (FloatingActionButton)  findViewById(R.id.fabnote);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                presentActivity(v);
            }
        });







        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        auth = FirebaseAuth.getInstance();
        audioFilePath = getExternalCacheDir().getAbsolutePath();

        audioFilePath += "/audiorecordtest.amr";

        FloatingActionButton voice = (FloatingActionButton)  findViewById(R.id.fabvoice);
        //testing purposes
        getLocation();
        PythonApiUrl = PythonApiUrl + "/" + state + "/" + city;
        getJsonInfo();
        updateweatherview();

        voice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!recstop)
                {
                    recordAudio();
                    recstop = true;
                }
                else if (recstop)
                {
                    recstop = false;
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    getLocation();
                    PythonApiUrl = PythonApiUrl + "/" + state + "/" + city;
                    playAudio();
                    uploadAudio();
                    getJsonInfo();
                    updateweatherview();
                }
            }
        });


    }

    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);

        Intent intent = new Intent(this, notesActivity.class);
        intent.putExtra(notesActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(notesActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.maps) {
            startActivity(new Intent(Homescreen_nav.this, MapsActivity.class));

        } else if (id == R.id.youtube) {
            startActivity(new Intent(Homescreen_nav.this, Youtube.class));

        } else if (id == R.id.banking) {

        } else if (id == R.id.food) {

        } else if (id == R.id.googleSearch) {

        } else if (id == R.id.scheduling) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {

        } else if (id == R.id.logout){
            auth.signOut();
            startActivity(new Intent(Homescreen_nav.this, MainActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void recordAudio() {
        isRecording = true;

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder.start();



    }
    public void stopAudio (View view)
    {
        if (isRecording)
        {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        } else {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    void playAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void uploadAudio()
    {
        StorageReference filepath = mStorage.child("new_audio.amr");
        Uri uri = Uri.fromFile(new File(audioFilePath));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }
    private void updateweatherview() {
        temp.setText(weatherhash.get("tempf") + (char) 0x00B0 + "F");
        loca.setText(weatherhash.get("city") + ", " + weatherhash.get("state"));
        temp.setVisibility(View.VISIBLE);
        loca.setVisibility(View.VISIBLE);
        iweather.setVisibility(View.VISIBLE);
    }
    private void getJsonInfo(){
        Jsonparserweather hand = new Jsonparserweather();

        // Making a request to url and getting response
        hand.makeServiceCall(PythonApiUrl);
        while (Jsonparserweather.isdoneconn != true);

        String jsonStr = Jsonparserweather.response;

        Log.e(TAG, "Response from url: " + jsonStr);

        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                String key = jsonObj.getString("key");
                String tempf = jsonObj.getString("tempf");
                String tempc = jsonObj.getString("tempc");
                String city = jsonObj.getString("city");
                String state = jsonObj.getString("state");
                weatherhash.put("key", key);
                weatherhash.put("tempf", tempf);
                weatherhash.put("tempc", tempc);
                weatherhash.put("city", city);
                weatherhash.put("state", state);

                // Getting JSON Array node
                //JSONArray weatherarray = jsonObj.getJSONArray("current_observation");

                //for (int i = 0; i < weatherarray.length(); i++) {
                //    JSONObject c = weatherarray.getJSONObject(i);

                //    String weather = c.getString("weather");
                //    String tempf = c.getString("temp_f");
                //    String tempc = c.getString("temp_c");
                //    String wind = c.getString("wind_string");
                //    String wind_mph = c.getString("wind_mph");

                //    // adding each child node to HashMap key => value
                //    weatherhash.put("weather", weather);
                //    weatherhash.put("tempf", tempf);
                //    weatherhash.put("tempc", tempc);
                //    weatherhash.put("wind", wind);
                //    weatherhash.put("wind_mph", wind_mph);

                //}
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
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

        }
    }

    void getLocation() {
        Double lon;
        Double lat;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addressList = geocoder.getFromLocation(lat, lon, 1);
                    city = addressList.get(0).getLocality();
                    if (city.contains(" "))
                    {
                        city = city.replace(" ", "_");
                    }
                    Zipcode = addressList.get(0).getPostalCode();
                    String x = addressList.get(0).getAdminArea();
                    switch (x) {
                        case "Alabama":
                            x = "AL";
                            break;

                        case "Alaska":
                            x = "AK";
                            break;

                        case "Arizona":
                            x = "AZ";
                            break;

                        case "Arkansas":
                            x = "AR";
                            break;

                        case "California":
                            x = "CA";
                            break;

                        case "Colorado":
                            x = "CO";
                            break;

                        case "Connecticut":
                            x = "CT";
                            break;

                        case "Delaware":
                            x = "DE";
                            break;

                        case "District of Columbia":
                            x = "DC";
                            break;

                        case "Florida":
                            x = "FL";
                            break;

                        case "Georgia":
                            x = "GA";
                            break;

                        case "Hawaii":
                            x = "HI";
                            break;

                        case "Idaho":
                            x = "ID";
                            break;

                        case "Illinois":
                            x = "IL";
                            break;

                        case "Indiana":
                            x = "IN";
                            break;

                        case "Iowa":
                            x = "IA";
                            break;

                        case "Kansas":
                            x = "KS";
                            break;

                        case "Kentucky":
                            x = "KY";
                            break;

                        case "Louisiana":
                            x = "LA";
                            break;

                        case "Maine":
                            x = "ME";
                            break;

                        case "Maryland":
                            x = "MD";
                            break;

                        case "Massachusetts":
                            x = "MA";
                            break;

                        case "Michigan":
                            x = "MI";
                            break;

                        case "Minnesota":
                            x = "MN";
                            break;

                        case "Mississippi":
                            x = "MS";
                            break;

                        case "Missouri":
                            x = "MO";
                            break;

                        case "Montana":
                            x = "MT";
                            break;

                        case "Nebraska":
                            x = "NE";
                            break;

                        case "Nevada":
                            x = "NV";
                            break;

                        case "New Hampshire":
                            x = "NH";
                            break;

                        case "New Jersey":
                            x = "NJ";
                            break;

                        case "New Mexico":
                            x = "NM";
                            break;

                        case "New York":
                            x = "NY";
                            break;

                        case "North Carolina":
                            x = "NC";
                            break;

                        case "North Dakota":
                            x = "ND";
                            break;

                        case "Ohio":
                            x = "OH";
                            break;

                        case "Oklahoma":
                            x = "OK";
                            break;

                        case "Oregon":
                            x = "OR";
                            break;

                        case "Pennsylvania":
                            x = "PA";
                            break;


                        case "Rhode Island":
                            x = "RI";
                            break;

                        case "South Carolina":
                            x = "SC";
                            break;

                        case "South Dakota":
                            x = "SD";
                            break;

                        case "Tennessee":
                            x = "TN";
                            break;

                        case "Texas":
                            x = "TX";
                            break;

                        case "Utah":
                            x = "UT";
                            break;

                        case "Vermont":
                            x = "VT";
                            break;

                        case "Virginia":
                            x = "VA";
                            break;

                        case "Washington":
                            x = "WA";
                            break;

                        case "West Virginia":
                            x = "WV";
                            break;

                        case "Wisconsin":
                            x = "WI";
                            break;

                        case "Wyoming":
                            x = "WY";
                            break;

                        default:
                            break;
                    }
                    state = x;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

