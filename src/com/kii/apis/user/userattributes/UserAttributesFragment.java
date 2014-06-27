package com.kii.apis.user.userattributes;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.ProgressDialogFragment;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiUser;

/**
 * Fragment for Uset Attribetes Page
 */
public class UserAttributesFragment extends Fragment {
    public static UserAttributesFragment newInstance() {
        UserAttributesFragment fragment = new UserAttributesFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";

    Spinner genderSpinner;
    TextView ageView;
    TextView passwordView;
    TextView oldpwdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_attributes, container, false);

        if (!Utils.isCurrentLogined()) {
            return root;
        }

        Context context = root.getContext();

        genderSpinner = (Spinner) root.findViewById(R.id.gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        ageView = (TextView) root.findViewById(R.id.age);
        passwordView = (TextView) root.findViewById(R.id.password);
        oldpwdView = (TextView) root.findViewById(R.id.old_password);
        root.findViewById(R.id.save_button).setOnClickListener(mClickListener);
        root.findViewById(R.id.change_password).setOnClickListener(mClickListener);

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

        activity.setTitle(R.string.user_attributes);
        new RefreshTask().execute();
    }

    void updateInfo() {
        KiiUser mUser = KiiUser.getCurrentUser();
        int gender = mUser.getInt(KEY_GENDER, 0);
        genderSpinner.setSelection(gender);
        int age = mUser.getInt(KEY_AGE, 0);
        ageView.setText(Integer.toString(age));
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity == null) { return; }

            switch (v.getId()) {
            case R.id.change_password:
                String password = passwordView.getText().toString();
                if (!KiiUser.isValidPassword(password)) {
                    Toast.makeText(activity, R.string.password_invalid, Toast.LENGTH_LONG).show();
                    return;
                }
                new ChangePasswordTask(oldpwdView.getText().toString(), password).execute();
                break;
            case R.id.save_button:
                new SaveInfoTask().execute();
                break;
            }
        }
    };

    class RefreshTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().refresh();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            updateInfo();
        }
    }

    class ChangePasswordTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;
        String newPwd = null, oldPwd = null;
        boolean succ = true;

        public ChangePasswordTask(String oldPassword, String password) {
            newPwd = password;
            oldPwd = oldPassword;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser.getCurrentUser().changePassword(newPwd, oldPwd);
            } catch (Exception e) {
                succ = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = getActivity();
            if (activity == null) { return; }

            Toast.makeText(activity,
                    succ ? R.string.change_pwd_succ : R.string.change_pwd_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    class SaveInfoTask extends AsyncTask<Void, Void, Void> {
        ProgressDialogFragment dialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiUser mUser = KiiUser.getCurrentUser();
                long g_id = genderSpinner.getSelectedItemId();
                if (g_id == AdapterView.INVALID_ROW_ID) {
                    g_id = 0;
                }
                mUser.set(KEY_GENDER, (int) g_id);
                String ageStr = ageView.getText().toString();
                int age = 0;
                try {
                    age = Integer.parseInt(ageStr);
                } catch (Exception e) {
                }
                mUser.set(KEY_AGE, age);
                mUser.update();
                mUser.refresh();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing_status));
            dialog.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            updateInfo();
        }
    }
}
