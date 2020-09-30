package com.example.usertaskmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;

import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TaskImage extends AppCompatActivity {

    private ImageView projectImage;
    private Button uploadProjectImage;
    private Button uploadProjectFile;
    private Button browseProjectImage;
    private Button browseProjectFile;
    private TextView fileText;
    private static final String IMAGE_DIRECTORY = "/";

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private static final int PDF = 3, AUDIO=4;
    private int GALLERY = 1, CAMERA = 2;
    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int REQUEST_GALLERY_IMAGE = 1;
    public static final int REQUEST_PDF = 0;
    public static final int REQUEST_AUDIO= 1;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri contentURI;
    String projectid;
    String type = "";

    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_image);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            if (bundle.get("projectid") != null)
                projectid = bundle.get("projectid").toString();
            else
                projectid = "";

        storageReference = FirebaseStorage.getInstance().getReference();
        projectImage = (ImageView) findViewById(R.id.projectImage);
        fileText = (TextView) findViewById(R.id.fileText);

        requestMultiplePermissions();


        //to display navigation menu
        dl=(DrawerLayout)findViewById(R.id.dl);
        abdt=new ActionBarDrawerToggle(this,dl, R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(abdt);
        abdt.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final NavigationView navView=(NavigationView)findViewById(R.id.navbar);
        SharedPreferences prefs = this.getSharedPreferences(
                "User", Context.MODE_PRIVATE);

        View header=navView.getHeaderView(0);

        ImageView profilePic= (ImageView)header.findViewById(R.id.badge);
        String uri = prefs.getString("photoURL", "");
        //   Toast.makeText(EditProfileActivity.this, uri,
        //           Toast.LENGTH_SHORT).show();
        // Toast.makeText(EditProfileActivity.this, photoUrl.toString(),
        //        Toast.LENGTH_SHORT).show();

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
                    Intent mainIntent=new Intent(TaskImage.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(TaskImage.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(TaskImage.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });

        uploadProjectImage = (Button) findViewById(R.id.uploadProtectImage);
        uploadProjectFile = (Button) findViewById(R.id.uploadProjectFile);
        browseProjectImage = (Button) findViewById(R.id.browseImageProject);
        browseProjectFile = (Button) findViewById(R.id.browseFileProject);

        browseProjectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();

            }
        });

        browseProjectFile.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                showAttachmentDialog();

            }});

        uploadProjectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(projectid, "media");

            }
        });

        uploadProjectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileText.getText().equals(""))
                    Toast.makeText(TaskImage.this, "No attachment selected", Toast.LENGTH_SHORT).show();

                else if(type.equals("pdf")) {
                    try {
                     //   Toast.makeText(TaskImage.this, "pdf", Toast.LENGTH_SHORT).show();
                        uploadAttachment(projectid, "pdf");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        uploadAttachment(projectid, "audio");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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

    private void showAttachmentDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select pdf attachment",
                "Select audio file" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                type = "pdf";
                                choosePDF();
                                break;
                            case 1:
                                type="audio";
                                chooseAudio();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void choosePDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent,PDF);
    }
    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,AUDIO);
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
                    String path = saveImage(bitmap);
                //    Toast.makeText(TaskImage.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    projectImage.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(TaskImage.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else if (requestCode == CAMERA) {
            contentURI = data.getData();
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            contentURI = getImageUri(this, thumbnail);
            projectImage.setImageBitmap(thumbnail);
            saveImage(thumbnail);
         //   Toast.makeText(TaskImage.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode== PDF || requestCode== AUDIO ){
            contentURI = data.getData();
       //     Toast.makeText(TaskImage.this, data.getData().toString(), Toast.LENGTH_SHORT).show();

            String FilePath = data.getData().getPath();
            fileText.setText(FilePath);
        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    // Function to check and request permission

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

    private void uploadImage(String projectid, String type) {

        Toast.makeText(TaskImage.this, "Upload image called", Toast.LENGTH_SHORT).show();
        if(contentURI != null)
        {

       //     Toast.makeText(TaskImage.this,contentURI.toString(), Toast.LENGTH_SHORT).show();
           /* final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/
            final String id = projectid;
            final String name = UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child("project_media/").child(projectid + "/" + name + ".png");
            ref.putFile(contentURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Bitmap hochlade
                                      contentURI = null;
                                      Log.d("oak1", uri.toString());
                                      putRequest("https://mcc-fall-2019-g03.appspot.com/project/attachment?projectId=" + id,  "media",name, uri.toString() );
                                      projectImage.setImageBitmap(null);
                                      //      Toast.makeText(TaskImage.this, "Uploaded" + uri.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });

                            //progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // progressDialog.dismiss();
                            Toast.makeText(TaskImage.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void uploadAttachment(String projectid, String type) throws FileNotFoundException {

      //  Toast.makeText(TaskImage.this, "Upload file called", Toast.LENGTH_SHORT).show();

        InputStream stream;
        if(type.equals("pdf") && contentURI!=null)
        {
         //   Toast.makeText(TaskImage.this, "pdf", Toast.LENGTH_SHORT).show();

           /* final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/
            final String id = projectid;
            final String name = UUID.randomUUID().toString();
      //      final StorageReference ref = storageReference.child("project_attachments/").child(projectid + "/" + UUID.randomUUID().toString());
            final StorageReference ref = storageReference.child("/project_attachments/").child(projectid + "/" + name);
            ref.putFile(contentURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           // Toast.makeText(TaskImage.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Bitmap hochlade
                                   contentURI = null;
                                   fileText.setText("");
                                   putRequest("https://mcc-fall-2019-g03.appspot.com/project/attachment?projectId=" + id, "pdf"  , name,uri.toString());
                                //    Toast.makeText(TaskImage.this, "Uploaded" + uri.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });

                            //progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // progressDialog.dismiss();
                            Toast.makeText(TaskImage.this, "Upload Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if(type.equals("audio")&& contentURI!=null)
        {
           /* final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/
            final String id = projectid;
            final String name = UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child("/project_audio/").child(projectid + "/" + name);
            ref.putFile(contentURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Bitmap hochlade
                                    contentURI = null;
                                    fileText.setText("");
                                    putRequest("https://mcc-fall-2019-g03.appspot.com/project/attachment?projectId=" + id , "audio"  , name, uri.toString());
                               //     Toast.makeText(TaskImage.this, "Uploaded" + uri.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });

                            //progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // progressDialog.dismiss();
                            Toast.makeText(TaskImage.this, "Upload Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void putRequest(String url,String type, String name, String attach_url) {


     //   Toast.makeText(TaskImage.this, url, Toast.LENGTH_LONG).show();
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = new JSONObject();
        try {
            postdata.put("attachment_type", type);
            postdata.put("attachment_name",name );
            postdata.put("attachment_url",attach_url );

            Log.d("msg",postdata.toString());
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .put(body)
                .url(url)
                .build();

        try {

            Response responses =  client.newCall(request).execute();
            Toast.makeText(TaskImage.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            //  Toast.makeText(TaskImage.this,responses.body().string() , Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

