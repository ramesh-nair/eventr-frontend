package com.eventr.app.eventr;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.eventr.app.eventr.utils.LruBitmapCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Suraj on 21/08/16.
 */
public class EventrRequestQueue extends Application {
    private RequestQueue mRequestQueue;
    private static EventrRequestQueue mInstance;
    private ImageLoader mImageLoader;

    private static final String DEFAULT_TAG = "request_tag";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        printHashKey(this);
    }

    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(DEFAULT_TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEFAULT_TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(DEFAULT_TAG, "printHashKey()", e);
        }
    }

    public static synchronized EventrRequestQueue getInstance () {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void cancel() {
        mRequestQueue.cancelAll(DEFAULT_TAG);
    }

    public void cancel(String tag) {
        mRequestQueue.cancelAll(tag);
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
}
