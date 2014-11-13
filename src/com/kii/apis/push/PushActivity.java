
package com.kii.apis.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiPushMessage;
import com.kii.cloud.storage.KiiPushMessage.Data;
import com.kii.cloud.storage.KiiPushSubscription;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiPushCallBack;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.exception.app.ConflictException;

import java.io.IOException;


public class PushActivity extends PushBaseActivity implements OnClickListener {
    static final String TopicName = "Test";
    private NotificationManager mNotificationManager;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.push);
        findViewById(R.id.create_topic).setOnClickListener(this);
        findViewById(R.id.push_msg).setOnClickListener(this);
        //google
//        registerGCM();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_topic:
                pushToApp();
                break;
            case R.id.push_msg:
                sendPush();
                break;
        }
    }

    void createTopicAndSubscribe() {
        new AsyncTaskWithProgress(this) {
            @Override
            protected Void doInBackground(Void... params) {
                KiiTopic topic = null;
                try {
                    // Create a user-scope group
                    topic = KiiUser.topic(TopicName);

                    // Save the topic to Kii Cloud
                    topic.save();
                } catch (IOException ioe) {
                    // failed.
                    ioe.printStackTrace();
                    has_exception = true;
                } catch (ConflictException e) {
                    e.printStackTrace();
                } catch (AppException e) {
                    // failed.
                    e.printStackTrace();
                    has_exception = true;
                } 
                if (topic != null) {
                    try {

                        // Subscribe the current user to the topic.
                        KiiUser user = KiiUser.getCurrentUser();
                        KiiPushSubscription sub = user.pushSubscription();
                        sub.subscribe(topic);
                    } catch (IOException ioe) {
                        // failed.
                        ioe.printStackTrace();
                        has_exception = true;
                    } catch (ConflictException e) {
                        // Already subscribed.
                        e.printStackTrace();
                    } catch (AppException e) {
                        // failed.
                        e.printStackTrace();
                        has_exception = true;
                    }
                }

                return null;
            }
        }.execute();
    }

    void pushToApp(){
        new AsyncTaskWithProgress(this) {

            @Override
            protected Void doInBackground(Void... params) {
                final KiiUser user = KiiUser.getCurrentUser();
                final KiiBucket bucket = user.bucket("push");
                bucket.object().save(new KiiObjectCallBack() {
                    @Override
                    public void onSaveCompleted(int token, KiiObject object, Exception exception) {
                        if (exception != null) {
                            // Error handling
                            return;
                        }
                        user.pushSubscription().subscribeBucket(bucket, new KiiPushCallBack() {
                            @Override
                            public void onSubscribeBucketCompleted(int taskId, KiiBucket target, Exception exception) {
                                if (exception != null) {
                                    // Error handling
                                    return;
                                }
                            }
                        });
                    }
                });

                return null;
            }
        }.execute();
    }

    void sendPush() {
        new AsyncTaskWithProgress(this) {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Instantiate a user-scope topic.
                    KiiTopic topic = KiiUser.topic(TopicName);

                    // Build a push message.
                    Data data = new Data();
                    data.put("Key", "Value");
                    KiiPushMessage message = KiiPushMessage.buildWith(data).build();

                    // Send the push message.
                    topic.sendMessage(message);
                } catch (IOException ioe) {
                    // failed.
                    ioe.printStackTrace();
                    has_exception = true;
                } catch (AppException e) {
                    // failed.
                    e.printStackTrace();
                    has_exception = true;
                }

                return null;
            }
        }.execute();
    }

}
