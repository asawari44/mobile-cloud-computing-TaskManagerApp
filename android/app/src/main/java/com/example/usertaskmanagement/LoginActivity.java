package com.example.usertaskmanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URI;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private EditText emailLogin;
    private EditText passwordLogin;
    private TextView statusText;
    private Button login;
    private TextView signupLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailLogin  = (EditText)findViewById(R.id.emailLogin);
        passwordLogin = (EditText)findViewById(R.id.passwordLogin) ;
        statusText = (TextView)findViewById(R.id.statusLogin) ;
        login = (Button)findViewById(R.id.login) ;
        signupLogin= (TextView)findViewById(R.id.signupLogin);

        mAuth = FirebaseAuth.getInstance();
          if(mAuth.getCurrentUser()!= null){
            //   Toast.makeText(LoginActivity.this,"user is not logged out",
             //         Toast.LENGTH_SHORT).show();

            Intent mainIntent=new Intent(LoginActivity.this,ListOfProjects.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   login();
            }
        });
        signupLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    } // on create

    private void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
          //  onLoginFailed();
            return;
        }

        login.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = emailLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        // firebase auth

          mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                         //   Toast.makeText(LoginActivity.this,user.getDisplayName() + user.getPhotoUrl() +  "Authentication successful.",
                          //          Toast.LENGTH_SHORT).show();
                         //   onLoginSuccess();



                            SharedPreferences.Editor editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                            editor.putString("displayname", user.getDisplayName());
                            editor.putString("uid",user.getUid());
                            if(user.getPhotoUrl() != null)
                                   editor.putString("photoURL",user.getPhotoUrl().toString() + "");
                            editor.putString("email", user.getEmail());
                            editor.apply();

                            Intent mainIntent=new Intent(LoginActivity.this,ListOfProjects.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                       //     Toast.makeText(LoginActivity.this, "Authentication failed.",
                       //             Toast.LENGTH_SHORT).show();

                        }
                        login.setEnabled(true);
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            login.setEnabled(true);
                            statusText.setText("Authentication Failed. Please try again");
                        }
                        progressDialog.dismiss();
                        // [END_EXCLUDE]
                    }

                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {


                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    private void onLoginSuccess() {
        login.setEnabled(true);
        //Set User Profile in loggeninuser and add object to common database
        Intent mainIntent=new Intent(LoginActivity.this,SearchActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        // finish();
    }



    private boolean validate() {
        boolean valid = true;

        String email = emailLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLogin.setError("enter a valid email address");
            valid = false;
        } else {
            emailLogin.setError(null);
        }

        if (password.isEmpty() || password.length() < 10 || password.length() > 40) {
            passwordLogin.setError("enter a valid password");
            valid = false;
        } else {
            passwordLogin.setError(null);
        }

        return valid;
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(mAuth.getCurrentUser()!= null){
            Intent mainIntent=new Intent(LoginActivity.this,ListOfProjects.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }
}
