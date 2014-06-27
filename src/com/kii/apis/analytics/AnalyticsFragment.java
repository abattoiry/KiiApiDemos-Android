package com.kii.apis.analytics;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kii.apis.R;

/**
 * Fragment for Analytics Page
 */
public class AnalyticsFragment extends ListFragment {
    public static AnalyticsFragment newInstance() {
        AnalyticsFragment fragment = new AnalyticsFragment();

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

        String[] items = getResources().getStringArray(R.array.cate_analytics);
        TypedArray idArray = getResources().obtainTypedArray(R.array.cate_analytics_ids);

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
        Intent intent;

        switch (itemId) {
        case R.id.flex_analytics:
            intent = new Intent(activity, FlexAnalyticsActivity.class);
            break;
        case R.id.event_analytics:
            intent = new Intent(activity, EventAnalyticsActivity.class);
            break;
        default:
            return;
        }
        startActivity(intent);
    }
}
