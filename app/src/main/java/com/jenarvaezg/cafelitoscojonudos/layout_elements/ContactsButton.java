package com.jenarvaezg.cafelitoscojonudos.layout_elements;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.jenarvaezg.cafelitoscojonudos.ChatActivity;

/**
 * Created by joseen on 13/04/16.
 */
public class ContactsButton extends Button{

    private static final TableRow.LayoutParams lp = new TableRow.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

    private String contact;
    private Integer n;
    private Boolean isGroup = false;


    public ContactsButton(Context context, String contact, String myID) {
        super(context);
        this.contact = contact;
        this.setText(contact);
        if(contact.startsWith("ÇGROUP:")){
            this.isGroup = true;
            this.setText(contact.substring("ÇGROUP:".length()));
        }
        this.setLayoutParams(lp);
        this.setTextColor(Color.WHITE);
        this.setOnClickListener(new contactButtonListener(context, myID));
    }

    public String getVisibleName(){
        if(contact.startsWith("ÇGROUP:")){
            return this.contact.substring("ÇGROUP:".length());
        }
        return this.contact;
    }

    public String getContact(){
        return this.contact;
    }

    public String toString(){
        String s = this.getVisibleName();
        if(n > 0){
            s += "(" + n + ")";
        }
        return s;
    }


    public void addNewMessagesNumber(Integer n){
        this.n = n;
        this.setText(this.toString());
        this.setTextColor(Color.RED);
    }

    private class contactButtonListener implements View.OnClickListener {

        Context ctx;
        String myID;

        contactButtonListener(Context ctx, String myID){
            this.ctx = ctx;
            this.myID = myID;
        }

        @Override
        public void onClick(View view) {
            ContactsButton.this.setTextColor(Color.WHITE);
            ContactsButton.this.n = 0;
            ContactsButton.this.setText(ContactsButton.this.getVisibleName());

            Toast.makeText(ctx, ContactsButton.this.getVisibleName(),
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ctx, ChatActivity.class);
            intent.putExtra("otherUser", ContactsButton.this.getContact());
            intent.putExtra("myID", myID);
            ctx.startActivity(intent);
        }
    }
}
