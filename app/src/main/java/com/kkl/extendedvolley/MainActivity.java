package com.kkl.extendedvolley;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestJson();
    }

    public void requestJson() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        Map<String, String> headers = new HashMap<>();
        String credentials = "admin:111111";
        String auth = "Basic "
                + Base64.encodeToString(credentials.getBytes(),
                Base64.NO_WRAP);
        headers.put("Authorization", auth);

        ExtendedVolley.getInstance(this)
                .load(url)
                .asString()
                .headers(headers)
                .listener(new ExtendedVolley.Listener() {
                    @Override
                    public void onResponse(String result) {
                        Log.v("kkl2", "result: " + result);
                    }
                }).error(new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v("kkl2", "error: " + error.toString());
                        }
                    })
                .get();
    }

    public void requestFile() {
        String dest = Environment.getExternalStorageDirectory() + "/test-data.zip";
        String url = "http://www.dynaexamples.com/examples-manual/ls-dyna_example.zip/at_download/file";

        Log.v("kkl2", "dest: " + dest);
        ExtendedVolley.getInstance(this)
                .load(url)
                .asFile(dest)
                .listener(new ExtendedVolley.Listener() {
                    @Override
                    public void onResponse(String result) {
                        Log.v("kkl2", "result: " + result);
                    }
                }).error(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("kkl2", "error: " + error.toString());
                    }
                })
                .get();

    }
}
