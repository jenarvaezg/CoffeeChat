package com.jenarvaezg.cafelitoscojonudos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {


    protected enum requestCodes{LOGIN}

    private static String myID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent activityIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(activityIntent, requestCodes.LOGIN.ordinal());

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCodes.values()[requestCode]) {
            case LOGIN:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    myID = res.getString("ID");
                }else{
                    myID = "NOT_VALID";
                }
                break;
        }

        if(myID.equals("NOT_VALID")){
            //TODO this sucks
            finish();
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
        }

        return super.onOptionsItemSelected(item);
    }
}
