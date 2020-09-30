package com.example.usertaskmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Add_project_members extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerAdapter fireballRecyclerAdapter;
    private Button mAddMember;

    public ArrayList<String> MemberList = new ArrayList<String>();
    public ArrayList<String> id_list = new ArrayList<String>();
    private SharedPreferences pref;
    private String project_id;

    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_members_main);

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
                    Intent mainIntent=new Intent(Add_project_members.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(Add_project_members.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(Add_project_members.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mAddMember = (Button) findViewById(R.id.add_members);



        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));


       // pref = getSharedPreferences("User", MODE_PRIVATE);
        //projectId= pref.getString("project_name", null);

        project_id = getIntent().getStringExtra("projectid");
        Log.d("msg",project_id);
        String url = "https://mcc-fall-2019-g03.appspot.com/project/members/get?projectIde=";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
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
                    Log.d("response", jsonObj.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        //String searchText = mSearchField.getText().toString();

       // fireUserSearch(searchText);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();
                Log.d("msg", searchText);
                if (searchText.length() == 5 || searchText.length() > 5 ){
                    fireUserSearch(searchText);
                }else{
                    Toast.makeText(Add_project_members.this, "Enter atleast 5 characters to begin search", Toast.LENGTH_LONG).show();
                }



            }
        });

        mAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(Add_project_members.this, , Toast.LENGTH_SHORT).show();
                Log.d("msgs", MemberList.toString());

                String url = "https://mcc-fall-2019-g03.appspot.com/project/members/add?projectId="+project_id;

                OkHttpClient client = new OkHttpClient();
/*                  [{
"userId":"dsafasdfas",
"role":"participant"
},
{
"userId":"sadfasf",
"role":"partidsafasfacipant"

}]
*/
                JSONArray jsonArray = new JSONArray();
                for (String key : id_list) {
                    JSONObject MemberJson = new JSONObject();
                    try {
                        MemberJson.put("userId", key);
                        MemberJson.put("userRole", "participant");
                        jsonArray.put(MemberJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("msg", jsonArray.toString());

                MediaType MEDIA_TYPE = MediaType.parse("application/json");

                RequestBody body = RequestBody.create(MEDIA_TYPE,jsonArray.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
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
                        Log.d("response", response.toString());
                        // String mMessage = response.body().string();
                       // try {
                            //JSONObject jsonObj = new JSONObject(response.body().string());
                            //String project_id = jsonObj.getString("id");

                        //} catch (JSONException e) {
                         //   e.printStackTrace();
                        //}

                    }
                });

                Toast.makeText(getApplicationContext(), "Members added Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void fireUserSearch(String searchText) {

        //Toast.makeText(Add_project_members.this, "Started Search", Toast.LENGTH_LONG).show();

        String regex = "^[a-zA-Z0-9]+$";
        Log.d("begin:",searchText);
        final Query fireSearchQuery = mUserDatabase.orderByChild("displayName").startAt(searchText).endAt(searchText+"\uf8ff");
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(fireSearchQuery, new SnapshotParser<Users>(){@NonNull
                        @Override
                        public Users parseSnapshot(@NonNull DataSnapshot snapshot) {

                            Log.d("msg12",snapshot.getKey());
                            return new Users(snapshot.getKey().toString(),snapshot.child("displayName").getValue().toString()
                                    );}})
                        .build();

        //Log.d("msg", fireSearchQuery.toString());
        //Log.d("msg", options.toString());

       fireballRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>( options ) {


            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //Log.d("msg", "1");
                //Toast.makeText(Add_project_members.this, "Started Search1", Toast.LENGTH_LONG).show();

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, final int position, @NonNull final Users model) {
                //Toast.makeText(Add_project_members.this, "Started Search2", Toast.LENGTH_LONG).show();

                holder.setDetails(getApplicationContext(), model.getName(), MemberList);
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //
                        // Toast.makeText(Add_project_members.this, String.valueOf(position), Toast.LENGTH_SHORT).show();

                    }
                });
                holder.root.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                           @Override
                                                           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                                                    if(isChecked){
                                                                        MemberList.add(model.getName());
                                                                        id_list.add(model.getUid());
                                                                    }else{
                                                                        if (MemberList.contains(model.getName())){
                                                                            MemberList.remove(model.getName());
                                                                            id_list.remove(model.getUid());
                                                                        }
                                                                    }
                                                           }
                                                       }
                );


                //Log.d("msg", "2");

            }



        };

        fireballRecyclerAdapter.startListening();
        mResultList.setAdapter(fireballRecyclerAdapter);
       // fireballRecyclerAdapter.stopListening();



    }

    /* @Override
    protected void onStart() {
        super.onStart();
        fireballRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fireballRecyclerAdapter.stopListening();
    }
*/

    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public CheckBox root;

        public UsersViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.chkMember);

            mView = itemView;

        }

        public void setDetails(Context ctx, String userName, ArrayList<String> memberList){

            TextView user_name = (TextView) mView.findViewById(R.id.name_text);
            //ImageView user_image = (ImageView) mView.findViewById(R.id.profile_image);
            Log.d("check", userName);
            Log.d("check", memberList.toString());
            if( memberList.contains(userName) == true ){
                root.setChecked(true);
            }

            user_name.setText(userName);

            //Glide.with(ctx).load(userImage).into(user_image);


        }




    }

}
