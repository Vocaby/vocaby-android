package com.vocaby.app.ui;

import android.content.Context;
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

import java.util.List;

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

        emailView = view.findViewById(R.id.email_input);
        passwordView = view.findViewById(R.id.password_input);
        passwordConfirmView = view.findViewById(R.id.password_confirm_input);
        emailAlertView = view.findViewById(R.id.email_header_alert);
        passwordAlertView = view.findViewById(R.id.password_header_alert);
        passwordConfirmAlertView = view.findViewById(R.id.password_confirm_header_alert);

        backButton.setOnClickListener(backListener);
        signInText.setOnClickListener(backListener);
        registerButton.setOnClickListener(registerListener);

        return view;
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }



    private final View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            String passwordConfirm = passwordConfirmView.getText().toString();
            boolean passwordIsValid = false;
            boolean emailIsValid = false;

            if(password.isEmpty()) {
                passwordAlertView.setText(getString(R.string.enter_password));
            } else {
                passwordAlertView.setText("");
            }

            if(passwordConfirm.isEmpty()) {
                passwordConfirmAlertView.setText(getString(R.string.enter_password));
            } else {
                passwordConfirmAlertView.setText("");
                if(!password.isEmpty()) {
                    if(password.equals(passwordConfirm)) {
                        passwordIsValid = true;
                    } else {
                        passwordConfirmAlertView.setText(getString(R.string.password_no_match));
                    }
                }
            }

            if(email.isEmpty()) {
                emailAlertView.setText(getString(R.string.enter_email));
            } else {
                if(!isValidEmail(email)) {
                    emailAlertView.setText(getString(R.string.enter_valid_email));
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

            RequestManager requestManager = RequestManager.getInstance(ctx);
            requestManager.makeRegisterRequest(jsonObject, registerListenerResponse, registerListenerError);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final Response.Listener<JSONObject> registerListenerResponse = response -> {
        FragmentManager fm = getParentFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.enter_right_to_left,
                        R.anim.exit_left_to_right,
                        R.anim.enter_right_to_left,
                        R.anim.exit_left_to_right
                )
                .add(R.id.login_fragment_container, new SuccessfulCreationFragment())
                .commit();
    };

    private final Response.ErrorListener registerListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String jsonError = new String(networkResponse.data);
                try {
                    JSONObject json = new JSONObject(jsonError);
                    if (json.has("code")) {
                        int code = json.getInt("code");
                        if(code == getResources().getInteger(R.integer.USER_EXISTS)) {
                            emailAlertView.setText(getString(R.string.user_exists));
                        } else {
                            Toast.makeText(ctx, "Error has occurred...", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ctx, "Error has occurred while registering...", Toast.LENGTH_SHORT).show();
            }

            registerButton.setEnabled(true);
        }
    };

    private final View.OnClickListener backListener = v -> getParentFragmentManager().popBackStack();
}