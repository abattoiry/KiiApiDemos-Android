
package com.kii.api_demos.analytics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiEvent;

import java.io.IOException;

public class EventAnalyticsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        //Below code shows how to send one event
        KiiEvent event = KiiAnalytics.event("LaunchEventAnalytics");
        event.set("aa", 1);
        try {
            event.push();
        } catch (IOException e) {
        }
        
        
    }

}
