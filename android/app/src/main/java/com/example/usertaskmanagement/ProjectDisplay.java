package com.example.usertaskmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ProjectDisplay extends AppCompatActivity {
    Button tasks;
    Button media;
    Button files;
    String project_id;

    ListView listView;
    Button add;
    private ArrayList<String> TaskList = new ArrayList<String>();;
    private ArrayList<Task> taskArray= new ArrayList<Task>();
    private List<String> TaskList_1, TaskList_2;

    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_display);

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
                    Intent mainIntent=new Intent(ProjectDisplay.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(ProjectDisplay.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(ProjectDisplay.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });

        //for floating action bar1
        FloatingActionButton add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mainIntent=new Intent(ProjectDisplay.this, CreateTask.class);
                mainIntent.putExtra("projectid",project_id);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });

        //tab activity triggers

        final RadioButton media= (RadioButton)findViewById(R.id.media);
        final RadioButton tasks= (RadioButton)findViewById(R.id.tasks);
        final RadioButton files=(RadioButton) findViewById(R.id.files);
        tasks.setChecked(true);
        final RadioGroup tabGroup=(RadioGroup) findViewById(R.id.groupTabs);
        tabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId== R.id.media)
                {
                    Intent mainIntent=new Intent(ProjectDisplay.this, GetAllPictures.class);
                    mainIntent.putExtra("projectid",project_id);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
                else if (checkedId== R.id.files)
                {
                    Intent mainIntent=new Intent(ProjectDisplay.this, GetAllFiles.class);
                    mainIntent.putExtra("projectid",project_id);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            }
        });


        listView = (ListView)findViewById(R.id.get_list);

        //project_id = "-LuHSMEYDT4mol7p9qYZ";
        project_id = getIntent().getStringExtra("projectid");
        Log.d("msg",project_id);

        SharedPreferences pref = getSharedPreferences("User", MODE_PRIVATE);
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


        //listView = (ListView)findViewById(R.id.get_task_view_id);
      /*  add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent=new Intent(ProjectDisplay.this, CreateTask.class);
                mainIntent.putExtra("projectid",project_id);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });

        media = (Button)findViewById(R.id.media);
        media.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent mainIntent=new Intent(ProjectDisplay.this, GetAllPictures.class);
                mainIntent.putExtra("projectid",project_id);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });

       */

        FloatingActionButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                taskArray.clear();
                new AddStringTask().execute( taskArray);
            }
        });


        new AddStringTask().execute( taskArray);

        /*final Handler handler = new Handler();

        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                taskArray.clear();
                new AddStringTask().execute( taskArray);
                handler.postDelayed(this, 2 * 1000);
            }
        };

        handler.postDelayed(refresh, 2 * 1000);

         */




    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }



    class AddStringTask extends AsyncTask<ArrayList, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            //Log.d("msg",adapter2.getItem(0).description.toString());
            try {

                JSONArray jsonarray = new JSONArray(result);
                //Log.d("msg",jsonarray.toString());
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    String name = "Task: "+jsonobject.getString("description");
                    String deadline ="Deadline: "+ jsonobject.getString("deadline");
                    String status = jsonobject.getString("status");
                    String id = jsonobject.getString("id");
                    taskArray.add(new Task(name, deadline, status, id));
                    //Log.d("name",name);
                    if(status == "on-going"){
                        TaskList_1.add(name);
                        taskArray.add(new Task(name, deadline, status, id));
                    }
                    if(status == "completed"){
                        TaskList_2.add(name);
                    }
                }
                //Log.d("msg",TaskList_1.toString());
                //Log.d("msg",TaskList_2.toString());



                //taskArray= new ArrayList<>();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            CustomAdapter adapter;
            adapter= new CustomAdapter(taskArray, project_id, ProjectDisplay.this);
            listView.setAdapter(adapter);

            //adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(ArrayList... arrayLists) {
            String myurl = "https://mcc-fall-2019-g03.appspot.com/task/?projectId="+project_id;


            String responseString = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                    // Do normal input or output stream reading
                    //responseString = conn.getContent().toString();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = new String();

                    for (String line; (line = br.readLine()) != null; response += line);
                    responseString = response;
                 }
                else {
                    String response = "FAILED"; // See documentation for more info on response handling
                }
            } catch (IOException e) {
                //TODO Handle problems..

            }
            //Log.d("msg", responseString);
            return responseString;



        }
    }

}


