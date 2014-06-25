package com.kii.api_demos.user_management;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kii.api_demos.R;
import com.kii.api_demos.ViewUtils;
import com.kii.api_demos.user_management.logoutdelete.LogoutDeleteFragment;
import com.kii.api_demos.user_management.signin.SignInFragment;
import com.kii.api_demos.user_management.signinup.SignInUpFragment;
import com.kii.api_demos.user_management.signup.SignupFragment;
import com.kii.api_demos.user_management.socialnetwork.SocialNetworkIntegrationFragment;
import com.kii.api_demos.user_management.userattributes.UserAttributesFragment;

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

        activity.setTitle(R.string.user_management);

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
        switch (itemId) {
        case R.id.simple_sign_up_in:
            ViewUtils.toNextFragment(getFragmentManager(), SignInUpFragment.newInstance(), true);
            return;
        case R.id.sign_up:
            ViewUtils.toNextFragment(getFragmentManager(), SignupFragment.newInstance(), true);
            return;
        case R.id.sign_in:
            ViewUtils.toNextFragment(getFragmentManager(), SignInFragment.newInstance(), true);
            return;
        case R.id.user_attributes:
            ViewUtils.toNextFragment(getFragmentManager(), UserAttributesFragment.newInstance(), true);
            return;
        case R.id.delete_user:
            ViewUtils.toNextFragment(getFragmentManager(), LogoutDeleteFragment.newInstance(), true);
            return;
        case R.id.social_network_integration:
            ViewUtils.toNextFragment(getFragmentManager(), SocialNetworkIntegrationFragment.newInstance(), true);
            return;
        default:
            return;
        }
    }
}
