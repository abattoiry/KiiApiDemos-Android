package com.kii.apis;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Utility class for View / Fragments
 */
public class ViewUtils {
    /**
     * Replaces the main area to specified fragment.
     * @param manager Fragment Manager
     * @param next Next Fragment
     * @param addToBackstack if true, addToBackStack() will be called.
     */
    public static void toNextFragment(FragmentManager manager, Fragment next, boolean addToBackstack) {
        if (manager == null) { return; }
        FragmentTransaction transaction = manager.beginTransaction();
        if (addToBackstack) {
            transaction.addToBackStack("");
        }
        transaction.replace(R.id.container, next);

        transaction.commit();
    }
}
