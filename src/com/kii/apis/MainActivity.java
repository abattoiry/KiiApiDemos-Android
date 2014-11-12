
package com.kii.apis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.kii.apis.push.JPushPreference;
import com.kii.apis.user.socialnetwork.SocialNetworkIntegrationFragment;
import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiPushInstallation;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiPushCallBack;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.social.KiiFacebookConnect;
import com.kii.cloud.storage.social.twitter.KiiTwitterConnect;

import cn.jpush.android.api.JPushInterface;

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

        // Initialize JPush interface
        JPushInterface.init(this);

        if (savedInstanceState == null) {
            ViewUtils.toNextFragment(getSupportFragmentManager(), MainFragment.newInstance(), false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KiiFacebookConnect.REQUEST_CODE ||
                requestCode == KiiTwitterConnect.REQUEST_CODE) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (!(fragment instanceof SocialNetworkIntegrationFragment)) {
                return;
            }
            fragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }
}
