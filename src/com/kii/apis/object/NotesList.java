
package com.kii.apis.object;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.util.List;

public class NotesList extends ListActivity {
    public static KiiUser kUser = null;
    ArrayAdapter<Note> mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        kUser = KiiUser.getCurrentUser();
        mAdapter = new ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1);
        setListAdapter(mAdapter);
        new FetchDataTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notepad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startActivityForResult(new Intent(this, NoteEditor.class), 0);
                break;
            case R.id.action_refresh:
                new FetchDataTask().execute();
                break;
        }
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Note n = mAdapter.getItem(position);
        Intent intent = new Intent(this, NoteEditor.class);
        intent.putExtra(Note.KEY_TITLE, n.title);
        intent.putExtra(Note.KEY_CONTENT, n.content);
        intent.putExtra(NoteEditor.EXTRA_URI, n.uri.toString());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new FetchDataTask().execute();
    }

    class FetchDataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = null;
        List<KiiObject> objLists = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NotesList.this);
            dialog.setCancelable(false);
            dialog.setMessage("Fetching notes…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            mAdapter.clear();
            if (objLists == null) {
                Toast.makeText(NotesList.this, "Fetch data failed", Toast.LENGTH_LONG).show();
                return;
            }
            for (KiiObject obj : objLists) {
                Note n = new Note();
                n.title = obj.getString(Note.KEY_TITLE);
                n.content = obj.getString(Note.KEY_CONTENT);
                n.uri = obj.toUri();
                mAdapter.add(n);
            }
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() == 0) {
                Toast.makeText(NotesList.this, "There're no notes now, please create one.",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KiiQuery all_query = new KiiQuery();
                KiiQueryResult<KiiObject> result = kUser.bucket(Note.BUCKET).query(all_query);
                objLists = result.getResult();
            } catch (Exception e) {
                objLists = null;
            }
            return null;
        }

    }

}
