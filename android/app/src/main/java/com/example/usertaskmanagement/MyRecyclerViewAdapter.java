package com.example.usertaskmanagement;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.DOWNLOAD_SERVICE;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private String[] projectid, projectname, lastModified,type,badge,isMedia,starred, member1,member2,member3;
    private LayoutInflater mInflater;
    private RecyclerViewClickListener listener;
    Context context;
    String userid;
    String[] owner,deadline;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, String[] id, String[] name, String[] lastModified, String[] type, String[] badge, String[] isMedia, String[] starred, String[] member1, String[] member2, String[] member3,String[] owner,String[] deadline, RecyclerViewClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.projectid = id;
        this.projectname = name;
        this.lastModified = lastModified;
        this.type = type;
        this.badge = badge;
        this.isMedia = isMedia;
        this.starred = starred;
        this.member1 = member1;
        this.member2 = member2;
        this.member3 = member3;
        this.context = context;
        this.listener = listener;
        this.owner = owner;
        this.deadline = deadline;
        SharedPreferences prefs = context.getSharedPreferences(
                "User", Context.MODE_PRIVATE);
        userid = prefs.getString("uid","") ;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.project_items, parent, false);
        return new ViewHolder(view, listener);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.titleTV.setText(projectname[position] );
        holder.lastmodifiedTV.setText("Last Modified:" + lastModified[position]);
        holder.deadlineTV.setText("Deadline:" + deadline[position]);
        holder.typeTv.setText(type[position]);
        if(isMedia[position].equals("true"))
            holder.ismediaIV.setImageResource(R.drawable.ic_image_blue_24dp);
        if(starred[position].equals("true"))
            holder.starredIV.setImageResource(android.R.drawable.btn_star_big_on);
        else
            holder.starredIV.setImageResource(android.R.drawable.btn_star_big_off);
        if(!badge[position].equals("")){
                Glide.with(this.context /* context */)
                        .load(badge[position].toString())
                        .into(holder.badgeImageIV);

        }
        else {
            holder.badgeImageIV.setImageResource(R.drawable.ic_folder_blue_24dp);
        }
        if(!member1[position].equals("")){
            Glide.with(this.context /* context */)
                    .load(member1[position].toString())
                    .into(holder.member1IV);

        }
        else {
            //holder.member1IV.setImageResource(android.R.drawable.ic_menu_info_details);
        }
        if(type[position].equals("Group")) {
            if (!member2[position].equals("")) {
                Glide.with(this.context /* context */)
                        .load(member2[position].toString())
                        .into(holder.member2IV);

            } else {
                //holder.member2IV.setImageResource(android.R.drawable.ic_menu_info_details);
            }
            if (!member3[position].equals("")) {
                Glide.with(this.context /* context */)
                        .load(member3[position].toString())
                        .into(holder.member3IV);

            } else {
             //   holder.member3IV.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }
        holder.menu.setImageResource(R.drawable.ic_more_vert_black_24dp);


    }

    // total number of rows
    @Override
    public int getItemCount() {
        return projectid.length;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTV,lastmodifiedTV,typeTv,deadlineTV;
        ImageView badgeImageIV,member1IV,member2IV,member3IV,ismediaIV,starredIV;
        ImageView menu;

        ViewHolder(View itemView, final RecyclerViewClickListener listener) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.title);
            lastmodifiedTV = itemView.findViewById(R.id.lastModified);
            deadlineTV = itemView.findViewById(R.id.deadline);
            typeTv = itemView.findViewById(R.id.type);
            ismediaIV = itemView.findViewById(R.id.media);
            badgeImageIV = itemView.findViewById(R.id.badge);
            member1IV = itemView.findViewById(R.id.member1);
            member2IV = itemView.findViewById(R.id.member2);
            member3IV = itemView.findViewById(R.id.member3);
            starredIV = itemView.findViewById(R.id.starred);
            menu = itemView.findViewById(R.id.menu);

            itemView.setOnClickListener(this);
            starredIV.setOnClickListener(this);
            menu.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {
            if(listener != null){
                if(v.getId() == -1) {
                    Intent mainIntent=new Intent(context,ProjectDisplay.class);
                    mainIntent.putExtra("projectid", projectid[getAdapterPosition()]);
                    mainIntent.putExtra("projecttype", type[getAdapterPosition()]);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(mainIntent);
                  //  Toast.makeText(context, projectid[getAdapterPosition()] + "", Toast.LENGTH_SHORT).show();
                   // listener.onRowClicked(v, getAdapterPosition());
                }
                else{
                    if(v.getId() == starredIV.getId()) {
                        if(starred[getAdapterPosition()].equals("false")) {
                            starredIV.setImageResource(android.R.drawable.btn_star_big_on);
                            starred[getAdapterPosition()] = "true";
                            putRequest("https://mcc-fall-2019-g03.appspot.com/project/starred?projectId=" + projectid[getAdapterPosition()] + "&userId=" + userid);


                        }
                        else
                        {
                            starredIV.setImageResource(android.R.drawable.btn_star_big_off);
                            starred[getAdapterPosition()] = "false";
                            putRequest("https://mcc-fall-2019-g03.appspot.com/project/starred?projectId=" + projectid[getAdapterPosition()] + "&userId=" + userid);

                        }
                     //   Toast.makeText(context,starred[getAdapterPosition()] , Toast.LENGTH_SHORT).show();

                        //   listener.onViewClicked(v, getAdapterPosition());
                    }
                    else if(v.getId() == menu.getId()) {

                        //listener.onViewClicked(v, getAdapterPosition());
                        PopupMenu popup = new PopupMenu(v.getContext(), menu);
                        //inflating menu from xml resource
                        if(type[getAdapterPosition()].equals("Personal"))
                                popup.inflate(R.menu.personal_project_menu);
                        else if(owner[getAdapterPosition()].equals(userid))
                            popup.inflate(R.menu.project_menu);
                        else
                            popup.inflate(R.menu.project_menu_nonowner);

                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.addattachments:
                                        Intent mainIntent=new Intent(v.getContext(),TaskImage.class);
                                        mainIntent.putExtra("projectid", projectid[getAdapterPosition()]);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        v.getContext().startActivity(mainIntent);
                                    //    Toast.makeText(context,projectid[getAdapterPosition()] , Toast.LENGTH_SHORT).show();

                                        //handle menu1 click
                                        break;
                                    case R.id.addmembers:
                                        Intent mainIntent1=new Intent(v.getContext(),Add_project_members.class);
                                        mainIntent1.putExtra("projectid", projectid[getAdapterPosition()]);
                                        mainIntent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        v.getContext().startActivity(mainIntent1);
                                        //handle menu2 click
                                        break;
                                    case R.id.deleteproject:
                                        //handle menu3 click
                                        AlertDialog diaBox = AskOption();
                                        diaBox.show();
                                        break;
                                    case R.id.generatereport:
                                        requestMultiplePermissions();
                                        String url = "https://mcc-fall-2019-g03.appspot.com/project/report/?projectId=" + projectid[getAdapterPosition()];
                                        Request request = new Request.Builder()
                                                .url(url)
                                                .header("Accept", "application/json")
                                                .header("Content-Type", "application/json")
                                                .build();

                                        OkHttpClient client = new OkHttpClient();


                                        try {
                                            Gson gson = new GsonBuilder().create();
                                            Response response = client.newCall(request).execute( );
                                            ResponseBody body = response.body();
                                            JsonObject reportrul = gson.fromJson(body.string(), JsonObject.class);
                                            String reporturl = reportrul.get("report_url").toString().replace("\"", "");;
                                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(reporturl);
                                       //    File localFile = new File(Environment.getExternalStorageDirectory() + "/reports/", "");

                                            File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "reports");
                                            if(!rootPath.exists()) {
                                                rootPath.mkdirs();
                                            }

                                            final File localFile = new File(rootPath, UUID.randomUUID().toString() + ".pdf");

                                         //   Toast.makeText(context, localFile.getAbsoluteFile().toString(), Toast.LENGTH_SHORT).show();


                                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    // Local temp file has been created
                                                    Toast.makeText(context,"Report successfully generated under Download/reports/" , Toast.LENGTH_LONG).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Handle any errors
                                                }
                                            });

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        //handle menu3 click
                                        //handle menu3 click
                                        break;
                                }
                                return false;
                            }
                        });
                        //displaying the popup
                        popup.show();
                    }

                }

            }


        }

        private void  requestMultiplePermissions(){
            Dexter.withActivity((Activity) context)
                    .withPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {

                                Toast.makeText(context, "All permissions are granted by user!", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(context, "Some Error! ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .onSameThread()
                    .check();
        }




        private void putRequest(String url) {


            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(null, new byte[]{});

            Request request = new Request.Builder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .put(body)
                    .url(url)
                    .build();

            try {

                Response responses =  client.newCall(request).execute();
              //  Toast.makeText(context,responses.body().string() , Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private AlertDialog AskOption() {
            AlertDialog myQuittingDialogBox = new AlertDialog.Builder(context)
                    // set message, title, and icon
                    .setTitle("Delete")
                    .setMessage("Do you want to Delete")
                    //   .setIcon(R.drawable.delete)

                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //your deleting code
                            String url = "https://mcc-fall-2019-g03.appspot.com/project/delete?projectId=" + projectid[getAdapterPosition()] + "&userId=" + userid;

                            dialog.dismiss();
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url).delete().build();
                            Response response = null;
                            try {
                                response = client.newCall(request).execute();
                                if(response.body().string().contains("success")) {
                                    Toast.makeText(context, "Project Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent=new Intent(context,ListOfProjects.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(mainIntent);

                                }
                                else
                                    Toast.makeText(context,"Deletion failed" , Toast.LENGTH_SHORT).show();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }

                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    })
                    .create();

            return myQuittingDialogBox;
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return projectid[id];
    }


    public interface RecyclerViewClickListener {

        void onRowClicked(View v, int position);
        void onViewClicked(View v, int position);
    }

}