package mansurbiryukov.testapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private List<String> data = new ArrayList<>();

    ListView lv;
    ArrayAdapter<String> adapter;
    AlertDialog.Builder builder;
    int selected;


    public void post(){
        Thread post = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody formBody = new FormBody.Builder()
                            .add("upload[imei]",android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID))
                            .add("upload[message]", "hello world")
                            .build();
                    Request request = new Request.Builder()
                            .url("https://obscure-shelf-31484.herokuapp.com/uploads")
                            .post(formBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.code() == 200){
                        Log.d("MYTAG", "SUCCESS");
                    }else{
                        Log.d("MYTAG", "ERROR" + response.code());
                    }
                } catch (IOException e) {
                    Log.d("MYTAG", "CATCHED" + e.toString());
                }

            }
        });
        post.start();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), String.format("Posit: %d", position), Toast.LENGTH_SHORT).show();
                selected = position+1;
            }
        });
        builder = new AlertDialog.Builder(this);
        post();
    }






    public void fetch(View view) {
        data.clear();
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                adapter.notifyDataSetChanged();
                selected=1;
                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            }
        };
        Thread fetch = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://obscure-shelf-31484.herokuapp.com/users.json")
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    JSONArray array = new JSONArray(response.body().string());
                    for (int i =0; i<array.length(); i++){
                        JSONObject object = array.getJSONObject(i);
                        data.add(object.getString("name")+""+object.getString("surname"));
                    }
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        fetch.start();



    }
    public void details(View view) {
        if (data.size() < 1) return;
        
        final String[] out = new String[1];
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                builder.setTitle("Details");
                builder.setMessage(out[0]);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        };
        Thread details = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(String.format("https://obscure-shelf-31484.herokuapp.com/users/%d.json", selected))
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    JSONObject object = new JSONObject(response.body().string());
                    out[0] = String.format("Name: %s\nSurname: %s\nInfo: %s\nCreated at: %s",
                            object.get("name"), object.get("surname"), object.get("info"), object.get("created_at"));
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("MYTAG", String.format("%d",selected));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("MYTAG", String.format("%d",selected));
                }
            }
        });
        details.start();
    }
}

