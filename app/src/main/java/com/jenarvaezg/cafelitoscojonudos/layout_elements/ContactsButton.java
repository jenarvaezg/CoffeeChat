package com.jenarvaezg.cafelitoscojonudos.layout_elements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.jenarvaezg.cafelitoscojonudos.ChatActivity;
import com.jenarvaezg.cafelitoscojonudos.MainActivity;
import com.jenarvaezg.cafelitoscojonudos.MessageHandler;
import com.jenarvaezg.cafelitoscojonudos.R;
import com.jenarvaezg.cafelitoscojonudos.messages.Message;

import java.io.File;

/**
 * Created by joseen on 13/04/16.
 */
public class ContactsButton extends Button{

    private static final TableRow.LayoutParams lp = new TableRow.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

    private String contact;
    private Integer n;
    private Boolean isGroup = false;


    public ContactsButton(MainActivity context, String contact, String myID) {
        super(context);
        this.contact = contact;
        this.setText(contact);
        if(contact.startsWith("ÇGROUP:")){
            this.isGroup = true;
            this.setText(contact.substring("ÇGROUP:".length()));
        }
        this.setLayoutParams(lp);
        this.setTextColor(Color.WHITE);
        contactButtonListener listener = new contactButtonListener(context, myID);
        this.setOnLongClickListener(listener);
        this.setLongClickable(true);
        this.setOnClickListener(listener);
        this.setBackgroundColor(Color.GRAY);
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

    private class contactButtonListener implements View.OnClickListener, View.OnLongClickListener {

        MainActivity ctx;
        String myID;

        contactButtonListener(MainActivity ctx, String myID){
            this.ctx = ctx;
            this.myID = myID;
        }

        @Override
        public void onClick(View view) {
            ContactsButton.this.setTextColor(Color.WHITE);
            ContactsButton.this.n = 0;
            ContactsButton.this.setText(ContactsButton.this.getVisibleName());

            Intent intent = new Intent(ctx, ChatActivity.class);
            intent.putExtra("otherUser", ContactsButton.this.getContact());
            intent.putExtra("myID", myID);
            ctx.startActivity(intent);
        }


        @Override
        public boolean onLongClick(final View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContactsButton.this.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            builder.setMessage("DO YOU WANT TO REMOVE "  + ContactsButton.this.getVisibleName() + "?");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    TableRow parentTR = (TableRow) view.getParent();
                    parentTR.removeView(view);
                    TableLayout gramps = (TableLayout) parentTR.getParent();
                    gramps.removeView(parentTR);
                    Message.removeHistory(ContactsButton.this.getContext(), ContactsButton.this.getContact());
                    contactButtonListener.this.ctx.removeContact(ContactsButton.this.getContact());

                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();

            return true;
        }
    }
}
