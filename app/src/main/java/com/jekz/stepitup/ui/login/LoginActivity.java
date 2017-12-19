package com.jekz.stepitup.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jekz.stepitup.R;
import com.jekz.stepitup.data.SharedPrefsManager;
import com.jekz.stepitup.data.request.RemoteLoginModel;
import com.jekz.stepitup.ui.home.HomeActivity;
import com.jekz.stepitup.ui.signup.SignupActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity implements LoginContract.View {
    @BindView(R.id.edittext_username)
    EditText usernameEditText;

    @BindView(R.id.edittext_password)
    EditText passwordEditText;


    @BindView(R.id.button_login)
    Button loginButton;

    @BindView(R.id.login_progress)
    ProgressBar progressBar;


    LoginContract.Presenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        SharedPrefsManager manager = SharedPrefsManager.getInstance(getApplicationContext());
        loginPresenter = new LoginPresenter(new RemoteLoginModel(manager));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginPresenter.onViewAttached(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.onViewDetached();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick({R.id.button_login, R.id.link_signup})
    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                loginPresenter.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
                break;
            case R.id.link_signup:
                loginPresenter.signup();
                break;
        }
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        loginButton.setAlpha(.5f);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        loginButton.setAlpha(1);
    }

    @Override
    public void showUsernameError(String message) {
        usernameEditText.setError(message);
        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        usernameEditText.startAnimation(shake);
    }

    @Override
    public void showPasswordError(String message) {
        passwordEditText.setError(message);
        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        passwordEditText.startAnimation(shake);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
