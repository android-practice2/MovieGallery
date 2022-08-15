package com.bignerdranch.android.moviegallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.User;
import com.bignerdranch.android.moviegallery.integration.model.UserRegisterRequest;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Inject
    AppClient mAppClient;

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login_button = findViewById(R.id.login_button);
        EditText nickname_editText = findViewById(R.id.nickname_editText);
        EditText phone_number_editText = findViewById(R.id.phone_number_editText);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = nickname_editText.getText().toString();
                String phoneNumber = phone_number_editText.getText().toString();
                UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
                userRegisterRequest.setNickname(userName);
                userRegisterRequest.setPhone_number(phoneNumber);
                Call<User> call = mAppClient.register(userRegisterRequest);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        Toast.makeText(LoginActivity.this, "register user success", Toast.LENGTH_SHORT).show();
                        User user = response.body();
                        Intent data = new Intent();
                        data.putExtra(Constants.EXTRA_USER, user);

                        setResult(Activity.RESULT_OK, data);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "register user failure", Toast.LENGTH_SHORT).show();

                        Log.e(TAG, "register user exception", t);
                        finish();

                    }
                });

            }
        });


    }

}
