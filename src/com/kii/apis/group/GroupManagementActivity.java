
package com.kii.apis.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.AsyncTaskWithProgress;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.GroupOperationException;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.util.List;

public class GroupManagementActivity extends FragmentActivity implements OnClickListener {

    static final String BUCKET_NAME = "group_bucket";
    static final String NOTE_FIELD_NAME = "note";
    KiiGroup currentGroup = null;
    KiiGroup currentGroup2 = null;
    TextView groupIdTextView = null;
    TextView groupId2TextView = null;
    EditText usernameField;
    EditText contentField;
    KiiObject group_content = null;

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
        findViewById(R.id.set_group_content).setOnClickListener(this);
        findViewById(R.id.select_belonged_group).setOnClickListener(this);
        groupIdTextView = (TextView) findViewById(R.id.group_id_field);
        groupId2TextView = (TextView) findViewById(R.id.group_id_field2);
        usernameField = (EditText) findViewById(R.id.username_field);
        contentField = (EditText) findViewById(R.id.content_field);
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
                            currentGroup = null;
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        groupIdTextView.setText(currentGroup.getGroupName());
//                        fetchGroupNote();
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
//                                    fetchGroupNote();
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
                    Toast.makeText(this, R.string.tip_get_group_first, Toast.LENGTH_LONG)
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
                    Toast.makeText(this, R.string.tip_get_group_first, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                new AsyncTaskWithProgress(this) {
                    List<KiiUser> members = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            members = currentGroup.listMembers();
                            for (KiiUser u : members) {
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
            case R.id.select_belonged_group: {
                new AsyncTaskWithProgress(this) {
                    List<KiiGroup> groups = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            groups = KiiUser.getCurrentUser().memberOfGroups();
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
                            Toast.makeText(mActivity, "You don't belong to any groups, create one.",
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
                                    currentGroup2 = groups.get(which);
                                    groupId2TextView.setText(currentGroup2.getGroupName());
                                    fetchGroupNote();
                                }
                            });
                            ab.setNegativeButton(android.R.string.cancel, null);
                            ab.show();
                        }
                    }
                }.execute();
            }
                break;
            case R.id.set_group_content: {
                if (currentGroup2 == null) {
                    Toast.makeText(this, R.string.tip_get_mem_group_first, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                final String text = contentField.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(this, "Input some text in above field first", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                if (group_content == null) {
                    Toast.makeText(this, "Try to select group again", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                new AsyncTaskWithProgress(this) {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            group_content.set(NOTE_FIELD_NAME, text);
                            group_content.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                            group_content = null;
                            has_exception = true;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        if (!has_exception) {
                            Toast.makeText(mActivity, "Change group note successfully",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
                break;
        }
    }

    protected void fetchGroupNote() {
        new AsyncTaskWithProgress(this) {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    KiiBucket bucket = currentGroup2.bucket(BUCKET_NAME);
                    KiiQuery all_query = new KiiQuery();
                    KiiQueryResult<KiiObject> result = bucket.query(all_query);

                    List<KiiObject> objLists = result.getResult();
                    if (objLists.size() > 0) {
                        group_content = objLists.get(0);
                        group_content.refresh();
                    } else {
                        group_content = bucket.object();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    group_content = null;
                    has_exception = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (group_content != null) {
                    contentField.setHint("Not set yet");
                    contentField.setText(group_content.getString(NOTE_FIELD_NAME, ""));
                }
            }
        }.execute();
    }

}
