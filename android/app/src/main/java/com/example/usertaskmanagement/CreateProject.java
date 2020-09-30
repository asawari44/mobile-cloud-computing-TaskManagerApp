package com.example.usertaskmanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CreateProject extends AppCompatActivity {

    EditText name;
    EditText description;
    EditText deadline;
    ImageView badge;
    String keywords="";
    Button create;
    Calendar myCalendar = Calendar.getInstance();
    int GALLERY = 1, CAMERA = 2;
    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private DatabaseReference projectDB ;
    FirebaseDatabase taskDB;
    Uri contentURI;
    String type;
    private String current_user;
    private SharedPreferences pref;
    private RadioGroup radioGroup;
    private SharedPreferences.Editor editor;
    private Uri imageURL;;
    private Uri imageuri;
    private String badge_url;
    private CheckBox checkbox_1, checkbox_2, checkbox_3;
    private EditText keyword_1, keyword_2,keyword_3;
    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;

    @Override
    public void onBackPressed()
    {

        super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_project);
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        deadline = (EditText) findViewById(R.id.deadline);
        badge = (ImageView) findViewById(R.id.badge);
        //keywords = (EditText) findViewById(R.id.keywords);
        //checkbox_1 = (CheckBox) findViewById(R.id.keyword_android);
        //checkbox_2 = (CheckBox) findViewById(R.id.keyword_website);
        //checkbox_3 = (CheckBox) findViewById(R.id.keyword_others);

        keyword_1 = (EditText)findViewById(R.id.keyword_1);
        keyword_2 = (EditText)findViewById(R.id.keyword_2);
        keyword_3 = (EditText)findViewById(R.id.keyword_3);


        create = (Button) findViewById(R.id.create);
        pref = getSharedPreferences("User", MODE_PRIVATE);
        current_user = pref.getString("uid", null);

        //to display navigation menu
        dl=(DrawerLayout)findViewById(R.id.dl);
        abdt=new ActionBarDrawerToggle(this,dl, R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(abdt);
        abdt.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final NavigationView navView=(NavigationView)findViewById(R.id.navbar);

        View header=navView.getHeaderView(0);

        ImageView profilePic= (ImageView)header.findViewById(R.id.badge);
        String uri = pref.getString("photoURL", "");
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
                    Intent mainIntent=new Intent(CreateProject.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(CreateProject.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(CreateProject.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });




        Log.d("msg", current_user.toString());

        radioGroup= (RadioGroup) findViewById(R.id.type);


        final boolean click = true;
        Dialog myDialog;


        Intent intent=getIntent();
        //fullname= intent.getStringExtra(projectID);



        mAuth=FirebaseAuth.getInstance();
        //userAuthDB = FirebaseDatabase.getInstance().getReference().child("projects");
        taskDB = FirebaseDatabase.getInstance();
        projectDB = taskDB.getReference().child("projects");
        // storage = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        deadline.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CreateProject.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        myDialog = new Dialog(this);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                boolean valid=validate();
                if (valid){

                //Log.d("image url", imageuri.toString());

                    keywords = keyword_1.getText().toString()+","+keyword_2.getText().toString()+","+keyword_3.getText().toString();
                    Log.d("msg", keywords);

                if (radioGroup.getCheckedRadioButtonId() == R.id.personal)
                {
                    type =  "Personal";
                }else{
                    type= "Group";
                }

                    imageuri=uploadImage(name.getText().toString());
                    Log.d("valid",Boolean.toString(valid));



                    if(contentURI == null){
                        makeRequest("");
                        Toast.makeText(getApplicationContext(), "Project created Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                   // finish();
                    // Storing string



                }

            }
        });

        badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] listItems = getResources().getStringArray(R.array.shopping_item);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CreateProject.this);
                mBuilder.setTitle("Select Options");
                mBuilder.setItems(listItems ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switch (i) {
                            case 0:
                                //you code for button at 0 index click
                                Intent intent_cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent_cam, CAMERA);

                                break;
                            case 1:
                                //you code for button at 1 index click
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY);
                                break;
                            case 2:
                                dialogInterface.dismiss();
                                break;
                            default:
                                break;
                        }

                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
                //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //myDialog.show();


            }
        });



    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        deadline.setText(sdf.format(myCalendar.getTime()));
    }


    private boolean validate() {
        boolean valid = true;

        String Name = name.getText().toString();
        String Description = description.getText().toString();
        String Deadline = deadline.getText().toString();
        String keyword1v = keyword_1.getText().toString();
        String keyword2v = keyword_2.getText().toString();
        String keyword3v = keyword_3.getText().toString();

        if (Name.isEmpty()) {
            name.setError("enter a valid name for the project");
            valid = false;
        } else {
            name.setError(null);
        }

        if (Description.isEmpty()) {
            description.setError("enter a valid description");
            valid = false;
        } else {
            description.setError(null);
        }


        if (Deadline.isEmpty()) {
        deadline.setError("enter a valid name for the project");
        valid = false;
        }else{
        deadline.setError(null);
        }
        if(!keyword1v.isEmpty()){
            if(!keyword1v.matches("[a-zA-Z0-9]*")||keyword1v.length()>=20){
                keyword_1.setError("Keyword should only contain letters and numbers and should be less than 20 characters");
                valid = false;
            }
        }
        else{
            keyword_1.setError(null);
        }
        if(!keyword2v.isEmpty()){
            if(!keyword2v.matches("[a-zA-Z0-9]*")||keyword2v.length()>=20){
                keyword_2.setError("Keyword should only contain letters and numbers and should be less than 20 characters");
                valid = false;
            }
        }
        else{
            keyword_2.setError(null);
        }
        if(!keyword3v.isEmpty()){
            if(!keyword3v.matches("[a-zA-Z0-9]*")||keyword3v.length()>=20){
                keyword_3.setError("Keyword should only contain letters and numbers and should be less than 20 characters");
                valid = false;
            }
        }
        else{
            keyword_3.setError(null);
        }


       /*if (!Deadline.isEmpty()){

            Date selected_deadline = new Date(Deadline);
           String myFormat = "dd/MM/yy"; //In which you need put here
           SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

           deadline.setText(sdf.format(myCalendar.getTime()));
            Date date = new Date();
            Log.d("dates: ",date.toString()+"and"+selected_deadline.toString());
            if(selected_deadline.before(date)){
                deadline.setError("enter a valid deadline(date after today)");
            }
        }*/

        return valid;
    }
    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
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
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
            }
        })
                .onSameThread()
                .check();
    }
    //public void onPermissionRationaleShouldBeShown(List&lt;PermissionRequest&gt; permissions, PermissionToken token)


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
                    //String path = saveImage(bitmap);
                    Toast.makeText(CreateProject.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    badge.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateProject.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            //badge.setAlpha(1);
            badge.setScaleX(1);
            badge.setScaleY(1);
            badge.setTranslationX(0);
            badge.setTranslationY(0);
            badge.setRotation(0);
            badge.setRotationY(0);
            badge.setRotationX(0);
            badge.setImageBitmap(thumbnail);
            //saveImage(thumbnail);
            Toast.makeText(CreateProject.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri uploadImage(String projectname) {
        //Toast.makeText(CreateProject.this, "Upload image called", Toast.LENGTH_SHORT).show();
        if(contentURI != null)
        {
           /* final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/

            final StorageReference ref = storageReference.child("badges/"+ projectname);
            ref.putFile(contentURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Bitmap hochladen
                                    imageURL = uri;
                                    Log.d("msg",imageURL.toString());
                                    //editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                                    //editor.putString("project_badgeURL",imageURL.toString());
                                    //editor.apply();
                                    makeRequest(imageURL.toString());
                                    Toast.makeText(CreateProject.this, "Uploaded" + imageURL.toString(), Toast.LENGTH_SHORT).show();
                                    finish();


                                }
                            });

                            //progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // progressDialog.dismiss();
                            Toast.makeText(CreateProject.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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


        return imageURL;
    }

    private void makeRequest(String imageURL) {
        String url = "https://mcc-fall-2019-g03.appspot.com/project/create";

        OkHttpClient client = new OkHttpClient();
/*                   {
                       "name":"project132",
                           "deadline":"23-12-2019",
                           "owner":"aashir",
                           "type":"abc",
                           "description":"New projkect",
                           "keywords":"safas,fasfasdf,fasfasf,sdfdasfas"
                   }*/
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", name.getText().toString());
            postdata.put("deadline",deadline.getText().toString() );
            postdata.put("owner", current_user.toString());
            postdata.put("type", type);
            postdata.put("badge", imageURL);
            postdata.put("isMedia", "false");
            postdata.put("starred", "false");
            postdata.put("description", description.getText().toString());
            postdata.put("keywords", keywords);


            Log.d("msg",postdata.toString());
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.d("failure Response", mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // String mMessage = response.body().string();
                try {
                    JSONObject jsonObj = new JSONObject(response.body().string());
                    String project_id = jsonObj.getString("id");
                    Log.d("response", project_id);
                    //Log.d("response", jsonObj.toString());

                    //put project related data
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("project_name", name.getText().toString());
                    editor.putString("project_id", project_id);
                    editor.putString("project_deadline", deadline.getText().toString());
                    editor.putString("project_owner", current_user);
                    editor.putString("type", type);
                    editor.commit();
                    //

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }


}