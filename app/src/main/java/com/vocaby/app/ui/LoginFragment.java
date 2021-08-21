package com.vocaby.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.api.RequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginFragment extends Fragment {
    private Context ctx;
    private TextView emailView;
    private EditText passwordView;
    private TextView emailAlertView;
    private TextView passwordAlertView;
    private TextView loginAlertView;
    private Button loginButton;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_login, container, false);
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(backListener);

        emailView = view.findViewById(R.id.email_input);
        passwordView = view.findViewById(R.id.password_input);
        emailAlertView = view.findViewById(R.id.email_header_alert);
        passwordAlertView = view.findViewById(R.id.password_header_alert);
        loginAlertView = view.findViewById(R.id.login_alert);
        TextView signUpText = view.findViewById(R.id.signup);
        loginButton = view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(loginListener);

        signUpText.setOnClickListener(v -> getParentFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_left_to_right,
                R.anim.enter_right_to_left,
                R.anim.exit_left_to_right
            )
            .addToBackStack(null)
            .add(R.id.login_fragment_container, new RegisterFragment())
            .commit());

        return view;
    }

    private final View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            boolean passwordIsValid = false;
            boolean emailIsValid = false;

            if(email.isEmpty()) {
                emailAlertView.setText(getString(R.string.enter_email));
            } else if(!isValidEmail(email)) {
                emailAlertView.setText(getString(R.string.enter_valid_email));
            } else {
                passwordAlertView.setText("");
                emailIsValid = true;
            }

            if(password.isEmpty()) {
                passwordAlertView.setText(getString(R.string.enter_password));
            } else {
                passwordAlertView.setText("");
                passwordIsValid = true;
            }

            if(emailIsValid && passwordIsValid) {
                login(email, password);
                loginButton.setEnabled(false);
            }
        }
    };

    private void login(String email, String password) {
        try {
            DataManager dataManager = DataManager.getInstance(ctx);
            List<String> saves = dataManager.getSaves();
            JSONArray jsonArray = new JSONArray();
            for(String word : saves) {
                jsonArray.put(word);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("saves", jsonArray);

            SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.email), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.email), email);
            editor.apply();

            RequestManager requestManager = RequestManager.getInstance(ctx);
            requestManager.makeLoginRequest(jsonObject, loginListenerResponse, loginListenerError);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final Response.Listener<JSONObject> loginListenerResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String token = response.getString("token");
                SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.token_key),token);
                editor.apply();

                DataManager dataManager = DataManager.getInstance(ctx);
                dataManager.overwriteSave(covertJsonToArray(response.getJSONArray("saves")));
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(
                                R.anim.enter_right_to_left,
                                R.anim.exit_right_to_left,
                                R.anim.enter_right_to_left,
                                R.anim.exit_right_to_left
                        )
                        .replace(R.id.login_fragment_container, new ProfileFragment())
                        .commit();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final Response.ErrorListener loginListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String jsonError = new String(networkResponse.data);
                try {
                    JSONObject json = new JSONObject(jsonError);
                    if (json.has("code")) {
                        int code = json.getInt("code");
                        if(code == getResources().getInteger(R.integer.WRONG_CREDENTIALS)) {
                            loginAlertView.setText(getString(R.string.wrong_credentials_desc));
                        } else {
                            Toast.makeText(ctx, "Error has occurred...", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ctx, "Error has occurred while logging in...", Toast.LENGTH_SHORT).show();
            }

            loginButton.setEnabled(true);
        }
    };

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private static List<String> covertJsonToArray(JSONArray json) throws JSONException {
        List<String> newList = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            newList.add(json.getString(i));
        }

        return newList;
    }

    private final View.OnClickListener backListener = v -> requireActivity().finish();
}