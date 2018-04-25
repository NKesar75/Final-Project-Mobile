package domain.hackathon.personal_assistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Youtube extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;
    EditText search;
    String videosearch;
    String searchurl = "https://personalassistant-ec554.appspot.com/recognize/";
    private String TAG = Youtube.class.getSimpleName();
    String finishedurlstring;
    HashMap<String, String> searchhash;
    YouTubePlayer  mplayer;
    boolean isplaying = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        searchhash = new HashMap<>();
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        final Intent intent = getIntent();
        if (intent.hasExtra("search"))
        {
            videosearch = intent.getStringExtra("search");
            finishedurlstring = searchurl + videosearch + "/key" + "/Youtube";
            getYoutubeJson();
            youTubeView.setVisibility(View.VISIBLE);
            youTubeView.initialize(Config.YOUTUBE_API_KEY, Youtube.this);
        }


        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6){
                    String texttosearch = search.getText().toString();
                    if (texttosearch.contains(" "))
                    {
                        texttosearch = texttosearch.replace(" ", "_");
                    }
                    videosearch = "play_" + texttosearch;
                    finishedurlstring = "";
                    finishedurlstring = searchurl + videosearch + "/key" + "/Youtube";
                    getYoutubeJson();

                    if (!isplaying)
                    {
                        youTubeView.initialize(Config.YOUTUBE_API_KEY, Youtube.this);
                        youTubeView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mplayer.loadVideo(searchhash.get("id").toString());
                    }
                }
                return true;
            }
        };

        search = (EditText) findViewById(R.id.txtyoutubesearch);
        search.setOnEditorActionListener(exampleListener);
        
        //youTubeView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        mplayer = player;

        mplayer.pause();
        if (!wasRestored) {
            isplaying = true;
            mplayer.loadVideo(searchhash.get("id").toString()); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.YOUTUBE_API_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
    }
    private void getYoutubeJson()
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

                String id = jsonObj.getString("id");
                searchhash.clear();
                searchhash.put("id", id);

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
