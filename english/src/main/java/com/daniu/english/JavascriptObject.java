package com.daniu.english;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.just.agentweb.AgentWebUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JavascriptObject {
    private MainActivity activity;
    private Handler deliver = new Handler(Looper.getMainLooper());

    public JavascriptObject(MainActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    public void postMessage(String msg) {
        Log.i("JavascriptObject", "postMessage:" + msg);
        try {
            JSONTokener jsonParser = new JSONTokener(msg);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            String scene = jsonObject.getString("scene");

            Log.i("JavascriptObject", "Thread:" + Thread.currentThread());
            try {
                switch (scene) {
                    case "system": {
                        SceneSystem(jsonObject);
                    }
                    break;
                    default: {
                        jsonObject.put("error", "unknown scene:" + scene);
                    }
                    break;
                }
            } catch (JSONException ex) {
                jsonObject.put("error", "unknown error:" + ex.getMessage());
            }
            this.activity.responseMessage(jsonObject);
            Log.i("JavascriptObject", "responseMessage:" + jsonObject.toString());
        } catch (Exception ex) {
            Log.i("JavascriptObject", "error:", ex);
        }
    }

    private void SceneSystem(@NonNull JSONObject jsonObject) throws JSONException {
        String action = jsonObject.optString("action");
        switch (action) {
            case "dataGet": {
                String key = jsonObject.optString("key");
                if (key == null || key.length() == 0) {
                    jsonObject.put("error", "key值为空");
                } else {
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("webview_data", MODE_PRIVATE);
                    String value = sharedPreferences.getString(key, "");
                    jsonObject.put("value", value);
                }
                break;
            }
            case "dataSet": {
                String key = jsonObject.optString("key");
                if (key == null || key.length() == 0) {
                    jsonObject.put("error", "key值为空");
                } else {
                    String value = jsonObject.optString("value");
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("webview_data", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(key, value);
                    editor.commit();
                    jsonObject.put("value", value);
                }
                break;
            }
            case "clearCache":
                AgentWebUtils.clearWebViewAllCache(activity.getBaseContext());
                break;
            case "desktop": {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                activity.startActivity(home);
                break;
            }
            case "ready":
                break;
            case "version":
                jsonObject.put("version_code", BuildConfig.VERSION_CODE);
                jsonObject.put("version_name", BuildConfig.VERSION_NAME);
                break;
            default:
                jsonObject.put("error", "unknown action:" + action);
                break;
        }
    }
}
