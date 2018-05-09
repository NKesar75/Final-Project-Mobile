package com.example.pawatch;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.telecom.RemoteConference;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class home extends WearableActivity implements
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView mVoice, mMessage;
    private Button mStart, mUpdate;
    private String voiceresult, city,state, finishedstring;
    private String PythonApiUrl = "https://personalassistant-ec554.appspot.com/recognize";
    private GoogleApiClient googleclient;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //mVoice = (TextView) findViewById(R.id.voiceInput);
        //mStart = (Button) findViewById(R.id.startTalk);
        //mMessage = (TextView) findViewById(R.id.message);
        mUpdate = (Button) findViewById(R.id.updateBtn);
        googleclient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/apiurl");
                putDataMapRequest.getDataMap().putString("message", "This is a message from Android Wear, connect to the API");
                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                putDataRequest.setUrgent();

                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleclient, putDataRequest);
            }
        });


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1);
        }

        //mStart.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {

        //        mStart.setEnabled(false);
        //        mStart.post(new Runnable() {
        //            @Override
        //            public void run() {

        //                voiceReconize();
        //                mStart.setEnabled(true);
        //            }
        //        });
        //    }
        //});
        // Enables Always-on
        setAmbientEnabled();
    }

    public void onDataChanged(DataEventBuffer dataEvents){

        for(DataEvent event: dataEvents){

            //data item changed
            if(event.getType() == DataEvent.TYPE_CHANGED){

                DataItem item = event.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(item);

                //RESPONSE back from mobile message
                if(item.getUri().getPath().equals("/responsemessage")){

                    //collect all info
                    String error = dataMapItem.getDataMap().getString("error");
                    String unixTime = dataMapItem.getDataMap().getString("unixTime");
                    String height = dataMapItem.getDataMap().getString("height");

                    //success
                    if(error == null){
                        mMessage.setText("Current Time Info");
                    }
                    //error
                    else {
                        mMessage.setText(error);
                    }

                }
            }
        }
    }

    public void onConnected(Bundle connectionHint){
        Wearable.DataApi.addListener(googleclient,this);
    }

    public void onResume(){
        super.onResume();
        googleclient.connect();
    }

    public void onConnectionSuspended(int cause){
        Wearable.DataApi.removeListener(googleclient,this);
    }

    public void onPause(){
        super.onPause();
        Wearable.DataApi.removeListener(googleclient,this);
        googleclient.disconnect();
    }

    public void onConnectionFailed(ConnectionResult result){
        Wearable.DataApi.removeListener(googleclient,this);
    }



    void voiceReconize()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(this.getApplicationContext());
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {

                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d("home","resultssss: " + voiceResults.get(0));

//                voiceresult = voiceResults.get(0);
//                if (voiceresult.contains(" "))
//                {
//                    voiceresult = voiceresult.replace(" ", "_");
//                }
//                finishedstring = "";
//                finishedstring = PythonApiUrl + "/" + voiceresult + "/" + state + "/" + city;
//                getJsonInfo();
//                updateweatherview();

                if (voiceResults == null) {
                    Log.d("home","emptystuff");
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("home", "ARE YOU READy GARCON");
            }

            @Override
            public void onError(int error) {
                if (error == 7)
                    Toast.makeText(getApplicationContext(), "Couldn't understand you. Please say your command again.", Toast.LENGTH_SHORT).show();
                else
                {
                    Log.d("home", "ERROR: " + error);
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("home", "it started");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.d("home", "ITZ DONE BIZNITCH");

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

    void getJsonInfo(){

    }

    void updateweatherview(){

    }

}

