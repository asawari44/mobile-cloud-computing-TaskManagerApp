package com.example.usertaskmanagement;

import android.content.Context;
import android.graphics.Paint;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class CustomAdapter extends ArrayAdapter<Task> implements View.OnClickListener{

    private final String project_id;
    private ArrayList<Task> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView description, deadline, status;
        CheckBox checkBox;
    }

    public CustomAdapter(ArrayList<Task> data, String project_id, Context context) {
        super(context, R.layout.get_task_view_item, data);
        this.dataSet = data;
        this.project_id = project_id;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Task task=(Task) object;

        switch (v.getId())
        {
            case R.id.cross_task:
                Log.d("msg", "item clicked"+((Task) object).description);
                TextView tv = (TextView) v.findViewById(R.id.taskdescription);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Task task = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.get_task_view_item, parent, false);
            viewHolder.description = (TextView) convertView.findViewById(R.id.taskdescription);
            viewHolder.deadline = (TextView) convertView.findViewById(R.id.taskdeadline);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.cross_task);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            //viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;


            View row = convertView;

            CheckBox Check = convertView.findViewById(R.id.cross_task);

            final View finalConvertView = convertView;

            Check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox chk = (CheckBox) finalConvertView.findViewById(R.id.cross_task);
                    if (chk.isChecked()) {
                        Log.v("msg123", "ROW PRESSED");
                        TextView tv = (TextView) finalConvertView.findViewById(R.id.taskdescription);
                        TextView tv2 = (TextView) finalConvertView.findViewById(R.id.status);
                        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tv2.setText("completed");
                        chk.setEnabled(false);


                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8)
                        {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            String myurl = "https://mcc-fall-2019-g03.appspot.com/task/status?projectId="+project_id+"&taskId="+task.getId()+"&status=completed";
                            System.out.println(myurl);


                            String responseString = null;
                            try {
                                URL url = new URL(myurl);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("PUT");
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


                        }
                    }

                }

            });
            /* ...Code for holder and so on... */
/*
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("msg123", "ROW PRESSED");
                    TextView tv = (TextView) view.findViewById(R.id.taskdescription);
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


                }
            });*/
            viewHolder.description.setText(task.getDescription());
            //viewHolder.description.setPaintFlags(viewHolder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.deadline.setText(task.getDeadline());
            viewHolder.status.setText(task.getStatus());
            if(viewHolder.status.getText().toString().contains("completed")){
                viewHolder.checkBox.setChecked(true);
                viewHolder.checkBox.setEnabled(false);
                viewHolder.description.setPaintFlags(viewHolder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }



        // Return the completed view to render on screen
        return convertView;
    }
}