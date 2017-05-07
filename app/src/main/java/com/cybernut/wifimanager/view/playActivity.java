package com.cybernut.wifimanager.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.cybernut.wifimanager.R;

import java.io.IOException;

public class playActivity extends AppCompatActivity {
    WebView view;
    //String url;
    private  String url = "http://192.168.10.96/zm/index.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_play);

        Intent intent = this.getIntent();
//取得傳遞過來的資料
        url = intent.getStringExtra("ip");
        view=(WebView)findViewById(R.id.view);
        RtspStream();
    }
    public void conn(View view) {
        RtspStream();
    }
    private void RtspStream(){
        view.getSettings().setJavaScriptEnabled(true);
        view.requestFocus();
        view.setWebViewClient(new MyWebViewClient());
        view.loadUrl(url);
    }
    class ParsePage extends AsyncTask<Void,Void,Void> {
        String words;

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {             //將內容交給UI執行緒做顯示
                public void run(){
                    view.getSettings().setJavaScriptEnabled(true);
                    view.requestFocus();
                    view.setWebViewClient(new MyWebViewClient());
                    view.loadUrl(url);
                }
            });

            return null;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
