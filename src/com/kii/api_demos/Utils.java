
package com.kii.api_demos;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.kii.cloud.storage.KiiUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This utility methods is used for reducing duplicated functions from different
 * activities Usually this APIs is not important for Kii Cloud
 */

public class Utils {
    public static final String LOG_TAG = "Kii Demo";
    public static final String PREFS_NAME = "prefs";
    public static final String KEY_TOEKN = "token";
    public static final int TAKE_OR_CHOOSE_PHOTO = 3024;
    public static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory()
            + "/DCIM/Camera");
    public static File mCurrentPhotoFile;

    public static boolean isCurrentLogined() {
        return KiiUser.getCurrentUser() != null;
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME,
                        Context.MODE_PRIVATE);
        Editor e = prefs.edit();
        e.putString(KEY_TOEKN, token);
        e.commit();
    }

    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.US);
        return dateFormat.format(date) + ".jpg";
    }

    public static void takeOrChoosePhoto(Activity context, int requestCode) {
        PHOTO_DIR.mkdirs();
        mCurrentPhotoFile = new File(PHOTO_DIR, Utils.getPhotoFileName());
        Uri outputFileUri = Uri.fromFile(mCurrentPhotoFile);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose photo");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[] {}));

        context.startActivityForResult(chooserIntent, requestCode);
    }

    public static File getPhotoFromResult(Context context, Intent data) {
        final boolean isCamera;
        if (data == null) {
            isCamera = true;
        } else {
            isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
        }
        File f = null;
        if (isCamera) {
            f = Utils.mCurrentPhotoFile;
        } else {
            if (data != null) {
                String uriStr = data.getData().toString();
                String path = null;
                if (uriStr.startsWith("file")) {
                    path = data.getData().getPath();
                } else if (uriStr.startsWith("content")) {
                    path = getRealPathFromURI(context, data.getData());
                }
                f = new File(path);
                if (f == null || !f.exists()) {
                    Log.e(LOG_TAG, "Cannot load photo:" + uriStr);
                }
            }
        }
        return f;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {
                    MediaStore.Images.Media.DATA
            };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
