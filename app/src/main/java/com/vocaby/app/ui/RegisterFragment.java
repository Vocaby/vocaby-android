package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.viewmodels.RegisterViewModel;

public class RegisterFragment extends Fragment {
    private EditText emailView;
    private EditText passwordView;
    private EditText passwordConfirmView;
    private EditText firstNameView;
    private EditText lastNameView;
    private Button registerButton;
    private TextView emailAlertView;
    private TextView passwordAlertView;
    private TextView passwordConfirmAlertView;
    private TextView registrationAlert;

    private RegisterViewModel registerViewModel;

    public RegisterFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        firstNameView = view.findViewById(R.id.first_name_input);
        lastNameView = view.findViewById(R.id.last_name_input);
        registrationAlert = view.findViewById(R.id.registration_alert);

        backButton.setOnClickListener(backListener);
        signInText.setOnClickListener(backListener);
        registerButton.setOnClickListener(registerListener);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);
        registerViewModel.getAuthModel().observe(getViewLifecycleOwner(), authModel -> {
            if(authModel.passwordIsEmpty()) {
                passwordAlertView.setText(getString(R.string.enter_password));
            } else {
                if(authModel.passwordIsClean()) {
                    passwordAlertView.setText("");
                } else {
                    passwordAlertView.setText(R.string.valid_password_characters);
                }
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
        });

        registerViewModel.getRegistrationStatus().observe(getViewLifecycleOwner(), registrationMessage -> {
            if(registrationMessage.equals("s")) {
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
                registrationAlert.setText(registrationMessage);
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
            String firstName = cleanUpName(firstNameView.getText().toString());
            String lastName = cleanUpName(lastNameView.getText().toString());
            registerViewModel.setAuthData(email, password, passwordConfirm, firstName, lastName);
        }
    };

    private String cleanUpName(String name) {
        String cleaned = "";
        if(!name.isEmpty()) {
            cleaned = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }

        return cleaned;
    }

    private final View.OnClickListener backListener = v -> getParentFragmentManager().popBackStack();
}