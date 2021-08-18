package com.vocaby.app.api;

import android.content.Context;

import com.android.volley.Response;
import org.json.JSONObject;

public class ApiManager {
    private static ApiManager instance;
    private static Context ctx;

    private ApiManager() {
    }

    public static synchronized ApiManager getInstance(Context context) {
        if (null == instance) {
            instance = new ApiManager();
        }

        ctx = context.getApplicationContext();

        return instance;
    }

    public void getWordData(String word, Response.Listener<JSONObject> listenerResponse) {
        RequestManager requestManager = RequestManager.getInstance(ctx);
        requestManager.getDefinition(word, listenerResponse, null);
    }
}
