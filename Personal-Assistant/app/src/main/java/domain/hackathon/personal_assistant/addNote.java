package domain.hackathon.personal_assistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class addNote extends AppCompatActivity {
    EditText add, name;
    Button save;
    Intent recintent;
    FirebaseAuth auth;
    DatabaseReference myRef;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        add = (EditText) findViewById(R.id.addlist);
        name = (EditText) findViewById(R.id.listname);
        save = (Button) findViewById(R.id.btnsave);
        recintent = getIntent();
        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        storage = FirebaseStorage.getInstance();

        storageRef = storage.getReference();

        if (recintent.hasExtra("listtoedit")) {
            add.setText(recintent.getStringExtra("listtoedit"));
            name.setText(recintent.getStringExtra("name"));
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!add.getText().toString().equals("")) {

//                    if (recintent.hasExtra("listtoedit"))
//                    {
//                        Intent intent = new Intent(addNote.this, notesActivity.class);
//                        intent.putExtra("replace", add.getText().toString());
//                        finish();
//                        startActivity(intent);
//                    }
                    //else {
                    Intent intent = new Intent(addNote.this, notesActivity.class);
                    //intent.putExtra("add", add.getText().toString());
                    //intent.putExtra("addname", name.getText().toString());


                    uploadfile();

                    finish();
                    startActivity(intent);
                    //}

                }
            }
        });

    }

    void uploadfile() {

        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("filetoupload.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(add.getText().toString());
            outputStreamWriter.close();


            // /data/user/0/domain.hackathon.personal_assistant/files/filetoupload.txt
            Uri urifile = Uri.fromFile(new File("/data/user/0/domain.hackathon.personal_assistant/files/filetoupload.txt"));
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("Text/Plain").build();
            //Uri file = Uri.fromFile(File.createTempFile("/data/user/0/domain.hackathon.personal_assistant/files/filetoupload","txt"));
            StorageReference riversRef = storageRef.child("text-files/").child(auth.getCurrentUser().getUid().toString()).child(name.getText().toString() + ".txt");
            riversRef.putFile(urifile, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseUser user = auth.getCurrentUser();
                    String duri = taskSnapshot.getDownloadUrl().toString();
                    myRef.child("users").child(user.getUid()).child("list").child(name.getText().toString()).setValue(duri);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("addNote", "File exception" + e);
                    Toast.makeText(getApplicationContext(), "fail to upload file", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {

        }

    }

    void readfile() {

        StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(name.getText().toString());
        final File fileNameOnDevice = new File("/data/user/0/domain.hackathon.personal_assistant/files/dlfile.txt");
        dlref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String line = null;

                try {
                    // FileReader reads text files in the default encoding.
                    FileReader fileReader = new FileReader(fileNameOnDevice.getAbsolutePath().toString());

                    // Always wrap FileReader in BufferedReader.
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    while ((line = bufferedReader.readLine()) != null) {
                        String[] parts = line.split("\n");
                        name.setText(parts[0]);
                        add.setText(parts[1]);
                    }

                    // Always close files.
                    bufferedReader.close();
                } catch (Exception ex) {

                }
            }
        });
    }

}
