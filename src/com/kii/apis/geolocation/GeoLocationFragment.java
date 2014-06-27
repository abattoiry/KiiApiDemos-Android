package com.kii.apis.geolocation;

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
 * Fragment for Geo Location Page
 */
public class GeoLocationFragment extends ListFragment {
    public static GeoLocationFragment newInstance() {
        GeoLocationFragment fragment = new GeoLocationFragment();

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

        String[] items = getResources().getStringArray(R.array.cate_geolocation);
        TypedArray idArray = getResources().obtainTypedArray(R.array.cate_geolocation_ids);

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
        super.onListItemClick(l, v, position, id);

        Activity activity = getActivity();
        if (activity == null) { return; }

        int itemId = itemIds[position];
        Intent intent;

        switch (itemId) {
        case R.id.add_geopoint:
            intent = new Intent(activity, AddPOIActivity.class);
            break;
        case R.id.query_pois_by_geobox:
            intent = new Intent(activity, QueryByGeoBoxActivity.class);
            break;
        case R.id.query_pois_by_geodistance:
            intent = new Intent(activity, QueryByGeoDistanceActivity.class);
            break;
        default:
            return;
        }
        startActivity(intent);
    }
}
