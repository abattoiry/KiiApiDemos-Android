package com.kii.apis;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.kii.apis.ToastUtil;
import com.kii.apis.push.PushActivity;
import com.kii.cloud.storage.DirectPushMessage;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.PushToUserMessage;
import com.kii.cloud.storage.ReceivedMessage;

import cn.jpush.android.api.JPushInterface;

public class KiiPushBroadcastReceiver extends BroadcastReceiver {

    private NotificationManager mNotificationManager;
    private Notification notification;
    private Context context;

    private static final int ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String jpushMessageType = intent.getAction();
        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(jpushMessageType)) {
            Bundle extras = intent.getExtras();
            ReceivedMessage message = PushMessageBundleHelper.parse(extras);
            KiiUser sender = message.getSender();
            PushMessageBundleHelper.MessageType type = message.pushMessageType();
            switch (type) {
                case PUSH_TO_APP:
                    PushToAppMessage appMsg = (PushToAppMessage)message;
                    ToastUtil.show(context, "pushed");
                    break;
                case PUSH_TO_USER:
                    PushToUserMessage userMsg = (PushToUserMessage)message;
                    ToastUtil.show(context, "pushed");
                    break;
                case DIRECT_PUSH:
                    DirectPushMessage directMsg = (DirectPushMessage)message;
                    showNotification(directMsg.getMessage().getString("data"));
                    ToastUtil.show(context, "pushed");
                    break;
            }
        }
    }


    private void showNotification(String data) {
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, PushActivity.class);
        // 获取PendingIntent,点击时发送该Intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("牛逼")
                .setContentText(data)
                .setTicker("测试通知来啦")
                .setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        notification = mBuilder.build();
        mNotificationManager.notify(ID, notification);
    }
}
