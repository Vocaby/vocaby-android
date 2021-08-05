package com.vocaby.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {
    private static RequestManager instance = null;
    private final Context ctx;
    public RequestQueue requestQueue;
    private static String API_KEY;
    private static String TOKEN_KEY;

    private RequestManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        this.ctx = context.getApplicationContext();
    }

    public static synchronized RequestManager getInstance(Context context) {
        if (null == instance) {
            instance = new RequestManager(context);
            API_KEY = context.getString(R.string.mobile_api_key);
            TOKEN_KEY = context.getString(R.string.token_key);
        }

        return instance;
    }

    public void getDefinition(String word, Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.GET, ctx.getString(R.string.dictionary_url) + word, null, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> m = new HashMap<>();
                m.put("Vocaby-Api-Key", API_KEY);
                return m;
            }
        };

        requestQueue.add(r);
    }

    public void makeSaveRequest(String word, Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, ctx.getString(R.string.save_url) + word, null, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPref = ctx.getSharedPreferences(TOKEN_KEY, Context.MODE_PRIVATE);
                String token = sharedPref.getString(TOKEN_KEY, "");
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key",  API_KEY);
                m.put("Authorization",  "Token " + token);
                return m;
            }
        };

        requestQueue.add(r);
    }

    public void makeDeleteRequest(String word, Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.DELETE, ctx.getString(R.string.save_url) + word, null, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPref = ctx.getSharedPreferences(TOKEN_KEY, Context.MODE_PRIVATE);
                String token = sharedPref.getString(TOKEN_KEY, "");
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key",  API_KEY);
                m.put("Authorization",  "Token " + token);
                return m;
            }
        };

        requestQueue.add(r);
    }

    public void makeLogoutRequest(Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, ctx.getString(R.string.logout_url), null, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPref = ctx.getSharedPreferences(TOKEN_KEY, Context.MODE_PRIVATE);
                String token = sharedPref.getString(TOKEN_KEY, "");
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key",  API_KEY);
                m.put("Authorization",  "Token " + token);
                return m;
            }
        };

        requestQueue.add(r);
    }

    public void makeRegisterRequest(JSONObject jsonObject, Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, ctx.getString(R.string.register_url), jsonObject, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key", API_KEY);

                return m;
            }
        };

        requestQueue.add(r);
    }

    public void makeLoginRequest(JSONObject jsonObject, Response.Listener<JSONObject> listenerResponse, Response.ErrorListener listenerError) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, ctx.getString(R.string.login_url), jsonObject, listenerResponse, listenerError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key", API_KEY);

                return m;
            }
        };

        requestQueue.add(r);
    }
}
