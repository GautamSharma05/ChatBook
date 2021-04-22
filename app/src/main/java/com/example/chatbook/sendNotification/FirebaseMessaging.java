package com.example.chatbook.sendNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.chatbook.Activity.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID","None");

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser != null && sent.equals(fUser.getUid())){
            if(!savedCurrentUser.equals(user)){
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                    sendOAndaboveNotification(remoteMessage);
                }
                else{
                    sendNormalNotification(remoteMessage);

                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user =remoteMessage.getData().get("user");
        String icon =remoteMessage.getData().get("icon");
        String title =remoteMessage.getData().get("title");
        String body =remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("receiverUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title).setContentText(body)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j =0 ;
        if(i>0){
            j=1;
        }
        notificationManager.notify(j, builder.build());


    }

    private void sendOAndaboveNotification(RemoteMessage remoteMessage) {
        String user =remoteMessage.getData().get("user");
        String icon =remoteMessage.getData().get("icon");
        String title =remoteMessage.getData().get("title");
        String body =remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("receiverUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotification(title,body,pIntent,defSoundUri,icon);


        int j =0 ;
        if(i>0){
            j=1;
        }
        notification1.getManager().notify(j, builder.build());
    }
}