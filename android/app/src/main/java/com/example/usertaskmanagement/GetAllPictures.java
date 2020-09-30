package com.example.usertaskmanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class GetAllPictures extends AppCompatActivity implements RecyclerForMedia.ItemClickListener{
    private String project_id;
    private DatabaseReference mPictureDatabase;
    private FirebaseRecyclerAdapter fireballRecyclerAdapter;
    private RecyclerView mResultList;
    public ArrayList<String> Url_list = new ArrayList<String>();
    public ArrayList<String> Time_list = new ArrayList<String>();
    private SharedPreferences pref;
    private ArrayList<Pictures> MediaArray=new ArrayList<Pictures>();
    private RecyclerForMedia adapter;

    private Context mContext;
    private Activity mActivity;
    private AsyncTask mMyTask;

    private CoordinatorLayout mCLayout;
    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent=new Intent(GetAllPictures.this, ListOfProjects.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_media_view);

        //to display navigation menu
        dl=(DrawerLayout)findViewById(R.id.dl);
        abdt=new ActionBarDrawerToggle(this,dl, R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(abdt);
        abdt.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final NavigationView navView=(NavigationView)findViewById(R.id.navbar);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id= menuItem.getItemId();
                if(id== R.id.item1)
                {
                    //remove this from all listofprojects
                    //write code to goto home activity
                    Intent mainIntent=new Intent(GetAllPictures.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(GetAllPictures.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(GetAllPictures.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });


        //tab activity triggers

        final RadioButton media= (RadioButton)findViewById(R.id.media);
        final RadioButton files=(RadioButton) findViewById(R.id.files);
        final RadioGroup tabGroup=(RadioGroup) findViewById(R.id.groupTabs);
        media.setChecked(true);
        tabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId== R.id.tasks)
                {
                    Intent mainIntent=new Intent(GetAllPictures.this, ProjectDisplay.class);
                    mainIntent.putExtra("projectid",project_id);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
                else if (checkedId== R.id.files)
                {
                    Intent mainIntent=new Intent(GetAllPictures.this, GetAllFiles.class);
                    mainIntent.putExtra("projectid",project_id);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            }
        });


        project_id = getIntent().getStringExtra("projectid");
        //project_id="-LuHSMEYDT4mol7p9qYJ";

        mContext = getApplicationContext();
        mActivity = GetAllPictures.this;

        // Get the widget reference from XML layout
       // mCLayout = (CoordinatorLayout) findViewById(R.id.);
        Log.d("msg",project_id);


        pref = getSharedPreferences("User", MODE_PRIVATE);
        String user_id = pref.getString("uid", null);

        String uri = pref.getString("photoURL", "");
        SharedPreferences prefs = this.getSharedPreferences(
                "User", Context.MODE_PRIVATE);
        View header=navView.getHeaderView(0);
        ImageView profilePic= (ImageView)header.findViewById(R.id.badge);
        String uri_new = prefs.getString("photoURL", "");
        //   Toast.makeText(EditProfileActivity.this, uri,
        //           Toast.LENGTH_SHORT).show();
        // Toast.makeText(EditProfileActivity.this, photoUrl.toString(),
        //        Toast.LENGTH_SHORT).show();

        if(uri != null){
            Glide.with(this /* context */)
                    .load(uri_new.toString())
                    .into(profilePic);

        }

        //String user_id = "0e6d02b6-2750-4d85-992f-9923cf4e5350";

        String myurl = "https://mcc-fall-2019-g03.appspot.com/project/attachments?projectId="+project_id+"&userid="+user_id+"&attachmentType=media";

        Log.d("msg",myurl);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String responseString = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    // Do normal input or output stream reading
                    //responseString = conn.getContent().toString();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = new String();

                    for (String line; (line = br.readLine()) != null; response += line) ;
                    responseString = response;
                } else {
                    String response = "FAILED"; // See documentation for more info on response handling
                }
            } catch (IOException e) {
                //TODO Handle problems..

            }
            Log.d("msg", responseString);

            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(responseString);
                Log.d("msg", jsonarray.toString());
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    String type = jsonobject.getString("type");
                    String url = jsonobject.getString("url");
                    String createdTime = jsonobject.getString("createdTime");
                    MediaArray.add(new Pictures(createdTime, type, url));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }



        //Log.d("msg123", MediaArray.get(0).url);
        // set up the RecyclerView
        set(MediaArray);
    }

    public void set(ArrayList<Pictures> MediaArray){

        RecyclerView recyclerView = findViewById(R.id.allpictures);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerForMedia(this, MediaArray);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(GetAllPictures.this);

        final CharSequence[] items = {"Original Quality", "High Quality", "Low Quality"};

        //set the title for alert dialog
        builder.setTitle("Choose quality to download: ");

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override public void onClick(DialogInterface dialog, int item) {
                            String url=  MediaArray.get(position).getUrl();
                            //setting the button text to the selected itenm from the list
                            if(item == 0){
                                mMyTask = new DownloadTask()
                                        .execute(stringToURL(
                                                url
                                              //"https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb"
                                        ));

                            }else if(item == 1){

                                url = url.toString().replaceAll("\\b.png\\b", "@720.png");
                                Log.d("url",url);
                                mMyTask = new DownloadTask()
                                        .execute(stringToURL(
                                                url
                                                //"https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb"
                                        ));

                            }else if(item == 2){
                                url = url.toString().replaceAll("\\b.png\\b", "@100.png");
                                Log.d("url",url);
                                mMyTask = new DownloadTask()
                                        .execute(stringToURL(
                                                url
                                                //"https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb"
                                        ));
                            }
                        }
                    });

        //Creating CANCEL button in alert dialog, to dismiss the dialog box when nothing is selected
        builder .setCancelable(false)
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {

                    @Override  public void onClick(DialogInterface dialog, int id) {
                        //When clicked on CANCEL button the dalog will be dismissed
                        dialog.dismiss();
                    }
                });





        //Creating alert dialog
        AlertDialog alert =builder.create();

        //Showingalert dialog
        alert.show();

    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }




    private class DownloadTask extends AsyncTask<URL,Void, Bitmap> {
        private ProgressDialog mProgressDialog;

        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog on async task start
            // Initialize the progress dialog
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setIndeterminate(true);
            // Progress dialog horizontal style
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Progress dialog title
            mProgressDialog.setTitle("Download image...");
            // Progress dialog message
            mProgressDialog.setMessage("Please wait, we are downloading your image file...");

            mProgressDialog.show();
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            // Hide the progress dialog
            mProgressDialog.dismiss();


            if (result != null) {
                // Display the downloaded image into ImageView
                //mImageView.setImageBitmap(result);

                // Save bitmap to internal storage
                int length = 5;
                boolean useLetters = true;
                boolean useNumbers = false;
                String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
                 saveImageToInternalStorage(result, "test"+generatedString+".jpeg");
                 Toast.makeText(getApplicationContext(), "Saved successfully under /Downloads/attachments folder.", Toast.LENGTH_LONG).show();

                // Set the ImageView image from internal storage
                //mImageViewInternal.setImageURI(imageInternalUri);
            } else {
                // Notify user that an error occurred while downloading image
                // Snackbar.make(mCLayout,"Error",Snackbar.LENGTH_LONG).show();
            }
        }

    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    // Custom method to save a bitmap into internal storage
    protected void saveImageToInternalStorage(Bitmap imageToSave, String fileName){

        Log.d("filename",fileName);

        File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "attachments");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }



        final File direct = new File(rootPath, fileName);
        // Initialize ContextWrapper
        //File direct = new File(Environment.getExternalStorageDirectory() + "/TaskPlanner");

        /*if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/TaskPlanner/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/sdcard/TaskPlanner/", fileName);
        if (file.exists()) {
            file.delete();
        }*/
        try {
            FileOutputStream out = new FileOutputStream(direct);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}










