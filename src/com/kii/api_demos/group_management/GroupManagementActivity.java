
package com.kii.api_demos.group_management;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.api_demos.AsyncTaskWithProgress;
import com.kii.api_demos.R;
import com.kii.api_demos.Utils;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.GroupOperationException;

import java.util.List;

public class GroupManagementActivity extends FragmentActivity implements OnClickListener {

    KiiGroup currentGroup = null;
    TextView groupIdTextView = null;
    EditText usernameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_management);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        findViewById(R.id.create_group).setOnClickListener(this);
        findViewById(R.id.select_owned_group).setOnClickListener(this);
        findViewById(R.id.add_to_group).setOnClickListener(this);
        findViewById(R.id.list_group_members).setOnClickListener(this);
        groupIdTextView = (TextView) findViewById(R.id.group_id_field);
        usernameField = (EditText) findViewById(R.id.username_field);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group:
                new AsyncTaskWithProgress(this) {
                    @Override
                    protected Void doInBackground(Void... params) {
                        currentGroup = Kii.group("g" + System.currentTimeMillis(), null);
                        try {
                            currentGroup.save();
                        } catch (GroupOperationException e) {
                            e.printStackTrace();
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        groupIdTextView.setText(currentGroup.getGroupName());
                    }
                }.execute();
                break;
            case R.id.select_owned_group:
                new AsyncTaskWithProgress(this) {
                    List<KiiGroup> groups = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            groups = KiiUser.getCurrentUser().ownerOfGroups();
                        } catch (Exception e) {
                            e.printStackTrace();
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        if (groups == null || groups.size() == 0) {
                            Toast.makeText(mActivity, "You don't own any groups, create one.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String[] items = new String[groups.size()];
                            for (int i = 0; i < groups.size(); i++) {
                                items[i] = groups.get(i).getGroupName();
                            }
                            AlertDialog.Builder ab = new AlertDialog.Builder(mActivity);
                            ab.setTitle(R.string.choose_group);
                            ab.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    currentGroup = groups.get(which);
                                    groupIdTextView.setText(currentGroup.getGroupName());
                                }
                            });
                            ab.setNegativeButton(android.R.string.cancel, null);
                            ab.show();
                        }
                    }
                }.execute();
                break;
            case R.id.add_to_group: {
                if (currentGroup == null) {
                    Toast.makeText(this, "Create or select a group first", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                new AsyncTaskWithProgress(this) {
                    KiiUser user_to_add = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        String username = usernameField.getText().toString();
                        try {
                            user_to_add = KiiUser.findUserByUserName(username);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (user_to_add == null) {
                            return null;
                        }
                        currentGroup.addUser(user_to_add);
                        try {
                            currentGroup.save();
                        } catch (GroupOperationException e) {
                            e.printStackTrace();
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        if (user_to_add == null) {
                            Toast.makeText(mActivity, "Cannot find the user", Toast.LENGTH_LONG)
                                    .show();
                        } else if (!has_exception) {
                            Toast.makeText(mActivity, "Add user successfully", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }.execute();
            }
                break;
            case R.id.list_group_members: {
                if (currentGroup == null) {
                    Toast.makeText(this, "Create or select a group first", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                new AsyncTaskWithProgress(this) {
                    List<KiiUser> members = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            members = currentGroup.listMembers();
                            for (KiiUser u:members) {
                                u.refresh();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        if (members == null || members.size() == 0) {
                            Toast.makeText(mActivity, "The group doesn't have any members yet",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String[] items = new String[members.size()];
                            for (int i = 0; i < members.size(); i++) {
                                items[i] = members.get(i).getUsername();
                                if (items[i] == null) {
                                    items[i] = "Unknown name";
                                }
                            }
                            AlertDialog.Builder ab = new AlertDialog.Builder(mActivity);
                            ab.setTitle(R.string.list_group_members);
                            ab.setItems(items, null);
                            ab.setNegativeButton(android.R.string.cancel, null);
                            ab.show();
                        }
                    }
                }.execute();
            }
                break;
        }
    }
}
