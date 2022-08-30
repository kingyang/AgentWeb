/*
 * Copyright (C)  Justson(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.just.agentweb;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cenxiaozhong
 */
public class VideoImpl implements IVideo, EventInterceptor {

    private Activity mActivity;
    private WebView mWebView;
    private static final String TAG = VideoImpl.class.getSimpleName();
    private Set<Pair<Integer, Integer>> mFlags = null;
    private View mMovieView = null;
    private ViewGroup mMovieParentView = null;
    private WebChromeClient.CustomViewCallback mCallback;
    private int mOriginalOrientation;

    public VideoImpl(Activity mActivity, WebView webView) {
        this.mActivity = mActivity;
        this.mWebView = webView;
        mFlags = new HashSet<>();
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        Activity mActivity;
        if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
            return;
        }
        mOriginalOrientation = mActivity.getRequestedOrientation();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Window mWindow = mActivity.getWindow();
        Pair<Integer, Integer> mPair = null;
        // 保存当前屏幕的状态
        if ((mWindow.getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mFlags.add(mPair);
        }
        // 开启硬件加速
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                && (mWindow.getAttributes().flags
                & WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            mFlags.add(mPair);
        }

        // 全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = mWindow.getInsetsController();
            controller.hide(WindowInsets.Type.statusBars());
        }
        mPair = new Pair<>(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);
        mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFlags.add(mPair);

        if (mMovieView != null) {
            callback.onCustomViewHidden();
            return;
        }
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }
        if (mMovieParentView == null) {
            FrameLayout mContentView = mActivity.findViewById(android.R.id.content);
            mMovieParentView = new FrameLayout(mActivity);
            mMovieParentView.setBackgroundColor(Color.BLACK);
            mContentView.addView(mMovieParentView);
        }
        this.mCallback = callback;
        mMovieParentView.addView(this.mMovieView = view);
        mMovieParentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHideCustomView() {
        if (mMovieView == null) {
            return;
        }
        if (mActivity != null) {
            mActivity.setRequestedOrientation(mOriginalOrientation);
        }
        Window mWindow = mActivity.getWindow();
        if (!mFlags.isEmpty()) {
            for (Pair<Integer, Integer> mPair : mFlags) {
                mWindow.setFlags(mPair.second, mPair.first);
            }
            mFlags.clear();
        }

        // 全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = mWindow.getInsetsController();
            controller.show(WindowInsets.Type.statusBars());
        }

        mMovieView.setVisibility(View.GONE);
        if (mMovieParentView != null && mMovieView != null) {
            mMovieParentView.removeView(mMovieView);
        }
        if (mMovieParentView != null) {
            mMovieParentView.setVisibility(View.GONE);
        }
        if (this.mCallback != null) {
            mCallback.onCustomViewHidden();
        }
        this.mMovieView = null;
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean isVideoState() {
        return mMovieView != null;
    }

    @Override
    public boolean event() {
        if (isVideoState()) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }
}
