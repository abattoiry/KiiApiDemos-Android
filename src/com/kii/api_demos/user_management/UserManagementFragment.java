package com.kii.api_demos.user_management;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kii.api_demos.R;

/**
 * Fragment for User Management Page
 */
public class UserManagementFragment extends ListFragment {
    public static UserManagementFragment newInstance() {
        UserManagementFragment fragment = new UserManagementFragment();

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

        String[] items = getResources().getStringArray(R.array.cate_user_management);
        TypedArray idArray = getResources().obtainTypedArray(R.array.cate_user_management_ids);

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
        case R.id.simple_sign_up_in:
            intent = new Intent(activity, SimpleSignUpInActivity.class);
            break;
        case R.id.sign_up:
            intent = new Intent(activity, SignUpActivity.class);
            break;
        case R.id.sign_in:
            intent = new Intent(activity, SignInActivity.class);
            break;
        case R.id.user_attributes:
            intent = new Intent(activity, UserAttributesActivity.class);
            break;
        case R.id.delete_user:
            intent = new Intent(activity, LogoutDeleteActivity.class);
            break;
        case R.id.social_network_integration:
            intent = new Intent(activity, SocialNetworkIntegrationActivity.class);
            break;
        default:
            return;
        }
        startActivity(intent);
    }
}
