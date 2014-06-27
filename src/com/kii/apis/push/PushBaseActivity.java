
package com.kii.apis.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.Constants;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.exception.app.ConflictException;

import java.io.IOException;

public class PushBaseActivity extends FragmentActivity {
    private final static String LOG_TAG = PushBaseActivity.class.getSimpleName();
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    String regid;

    protected void registerGCM() {
        Context context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (TextUtils.isEmpty(regid)) {
                registerInBackground();
            }
        } else {
            Log.i(LOG_TAG, "No valid Google Play Services APK found.");
            finish();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    protected boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     * 
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    protected String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (TextUtils.isEmpty(registrationId)) {
            Log.i(LOG_TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    protected static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    protected SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(PushBaseActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    protected void registerInBackground() {
        new AsyncTaskWithProgress(this, "Registering push service") {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mActivity);
                    }
                    regid = gcm.register(Constants.GCM_SENDER_ID);
                    Log.i(LOG_TAG, "Device registered, registration ID=" + regid);

                    // You should send the registration ID to your server over
                    // HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your
                    // app.
                    // The request to your server should be authenticated if
                    // your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the
                    // device
                    // will send upstream messages to a server that echo back
                    // the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mActivity, regid);
                } catch (IOException ex) {
                    Log.e(LOG_TAG, Log.getStackTraceString(ex));
                    has_exception = true;
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return null;
            }
        }.execute();
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     * 
     * @param context application's context.
     * @param regId registration ID
     */
    protected void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */
    protected void sendRegistrationIdToBackend() {
        try {
            KiiUser.pushInstallation().install(
                    regid);
            Toast.makeText(this, "Register push service successfully", Toast.LENGTH_SHORT).show();
        } catch (ConflictException e) {
            // Already installed.
        } catch (AppException e) {
            // Please check configuration on developer portal.
        } catch (IOException e) {
            // Network error.
        }
    }
}
