package com.jenarvaezg.cafelitoscojonudos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jenarvaezg.cafelitoscojonudos.layout_elements.ContactsButton;
import com.jenarvaezg.cafelitoscojonudos.messages.Message;


import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.ConnectionResult;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener, SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mLight;
    private Sensor mCompass;


    protected enum requestCodes{LOGIN}
    private static String myID;
    private static GoogleApiClient mGoogleApiClient;

    private static final String IDFILE = "ID_FILE";
    private static final String CONTACTSFILE = "CONTACTS_FILE";

    private AsyncTask mTask;

    protected static boolean active = false;



    float[] mGravity;
    float[] mGeomagnetic;
    float toNorth;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()){
            case(Sensor.TYPE_AMBIENT_TEMPERATURE):
                float temp = sensorEvent.values[0];
                TextView tv = (TextView) findViewById(R.id.tempTextView);
                tv.setText(Double.toString(Math.round(temp * 1000d) / 1000d) + "ºC");
                break;
            case(Sensor.TYPE_LIGHT):
                float light = sensorEvent.values[0];
                float maxRange = 1000f;
                if(light > maxRange){
                    light = maxRange;
                }
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 1-(light/maxRange);
                getWindow().setAttributes(layout);
                break;
            case(Sensor.TYPE_ORIENTATION):
                toNorth = Math.round(sensorEvent.values[0]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //NOTHING
    }


    protected static Location getLocation(){
        try {
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (Exception e){
            Log.wtf("JOSE", "FUCK YOU: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("JOSE", "CONNECTED");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("JOSE", "CONNECTION SUSPENDED");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("JOSE", "CONNECTION FAILED");

    }




    synchronized protected void updateContactList(){
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
            fos = openFileOutput(IDFILE, Context.MODE_APPEND | Context.MODE_PRIVATE);
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

    synchronized public  void removeContact(String contact){
        String[] contacts = getContactsFromFile();
        ArrayList<String> contactsLeft = new ArrayList<>();
        for(int i = 0; i < contacts.length; i++){
            if(!contacts[i].equals(contact)){
                contactsLeft.add(contacts[i]);
            }
        }
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = openFileOutput(CONTACTSFILE, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            for(String c : contactsLeft) {
                writer.write(c + "\n");
            }
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
        }else
        updateContactList();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Log.d("JOSE", mSensorManager.getSensorList(Sensor.TYPE_ALL).toString());
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
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
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);


        active = true;
        PollerService.notifText = "";
        if(myID != null) {
            try {
                PollerService.setPollerVars(this, myID);
                Intent i = new Intent(this, PollerService.class);
               /* i.putExtra("act", this);
                i.putExtra("id", this.myID);*/
                startService(i);
            }catch (Exception e){
                Log.e("JOSE", e.getMessage());
            }
            //new PollerThread(myID, getApplicationContext(), findViewById(android.R.id.content), this).start();
        }
        mTask = new ContactsUpdater().execute();

    }

    @Override
    protected void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this);
        active = false;
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
        if (id == R.id.action_find) {
            findOffice();
        }else if(id == R.id.action_add){
            addNewUser();
        }else if(id == R.id.action_create_group){
            createGroup();
        }

        return super.onOptionsItemSelected(item);
    }

    private void findOffice() {
        View compassView = View.inflate(this, R.layout.compass_layout, null);
        final ImageView compassArrow = (ImageView) compassView.findViewById(R.id.compass_arrow);
        compassArrow.setRotation(90);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(compassView);
        builder.setTitle("Where is the Office!?");
        final RotationTask task = new RotationTask(compassArrow);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d("JOSE", "EXECUTED?");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                task.kill();
                task.cancel(true);
            }
        });

        builder.show();

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
                    Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
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
                Message.UnreadMessagesNumber[] msgs = Message.getUnreadMessagesNumber();
                if(msgs != null && msgs.length > 0){
                    publishProgress(msgs);
                }
                try {
                    Thread.sleep(2000, 0);
                } catch (InterruptedException e) {
                    break;
                }
            }
            return null;
        }
        protected void onProgressUpdate(Message.UnreadMessagesNumber... msgs){
            TableLayout layout = (TableLayout) findViewById(R.id.contacts_table);
            ArrayList<ContactsButton> cbs = new ArrayList<>();
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
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    class RotationTask extends AsyncTask<String, Float, Float>{

        ImageView compassArrow;

        Boolean kill = false;

        public void kill(){
            this.kill = true;
        }

        public RotationTask(ImageView compassArrow){
            this.compassArrow = compassArrow;
        }

        private float normalizeDegree(float value) {
            if (value >= 0.0f && value <= 180.0f) {
                return value;
            } else {
                return 180 + (180 + value);
            }
        }

        @Override
        protected Float doInBackground(String... strings) {
            Log.d("JOSE", "ROLLING");
            Location targetLocation = new Location("");//provider name is unecessary
            targetLocation.setLatitude(40.287610d);//your coords of course
            targetLocation.setLongitude(-3.808517);
            Location myLoc;
            mSensorManager.registerListener(MainActivity.this, mCompass, SensorManager.SENSOR_DELAY_GAME);
            while(!kill){
                try {

                    myLoc = MainActivity.getLocation();
                    GeomagneticField geoField = new GeomagneticField(
                            Double.valueOf(myLoc.getLatitude()).floatValue(),
                            Double.valueOf(myLoc.getLongitude()).floatValue(),
                            Double.valueOf(myLoc.getAltitude()).floatValue(),
                            System.currentTimeMillis()
                    );
                    Float heading = -toNorth;
                    Log.d("JOSE", "NORTH: " + Float.toString(toNorth));
                    heading += geoField.getDeclination();
                    heading = (myLoc.bearingTo(targetLocation) - heading) * -1;
                    Log.d("JOSE", Float.toString((heading + 360) % 360));
                    publishProgress((heading + 360) % 360);
                }catch (Exception e){
                    Log.e("JOSE", "BYE VIETNAM");
                    break;
                }
            }
            mSensorManager.unregisterListener(MainActivity.this, mCompass);
            return 0.0f;
        }

        @Override
        protected void onProgressUpdate(Float... bearing){
            compassArrow.setRotation((bearing[0] + 180) % 360);
        }
    }
}