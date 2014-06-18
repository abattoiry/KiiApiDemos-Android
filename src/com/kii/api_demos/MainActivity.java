
package com.kii.api_demos;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.storage.Kii;

/**
 * This activity shows the categories.
 */

public class MainActivity extends FragmentActivity {
    static {
        Kii.initialize(Constants.KII_APP_ID, Constants.KII_APP_KEY, Constants.KII_SITE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KiiAnalytics.initialize(this, Constants.KII_APP_ID, Constants.KII_APP_KEY,
                Constants.KII_ANALYTICS_SITE);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            ViewUtils.toNextFragment(getSupportFragmentManager(), MainFragment.newInstance(), false);
        }
    }
}
