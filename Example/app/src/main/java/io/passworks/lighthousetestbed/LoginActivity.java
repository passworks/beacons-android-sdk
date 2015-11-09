package io.passworks.lighthousetestbed;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;

import io.passworks.lighthouse.Lighthouse;


public class LoginActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private OkHttpClient mOkHttpClient;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressDialog = new ProgressDialog(this);
        mOkHttpClient = new OkHttpClient();
        mUsernameEditText = (EditText) findViewById(R.id.usernameEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClick();
            }
        });
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUsernameEditText.setText(getPreferences(Context.MODE_PRIVATE).getString("username", ""));
        mPasswordEditText.setText(getPreferences(Context.MODE_PRIVATE).getString("password", ""));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private void loginClick() {

        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.show();

        final String username = mUsernameEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();

        final Request request = new Request.Builder()
                .url("http://magellan.beacons.staging.passworks.io/v1/apps/")
                .build();

        mOkHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            }
        }).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                if (Looper.myLooper() == null)
                    Looper.prepare();
                SharedPreferences sharedPreferences = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("username", "").putString("password", "").apply();
                mProgressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Failed to log in.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                SharedPreferences sharedPreferences = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("username", username).putString("password", password).apply();
                mProgressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    JSONObject jsonObject = jsonArray.optJSONObject(0);
                    if (jsonObject != null) {
                        String token = jsonObject.optString("api_secret_android");
                        LoginActivity.this.getPreferences(Context.MODE_PRIVATE).edit().putString("api_secret_android", token).apply();
                        //Lighthouse.setup(getApplicationContext(), token);
                        Intent intent = new Intent(LoginActivity.this, BeaconsActivity.class);
                        intent.putExtra("token", token);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
