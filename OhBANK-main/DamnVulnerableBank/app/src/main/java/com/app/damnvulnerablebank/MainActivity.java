package com.app.damnvulnerablebank;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                        System.exit(0);
                    }
                }).create().show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_banklogin);

       boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
       FridaCheckJNI fridaCheck = new FridaCheckJNI();


       if(android.os.Debug.isDebuggerConnected()){
            Toast.makeText(getApplicationContext(), "Debug from vm",Toast.LENGTH_LONG).show();
        }

        if(EmulatorDetectortest.isEmulator()){
            Toast.makeText(getApplicationContext(), "Emulator Detected",Toast.LENGTH_LONG).show();
        }

        if(isDebuggable){
            Toast.makeText(getApplicationContext(),"Debbuger is Running", Toast.LENGTH_SHORT).show();
        }

        if(RootUtil.isDeviceRooted()) {
            Toast.makeText(getApplicationContext(), "Phone is Rooted", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check frida
        if(fridaCheck.fridaCheck() == 1) {
            Toast.makeText(getApplicationContext(), "Frida is running", Toast.LENGTH_SHORT).show();
            Log.d("FRIDA CHECK", "FRIDA Server DETECTED");

            finish();
        } else {
            Log.d("FRIDA CHECK", "FRIDA Server NOT RUNNING");
            Toast.makeText(getApplicationContext(), "Frida is NOT running", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
        boolean isloggedin=sharedPreferences.getBoolean("isloggedin", false);
        if(isloggedin)
        {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            finish();
        }

    }

    public void loginPage(View view){
        Intent intent =new Intent(getApplicationContext(), BankLogin.class);
        startActivity(intent);
    }

    public void signupPage(View view){
        Intent intent =new Intent(getApplicationContext(), RegisterBank.class);
        startActivity(intent);
    }

    public void healthCheck(View v){
        SharedPreferences secretPref = getSharedPreferences("secret", MODE_PRIVATE);
        SharedPreferences.Editor secretEditor = secretPref.edit();
//        keystore encryption
//        key : bf3c199c2470cb477d907b1e0917c17b
//        iv : 5183666c72eec9e4
        secretEditor.putString("key", "JO1QhyBz6ywDQ8sm5vWJCk3z55vFmQ9DWQaXRI1P2s56jsKpILo1CM7y5/74+TwQ/m12sIUjbpfOQAhjHjcWoYxitR7Djq5jyHgFJS6VuGiT2elhqmQnQOhLEmPAr1R4Y+nJHyjCjdLDAwqKFtA7WJiXBJCOirrKra69fO+4R4C9MeQW6P2IQ5wBDRfwcl8VkKnlZyQzbSUcKQSUAUPYZUD92f+5YbTRgGsiSH8rnx7p1PJXkeMeWUqsA9GQC8K5UrWh1tMlsblH2oG+xqvWrDaZUb4E1xrNJf8sGOJM+jsGlFXOxgZLDSa2dvqwUDdmZDhNd1T7hGo4Ttk4U/gbXg==");
        secretEditor.putString("iv", "BZZz4cueSgUnd3cpLgbxmQ+3BEzQiaE9SkklQBw1ZNesWaKNLYxInuC4drzSsG3QktZdyva5PnzwwYItG//E7xUwMVPsoYEDBW1O9/ytd5lOgFp6KoXzieCq9tlgIatck0cBZAZ8Mc+qBmtDRVoCpQ4YVAeqDVCHPcwhYoLRqSyy2//tZ/iCeOQFmCCM5/Ka6ni/dcJ6WUs6y/Gq5avPwaeInhno3ne6JhygO2C5lTwweEVe/oKt5KM/IOLsbvfDIxeevZHuSs9q+VIgM+kKQr9XdCJr9CBXaFweYobg6U+Z+vQTtQcgGiH3TF7i8PKCuBKzwnixn+KpxleJmTd6aQ==");
        secretEditor.apply();
        EncryptDecrypt.getInstance().setKey(secretPref.getString("key", null));
        EncryptDecrypt.getInstance().setIv(secretPref.getString("iv", null));

        Log.d("ENCRYPTED", EncryptDecrypt.getInstance().getKey());
        Log.d("ENCRYPTED", EncryptDecrypt.getInstance().getIv());

        SharedPreferences pref = getApplicationContext().getSharedPreferences("apiurl", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        EditText ed=findViewById(R.id.apiurl);

        final String api = EncryptDecrypt.encrypt(ed.getText().toString().trim());
        editor.putString("apiurl", api);
        editor.apply();
        final View vButton = findViewById(R.id.healthc);
        final Button bButton = (Button) findViewById(R.id.healthc);
        RequestQueue queue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = EncryptDecrypt.decrypt(sharedPreferences.getString("apiurl",null));

        String endpoint="/api/health/check";
        String finalurl = url+endpoint;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        bButton.setText("Api is Up");
                        bButton.setTextColor(Color.GREEN);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                bButton.setText("Api is Down");
                bButton.setTextColor(Color.RED);
            }
        });
        queue.add(stringRequest);
        queue.getCache().clear();
    }
}