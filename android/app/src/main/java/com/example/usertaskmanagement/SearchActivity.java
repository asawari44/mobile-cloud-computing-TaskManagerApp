package com.example.usertaskmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchActivity extends AppCompatActivity implements MyRecyclerViewAdapter.RecyclerViewClickListener, SearchView.OnQueryTextListener {
    Button create;
    Button editprof;
    Button logout;
    Button addmember;
    Button addattachment;
    Button addtasks;
    Button delete;
    Button report;
    FirebaseStorage storage;
    StorageReference storageReference;
    String userid;
    MyRecyclerViewAdapter adapter;
    String projectlist = null;
    String[] projectid,projectname,type,
            lastmodified ,
            badge,
            isMedia,
            starred,
            member1,
            member2,
            member3, owner,deadline;
    String searchtype = "keywords";
    String searchword="";
    String keyword = "";
    String[] keywords = null;
    SearchView editsearch;
    JsonArray projects=null;
    String keywordurl,projecturl,url;
    Gson gson;
    Request request;
    private RadioGroup radioGroup;
    private RadioButton byKeyword,byProjectName;
    OkHttpClient client;
    Response response;
    RecyclerView recyclerView;
    String getKeywordurl;
    Spinner spinner;
    private int code=0;

    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        SharedPreferences prefs = this.getSharedPreferences(
                "User", Context.MODE_PRIVATE);
        userid = prefs.getString("uid", "");

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
                    Intent mainIntent=new Intent(SearchActivity.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(SearchActivity.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(SearchActivity.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        editsearch = (SearchView) findViewById(R.id.searchView);
        spinner = (Spinner)findViewById(R.id.spinner);
        editsearch.setOnQueryTextListener(this);
        gson = new GsonBuilder().create();
        client = new OkHttpClient();

        getKeywordurl = "https://mcc-fall-2019-g03.appspot.com/user/projects?userId=" + userid + "&sortBy=name";

        recyclerView = findViewById(R.id.allProjectsAlpha);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        editsearch.setQueryHint("Search Projects");

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.byKeyword) {
                    searchtype = "keywords";

                    //   editsearch.setQueryHint((CharSequence)keywords);
                //    Toast.makeText(getApplicationContext(), "keyword",
                  //          Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.byName) {
                    searchtype = "name";
                 //   Toast.makeText(getApplicationContext(), "projectname",
                   //         Toast.LENGTH_SHORT).show();

                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                url = null;
                searchword = spinner.getSelectedItem().toString();
                url = "https://mcc-fall-2019-g03.appspot.com/project/keyword/get?keywords=" + searchword + "&userId=" + userid;



            //    Toast.makeText(getApplicationContext(), url + "", Toast.LENGTH_SHORT).show();
                request = null;
                request = new Request.Builder()
                        .url(url)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build();

                response = null;

                try {
                    response = client.newCall(request).execute();
                  //  Toast.makeText(getApplicationContext(), response.code() + "", Toast.LENGTH_SHORT).show();
                    code = response.code();
                    Log.e("code", code + "");
                    if (code == 200)
                        projectlist = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (code == 200) {
                    projects = gson.fromJson(projectlist, JsonArray.class);
                    projectid = new String[projects.size()];
                    projectname = new String[projects.size()];
                    type = new String[projects.size()];
                    lastmodified = new String[projects.size()];
                    badge = new String[projects.size()];
                    isMedia = new String[projects.size()];
                    starred = new String[projects.size()];
                    member1 = new String[projects.size()];
                    member2 = new String[projects.size()];
                    member3 = new String[projects.size()];
                    owner = new String[projects.size()];
                    deadline = new String[projects.size()];

                    for (int i = 0; i < projects.size(); i++) {
                        projectid[i] = projects.get(i).getAsJsonObject().get("id").toString().replace("\"", "");
                        projectname[i] = projects.get(i).getAsJsonObject().get("name").toString().replace("\"", "");
                        type[i] = projects.get(i).getAsJsonObject().get("type").toString().replace("\"", "").replace("\"", "");
                        lastmodified[i] = projects.get(i).getAsJsonObject().get("lastModified").toString().replace("\"", "");
                        deadline[i] = projects.get(i).getAsJsonObject().get("deadline").toString().replace("\"", "");
                        ;
                        owner[i] = projects.get(i).getAsJsonObject().get("owner").toString().replace("\"", "");
                        badge[i] = projects.get(i).getAsJsonObject().get("badge").toString().replace("\"", "");
                        isMedia[i] = projects.get(i).getAsJsonObject().get("isMedia").toString().replace("\"", "");
                        starred[i] = projects.get(i).getAsJsonObject().get("starred").toString().replace("\"", "");

                        member1[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(0).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                        if (type[i].equals("Group")) {
                            if (projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().size() > 1)
                                member2[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(1).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                            else
                                member2[i] = "";
                            if (projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().size() > 2)
                                member3[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(2).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                            else
                                member3[i] = "";

                        } else {
                            member2[i] = "";
                            member3[i] = "";
                        }
                    }

                    //


                    // set up the RecyclerView

                    adapter = new MyRecyclerViewAdapter(SearchActivity.this, projectid, projectname, lastmodified, type, badge, isMedia, starred, member1, member2, member3, owner, deadline, new MyRecyclerViewAdapter.RecyclerViewClickListener() {
                        @Override
                        public void onRowClicked(View v, int position) {

                        }

                        @Override
                        public void onViewClicked(View v, int position) {

                        }
                    });
                    //     adapter.setClickListener(this);
                    recyclerView.setAdapter(adapter);




                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });



    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewClicked(View v, int position) {
        if(v.getId() == R.id.starred){
            // Do your stuff here
         //   Toast.makeText(this, "You clicked starred" + v.getId(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRowClicked(View v, int position) {
        // Clicked entire row


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchword = query.toString();
        url = null;
        if(searchtype.equals("keywords"))
               url = "https://mcc-fall-2019-g03.appspot.com/project/keyword/get?" + searchtype + "=" + searchword + "&userId=" + userid;
        else
            url = "https://mcc-fall-2019-g03.appspot.com/project/name/get?" + searchtype + "=" + searchword + "&userId=" + userid;



      //  Toast.makeText(getApplicationContext(), url + "", Toast.LENGTH_SHORT).show();
        request = null;
        request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        response = null;

        try {
            response = client.newCall(request).execute();
          //  Toast.makeText(getApplicationContext(), response.code() + "", Toast.LENGTH_SHORT).show();
            code = response.code();
            Log.e("code", code + "");
            if (code == 200)
                projectlist = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (code == 200) {
            projects = gson.fromJson(projectlist, JsonArray.class);
            projectid = new String[projects.size()];
            projectname = new String[projects.size()];
            type = new String[projects.size()];
            lastmodified = new String[projects.size()];
            badge = new String[projects.size()];
            isMedia = new String[projects.size()];
            starred = new String[projects.size()];
            member1 = new String[projects.size()];
            member2 = new String[projects.size()];
            member3 = new String[projects.size()];
            owner = new String[projects.size()];
            deadline = new String[projects.size()];

            for (int i = 0; i < projects.size(); i++) {
                projectid[i] = projects.get(i).getAsJsonObject().get("id").toString().replace("\"", "");
                projectname[i] = projects.get(i).getAsJsonObject().get("name").toString().replace("\"", "");
                type[i] = projects.get(i).getAsJsonObject().get("type").toString().replace("\"", "").replace("\"", "");
                lastmodified[i] = projects.get(i).getAsJsonObject().get("lastModified").toString().replace("\"", "");
                deadline[i] = projects.get(i).getAsJsonObject().get("deadline").toString().replace("\"", "");
                ;
                owner[i] = projects.get(i).getAsJsonObject().get("owner").toString().replace("\"", "");
                badge[i] = projects.get(i).getAsJsonObject().get("badge").toString().replace("\"", "");
                isMedia[i] = projects.get(i).getAsJsonObject().get("isMedia").toString().replace("\"", "");
                starred[i] = projects.get(i).getAsJsonObject().get("starred").toString().replace("\"", "");
                member1[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(0).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                if (type[i].equals("Group")) {
                    if (projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().size() > 1)
                        member2[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(1).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                    else
                        member2[i] = "";
                    if (projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().size() > 2)
                        member3[i] = projects.get(i).getAsJsonObject().get("user_images").getAsJsonArray().get(2).getAsJsonObject().get("profilePictureUrl").toString().replace("\"", "");
                    else
                        member3[i] = "";

                } else {
                    member2[i] = "";
                    member3[i] = "";
                }
            }

            //


            // set up the RecyclerView

            adapter = new MyRecyclerViewAdapter(this, projectid, projectname, lastmodified, type, badge, isMedia, starred, member1, member2, member3, owner, deadline, this);
            //     adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);




        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        recyclerView.setAdapter(null);
        spinner.setSelection(0);
        return false;
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(searchtype.equals("keywords")) {

            request = null;
            request = new Request.Builder()
                    .url(getKeywordurl)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            try {
                response = null;
                response = client.newCall(request).execute();
             //   Toast.makeText(getApplicationContext(), response.code() + "", Toast.LENGTH_SHORT).show();
                code = response.code();
                Log.e("code",  code+ "");
                if (code == 200)
                    projectlist = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (code == 200) {
                JsonArray projects = gson.fromJson(projectlist, JsonArray.class);
                for (int i = 0; i < projects.size(); i++) {
                    keyword = keyword + "," + projects.get(i).getAsJsonObject().get("keywords").toString().replace("\"", "");

                }
                keyword = keyword.replace(",,", ",");
                keywords = keyword.split(",");
                List<String> finalList = new ArrayList<String>();
                finalList.add("Choose Keyword");
                for(String val : keywords) {
                    val = val.replace(",", "");
                    if(!finalList.contains(val)&& !val.equals("") ) {
                        finalList.add(val);
                    }
                }
                //Toast.makeText(getApplicationContext(),finalList.toString(), Toast.LENGTH_SHORT).show();
                // set searchview onqueryhint here
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, finalList);
                 spinner.setAdapter(adapter);
            }
            else{
                List<String> List = new ArrayList<String>();
                List.add("No Keywords");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, List);
                spinner.setAdapter(adapter);


            }
        }
    }
}