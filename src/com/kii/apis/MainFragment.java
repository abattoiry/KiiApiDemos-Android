package com.kii.apis;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kii.apis.abtest.ABTestsActivity;
import com.kii.apis.analytics.AnalyticsFragment;
import com.kii.apis.file.FileStorageFragment;
import com.kii.apis.geolocation.GeoLocationFragment;
import com.kii.apis.group.GroupManagementActivity;
import com.kii.apis.object.NotesList;
import com.kii.apis.push.PushActivity;
import com.kii.apis.extension.ServerExtensionActivity;
import com.kii.apis.user.UserManagementFragment;
import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiEvent;

import java.io.IOException;

/**
 * Fragment for main page
 */
public class MainFragment extends ListFragment {
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private int[] itemIds;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity == null) { return; }

        String[] items = getResources().getStringArray(R.array.kii_components);
        TypedArray idArray = getResources().obtainTypedArray(R.array.kii_components_ids);
        this.itemIds = new int[idArray.length()];
        for (int i = 0; i < this.itemIds.length; i++) {
            this.itemIds[i] = idArray.getResourceId(i, 0);
        }
        idArray.recycle();

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_1, items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Activity activity = getActivity();
        if (activity == null) { return; }

        int itemId = itemIds[position];
        switch (itemId) {
        case R.id.cate_user_management:
            ViewUtils.toNextFragment(getFragmentManager(), UserManagementFragment.newInstance(), true);
            break;
        case R.id.cate_group_management: {
            Intent intent = new Intent(activity, GroupManagementActivity.class);
            startActivity(intent);
            break;
        }
        case R.id.cate_object_storage: {
            KiiEvent event = KiiAnalytics.event("ClickOnNotepad");
            try {
                event.push();
            } catch (IOException e) {
            }
            Intent intent = new Intent(activity, NotesList.class);
            startActivity(intent);
            break;
        }
        case R.id.cate_file_storage:
            ViewUtils.toNextFragment(getFragmentManager(), FileStorageFragment.newInstance(), true);
            break;
        case R.id.cate_geolocation:
            ViewUtils.toNextFragment(getFragmentManager(), GeoLocationFragment.newInstance(), true);
            break;
        case R.id.cate_analytics:
            ViewUtils.toNextFragment(getFragmentManager(), AnalyticsFragment.newInstance(), true);
            break;
        case R.id.cate_abtests: {
            KiiEvent event = KiiAnalytics.event("ClickOnABTest");
            try {
                event.push();
            } catch (IOException e) {
            }
            Intent intent = new Intent(activity, ABTestsActivity.class);
            startActivity(intent);
            break;
        }
        case R.id.cate_push: {
            Intent intent = new Intent(activity, PushActivity.class);
            startActivity(intent);
            break;
        }
        case R.id.cate_server_extension: {
            Intent intent = new Intent(activity, ServerExtensionActivity.class);
            startActivity(intent);
            break;
        }
        }
    }
}
