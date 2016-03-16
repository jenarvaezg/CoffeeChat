package com.jenarvaezg.cafelitoscojonudos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {


    protected enum requestCodes{LOGIN}
    private static String myID;
    private static EditText messageBox;
    private static Button sendButton;

    private static String IDFILE = "ID_FILE";


    private String getIDFromFile(){
        FileInputStream fis = null;
        String id = null;
        try {
            fis = openFileInput(IDFILE);
            byte[] buffer = new byte[1024];
            fis.read(buffer);
            fis.close();
            id = new String(buffer);
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

    private void saveIDToFile(String id){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(IDFILE, Context.MODE_PRIVATE);
            fos.write(id.getBytes());
        } catch (Exception e) {
            Log.e("JOSE", "ERROR WITH FILE, " + e.getMessage());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("JOSE", "ONCREATED");
        setContentView(R.layout.activity_main);
        /*messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageBox.getText().toString();
                if (msg == null) {
                    return;
                }
                if (MessageHandler.sendMessage(msg, "1234", myID) != null) {
                    messageBox.setText("");
                }
            }
        });*/
        //myID = getIDFromFile();
        if(myID == null) {
            Intent activityIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(activityIntent, requestCodes.LOGIN.ordinal());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCodes.values()[requestCode]) {
            case LOGIN:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    myID = res.getString("ID");
                    saveIDToFile(myID);
                    Log.d("JOSE", "GOT " + myID);
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
            new PollerThread(myID).start();
        }
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
        }

        return super.onOptionsItemSelected(item);
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
                if (userToAdd == null) {
                    return;
                }
                if(!MessageHandler.addUserAsFriend(userToAdd, myID))
                    Toast.makeText(MainActivity.this, "User does not exist",Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        builder.show();
    }
}