package com.example.usertaskmanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private TextView emailEdit;
    private EditText passwordEdit;
    private TextView displayNameEdit;
    private Button submitEdit;
    private Button uploadImageEdit;
    private ImageView displayImageEdit;
    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference userAuthDB, userDB ;
    private FirebaseUser user;
    boolean imageEdit = false;
    FirebaseStorage storage;
    StorageReference storageReference;
    private int GALLERY = 1, CAMERA = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    Uri contentURI;
    String name;
    SharedPreferences.Editor editor;

    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;
    private DatabaseReference current_user_db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mAuth=FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user = mAuth.getCurrentUser();
        userAuthDB = FirebaseDatabase.getInstance().getReference().child("users");

        Uri imageUri = user.getPhotoUrl();
        mProgress=new ProgressDialog(this,R.style.Theme_AppCompat_DayNight_Dialog);

        emailEdit=(TextView)findViewById(R.id.emailEdit);
        passwordEdit=(EditText) findViewById(R.id.passwordEdit);
        displayNameEdit=(TextView) findViewById(R.id.displayNameEdit);
        submitEdit=(Button) findViewById(R.id.submitEdit);
        uploadImageEdit=(Button) findViewById(R.id.uploadImageEdit);
        displayImageEdit = (ImageView ) findViewById(R.id.imageEdit);
        if (user != null) {
            // Name, email address, and profile photo Url
            name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            String uid = user.getUid();

            emailEdit.setText(email);
            displayNameEdit.setText(name);

            uploadImageEdit.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    imageEdit = true;
                    requestMultiplePermissions();
                }
            });


            submitEdit.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    startEdit();
                }
            });

            SharedPreferences prefs = this.getSharedPreferences(
                    "User", Context.MODE_PRIVATE);
            String uri = prefs.getString("photoURL", "");
         //   Toast.makeText(EditProfileActivity.this, uri,
         //           Toast.LENGTH_SHORT).show();
           // Toast.makeText(EditProfileActivity.this, photoUrl.toString(),
            //        Toast.LENGTH_SHORT).show();

            if(photoUrl != null){
                  Glide.with(this /* context */)
                        .load(photoUrl.toString())
                        .into(displayImageEdit);

            }

            //to display navigation menu
            dl=(DrawerLayout)findViewById(R.id.dl);

            abdt=new ActionBarDrawerToggle(this,dl,R.string.Open,R.string.Close);
            abdt.setDrawerIndicatorEnabled(true);
            dl.addDrawerListener(abdt);
            abdt.syncState();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            final NavigationView navView=(NavigationView)findViewById(R.id.navbar);


               View header=navView.getHeaderView(0);

            ImageView profilePic= (ImageView)header.findViewById(R.id.badge);

            if(uri != null){
                Glide.with(this /* context */)
                       .load(uri.toString())
                        .into(profilePic);

            }

            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    int id= menuItem.getItemId();
                    if(id== R.id.item1)
                    {
                        //remove this from all listofprojects
                        //write code to goto home activity
                        Intent mainIntent=new Intent(EditProfileActivity.this, ListOfProjects.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    }
                    else if(id== R.id.item2)
                    {
                        Intent mainIntent=new Intent(EditProfileActivity.this, EditProfileActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else if(id== R.id.item3)
                    {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences preferences =getSharedPreferences("User", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent mainIntent=new Intent(EditProfileActivity.this, LoginActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        finish();

                    }
                    return true;
                }
            });



        }
    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void startEdit() {
        if (passwordEdit.getText().length()>1) {
            if(validate()) {
                submitEdit.setEnabled(false);
                mProgress.show();
                String passwordval = passwordEdit.getText().toString();
                String user_id = mAuth.getCurrentUser().getUid();
                Uri picUri = Uri.parse("www.google.com");
                user.updatePassword(passwordval)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditProfileActivity.this, "Password Updated.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d("", "User password updated.");
                                    if(imageEdit)
                                        uploadImage(name);
                                    else {
                                        mProgress.show();
                                        //move to list of projects
                                    }
                                }
                            }
                        });
            }
        }
        else if(imageEdit)
            uploadImage(name);
        submitEdit.setEnabled(true);

    }

    private boolean validate() {
        if(passwordEdit.getText().length()>1) {
            if (passwordEdit.getText().length() < 10 || passwordEdit.getText().length() > 40) {
                passwordEdit.setError("enter a valid password");
                return false;
            } else {
                passwordEdit.setError(null);
                return true;
            }
        }
        return false;
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
                //    Toast.makeText(EditProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    displayImageEdit.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EditProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            contentURI = data.getData();
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            displayImageEdit.setImageBitmap(Bitmap.createScaledBitmap(thumbnail, 120, 120, false));

            Toast.makeText(EditProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImage(String username) {
        submitEdit.setEnabled(false);
        mProgress.show();
   //     Toast.makeText(EditProfileActivity.this, "Upload image called", Toast.LENGTH_SHORT).show();
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
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri).build();
                                    mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("", "User profile uri updated.");
                                            }
                                        }
                                    });;
                                    editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                                    editor.putString("photoURL",uri.toString());
                                    editor.apply();
                                    current_user_db = userAuthDB.child(mAuth.getUid());  // adding values to fb auth
                                    //current_user_db.updateChildren(One);
                                    current_user_db.child("profilePictureUrl").setValue(uri.toString());

                                    mProgress.dismiss();

                                    //move to list of projects
                                    Toast.makeText(EditProfileActivity.this, "Image Uploaded" , Toast.LENGTH_SHORT).show();

                                }
                            });

                            //progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // progressDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Image Upload Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

}
