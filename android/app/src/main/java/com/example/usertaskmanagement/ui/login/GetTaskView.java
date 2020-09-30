package com.example.usertaskmanagement.ui.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usertaskmanagement.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetTaskView extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_task_view);
        //setSupportActionBar(toolbar);

        listView=(ListView)findViewById(R.id.get_task_view_id);
        String projectId = "-LuHSMEYDT4mol7p9qYJ";
        String url = "https://mcc-fall-2019-g03.appspot.com/task/?projectId="+projectId;

        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = new JSONObject();

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
                    String project_id = jsonObj.getString("id");
                    Log.d("response", project_id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

}
