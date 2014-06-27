
package com.kii.apis;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SimpleExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowCodeActivity extends ExpandableListActivity {
    public static final String EXTRA_GROUPS = "groups";
    public static final String EXTRA_CHILD = "child";
    public static final String EXTRA_TITLE = "title";
    public static final String KEY_CONTENT = "content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        setTitle(data.getStringExtra(EXTRA_TITLE));
        String[] groups = data.getStringArrayExtra(EXTRA_GROUPS);
        String[] child = data.getStringArrayExtra(EXTRA_CHILD);
        ArrayList<HashMap<String, String>> group_list = new ArrayList<HashMap<String, String>>();
        for (String s : groups) {
            HashMap<String, String> h = new HashMap<String, String>();
            h.put(KEY_CONTENT, s);
            group_list.add(h);
        }
        ArrayList<ArrayList<HashMap<String, String>>> child_list = new ArrayList<ArrayList<HashMap<String, String>>>();
        for (String s : child) {
            HashMap<String, String> h = new HashMap<String, String>();
            h.put(KEY_CONTENT, s);
            ArrayList<HashMap<String, String>> one_child = new ArrayList<HashMap<String, String>>();
            one_child.add(h);
            child_list.add(one_child);
        }
        SimpleExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(this, group_list,
                R.layout.code_group_item,
                new String[] {
                        KEY_CONTENT
                }, new int[] {
                        android.R.id.text1
                },
                child_list, R.layout.code_item, new String[] {
                        KEY_CONTENT
                }, new int[] {
                        android.R.id.text1
                });
        setListAdapter(mAdapter);
    }
}
