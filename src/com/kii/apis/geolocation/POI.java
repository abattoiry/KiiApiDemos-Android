
package com.kii.apis.geolocation;

import android.net.Uri;

import com.kii.cloud.storage.GeoPoint;

public class POI {
    public String title;
    public GeoPoint location;
    public Uri uri;

    @Override
    public String toString() {
        return title;
    }
}
