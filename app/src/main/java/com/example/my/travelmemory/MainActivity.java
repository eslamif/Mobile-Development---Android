//Frank Eslami, CS 496, Final Project
//App Name: Travel Memory
//Description: During a trip, enter snippets of memorable experiences to reminisce on later.
//Include pictures, menus, or other documents.
//Future enhancements: include geolocation and timestamp to map your travel. Export as scrapbook

//References
//https://www.youtube.com/watch?v=BXTanDpOTVU
//https://www.firebase.com/docs/android/quickstart.html
//https://cloud.google.com/solutions/articles#mobile

package com.example.my.travelmemory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(this);

        //Listen for Log-In or Register Button Presses
        final Button loginButt = (Button) findViewById(R.id.buttLogin);
        Button regButt = (Button) findViewById(R.id.buttReg);

        //Log In User
        loginButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(">>> logIn button pressed");

                //Get user's email
                final EditText etEmail = (EditText) findViewById(R.id.etEmail);
                String userEmail = etEmail.getText().toString();
                System.out.println(">>> Email = " + userEmail);

                //Get user's password
                final EditText etPass = (EditText) findViewById(R.id.etPassword);
                String userPass = etPass.getText().toString();
                System.out.println(">>> Pass = " + userPass);

                //Log in to Firebase
                final Firebase ref = new Firebase("https://travel-memory.firebaseio.com");
                //ref.authWithPassword("bob@firebase.com", "pass", new Firebase.AuthResultHandler() {
                ref.authWithPassword(userEmail, userPass, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        System.out.println(">>> User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());

                        if ( authData.getProvider().equals("password")) {
                            System.out.println( ">>> User logged in successfully");

                            final TextView logMessage = (TextView) findViewById(R.id.tvLogMess);
                            logMessage.setText("Login Successful");

                            /*
                            //Test storing data to user's profile
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("provider", authData.getProvider());
                            map.put("testCate", "testVal");
                            ref.child("users").child(authData.getUid()).setValue(map);
                            */

                            //Send user's uid to CreateMemory class
                            Intent intent = new Intent(MainActivity.this, CreateMemory.class);
                            intent.putExtra("userKey", authData.getUid());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        System.out.println( ">>> User Login Failed.");
                        final TextView logMessage = (TextView) findViewById(R.id.tvLogMess);
                        logMessage.setText("Invalid Email or Password. Please try again.");
                    }
                });
            }
        });

        //Register User
        regButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(">>> Reg button pressed");

                //Get user's email
                final EditText etEmail = (EditText) findViewById(R.id.etEmail);
                String userEmail = etEmail.getText().toString();
                System.out.println(">>> Email = " + userEmail);

                //Get user's password
                final EditText etPass = (EditText) findViewById(R.id.etPassword);
                String userPass = etPass.getText().toString();
                System.out.println(">>> Pass = " + userPass);

                //Register to Firebase
                Firebase ref = new Firebase("https://travel-memory.firebaseio.com");
                //ref.createUser("bob@firebase.com", "pass", new Firebase.ValueResultHandler<Map<String, Object>>() {
                ref.createUser(userEmail, userPass, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        System.out.println(">>> Successfully created user account with uid: " + result.get("uid"));
                        final TextView logMessage = (TextView) findViewById(R.id.tvLogMess);
                        logMessage.setText("Successfully Registered. Press the Log In button to log in.");
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        final TextView logMessage = (TextView) findViewById(R.id.tvLogMess);
                        logMessage.setText("Unable to register you to the server. Please try again later.");
                    }
                });
            }
        });
    } //end onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
