package com.example.lanch.scanner;

import android.app.Application;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.lanch.scanner.network.LruBitmapCache;

public class MyApplication extends Application
{
    private RequestQueue mRequestQueue;
    private static MyApplication mInstance;
    private ImageLoader imageLoader;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance()
    {
        return mInstance;
    }

    public RequestQueue getReqQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(getInstance());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader()
    {
        getReqQueue();
        if(imageLoader == null)
        {
            imageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache());
        }

        return this.imageLoader;
    }


    public <T> void addToReqQueue(Request<T> req, String tag)
    {

        getReqQueue().add(req);
    }

    public <T> void addToReqQueue(Request<T> req)
    {

        getReqQueue().add(req);
    }

    public void cancelPendingReq(Object tag)
    {
        if (mRequestQueue != null)
        {
            mRequestQueue.cancelAll(tag);
        }
    }
}

