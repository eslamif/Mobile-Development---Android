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
import android.net.Uri;
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


import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CreateMemory extends AppCompatActivity {
    String userKey;
    Uri imagePath;
    String fileName;
    int includFile = 0;   //set to false

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memory);

        //Get user's uid from MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userKey = extras.getString("userKey");
            System.out.println(">>> Extra value = " + userKey);
        }

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(this);

        final Firebase ref = new Firebase("https://travel-memory.firebaseio.com/users/" + userKey);
                ref.addChildEventListener(new ChildEventListener() {
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        System.out.println(">>> onChildAdded");
                        adapter.add((String) dataSnapshot.child("text").getValue());

                        // Log off Firebase
                        final Button logoffButt = (Button) findViewById(R.id.buttLogout);
                        logoffButt.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                ref.unauth();
                                userKey = "";
                                System.out.println(">>> User Logged Off");

                                //Redirect to MainActivity
                                Intent intent = new Intent(CreateMemory.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }

                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        System.out.println(">>> onChildRemoved");
                        adapter.remove((String) dataSnapshot.child("text").getValue());
                    }

                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        System.out.println(">>> onChildChanged");
                        int itemPos = adapter.getPosition((String) dataSnapshot.child("text").getValue());
                        //System.out.println(">>> itemPos = " + itemPos);
                    }

                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        //Add File
        final Button photoButton = (Button) findViewById(R.id.photoButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println(">>> Adding Photo...");

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                int PICK_IMAGE = 1;
                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        // Add items via the Button and EditText at the bottom of the window.
        final EditText text = (EditText) findViewById(R.id.todoText);
                final Button button = (Button) findViewById(R.id.addButton);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        System.out.println("Adding Child");
                        if (includFile == 0) {
                            Firebase pushRef = new Firebase("https://travel-memory.firebaseio.com/users/" + userKey);
                            pushRef.push()
                                .child("text")
                                .setValue(text.getText().toString());
                        }
                        else if (includFile == 1) {
                            Firebase ref = new Firebase("https://travel-memory.firebaseio.com/users/" + userKey);
                            Firebase pushRef =  ref.push();
                            pushRef.child("text")
                                .setValue(text.getText().toString());
                            pushRef.child("file")
                                    .setValue(fileName);
                            includFile = 0;
                        }
                        text.setText("");
                    }
                });

                // Delete items when clicked
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        new Firebase("https://travel-memory.firebaseio.com/users/" + userKey)
                                .orderByChild("text")
                                .equalTo((String) listView.getItemAtPosition(position))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                            firstChild.getRef().removeValue();
                                        }
                                    }

                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
                    }
                });

                //Update item on long click
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   final int position, long id) {
                        final TextView userinputtext = (TextView) findViewById(R.id.userinputtext);
                        View v = (LayoutInflater.from(CreateMemory.this)).inflate(R.layout.user_input, null);

                        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CreateMemory.this);
                        alertBuilder.setView(v);
                        final EditText userInput = (EditText) v.findViewById(R.id.userinput);

                        //System.out.println("position = " + position);
                        //System.out.println("id = " + id);

                        alertBuilder.setCancelable(true)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Firebase mylist = new Firebase("https://travel-memory.firebaseio.com/users/" + userKey);
                                        mylist.orderByChild("text")
                                                .equalTo((String) listView.getItemAtPosition(position))
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                                        //Update Firebase
                                                        Firebase dataRef = firstChild.getRef();
                                                        dataRef.child("text").setValue(userInput.getText().toString());

                                                    }

                                                    public void onCancelled(FirebaseError firebaseError) {
                                                    }
                                                });
                                    }
                                });
                        Dialog dialog = alertBuilder.create();
                        dialog.show();
                        return true;
                    }
                });
            }

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

            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data){
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    System.out.println(">>> In function onActivityResult");

                    // Get the URI that points to the selected contact
                    imagePath = data.getData();
                    fileName = imagePath.getLastPathSegment();
                    System.out.println(">>> Uri = " + imagePath);
                    System.out.println(">>> File name = " + fileName);

                    includFile = 1; //include file to Firebase
                }

            }
        }


