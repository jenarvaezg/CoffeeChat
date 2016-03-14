package com.jenarvaezg.cafelitoscojonudos;

/**
 * Created by joseen on 10/03/16.
 */
public class PollerThread extends Thread{


    private String id;

    public PollerThread(String id){
        this.id = id;
    }

    @Override
    public void run() {
        for(;;){
            try {
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                break;
            }
            //MessageHandler.poll(id);
        }
    }
}
