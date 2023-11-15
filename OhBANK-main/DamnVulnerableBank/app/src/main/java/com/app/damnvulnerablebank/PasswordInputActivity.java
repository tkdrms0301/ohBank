package com.app.damnvulnerablebank;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PasswordInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_input);

        final EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = passwordEditText.getText().toString();

                Log.d("enteredPassword", enteredPassword+"");
                if (enteredPassword != null ) {
                    isPasswordValid(enteredPassword);
                }

            }
        });
    }

    private void isPasswordValid(String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = EncryptDecrypt.decrypt(sharedPreferences.getString("apiurl",null));
        String endpoint = "/api/user/password-check";
        final String finalurl = url+endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {
            requestData.put("password", password);
            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Enter the correct url for your api service site
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                            JSONObject data = decryptedResponse.getJSONObject("data");

                            String message = data.getString("message");
                            Log.d("message PW : ", message);
                            if (message.trim().equals("success")) {
                                Log.d("message PW : ", message);
                                setResult(RESULT_OK);
                            }else {
                                // Password is invalid, show error message
                                Toast.makeText(PasswordInputActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                            }
                            finish();

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
                final String retrivedToken  = EncryptDecrypt.decrypt(sharedPreferences.getString("accesstoken",null));
                HashMap headers=new HashMap();
                headers.put("Authorization","Bearer " + retrivedToken);
                Log.d("accesstoken", "accesstoken is " + retrivedToken);
                return headers;
            }};

        requestQueue.add(jsonObjectRequest);

    }
}