package com.example.pawatch;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.RemoteInput;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import domain.hackathon.personal_assistant.R;

public class home extends WearableActivity implements
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener {
    private TextView mVoice, mMessage;
    private Button mStart, mUpdate;
    private String voiceresult, city, state, finishedstring;
    private String PythonApiUrl = "https://personalassistant-ec554.appspot.com/recognize";
    private DataClient mDataClient;
    private GoogleApiClient googleClient;
    public static final String VOICE_TRANSCRIPTION_MESSAGE_PATH = "/voice_transcription";
    private static final String
            VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription";

    // private DataFragment mDataFragment;

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
        mStart = (Button) findViewById(R.id.startTalk);
        mMessage = (TextView) findViewById(R.id.Message);
        mUpdate = (Button) findViewById(R.id.updateBtn);


        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleClient.connect();

        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//          The first try
//                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/connection");
//                putDataMapRequest.getDataMap().putString("message", "This is a message from Android Wear, connect to the API");
//                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
//                putDataRequest.setUrgent();


                Thread name2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getNodes("IT WONT WORK THIS WAY".getBytes());
                    }
                });
                name2.start();

//                Task<DataItem> dataItemTask =
//                        Wearable.getDataClient(getApplicationContext()).putDataItem(putDataRequest);
//
//                dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
//                    @Override
//                    public void onSuccess(DataItem dataItem) {
//                        Log.d("home", "Sending image was successful: " + dataItem);
//                    }
//                });
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                //

            }


//                try {
//                    DataItem dataItem = Tasks.await(dataItemTask);
//                }catch(ExecutionException exception){
//                    Log.d("home", exception.toString());
//                } catch(InterruptedException exception){
//                Log.d("home", exception.toString());
//            }}
        });


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1);
        }

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStart.setEnabled(false);
                mStart.post(new Runnable() {
                    @Override
                    public void run() {
                        voiceReconize();
                        mStart.setEnabled(true);
                        //    Toast.makeText(getApplicationContext(), "The button", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        // Enables Always-on
        setAmbientEnabled();
    }


    private void getNodes(byte[] voiceData) {

        try {
            List<Node> nodes =
                    Tasks.await(Wearable.getNodeClient(getApplicationContext()).getConnectedNodes());

            Task<Integer> sendTask =
                    Wearable.getMessageClient(getApplicationContext()).sendMessage(
                            nodes.get(0).getId().toString(), "/connection", voiceData);
            sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    Log.d("home", "Message: It works boi" + integer);
                    Toast.makeText(getApplicationContext(),"hey it sent",Toast.LENGTH_LONG).show();
                }
            });
            sendTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("home", "Message: It failed boi" + e);
                }
            });

        } catch (ExecutionException error) {
            Log.d("home", "Failed retrieving asset, Task failed: " + error);
        } catch (InterruptedException error) {
            Log.d("home", "Failed retrieving asset, interrupt occurred: " + error);
        }

    }

//    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
//        Set<Node> connectedNodes = capabilityInfo.getNodes();
//
//        transcriptionNodeId = pickBestNodeId(connectedNodes);
//    }
//
//    private String pickBestNodeId(Set<Node> nodes) {
//        String bestNodeId = null;
//        // Find a nearby node or pick one arbitrarily
//        for (Node node : nodes) {
//            if (node.isNearby()) {
//                return node.getId();
//            }
//            bestNodeId = node.getId();
//        }
//        return bestNodeId;
//    }
//
//    private void requestTranscription(byte[] voiceData) {
//        if (transcriptionNodeId != null) {
//            Task<Integer> sendTask =
//                    Wearable.getMessageClient(getApplicationContext()).sendMessage(
//                            transcriptionNodeId, "/connection", voiceData);
//            // You can add success and/or failure listeners,
//            // Or you can call Tasks.await() and catch ExecutionException
//            sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
//                @Override
//                public void onSuccess(Integer integer) {
//                    Log.d("home", "Message: It works boi");
//                }
//            });
//            sendTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d("home", "Message: It failed boi");
//                }
//            });
//        } else {
//            // Unable to retrieve node with transcription capability
//        }
//    }

//    private void setupVoiceTranscription() {
//        try {
//            CapabilityInfo capabilityInfo = Tasks.await(
//                    Wearable.getCapabilityClient(getApplicationContext()).getCapability(
//                            VOICE_TRANSCRIPTION_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
//            // capabilityInfo has the reachable nodes with the transcription capability
//            updateTranscriptionCapability(capabilityInfo);
//        }
//        catch(ExecutionException error)
//        {
//            Log.d("home", "Message Eerror: " + error);
//
//        }
//        catch(InterruptedException error)
//        {
//            Log.d("home", "Message Ierror: " + error);
//        }
//    }

    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {

            //data item changed
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataItem item = event.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(item);

                //RESPONSE back from mobile message
                if (item.getUri().getPath().equals("/responsemessage")) {

                    //collect all info
                    String error = dataMapItem.getDataMap().getString("error");
                    String unixTime = dataMapItem.getDataMap().getString("unixTime");
                    String height = dataMapItem.getDataMap().getString("height");
                    //success
                    if (error == null) {
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


    void voiceReconize() {
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
                Log.d("home", "results: " + voiceResults.get(0));

                mMessage.setText(voiceResults.get(0));
                voiceresult = voiceResults.get(0);

                if (voiceresult == null) {
                    Log.d("home", "emptystuff");
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("home", "The speech recognition is ready");
            }

            @Override
            public void onError(int error) {
                if (error == 7)
                    Toast.makeText(getApplicationContext(), "Couldn't understand you. Please say your command again.", Toast.LENGTH_SHORT).show();
                else {
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
                Log.d("home", "Its done");

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
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

        Log.d("home", "onCapabilityChanged: " + capabilityInfo);
        //mDataFragment.appendItem("onCapabilityChanged", capabilityInfo.toString());
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(
                        this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

}

