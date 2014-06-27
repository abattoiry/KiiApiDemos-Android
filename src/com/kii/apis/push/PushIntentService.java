
package com.kii.apis.push;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.apis.MainActivity;
import com.kii.apis.R;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushMessageBundleHelper.MessageType;
import com.kii.cloud.storage.ReceivedMessage;

public class PushIntentService extends IntentService {
    static final String LOG_TAG = PushIntentService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public PushIntentService() {
        super("PushIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types, just
             * ignore any message types you're not interested in, or that you
             * don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d(LOG_TAG, "Push message send error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(LOG_TAG, "Push message is deleted");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                processIntent(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        PushBroadcastReceiver.completeWakefulIntent(intent);
    }

    void processIntent(Bundle extras) {
        ReceivedMessage message = PushMessageBundleHelper.parse(extras);

        // Determine the push notification type and start parsing.
        MessageType type = message.pushMessageType();
        String msg = "Got kii push message. Type:" + type.toString();
        sendNotification(msg);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Kii Push Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
