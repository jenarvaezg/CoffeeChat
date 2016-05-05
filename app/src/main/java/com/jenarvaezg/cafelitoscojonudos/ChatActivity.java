package com.jenarvaezg.cafelitoscojonudos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jenarvaezg.cafelitoscojonudos.messages.*;


import org.w3c.dom.Text;

import java.util.Arrays;

import static com.jenarvaezg.cafelitoscojonudos.messages.Message.getMessages;

public class ChatActivity extends Activity {

    String otherUserG;
    String myIDG;

    private AsyncTask mTask;

    private void scrollDown(){
        final ScrollView scrollview = (ScrollView) findViewById(R.id.scrollChat);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void addMsg(Message msg){
        final LinearLayout chatLayout = (LinearLayout) findViewById(R.id.chatLayout);
        String text = "";
        if (msg.isPrivateForYou()){
            text = msg.getPrivateChatString();
        }else{
            text = msg.getGroupChatString();
        }
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setBackgroundColor(Color.WHITE);
        tv.setTextSize(13f);
        tv.setMaxEms(7);
        tv.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        if(msg.isFromYou()){
            params.gravity = Gravity.END;
        }
        params.setMargins(2, 4, 2, 4);

        tv.setLayoutParams(params);
        chatLayout.addView(tv);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        otherUserG =  getIntent().getStringExtra("otherUser");
        myIDG = getIntent().getStringExtra("myID");

        final EditText msgBox = (EditText) findViewById(R.id.msgBox);

        Button sendButton = (Button) findViewById(R.id.sendButton);
        final String otherUser = otherUserG;
        final String myID = myIDG;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = msgBox.getText().toString();
                if (text.equals("")) {
                    return;
                }
                if (MessageHandler.sendMessage(text, otherUser, myID) != null) {
                    msgBox.setText("");
                    Message msg = new Message("YOU", otherUser, System.currentTimeMillis(), text, otherUser);
                    addMsg(msg);
                    msg.addMsg(getApplicationContext(), true);
                    final ScrollView scrollview = (ScrollView) findViewById(R.id.scrollChat);
                    scrollview.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "ERROR SENDING MSG",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        Message[] oldMsgs = getMessages(otherUser, this, true);
        if(oldMsgs != null) {
            for (Message msg : oldMsgs) {
                addMsg(msg);
            }
        }
        scrollDown();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mTask = new ChatUpdater().execute(otherUserG);
    }


    @Override
    protected void onStop(){
        super.onStop();
        mTask.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private class ChatUpdater extends AsyncTask<String, Message, String>{

        @Override
        protected String doInBackground(String... strings) {
            String name = strings[0];
            if(name == null){
                return null;
            }
            for(;;){
                try {
                    Thread.sleep(2000, 0);
                } catch (InterruptedException e) {
                    break;
                }
                Message[] msgs = Message.getMessages(name, ChatActivity.this, false);
                if(msgs != null){
                    publishProgress(msgs);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Message... msgs){
            for(Message msg: msgs){
                Log.d("JOSE", msg.toString());
                addMsg(msg);
                scrollDown();
            }
        }
    }
}
