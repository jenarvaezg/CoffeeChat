package com.jenarvaezg.cafelitoscojonudos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jenarvaezg.cafelitoscojonudos.layout_elements.ContactsButton;
import com.jenarvaezg.cafelitoscojonudos.messages.Message;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener {


    @Override
    public void onConnected(Bundle bundle) {
        try {
            Log.d("JOSE", "CONNECTED");
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("JOSE", mLastLocation.toString());
            Log.d("JOSE", "PERMISSION");
        }catch (Exception e){
            Log.e("JOSE", "FUCK YOU: " + e.getMessage() );
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("JOSE", "CONNECTION SUSPENDED");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("JOSE", "CONNECTION FAILED");

    }

    protected enum requestCodes{LOGIN}
    private static String myID;
    private static GoogleApiClient mGoogleApiClient;

    private static final String IDFILE = "ID_FILE";
    private static final String CONTACTSFILE = "CONTACTS_FILE";

    private AsyncTask mTask;

    protected void updateContactList(){
        TableLayout contactsLayout = (TableLayout) findViewById(R.id.contacts_table);
        contactsLayout.removeAllViews();
        String[] contants = getContactsFromFile();

        Log.d("JOSE", "CONTACTS: " + Arrays.toString(contants));

        for(String contact : contants) {
            TableRow tr = new TableRow(this);
            Button button = new ContactsButton(this, contact, myID);
            tr.addView(button);
            contactsLayout.addView(tr);
        }
    }

    synchronized private String getIDFromFile(){
        FileInputStream fis = null;
        String id = null;
        try {
            fis = openFileInput(IDFILE);
            byte[] buffer = new byte[1024];
            Integer n = fis.read(buffer);
            fis.close();
            id = new String(buffer, 0, n);
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
        }finally{
            try{
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    synchronized private void saveIDToFile(String id){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(IDFILE, Context.MODE_APPEND|Context.MODE_PRIVATE);
            fos.write(id.getBytes());
            fos.flush();
            Log.d("JOSE", "SAVED ID! " + getIDFromFile());

        } catch (Exception e) {
            Log.e("JOSE", "SAVE ID ERROR WITH FILE, " + e.getMessage());
        }finally{
            try{
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    synchronized private String[] getContactsFromFile(){
        ArrayList<String> contacts = new ArrayList<>();
        FileInputStream fis = null;
        BufferedReader reader = null;
        String id = null;
        try {
            fis = openFileInput(CONTACTSFILE);
            reader = new BufferedReader(new InputStreamReader(fis));
            for(String contact = reader.readLine(); contact != null; contact = reader.readLine()) {
                contacts.add(contact);
            }
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
        }finally{
            try{
                if(reader != null){
                    reader.close();
                }
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contacts.toArray(new String[0]);
    }

    synchronized protected void addContact(String contact){
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = openFileOutput(CONTACTSFILE, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(contact + "\n");
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
        }finally{
            try{
                if(writer != null){
                    writer.close();
                }
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("JOSE", Arrays.toString(getContactsFromFile()));
        myID = getIDFromFile();
        if(myID == null) {
            Intent activityIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(activityIntent, requestCodes.LOGIN.ordinal());
        }
        updateContactList();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCodes.values()[requestCode]) {
            case LOGIN:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    myID = res.getString("ID");
                    saveIDToFile(myID);
                }else{
                    myID = "NOT_VALID";
                }
                break;
        }
        if(myID.equals("NOT_VALID")){
            finish();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(myID != null) {
            new PollerThread(myID, getApplicationContext(), findViewById(android.R.id.content), this).start();
        }
        Log.d("JOSE", "GOING TO EXECUTE CONTACTS UPDATER");
        mTask = new ContactsUpdater().execute();

    }

    @Override
    protected void onStop(){
        super.onStop();
        super.onStop();
        mTask.cancel(true);
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
        }else if(id == R.id.action_add){
            addNewUser();
        }else if(id == R.id.action_create_group){
            createGroup();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createGroup() {
        View addGroupView = View.inflate(this, R.layout.add_group_layout, null);
        final LinearLayout checkboxesLayout = (LinearLayout) addGroupView.findViewById(R.id.checkboxes_layout);
        final EditText groupNameText = (EditText) addGroupView.findViewById(R.id.group_name_box);
        for(String contact: getContactsFromFile()){
            if(!contact.startsWith("ÇGROUP:")) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(contact);
                checkboxesLayout.addView(checkBox);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(addGroupView);
        builder.setTitle("Add Group");
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                ArrayList<String> contactsToAdd = new ArrayList<String>();
                for(int i = 0; i < checkboxesLayout.getChildCount(); i++){
                    CheckBox checkBox = (CheckBox) checkboxesLayout.getChildAt(i);
                    if(checkBox.isChecked()){
                        contactsToAdd.add(checkBox.getText().toString());
                    }
                }
                String groupName = groupNameText.getText().toString();
                if(!groupName.equals("") && contactsToAdd.size() > 0) {
                    int status = MessageHandler.createGroup(myID, groupName, contactsToAdd.toArray(new String[0]));
                    if(status == 0){
                        addContact("ÇGROUP:" + groupName);
                        updateContactList();
                    }
                    Toast.makeText(MainActivity.this, Integer.toString(status), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
    }

    private void addNewUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText input = new EditText(MainActivity.this);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setMessage(R.string.add_user_dialog);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String userToAdd = input.getText().toString();
                if(myID.equals(userToAdd)){
                    return;
                }
                if (!MessageHandler.addUserAsFriend(userToAdd, myID)) {
                    Toast.makeText(MainActivity.this, "User does na verot exist", Toast.LENGTH_LONG).show();
                } else {
                    addContact(userToAdd);
                    updateContactList();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private class ContactsUpdater extends AsyncTask<String, Message.UnreadMessagesNumber, String>{

        @Override
        protected String doInBackground(String... strings) {
            for(;;){
                try {
                    Thread.sleep(2000, 0);
                } catch (InterruptedException e) {
                    break;
                }
                Message.UnreadMessagesNumber[] msgs = Message.getUnreadMessagesNumber();
                if(msgs != null && msgs.length > 0){
                    publishProgress(msgs);
                }
            }
            return null;
        }
        protected void onProgressUpdate(Message.UnreadMessagesNumber... msgs){
            TableLayout layout = (TableLayout) findViewById(R.id.contacts_table);
            ArrayList<ContactsButton> cbs = new ArrayList<>();
            ArrayList<Message.UnreadMessagesNumber> unknMsgs = new ArrayList<>();
            for (int i = 0; i < layout.getChildCount(); i++) {
                TableRow tr = (TableRow) layout.getChildAt(i);
                cbs.add((ContactsButton) tr.getChildAt(0));
            }

            for(Message.UnreadMessagesNumber msg: msgs){
                Boolean added = false;
                ContactsButton[] cbsArr = cbs.toArray(new ContactsButton[0]);
                for(int i = 0; i < cbsArr.length; i++){
                    if(msg.getFrom().equals(cbsArr[i].getContact())){
                        cbsArr[i].addNewMessagesNumber(msg.getN());
                        cbs.remove(cbsArr[i]);
                        added = true;
                        break;
                    }
                }
                if(!added){
                    if (!MessageHandler.addUserAsFriend(msg.getFrom(), myID)) {
                        Toast.makeText(MainActivity.this, "User " + msg.getFrom() + " does not exist", Toast.LENGTH_LONG).show();
                    } else {
                        addContact(msg.getFrom());
                        updateContactList();
                    }
                }
            }

        }
    }
}