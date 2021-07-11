package com.example.vocaby;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private Context context;

    private VolleySingleton(Context context) {
        requestQueue = getRequestQueue();
        this.context = context;

    }

    public static synchronized VolleySingleton getInstance(Context ctx) {
        if (instance == null) {
            instance = new VolleySingleton(ctx);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
