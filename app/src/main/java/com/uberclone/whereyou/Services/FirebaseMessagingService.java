package com.uberclone.whereyou.Services;

/**
 * Created by Umair Ali on 1/29/2018.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.uberclone.whereyou.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_body=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");
        String from_group_id=remoteMessage.getData().get("from_group_id");

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this).setSmallIcon(R.drawable.whereyou)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setAutoCancel(true);

        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("from_group_id",from_group_id);

        PendingIntent resultPendingIntent=PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId=(int) System.currentTimeMillis();
        NotificationManager mNotify=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotify.notify(mNotificationId,mBuilder.build());

    }
}
