package com.onegravity.rteditor.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;

public class RTEditorPreview extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rte_preview);

        WebView webView = (WebView) findViewById(R.id.webView);

        String html = getIntent().getStringExtra("text");
        html = html.replaceAll("<img src=\"", "<img src=\"file://");

        webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
    }

}


