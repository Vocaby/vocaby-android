package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.viewmodels.RegisterViewModel;

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

    private RegisterViewModel registerViewModel;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);
        registerViewModel.getAuthModel().observe(getViewLifecycleOwner(), new Observer<AuthModel>() {
            @Override
            public void onChanged(AuthModel authModel) {
                if(authModel.passwordIsEmpty()) {
                    passwordAlertView.setText(getString(R.string.enter_password));
                } else {
                    passwordAlertView.setText("");
                }

                if(authModel.confirmationPasswordIsEmpty()) {
                    passwordConfirmAlertView.setText(getString(R.string.enter_password));
                } else {
                    passwordConfirmAlertView.setText("");

                    if(!authModel.passwordsMatch()) {
                        passwordConfirmAlertView.setText(getString(R.string.password_no_match));
                    }
                }

                if(authModel.emailIsEmpty()) {
                    emailAlertView.setText(getString(R.string.enter_email));
                } else {
                    if(!authModel.isEmail()) {
                        emailAlertView.setText(getString(R.string.enter_valid_email));
                    } else {
                        emailAlertView.setText("");
                    }
                }

                if(authModel.registrationIsValid()) {
                    registerViewModel.register();
                    registerButton.setEnabled(false);
                }
            }
        });

        registerViewModel.getRegistrationStatus().observe(getViewLifecycleOwner(), registrationSuccessful -> {
            if(registrationSuccessful) {
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
            } else {
                emailAlertView.setText(getString(R.string.user_exists));
                registerButton.setEnabled(true);
            }
        });
    }

    private final View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            String passwordConfirm = passwordConfirmView.getText().toString();
            registerViewModel.setAuthData(email, password, passwordConfirm);
        }
    };

    private final View.OnClickListener backListener = v -> getParentFragmentManager().popBackStack();
}