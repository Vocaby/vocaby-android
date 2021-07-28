package com.vocaby.app;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private Context ctx;
    private TextView emailView;
    private EditText passwordView;
    private EditText passwordConfirmView;
    private Button registerButton;
    private TextView emailAlertView;
    private TextView passwordAlertView;
    private TextView passwordConfirmAlertView;

    public RegisterFragment() {

    }

    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_register, container, false);
        Button backButton = view.findViewById(R.id.back_button);
        TextView signInText = view.findViewById(R.id.signin);
        registerButton = view.findViewById(R.id.register_button);

        emailView = view.findViewById(R.id.email);
        passwordView = view.findViewById(R.id.password);
        passwordConfirmView = view.findViewById(R.id.password_confirm);
        emailAlertView = view.findViewById(R.id.email_alert);
        passwordAlertView = view.findViewById(R.id.password_alert);
        passwordConfirmAlertView = view.findViewById(R.id.password_confirm_alert);

        backButton.setOnClickListener(backListener);
        signInText.setOnClickListener(backListener);
        registerButton.setOnClickListener(registerListener);

        return view;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }



    private View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            String passwordConfirm = passwordConfirmView.getText().toString();
            boolean passwordIsValid = false;
            boolean emailIsValid = false;

            if(password.isEmpty()) {
                passwordAlertView.setText("Please enter a password");
            } else {
                passwordAlertView.setText("");
            }

            if(passwordConfirm.isEmpty()) {
                passwordConfirmAlertView.setText("Please enter a password");
            } else {
                passwordConfirmAlertView.setText("");
                if(!password.isEmpty()) {
                    if(password.equals(passwordConfirm)) {
                        passwordIsValid = true;
                    } else {
                        passwordConfirmAlertView.setText("Password does not match!");
                    }
                }
            }

            if(email.isEmpty()) {
                emailAlertView.setText("Please enter an email");
            } else {
                if(!isValidEmail(email)) {
                    emailAlertView.setText("Email is not valid");
                } else {
                    emailAlertView.setText("");
                    emailIsValid = true;
                }
            }

            if(emailIsValid && passwordIsValid) {
                postDataToServer(email, password);
                registerButton.setEnabled(false);
            }
        }
    };

    private void postDataToServer(String email, String password) {
        String url = getString(R.string.register_url);
        RequestQueue q = Volley.newRequestQueue(ctx);
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
            jsonObject.put("first_name", "");
            jsonObject.put("last_name", "");
            jsonObject.put("saves", jsonArray);

            JsonObjectRequest jsonObjectRequest = makeRequest(url, jsonObject);
            q.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JsonObjectRequest makeRequest(String url, JSONObject json) {
        return new JsonObjectRequest
                (Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        FragmentManager fm = getParentFragmentManager();
                        fm.beginTransaction()
                                .setCustomAnimations(
                                        R.anim.enter_right_to_left,
                                        R.anim.exit_left_to_right,
                                        R.anim.enter_right_to_left,
                                        R.anim.exit_left_to_right
                                )
                                .add(R.id.fragment_container, new SuccessfulCreationFragment())
                                .commit();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> m = new HashMap<>();
                // m.put("Authorization", Token);

                return m;
            }
        };
    }

    private View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MainActivity)requireActivity()).onBackPressed();
        }
    };
}