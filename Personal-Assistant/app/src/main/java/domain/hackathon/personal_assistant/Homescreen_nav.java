package domain.hackathon.personal_assistant;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;
import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.MessageClient;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestDeeplink;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIDialog;


public class Homescreen_nav extends AppCompatActivity implements
        MessageClient.OnMessageReceivedListener, DataClient.OnDataChangedListener, AIListener {

    private FirebaseAuth auth;
    private String PythonApiUrl = "https://personalassistant-ec554.appspot.com/recognize";
    private String PythonApiUrlText = "https://personalassistant-ec554.appspot.com/recognize/text_weather";
    private String finishedstring = "";
    public static String whichlayout;
    ArrayList<String> searchlist;
    ArrayList<HashMap<String, String>> weatherhash;
    LocationManager locationManager;
    String city;
    String state;
    String Zipcode;
    Geocoder geocoder;
    List<Address> addressList;
    static final int REQUEST_LOCATION = 1;
    private String TAG = MainActivity.class.getSimpleName();
    HashMap<String, String> hashjson;
    private StorageReference mStorage;
    TextView commands;
    public ProgressDialog progress;
    private String voiceresult;
    public static final String VOICE_TRANSCRIPTION_MESSAGE_PATH = "/connection";

    GoogleSignInClient mGoogleSignInClient;

    AIService aiService;
    TextToSpeech tts;

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
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null)
        {
            //if account = null the user needs to sign in
            signIn();
        }

        //set up the config for api.ai
        AIConfiguration config = new AIConfiguration("fb57e9c4edb34e31bfdd91ece7f5427c",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(getApplicationContext(), config);
        aiService.setListener(this);

        weatherhash = new ArrayList<>();

        hashjson = new HashMap<>();
        searchlist = new ArrayList<String>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        mStorage = FirebaseStorage.getInstance().getReference();


        commands = (TextView) findViewById(R.id.commands);
        commands.setText("Try using a command\nWhat's the weather\nWhat is the weather in\nSearch for\nPlay");
        progress = new ProgressDialog(this);

        //start te notesactivity with the animation
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabnote);
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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.maps) {
                    startActivity(new Intent(Homescreen_nav.this, MapsActivity.class));
                } else if (id == R.id.youtube) {
                    startActivity(new Intent(Homescreen_nav.this, Youtube.class));
                } else if (id == R.id.banking) {

                } else if (id == R.id.food) {

                } else if (id == R.id.weatheritem) {
                    getLocation();
                    //start the weather activity with the location getting passed on
                    Intent intent = new Intent(Homescreen_nav.this, weather.class);
                    intent.putExtra("state", state);
                    startActivity(intent);
                } else if (id == R.id.googleSearch) {
                    startActivity(new Intent(Homescreen_nav.this, GoogleSearch.class));
                } else if (id == R.id.scheduling) {
                    //start Google Calender Activity
                    Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                    builder.appendPath("time");
                    //Set the Calender to the current time
                    ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                    startActivity(intent);
                } else if (id == R.id.rememeber){
                    startActivity(new Intent(Homescreen_nav.this, Remember.class));
                } else if (id == R.id.settings) {

                } else if (id == R.id.about) {

                } else if (id == R.id.logout) {
                    auth.signOut();
                    finish();
                    startActivity(new Intent(Homescreen_nav.this, MainActivity.class));
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        auth = FirebaseAuth.getInstance();

        FloatingActionButton voice = (FloatingActionButton) findViewById(R.id.fabvoice);
        getLocation();
        //finishedstring = PythonApiUrl + "/weather" + "/" + state + "/" + city + "/yes";

        //getJsonInfo();
        //updateweatherview();

        voice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                voiceReconize();
            }
        });
        //api.ai test
        FloatingActionButton api = (FloatingActionButton) findViewById(R.id.fabapiai);
        api.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aiService.startListening();
            }
        });


    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(TAG, "Message: A message from watch was received:"
                + messageEvent.getRequestId() + " " + messageEvent.getPath());
        Toast.makeText(this, "You have a new Message", Toast.LENGTH_LONG).show();
        //mDataItemListAdapter.add(new Event("Message from watch", messageEvent.toString()));
    }

    @Override
    public void onDataChanged(DataEventBuffer data) {
        Log.d(TAG, "Message: A message from watch was received from: ondatachanged");
        Toast.makeText(this, "You have a new Message", Toast.LENGTH_LONG).show();
    }

    public void presentActivity(View view) {
        //start the new activity
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);
        //pass the ints needed for the animation opening up from bottom left and ending on the top right
        Intent intent = new Intent(this, notesActivity.class);
        intent.putExtra(notesActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(notesActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onBackPressed() {
        //close the drawer if the drawer is open and the back button is pressed
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

    @Override
    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
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

    private void getJsonInfo() {

        Thread mThread = new Thread() {
            @Override
            public void run() {
                Jsonparserweather hand = new Jsonparserweather();

                // Making a request to url and getting response
                String jsonStr = hand.makeServiceCall(finishedstring);
                //while (Jsonparserweather.isdoneconn != true) ;


                //region DO NOT LOOK I WARNED YOU
//                try {
//                    Thread.sleep(1500);
//                } catch (Exception C) {
//                    C.printStackTrace();
//                }
                //I WARNED YOU!
                //endregion

                //String jsonStr = Jsonparserweather.response;

                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        String key = jsonObj.getString("key");
                        //checking the key to get the correct information from the json
                        if (key.equals("weather")) {
                            weatherhash.clear();
                            // Getting JSON Array node
                            JSONArray jsonArray = jsonObj.getJSONArray("results");
                            String city = jsonObj.getString("city");
                            String state = jsonObj.getString("state");


                            //loop through each day and get the information for each day
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
                            //starting the correct activity based on the key
                            Intent intent = new Intent(Homescreen_nav.this, weather.class);
                            intent.putExtra("wsearch", weatherhash);
                            startActivity(intent);


                        } else if (key.equals("google")) {
                            JSONArray jsonArray = jsonObj.getJSONArray("results");
                            searchlist.clear();

                            //loop through and get each title, url and snippet of each result from the google search
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject inside = jsonArray.getJSONObject(i);
                                String title = inside.getString("title");
                                String snippet = inside.getString("snippet");
                                String url = inside.getString("url");
                                searchlist.add(title);
                                searchlist.add(snippet);
                                searchlist.add(url);
                            }
                            //start the googlesearch activity with the information needed
                            Intent intent = new Intent(Homescreen_nav.this, GoogleSearch.class);
                            intent.putExtra("gsearch", searchlist);
                            startActivity(intent);
                        } else if (key.equals("youtube")) {
                            //get the id of the youtube link to play the video
                            String id = jsonObj.getString("id");
                            hashjson.clear();
                            hashjson.put("id", id);
                            //pass the id into the youtube activity
                            Intent intent = new Intent(Homescreen_nav.this, Youtube.class);
                            intent.putExtra("search", (Serializable) hashjson);
                            startActivity(intent);
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
                progress.dismiss();
            }
        };
        mThread.start();


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
                    if (city.contains(" ")) {
                        city = city.replace(" ", "_");
                    }
                    Zipcode = addressList.get(0).getPostalCode();
                    String x = addressList.get(0).getAdminArea();
                    //need state abbreviation for the api
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

    void voiceReconize() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "domain.hackathon.personal_assistant");

        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(this.getApplicationContext());
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                progress.setTitle("Please wait");
                progress.show();

                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, "Voiceresults: " + voiceResults.toString());
                voiceresult = voiceResults.get(0);
                if (voiceresult.contains(" ")) {
                    voiceresult = voiceresult.replace(" ", "_");
                }
                finishedstring = "";
                //apend to the api url with the information needed for the right call
                finishedstring = PythonApiUrl + "/" + voiceresult + "/" + state + "/" + city + "/yes";
                getJsonInfo();
                //updateweatherview();

                if (voiceResults == null) {
                    Log.e(TAG, "No voice results");
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onError(int error) {
                //if the error is 7 it could not recognize what the user said
                if (error == 7)
                    Toast.makeText(getApplicationContext(), "Couldn't understand you. Please say your command again.", Toast.LENGTH_SHORT).show();
                else
                    Log.d(TAG, "Error listening for speech: " + error);
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech starting");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }
        };
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);

    }

    @Override
    public void onResult(AIResponse result) {
        Result info = result.getResult();
        Log.d(TAG, "api info " + info);
        Log.d(TAG, "api Query " +info.getResolvedQuery());
        Log.d(TAG, "api result " + info.getFulfillment().getSpeech().toString());
        //Toast.makeText(getApplicationContext(), "IT WORKED " + info.getFulfillment().getSpeech().toString(), Toast.LENGTH_LONG).show();

        //current way to call Text to Speech
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Bundle bundle = new Bundle();
            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            tts.speak(info.getFulfillment().getSpeech().toString(), TextToSpeech.QUEUE_FLUSH, bundle, null);
        }
        //old way to call Text to Speech
        else{
            HashMap<String, String> paramaters = new HashMap<>();
            paramaters.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            tts.speak(info.getFulfillment().getSpeech().toString(), TextToSpeech.QUEUE_FLUSH, paramaters);
        }
    }

    @Override
    public void onError(AIError error) {
        Log.d(TAG, "api error " + error.getMessage().toString());
        Toast.makeText(getApplicationContext(), "ERROR " + error.getMessage().toString(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        Log.d(TAG, "api started to listen");
        //Toast.makeText(getApplicationContext(), "ITS LISTENING", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onListeningCanceled() {
        Log.d(TAG, "api canceled");

    }

    @Override
    public void onListeningFinished() {
        Log.d(TAG, "api stopped listening");

    }

    private void signIn() {
        //sign into google
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == 101) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            //handleSignInResult(task);
//        }
//    }

//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//
//            // Signed in successfully, show authenticated UI.
//            updateUI(account);
//        } catch (ApiException e) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
//        }
//    }
}

