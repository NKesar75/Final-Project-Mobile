package domain.hackathon.personal_assistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class notesActivity extends AppCompatActivity {

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private recAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    View rootLayout;
    private int revealX;
    private int revealY;
    static List<String> list;
    List<String> listname;
    RecyclerView reclist;
    int pos;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference myRef;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth auth;
    String dis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        final Intent intent = getIntent();
        Homescreen_nav.whichlayout = "notes";
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        storage = FirebaseStorage.getInstance();

        storageRef = storage.getReference();
        auth = FirebaseAuth.getInstance();

        rootLayout = findViewById(R.id.root_layout);

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }

        reclist = (RecyclerView) findViewById(R.id.Reclist);

        list = new ArrayList<String>();
        listname = new ArrayList<String>();

        myRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid().toString()).child("list");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                listname.clear();
                String name;
                for (DataSnapshot lists : dataSnapshot.getChildren()) {
                    name = lists.getKey();
                    listname.add(name);
                }
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAdapter = new recAdapter(listname);
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mAdapter);
        reclist.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(notesActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child  = reclist.findChildViewUnder(e.getX(), e.getY());
                    pos = reclist.getChildAdapterPosition(child);
                    AlertDialog.Builder builder = new AlertDialog.Builder(notesActivity.this);
                    builder.setTitle("Modification");
                    builder.setMessage("what do you want to do with " + listname.get(pos));
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            ref.child(auth.getCurrentUser().getUid()).child("list").child(listname.get(pos)).removeValue();

                            StorageReference deleteref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(listname.get(pos) + ".txt");
                            deleteref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // File deleted successfully
                                    Toast.makeText(getApplicationContext(),"list been deleted", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getApplicationContext(),"Failed to delete the list", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    builder.setNeutralButton("Remember", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference reflisten =  ref.child("users").child(auth.getCurrentUser().getUid()).child("list").child(listname.get(pos));
                            reflisten.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String key = listname.get(pos);
                                    // .
                                    //$
                                    //[
                                    //]
                                    //#
                                    ///

                                    String characterstorid = ".$[]#/";
                                    FirebaseUser user = auth.getCurrentUser();


                                    for (int j = 0; j < characterstorid.length(); j++)
                                    {
                                        if (key.contains(String.valueOf(characterstorid.charAt(j))))
                                        {
                                            key = key.replace(String.valueOf(characterstorid.charAt(j)), "");
                                        }
                                    }

                                    //DatabaseReference reftosave = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("remeb");
                                    DatabaseReference reftosave = FirebaseDatabase.getInstance().getReference();

                                    reftosave.child("users").child(user.getUid()).child("remeb").child(key).setValue("List," + dataSnapshot.getValue());

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

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
                    readfile(listname.get(pos));
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

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fablist);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listname.clear();
                list.clear();
                mAdapter.notifyDataSetChanged();


                Intent intent = new Intent(notesActivity.this, addNote.class);
                finish();
                startActivity(intent);
            }
        });
    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });

            circularReveal.start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            unRevealActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    void readfile(final String name) {

        StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(name + ".txt");

        final File fileNameOnDevice = new File(getApplicationContext().getFilesDir() + "dlfile.txt");
        dlref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String line = null;
                StringBuilder sb = new StringBuilder();

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
                    Intent sendintent = new Intent(notesActivity.this, addNote.class);
                    sendintent.putExtra("listtoedit", dis);
                    sendintent.putExtra("name", listname.get(pos));
                    //finish();
                    startActivity(sendintent);
                } catch (Exception ex) {
                    Log.d("notesActivity", "File exception " + ex);
                }
            }
        });

    }
}
