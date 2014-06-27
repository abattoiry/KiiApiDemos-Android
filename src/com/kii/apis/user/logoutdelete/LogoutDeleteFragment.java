package com.kii.apis.user.logoutdelete;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

/**
 * Fragment for Logout / Delete Page
 */
public class LogoutDeleteFragment extends Fragment {
    public static LogoutDeleteFragment newInstance() {
        LogoutDeleteFragment fragment = new LogoutDeleteFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.logout_delete, container, false);
        if (!Utils.isCurrentLogined()) {
            return root;
        }

        TextView tv = (TextView) root.findViewById(R.id.user);
        tv.setText(getString(R.string.current_user, KiiUser.getCurrentUser().getUsername()));
        View v = root.findViewById(R.id.logout);
        v.setOnClickListener(mClickListener);
        v = root.findViewById(R.id.delete);
        v.setOnClickListener(mClickListener);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity == null) { return; }

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
            return;
        }

        activity.setTitle(R.string.logout_delete_title);
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity == null) { return; }

            switch (v.getId()) {
            case R.id.logout:
                KiiUser.logOut();
                Toast.makeText(activity,
                        "Current user is loged out, please login again",
                        Toast.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
                break;
            case R.id.delete:
                new DeleteUserTask().execute();
                break;
            }
        }
    };

    class DeleteUserTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        boolean succ = true;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().delete();
            } catch (Exception e) {
                e.printStackTrace();
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance("Deleting from server...");
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = getActivity();
            if (activity == null) { return; }

            if (succ) {
                Toast.makeText(activity,
                        "Current user is deleted, please login again",
                        Toast.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            } else {
                Toast.makeText(activity,
                        "Delete current user failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
