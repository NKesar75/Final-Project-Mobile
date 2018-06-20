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


    RecyclerView reclist;
    RecyclerView recYlist;
    RecyclerView recLlist;
    DatabaseReference myRef;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth auth;
    StorageReference storageRef;
    FirebaseStorage storage;
    private rememberAdapter mgAdapter;
    private rememberAdapter myAdapter;
    private rememberAdapter mlAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mYoutubeLayoutManager;
    private RecyclerView.LayoutManager mListLayoutManager;

    int pos;
    List<rememberDisplay> googlelist;
    List<rememberDisplay> youtubelist;
    List<rememberDisplay> listlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remember);


        Homescreen_nav.whichlayout = "notes";
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        googlelist = new ArrayList<>();
        youtubelist = new ArrayList<>();
        listlist = new ArrayList<>();


        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        auth = FirebaseAuth.getInstance();

        //get the information from firebase
        myRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid().toString()).child("remeb");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clear the current data so there is no duplicates
                googlelist.clear();
                youtubelist.clear();
                listlist.clear();
                String thingtoremember;
                for (DataSnapshot lists : dataSnapshot.getChildren()) {
                    thingtoremember = lists.getKey();
                    String parse = thingtoremember + "," + lists.getValue();
                    String[] parts = parse.split(",");
                    rememberDisplay temp = new rememberDisplay(parts[1], parts[0], parts[2]);
                    if (temp.type.equals("Google"))
                        googlelist.add(temp);
                    else if (temp.type.equals("Youtube"))
                        youtubelist.add(temp);
                    else if (temp.type.equals("List"))
                        listlist.add(temp);

                }
                mgAdapter.notifyDataSetChanged();
                mlAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reclist = (RecyclerView) findViewById(R.id.remembergreclist);
        recYlist = (RecyclerView) findViewById(R.id.rememberyreclist);
        recLlist = (RecyclerView) findViewById(R.id.rememberlreclist);


        mgAdapter = new rememberAdapter(googlelist, getApplicationContext());
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mgAdapter);
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
                    final rememberDisplay toparse = googlelist.get(pos);
                    builder.setTitle("Remove");
                    builder.setMessage("Would you like to remove  " + toparse.title + " from Remember");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            //remove the data from firebase
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("remeb").child(toparse.title).removeValue();
                            mgAdapter.notifyDataSetChanged();
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
                    rememberDisplay toparse = googlelist.get(pos);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(toparse.body));
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

        myAdapter = new rememberAdapter(youtubelist, getApplicationContext());
        mYoutubeLayoutManager = new LinearLayoutManager(this);
        recYlist.setLayoutManager(mYoutubeLayoutManager);
        recYlist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recYlist.setItemAnimator(new DefaultItemAnimator());
        recYlist.setAdapter(myAdapter);
        recYlist.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Remember.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = recYlist.findChildViewUnder(e.getX(), e.getY());
                    pos = recYlist.getChildAdapterPosition(child);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remember.this);
                    final rememberDisplay toparse = youtubelist.get(pos);
                    builder.setTitle("Remove");
                    builder.setMessage("Would you like to remove  " + toparse.title + " from Remember");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            //remove the data from firebase
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("remeb").child(toparse.title).removeValue();
                            myAdapter.notifyDataSetChanged();
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
                    rememberDisplay toparse = youtubelist.get(pos);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("id", toparse.body);
                    map.put("title", toparse.title);
                    Intent intent = new Intent(Remember.this, Youtube.class);
                    intent.putExtra("search", map);
                    startActivity(intent);
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

        mlAdapter = new rememberAdapter(listlist, getApplicationContext());
        mListLayoutManager = new LinearLayoutManager(this);
        recLlist.setLayoutManager(mListLayoutManager);
        recLlist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recLlist.setItemAnimator(new DefaultItemAnimator());
        recLlist.setAdapter(mlAdapter);
        recLlist.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Remember.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = recLlist.findChildViewUnder(e.getX(), e.getY());
                    pos = recLlist.getChildAdapterPosition(child);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remember.this);
                    final rememberDisplay toparse = listlist.get(pos);
                    builder.setTitle("Remove");
                    builder.setMessage("Would you like to remove  " + toparse.title + " from Remember");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            //remove the data from firebase
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("remeb").child(toparse.title).removeValue();
                            mlAdapter.notifyDataSetChanged();
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
                    final rememberDisplay toparse = listlist.get(pos);
                    StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(toparse.title + ".txt");

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
                                sendintent.putExtra("name", toparse.title);
                                //finish();
                                startActivity(sendintent);
                            } catch (Exception ex) {
                                Log.d("notesActivity", "File exception " + ex);
                            }
                        }
                    });
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
