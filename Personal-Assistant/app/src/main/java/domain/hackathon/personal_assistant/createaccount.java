package domain.hackathon.personal_assistant;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class createaccount extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Button signup = (Button) findViewById(R.id.btncreate);
        final EditText Email = (EditText) findViewById(R.id.txtcreateemail);
        final EditText Password = (EditText) findViewById(R.id.txtcreatepassword);
        final EditText firstname = (EditText) findViewById(R.id.txtcfirstname);
        final EditText lastname = (EditText) findViewById(R.id.txtclastname);
        final EditText month = (EditText) findViewById(R.id.txtcmonth);
        final EditText day = (EditText) findViewById(R.id.txtcday);
        final EditText year = (EditText) findViewById(R.id.txtcyear);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(firstname.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(), "Please enter your first name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(lastname.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(), "Please enter your last name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (month.getText().toString().length() < 2)
                {
                    Toast.makeText(getApplicationContext(), "The month needs to be two characters. ex: 01", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (day.getText().toString().length() < 2)
                {
                    Toast.makeText(getApplicationContext(), "The day must be two characters. ex: 02", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (year.getText().toString().length() < 4)
                {
                    Toast.makeText(getApplicationContext(), "Year needs to be the full year! ex: 1942", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(createaccount.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(createaccount.this, "Create Account failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null)
                                    {
                                        String DOB = month.getText().toString() + "/" + day.getText().toString() + "/" + year.getText().toString();
                                        myRef.child("users").child(user.getUid()).child("profile").child("dob").setValue(DOB);
                                        myRef.child("users").child(user.getUid()).child("profile").child("last_name").setValue(lastname.getText().toString());
                                        myRef.child("users").child(user.getUid()).child("profile").child("name").setValue(firstname.getText().toString());

                                    }
                                    startActivity(new Intent(createaccount.this, Homescreen_nav.class));
                                    finish();
                                }
                            }
                        });
            }
        });


    }
}
