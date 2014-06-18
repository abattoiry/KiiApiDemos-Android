
package com.kii.apis.object;

import android.net.Uri;

public class Note {
    public static final String BUCKET = "notes";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    String title;
    String content;
    public Uri uri;

    @Override
    public String toString() {
        return title;
    }

}
