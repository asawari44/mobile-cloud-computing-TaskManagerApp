package com.example.usertaskmanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.CharacterPredicate;
import org.apache.commons.text.RandomStringGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private EditText email;
    private EditText password;
    private EditText displayName;
    private Button signup;
    private Button uploadImage;
    private ImageView  displayImage;
    private ProgressDialog mProgress;
    private static final String IMAGE_DIRECTORY = "/";
    private int GALLERY = 1, CAMERA = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    Uri contentURI;
    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private DatabaseReference userAuthDB, userDB ;
    FirebaseDatabase taskDB;
    private Uri imageURL;
    boolean uploadSuccess= false;
    SharedPreferences.Editor editor;
    private DatabaseReference current_user_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!= null){
            Intent mainIntent=new Intent(SignupActivity.this,ListOfProjects.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }

        userAuthDB = FirebaseDatabase.getInstance().getReference().child("users");
        taskDB = FirebaseDatabase.getInstance();
        userDB = taskDB.getReference().child("users");
       // storage = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mProgress=new ProgressDialog(this,R.style.Theme_AppCompat_DayNight_Dialog);

        email=(EditText)findViewById(R.id.emailSignup);
        password=(EditText) findViewById(R.id.passwordSignup);
        displayName=(EditText) findViewById(R.id.displayNameSignup);
        signup=(Button) findViewById(R.id.signup);
        uploadImage=(Button) findViewById(R.id.uploadProfileImage);
        displayImage = (ImageView ) findViewById(R.id.profileImage);

        uploadImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                requestMultiplePermissions();

            }
        });


        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {

        if (!validate()) {
            onSignupFailed();
            return;
        }
        signup.setEnabled(false);
        mProgress.show();
        String emailval = email.getText().toString();
        String passwordval = password.getText().toString();
        final String displayname = displayName.getText().toString();

        mAuth.createUserWithEmailAndPassword(emailval, passwordval).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    String user_id = mAuth.getCurrentUser().getUid();
                    uploadImage(displayname);
                    Uri picUri = Uri.parse("www.google.com");
                    if(uploadSuccess)
                        picUri = imageURL;
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayname).build();
                    mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }
                    });;

                    current_user_db = userAuthDB.child(user_id);  // adding values to fb auth
                    //current_user_db.updateChildren(One);
                    current_user_db.child("displayName").setValue(displayname);

               //     Toast.makeText(getApplicationContext(), "" + picUri,
               //             Toast.LENGTH_SHORT).show();
               //     Toast.makeText(getApplicationContext(), " Signup Successful",
               //             Toast.LENGTH_SHORT).show();


                    editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                    editor.putString("displayname", displayname);
                    editor.putString("uid",mAuth.getCurrentUser().getUid());

                     editor.putString("email", mAuth.getCurrentUser().getEmail());
                //    editor.putInt("idName", 12);
                    editor.apply();
                    Intent mainIntent=new Intent(SignupActivity.this,ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    mProgress.dismiss();
                    signup.setEnabled(true);

                }
                else{
                    Log.w(TAG, "signUpWithEmail:failure", task.getException());
                    if(task.getException().toString().contains("email"))
                          email.setError(task.getException().getMessage());
              //      Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage(),
              //              Toast.LENGTH_SHORT).show();
              //       Toast.makeText(SignupActivity.this, " Signup unsuccessful",
              //              Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                    signup.setEnabled(true);



                }

            }
        });

    }


    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        signup.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;
        String emaill = email.getText().toString();
        String passwordd = password.getText().toString();
        String displayname = displayName.getText().toString();
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        if (emaill.equals("") || !emaill.matches(regex)) {
            email.setError("enter a valid email address");
            valid = false;
        }
        else {
            email.setError(null);
        }

        if (passwordd.equals("") || passwordd.length() <= 10 || passwordd.length() > 40) {
            password.setError("Enter a valid password. Password length should be between 10 and 40 characters ");
            valid = false;
        } else {
            password.setError(null);
        }
        if (displayname.equals("") || displayname.length() < 5) {
            displayName.setError("Display name should be greater than or equal to 5");
            valid = false;
        }
        else if(!displayname.matches("[a-zA-Z0-9]*")){
            displayName.setError("Display name should contain only letters and numbers.");
            valid = false;
        }
        else if(isDisplayNameAvailable(displayname) == true){ // condition to check display name is taken and compute new
            Log.e("DISPLAYNAMEAPI","not avail");
            String usernames[] = new String[3];
            usernames[0] = ""; usernames[1] = ""; usernames[2] = "";
            for(int i = 0; i<=2; i ++) {
                boolean usernameGenerated = false;
                while (usernameGenerated == false) {
                     String name = generateUsername(displayname, 5);
                    Log.e("generatedname", usernames[i]);
                     if(isDisplayNameAvailable(name) ){
                        continue;
                    }
                     else if(name.equals( usernames[0])||name.equals( usernames[1])||name.equals( usernames[2])){

                         continue;
                     }
                    else{
                         usernames[i] =name;
                         Log.e("generatedname", "generated");
                        usernameGenerated = true;
                    }
                }
            }
            valid= false;
            displayName.setError("This name is already taken. Please choose another one. Recommended names are: " + usernames[0] +  ", " + usernames[1] + ", " + usernames[2]);


        }
        else {
            displayName.setError(null);
        }
        return valid;
    }

    private boolean isDisplayNameAvailable(String displayname){
        //write code to check if username exists in db
        boolean available = false; // by default true means it is in db
        // set to false if the name is in db
        String url = "https://mcc-fall-2019-g03.appspot.com/isNameExist?name=" + displayname;
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        OkHttpClient client = new OkHttpClient();
        try {
            String body = client.newCall(request).execute( ).body().string().trim();
            if(body.equals("true")) {
                available = true;
                Log.e("avail", body);
            }
            else {
                available = false;
                Log.e("availfalse", body);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return available;
    }


    private String generateUsername(String displayname, int length){
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = displayname + RandomStringUtils.random(length, useLetters, useNumbers);
        return generatedString;
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            showPictureDialog();
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();

                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {

            if (data != null) {
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Toast.makeText(SignupActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    displayImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SignupActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            contentURI = getImageUri(this, thumbnail);
       //     Toast.makeText(SignupActivity.this, contentURI.toString(), Toast.LENGTH_SHORT).show();
            displayImage.setImageBitmap(Bitmap.createScaledBitmap(thumbnail, 120, 120, false));

            Toast.makeText(SignupActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }

    }
    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage(String username) {

    //    Toast.makeText(SignupActivity.this, "Upload image called", Toast.LENGTH_SHORT).show();
 //       Toast.makeText(SignupActivity.this, contentURI.toString() + "", Toast.LENGTH_SHORT).show();

        if(contentURI != null)
        {
           /* final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/

            final StorageReference ref = storageReference.child("profilepictures/"+ username);
            ref.putFile(contentURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Bitmap hochladen
                                    imageURL = uri;
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(imageURL).build();
                                    mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile uri updated.");
                                            }
                                        }
                                    });;
                                    editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                                    editor.putString("photoURL",imageURL.toString());
                                    editor.apply();
                                    current_user_db = userAuthDB.child(mAuth.getUid());  // adding values to fb auth
                                    //current_user_db.updateChildren(One);
                                    current_user_db.child("profilePictureUrl").setValue(imageURL.toString());




                                    Toast.makeText(SignupActivity.this, "Profile Created Successfully" + imageURL.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });

                            //progressDialog.dismiss();
                            }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                           // progressDialog.dismiss();
                            uploadSuccess = false;
                            Toast.makeText(SignupActivity.this, "Image Upload Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                  /*  .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    }); */
        }
        else{
           // Toast.makeText(SignupActivity.this, "content empty", Toast.LENGTH_SHORT).show();

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onStart() {

        super.onStart();
        if(mAuth.getCurrentUser()!= null){
            Intent mainIntent=new Intent(SignupActivity.this,ListOfProjects.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }

}
