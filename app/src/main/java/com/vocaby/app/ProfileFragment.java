package com.vocaby.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.vocaby.app.api.RequestManager;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ProfileFragment extends Fragment {
    private Context ctx;
    private Button logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
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
        navButton.setOnClickListener(v -> ((MainActivity)requireActivity()).showSettings());

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.token_key), "");

        sharedPref = ctx.getSharedPreferences(getString(R.string.email), Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.email), "");
        TextView currentUser = view.findViewById(R.id.current_user);
        currentUser.setText(email);

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
        RequestManager requestManager = RequestManager.getInstance(ctx);
        requestManager.makeLogoutRequest(logoutListenerResponse, logoutListenerError);
    }

    private final Response.Listener<JSONObject> logoutListenerResponse = new Response.Listener<JSONObject>() {
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
    };

    private final Response.ErrorListener logoutListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Toast.makeText(ctx, body, Toast.LENGTH_SHORT).show();
            }

            logoutButton.setEnabled(true);
        }
    };
}