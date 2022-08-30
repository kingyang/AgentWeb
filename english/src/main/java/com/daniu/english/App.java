package com.daniu.english;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.webkit.WebView;

import com.just.agentweb.AgentWebCompat;
import com.queue.library.GlobalQueue;

public class App extends Application {


    public static Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mContext = base;
        AgentWebCompat.setDataDirectorySuffix(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Normal app init code...

        // 初始化webview
        GlobalQueue.getMainQueue().postRunnableInIdleRunning(new Runnable() {
            @Override
            public void run() {
                try {
                    startService(new Intent(App.this, WebService.class));
                } catch (Throwable throwable) {

                }
            }
        });
    }




}