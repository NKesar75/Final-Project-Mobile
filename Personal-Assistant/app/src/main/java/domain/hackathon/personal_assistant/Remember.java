package domain.hackathon.personal_assistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Remember extends AppCompatActivity {


    List<String> rememberlist;
    List<String> parsedlisst;
    RecyclerView reclist;
    DatabaseReference myRef;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth auth;
    StorageReference storageRef;
    FirebaseStorage storage;
    private recAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    int pos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remember);


        Homescreen_nav.whichlayout = "notes";
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        rememberlist = new ArrayList<String>();
        parsedlisst = new ArrayList<String>();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        auth = FirebaseAuth.getInstance();

        //get the information from firebase
        myRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid().toString()).child("remeb");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clear the current data so there is no duplicates
                rememberlist.clear();
                parsedlisst.clear();
                String thingtoremember;
                for (DataSnapshot lists : dataSnapshot.getChildren()) {
                    thingtoremember = lists.getKey();
                    String parse = thingtoremember + "," + lists.getValue();
                    thingtoremember = thingtoremember + "," + lists.getValue();
                    //put a comma inbetween the key and the value for easier lookup later
                    rememberlist.add(thingtoremember);

                    //put a new line between the list that is getting displayed so it looks nicer
//                    String[] parts = parse.split(",");
//                    parse = parts[0] + "\n" + parts[2];
//                    parsedlisst.add(parse);
                }

                for (int i = 0; i < rememberlist.size(); i++){
                    String parse = rememberlist.get(i);
                    String[] parts = parse.split(",");
                    if (parts[1].equals("Google"))
                    {
                        parse = "Google Search\n" + parts[0] + "\n" + parts[2];
                        parsedlisst.add(parse);
                    }
                }
                for (int i = 0; i < rememberlist.size(); i++){
                    String parse = rememberlist.get(i);
                    String[] parts = parse.split(",");
                    if (parts[1].equals("Youtube"))
                    {
                        parse = "Youtube video\n" + parts[0] + "\n" + parts[2];
                        parsedlisst.add(parse);
                    }
                }
                for (int i = 0; i < rememberlist.size(); i++){
                    String parse = rememberlist.get(i);
                    String[] parts = parse.split(",");
                    if (parts[1].equals("List"))
                    {
//                        StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(parts[0] + ".txt");
//
//                        final File fileNameOnDevice = new File(getApplicationContext().getFilesDir() + "dlfile.txt");
//                        dlref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                                String line = null;
//                                StringBuilder sb = new StringBuilder();
//                                String dis;
//
//                                try {
//                                    FileReader fileReader = new FileReader(fileNameOnDevice.getAbsolutePath().toString());
//
//                                    BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//                                    while ((line = bufferedReader.readLine()) != null) {
//                                        sb.append(line + "\n");
//                                    }
//                                    sb = sb.deleteCharAt(sb.length() - 1);
//                                    dis = sb.toString();
//                                    if (fileNameOnDevice != null)
//                                        fileNameOnDevice.delete();
//
//                                    bufferedReader.close();
//
//
//                                } catch (Exception ex) {
//                                    Log.d("notesActivity", "File exception " + ex);
//                                }
//                            }
//                        });
                        parse = "List\n" + parts[0] + "\n" + parts[2];
                        parsedlisst.add(parse);
                    }
                }
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reclist = (RecyclerView) findViewById(R.id.rememberreclist);


        mAdapter = new recAdapter(parsedlisst);
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mAdapter);
        reclist.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Remember.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = reclist.findChildViewUnder(e.getX(), e.getY());
                    pos = reclist.getChildAdapterPosition(child);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remember.this);
                    String toparse = rememberlist.get(pos);
                    final String[] parts = toparse.split(",");
                    builder.setTitle("Remove");
                    builder.setMessage("Would you like to remove  " + parts[0] + " from Remember");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            //remove the data from firebase
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("remeb").child(parts[0]).removeValue();
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }


            });

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View ChildView;

                ChildView = rv.findChildViewUnder(e.getX(), e.getY());

                if (ChildView != null && gestureDetector.onTouchEvent(e)) {
                    pos = rv.getChildAdapterPosition(ChildView);
                    String toparse = parsedlisst.get(pos);
                    final String[] parts = toparse.split("\n");
                    //start the correct activity base on what information is (google, Youtube, List)
                    if (parts[0].equals("Google Search")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(parts[2]));
                        startActivity(browserIntent);
                    } else if (parts[0].equals("Youtube video")) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("id", parts[2]);
                        map.put("title", parts[1]);
                        Intent intent = new Intent(Remember.this, Youtube.class);
                        intent.putExtra("search", map);
                        startActivity(intent);
                    } else if (parts[0].equals("List")) {
                        StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(parts[1] + ".txt");

                        final File fileNameOnDevice = new File(getApplicationContext().getFilesDir() + "dlfile.txt");
                        dlref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                String line = null;
                                StringBuilder sb = new StringBuilder();
                                String dis;

                                try {
                                    FileReader fileReader = new FileReader(fileNameOnDevice.getAbsolutePath().toString());

                                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                                    while ((line = bufferedReader.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }
                                    sb = sb.deleteCharAt(sb.length() - 1);
                                    dis = sb.toString();
                                    if (fileNameOnDevice != null)
                                        fileNameOnDevice.delete();

                                    bufferedReader.close();
                                    Intent sendintent = new Intent(Remember.this, addNote.class);
                                    sendintent.putExtra("listtoedit", dis);
                                    sendintent.putExtra("name", parts[1]);
                                    //finish();
                                    startActivity(sendintent);
                                } catch (Exception ex) {
                                    Log.d("notesActivity", "File exception " + ex);
                                }
                            }
                        });
                    }
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
}
