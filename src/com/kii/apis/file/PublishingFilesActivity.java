
package com.kii.apis.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.apis.file.DownloadingFilesActivity.ViewHolder;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class PublishingFilesActivity extends Activity {
    private static final String BUCKET_NAME = Constants.FILE_BUCKET;

    private ListView mListView;
    private KiiFileAdapter mListAdapter;
    private ProgressDialog mProgress;

    private boolean isError;
    private EditText mEditTextUrl;
    private EditText mEditTextExpirationTime;
    private String mPublishedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publishing_files);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mEditTextUrl = (EditText) findViewById(R.id.editTextResult);
        mEditTextExpirationTime = (EditText) findViewById(R.id.editTextExpirationTime);
        mListAdapter = new KiiFileAdapter(this, R.layout.kiifile_list_item,
                new ArrayList<KiiObject>());
        mListView = (ListView) this.findViewById(R.id.listViewKiiFiles);
        // set it to our view's list
        mListView.setAdapter(mListAdapter);
        mListAdapter.clear();
        this.loadKiiFiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_showcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_code:
                String title = getString(R.string.showcode_title, getTitle());
                String[] group = new String[] {
                        getString(R.string.publishing_files)
                };
                String[] child = new String[] {
                        "Uri uri = Uri.parse(\"some existing file uri on cloud\");\r\n"
                                + "KiiFile kFile = KiiFile.createByUri(uri);\r\n"
                                + "try {\r\n"
                                + "    long expiration = System.currentTimeMillis() + 24 * 60 * 60 * 1000;\r\n"
                                + "    String publishedUri = kFile.publish(expiration);\r\n"
                                + "} catch (AppException e) {\r\n"
                                + "    // Failure. Handle error.\r\n"
                                + "} catch (IOException e) {\r\n"
                                + "    // Failure. Handle error.\r\n"
                                + "}"
                };
                Intent intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
                intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
                intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void loadKiiFiles() {
        mProgress = ProgressDialog.show(PublishingFilesActivity.this, "",
                getString(R.string.in_progressing), true);
        KiiQuery query = new KiiQuery(null);
        query.sortByAsc("_created");

        KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        fbucket.query(new KiiQueryCallBack<KiiObject>() {
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result,
                    Exception e)
            {
                if (e == null) {
                    // add the KiiFiles to the adapter (adding to the listview)
                    List<KiiObject> kiiFilesLists = result.getResult();
                    for (KiiObject kiiFile : kiiFilesLists) {
                        mListAdapter.add(kiiFile);
                    }
                }
                else {
                    Toast.makeText(PublishingFilesActivity.this, R.string.load_kiifile_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, query);
        mProgress.dismiss();
    }

    public class KiiFileAdapter extends ArrayAdapter<KiiObject> {

        private LayoutInflater inflater = null;

        // initialize the adapter
        public KiiFileAdapter(Context context, int resource, List<KiiObject> items) {
            super(context, resource, items);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // create the view
            ViewHolder mHolder = null;

            KiiObject mKiiFile = getItem(position);

            // if it's not already created
            if (convertView == null) {

                // create the view by inflating the xml resource
                // (res/layout/row.xml)
                mHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.kiifile_list_item, null);
                mHolder.progress = (ProgressBar) (convertView.findViewById(R.id.progressBarKiiFile));
                mHolder.title = (TextView) (convertView.findViewById(R.id.textViewTitle));
                mHolder.status = (TextView) (convertView.findViewById(R.id.textViewStatus));
                convertView.setTag(mHolder);
            }
            // it's already created, reuse it
            else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            mHolder.title.setText(mKiiFile.getString("title", " "));
            mHolder.progress.setVisibility(View.GONE);
            mHolder.status.setText(String.valueOf(mKiiFile.getLong("fileSize", 0)));
            mListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                    onListItemClick(null, arg1, position, id);
                }
            });

            return convertView;
        }
    }

    public void onListItemClick(ListView l, View v, final int position, long id) {
        final KiiObject mKiiFile = PublishingFilesActivity.this.mListAdapter.getItem(position);

        // build the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ask_publish_file)
                .setCancelable(true)
                .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                    // if the user chooses 'yes',
                    public void onClick(DialogInterface dialog, int id) {

                        // perform the delete action on the tapped object
                        new PublishFileTask().execute(mKiiFile);
                    }
                })
                .setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {

                    // if the user chooses 'no'
                    public void onClick(DialogInterface dialog, int id) {

                        // simply dismiss the dialog
                        dialog.cancel();
                    }
                });

        // show the dialog
        builder.create().show();
    }

    private class PublishFileTask extends AsyncTask<KiiObject, String, String> {

        ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(PublishingFilesActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.in_progressing));
            dialog.show();
            isError = false;
        }

        @Override
        protected String doInBackground(KiiObject... params) {

            KiiObject mKiiFile = params[0];

            // Publish the file
            try {
                // Set the expiration time (24 hours from now)
                long expiration = Integer.valueOf(mEditTextExpirationTime.getText().toString());
                if (expiration > 0) {
                    expiration = System.currentTimeMillis() + expiration * 60 * 1000;
                    mPublishedUri = mKiiFile.publishBodyExpiresAt(expiration);
                }
                else {
                    mPublishedUri = mKiiFile.publishBody();
                }

                // Publish the file
                // The file is now accessible with publishedUri.
            } catch (AppException e) {
                // handle error
                isError = true;
            } catch (IOException e) {
                // handle error
                isError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (isError) {
                Toast.makeText(PublishingFilesActivity.this, R.string.publish_failed,
                        Toast.LENGTH_LONG).show();
            } else {
                mEditTextUrl.setText(mPublishedUri);
                Toast.makeText(PublishingFilesActivity.this, R.string.publish_succ,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void urlGoHandler(View v)
    {
        try {
            Uri uri = Uri.parse(mPublishedUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(PublishingFilesActivity.this, R.string.open_url_failed,
                    Toast.LENGTH_LONG).show();
        }
    }
}
