package com.vocaby.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private Context ctx;
    private Button logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ctx = requireActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageButton navButton = view.findViewById(R.id.settings_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)requireActivity()).showSettings();
            }
        });

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.token_key), "");

        if (token.isEmpty()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.enter_right_to_left,
                            R.anim.exit_right_to_left,
                            R.anim.enter_right_to_left,
                            R.anim.exit_right_to_left
                    )
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }

        return view;
    }

    private void logout() {
        String url = getString(R.string.logout_url);
        RequestQueue q = Volley.newRequestQueue(ctx);
        JsonObjectRequest jsonObjectRequest = makeRequest(url);
        q.add(jsonObjectRequest);
    }

    private JsonObjectRequest makeRequest(String url) {
        return new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.token_key), null);
                        editor.apply();
                        FragmentManager fm = getParentFragmentManager();
                        fm.beginTransaction()
                                .replace(R.id.fragment_container, new ProfileFragment())
                                .commit();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.d("RESPONSE", "Error: " + error
                                    + "\nStatus Code " + error.networkResponse.statusCode
                                    + "\nData " + jsonError);
                        }

                        logoutButton.setEnabled(true);
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
                String token = sharedPref.getString(getString(R.string.token_key), "");
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key",  getString(R.string.mobile_api_key));
                m.put("Authorization",  "Token " + token);

                return m;
            }
        };
    }
}