package com.example.usertaskmanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CreateTask extends AppCompatActivity {


    //to display navigation menu
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private NavigationView nv;

    Calendar myCalendar = Calendar.getInstance();
    Dialog myDialog;
    EditText description;
    EditText deadline;
    SearchView assigned_to;
    Button create;
    public ArrayList<String> MemberList = new ArrayList<String>();
    private Adapter adapter;
    private SharedPreferences pref;
    String userId;
    String member_list_str="";
    TextView assigned_member;
    private static int RESULT_LOAD_IMAGE = 1;
    private static final String IMAGE_DIRECTORY = "/";
    private String member_url;
    public List<String> MemberList_names =new ArrayList<String>();
    String project_type = "Group";
    String member_id;
    Button ocr_scan;
    private String members = "";
    String projectId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



       // final String projectId = "-LvHxw-PJuGb_3CPTtj9";
        projectId = getIntent().getStringExtra("projectid");
        //projectId = "-LuHSMEYDT4mol7p9qYZ";
        Log.d("msg",projectId);

        if (project_type == "Personal"){
            setContentView(R.layout.create_task_personal);
            assigned_member = (TextView) findViewById(R.id.textView4);
            description = (EditText) findViewById(R.id.description);
            deadline = (EditText) findViewById(R.id.deadline);
            ocr_scan = (Button) findViewById(R.id.ocr);


        }

        else{
            setContentView(R.layout.create_task);
            Button clickButton = (Button) findViewById(R.id.assigned_to);

            assigned_member = (TextView) findViewById(R.id.textView4);
            description = (EditText) findViewById(R.id.description);
            deadline = (EditText) findViewById(R.id.deadline);
            //assigned_to = (SearchView) findViewById(R.id.assigned_to);

            ocr_scan = (Button) findViewById(R.id.ocr);

            pref = getSharedPreferences("User", MODE_PRIVATE);
            userId= pref.getString("uid", null);


            final OkHttpClient client = new OkHttpClient();

            String url = "https://mcc-fall-2019-g03.appspot.com/project/members/get?projectId="+projectId;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            Log.d("msg",url);
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

                        //JSONObject jsonObject= new JSONObject(response.body().string());
                        //Log.d("msg",jsonObject.toString());
                        JSONArray jsonarray = new JSONArray(response.body().string());
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            String name = jsonobject.getString("userId");
                            Log.d("name",name);
                            MemberList.add(name);
                            member_list_str = member_list_str + ","+name;
                            Log.d("msg",member_list_str);
                        }

                        Log.d("msg",MemberList.toString());
                        if(!member_list_str.isEmpty()) {
                            Log.d("msg","==========================================");
                            //member_list_str = member_list_str.substring(1);
                            Log.d("msg",member_list_str);
                            member_url = "https://mcc-fall-2019-g03.appspot.com/user/name/list?userId&userIds="+member_list_str;
                            Log.d("msg",member_url);
                            Request request_2 = new Request.Builder()
                                    .url(member_url)
                                    .get()
                                    .header("Accept", "application/json")
                                    .header("Content-Type", "application/json")
                                    .build();
                            Log.d("msg", member_url);
                            client.newCall(request_2).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    String mMessage = e.getMessage().toString();
                                    Log.d("failure Response", mMessage);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    // String mMessage = response.body().string();
                                    try {


                                        JSONArray jsonarray = new JSONArray(response.body().string());

                                        for (int i = 1; i < jsonarray.length(); i++) {
                                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                                            String name = jsonobject.getString("name");
                                            Log.d("name", name);
                                            MemberList_names.add(name);


                                        }

                                        Log.d("msg", MemberList_names.toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            Log.d("msg",member_list_str);







            clickButton.setOnClickListener(new View.OnClickListener(){

                @Override  public void onClick(View v) {
                    //List of items to be show in  alert Dialog are stored in array of strings/char sequences  final
                    //final String[] items = {"AAAAAA","BBBBBBB", "CCCCCCC","DDDDDDDD"};

                    final String items[] = new String[ MemberList_names.size()];

                    // ArrayList to Array Conversion
                    for (int j = 0; j <  MemberList_names.size(); j++) {

                        // Assign each value to String array
                        items[j] =  MemberList_names.get(j);
                    }



                    //Context context = getApplicationContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateTask.this);

                    //set the title for alert dialog
                    builder.setTitle("Choose members to assign task to: ");

                    //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
                   /* builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override public void onClick(DialogInterface dialog, int item) {
                            //setting the button text to the selected itenm from the list
                            assigned_member.setText(items[item]);
                            member_id = MemberList.get(item);
                        }
                    });*/

                    //Creating CANCEL button in alert dialog, to dismiss the dialog box when nothing is selected
                    builder .setCancelable(false)
                            .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {

                                @Override  public void onClick(DialogInterface dialog, int id) {
                                    //When clicked on CANCEL button the dalog will be dismissed
                                    dialog.dismiss();
                                }
                            });
                    final boolean[] mPendingItems = new boolean[items.length];
                    //boolean[] mCheckedItems=new boolean[items.length];
                    //System.arraycopy(mCheckedItems, 0, mPendingItems, 0, mCheckedItems.length);

                    builder.setMultiChoiceItems(items, mPendingItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which,
                                                    final boolean isChecked) {
                                    mPendingItems[which] = isChecked;
                                    Log.d("msg",items[which].toString());

                                    if(!members.isEmpty()){
                                        members = members+"," + MemberList_names.get(which);
                                        member_id = member_id+","+ MemberList.get(which);
                                    }else{
                                        members =MemberList_names.get(which);
                                        member_id = MemberList.get(which);
                                    }


                                }
                            });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.cancel();
                            assigned_member.setText(members);
                          //  Log.d("msg members: ",member_id);

                        }
                    });

                    

                    //Creating alert dialog
                    AlertDialog alert =builder.create();

                    //Showingalert dialog
                    alert.show();

                }


            });

        }






        ocr_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);

            }

        });




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
                new DatePickerDialog(CreateTask.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        myDialog = new Dialog(this);

        create = (Button)findViewById(R.id.create);


        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean valid = validate();

                if (valid){
                    Log.d("valid",Boolean.toString(valid));
                    pref = getSharedPreferences("User", MODE_PRIVATE);
                    userId= pref.getString("uid", null);
                    String url = "https://mcc-fall-2019-g03.appspot.com/task/create?projectId="+projectId+"&userId="+userId;;
                    Log.d("msg", url);
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
                        if(assigned_member.getText().toString().isEmpty() && project_type == "Group" ){
                            postdata.put("status", "pending");
                        }else{
                            postdata.put("status", "on-going");
                        }

                        postdata.put("deadline",deadline.getText().toString() );
                        if (project_type == "Personal"){
                            postdata.put("assigned_members", userId);
                        }
                        else{
                            postdata.put("assigned_members", member_id);
                        }

                        postdata.put("description", description.getText().toString());
                        Log.d("msg",postdata.toString());
                    } catch(JSONException e){

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
                                String task_id = jsonObj.getString("taskId");
                                Log.d("response", task_id);
                                //Log.d("response", jsonObj.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    Toast.makeText(getApplicationContext(), "Task created Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                    // Storing string



                }

            }

        });

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
                    Intent mainIntent=new Intent(CreateTask.this, ListOfProjects.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
                else if(id== R.id.item2)
                {
                    Intent mainIntent=new Intent(CreateTask.this, EditProfileActivity.class);
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
                    Intent mainIntent=new Intent(CreateTask.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }
                return true;
            }
        });
    }

    private void call_next_func(String member_list_str) {
    }


    private boolean validate() {
        boolean valid = true;

        String Deadline = deadline.getText().toString();
        String Description = description.getText().toString();

        if (Deadline.isEmpty()) {
            deadline.setError("enter a valid name for the project");
            valid = false;
        } else {
            deadline.setError(null);
               /* SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                Date selected_deadline = new Date(Deadline);
                Date date = new Date();
                Log.d("dates: ",date.toString()+"and"+selected_deadline.toString());
                if(selected_deadline.before(date)){
                    deadline.setError("enter a valid deadline(date after today)");
                }*/

        }

        if (Description.isEmpty()) {
            description.setError("enter a valid password");
            valid = false;
        } else {
            description.setError(null);
        }

        return valid;
    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        deadline.setText(sdf.format(myCalendar.getTime()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    bitmap = (Bitmap.createScaledBitmap(bitmap, 400, 300, false));
                    //  String path = saveImage(bitmap);
                    Toast.makeText(CreateTask.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    //taskImage.setImageBitmap(bitmap);
                    imagetoText(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateTask.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
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

    private void imagetoText(Bitmap bitmap){
        // To get bitmap from resource folder of the application.
        //bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ocr_sample);
// Starting Text Recognizer
        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!txtRecognizer.isOperational()) {
            description.setText("Cannot convert to text, please enter manually.");
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray items = txtRecognizer.detect(frame);
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = (TextBlock) items.valueAt(i);
                strBuilder.append(item.getValue());
                strBuilder.append("/");
                for (Text line : item.getComponents()) {
                    //extract scanned text lines here
                    Log.v("lines", line.getValue());
                    for (Text element : line.getComponents()) {
                        //extract scanned text words here
                        Log.v("element", element.getValue());

                    }
                }
            }
            description.setText(strBuilder.toString());
            Toast.makeText(getBaseContext(), strBuilder.toString(), Toast.LENGTH_LONG).show();


        }
    }

    //method for navigation menu


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}

