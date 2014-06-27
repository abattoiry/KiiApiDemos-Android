
package com.kii.apis.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.api_demos.R;
import com.kii.apis.Constants;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.resumabletransfer.KiiRTransfer;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferCallback;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferManager;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;
import com.kii.cloud.storage.resumabletransfer.StateStoreAccessException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadingFilesActivity extends Activity {

    private static final String BUCKET_NAME = Constants.FILE_BUCKET;
    private static final String STATUS_SUSPENDED = "suspended";
    private static final String STATUS_ERROR = "error";
    private static final String STATUS_UPLOADING = "uploading";
    private static final String STATUS_FINISHED = "finished";

    private ListView mListView;
    private UploaderAdapter mListAdapter;
    private ProgressDialog mProgress;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploading_files);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mListAdapter = new UploaderAdapter(this, R.layout.kiifile_list_item,
                new ArrayList<UploaderObj>());
        mListView = (ListView) this.findViewById(R.id.listViewUploadingFiles);
        mListView.setAdapter(mListAdapter);
        this.loadUploaders();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        UploaderObj mUploaderObj = null;
        KiiObject kfile = null;
        KiiUploader uploader = null;

        // TODO Auto-generated method stub
        switch (resultCode) {
            case RESULT_OK:
                mFilePath = data.getExtras().getString("data");
                if (mFilePath != null) {
                    // File localFile = new
                    // File(Environment.getExternalStorageDirectory(),
                    // mFilePath);
                    File localFile = new File(mFilePath);
                    if (localFile.isFile()) {
                        try {
                            KiiBucket bucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
                            kfile = bucket.object();
                            kfile.set("title", localFile.getName());
                            kfile.set("fileSize", localFile.length());
                            uploader = kfile.uploader(getApplicationContext(), localFile);
                        } catch (Exception e) {
                            Toast.makeText(UploadingFilesActivity.this,
                                    R.string.create_uploader_failed,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        mUploaderObj = new UploaderObj();
                        mUploaderObj.uploader = uploader;
                        mListAdapter.add(mUploaderObj);
                        new UploadFileTask().execute(mUploaderObj);
                    }
                    else
                    {
                        Toast.makeText(UploadingFilesActivity.this, R.string.file_does_not_exist,
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                mFilePath = null;
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addItem(View v) {

        Intent intent = new Intent(this, FileBrowserActivity.class);
        startActivityForResult(intent, 1);
    }

    // define a custom list adapter to handle KiiUploaders
    public class UploaderAdapter extends ArrayAdapter<UploaderObj> {

        int resource;
        String response;
        Context context;
        private LayoutInflater inflater = null;

        // initialize the adapter
        public UploaderAdapter(Context context, int resource, List<UploaderObj> items) {
            super(context, resource, items);

            // save the resource for later
            this.resource = resource;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // create the view
            ViewHolder mHolder = null;

            // get a reference to the uploader
            UploaderObj mUploaderObj = getItem(position);

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
            mHolder.title.setText(mUploaderObj.uploader.getSourceFile().getName().toString());
            mHolder.progress
                    .setProgress((int) ((mUploaderObj.completedInBytes * 100) / (mUploaderObj.totalSizeinBytes + 1)));
            if (mUploaderObj.status == STATUS_SUSPENDED) {
                mHolder.status.setText(R.string.suspended);
            }
            else if (mUploaderObj.status == STATUS_ERROR) {
                mHolder.status.setText(R.string.error);
            }
            else if (mUploaderObj.status == STATUS_UPLOADING) {
                mHolder.status.setText(R.string.uploading);
            }
            else if (mUploaderObj.status == STATUS_FINISHED) {
                mHolder.status.setText(R.string.finished);
            }

            mListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                    onListItemClick(null, arg1, position, id);
                }
            });

            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                        final int position, long id)
                {
                    onListItemLongClick(null, null, position, id);
                    return true;
                }
            });

            return convertView;
        }
    }

    // load any existing uploaders associated with this user from the server.
    // this is done on view creation
    private void loadUploaders() {
        boolean isError = false;
        UploaderObj mUploaderObj = null;
        mProgress = ProgressDialog.show(UploadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);

        // default to an empty adapter
        mListAdapter.clear();

        KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        KiiRTransferManager manager = fbucket.getTransferManager();
        List<KiiUploader> suspended = null;
        try {
            suspended = manager.listUploadEntries(getApplicationContext());
        } catch (StateStoreAccessException e1) {
            // Failed to access the local storage.
            // This is a rare error; you should be able to safely retry.
            Toast.makeText(UploadingFilesActivity.this, R.string.load_uploaders_failed,
                    Toast.LENGTH_SHORT).show();
            isError = true;
        }
        if (isError == false) {
            for (KiiUploader uploader : suspended) {
                mUploaderObj = new UploaderObj();
                mUploaderObj.uploader = uploader;
                mListAdapter.add(mUploaderObj);
                new UploadFileTask().execute(mUploaderObj);
            }
        }
        mProgress.dismiss();
    }

    static class ViewHolder {
        TextView title = null;
        ProgressBar progress = null;
        TextView status = null;
    }

    static class UploaderObj {
        KiiUploader uploader = null;
        long completedInBytes = 0;
        long totalSizeinBytes = 0;
        String status = STATUS_UPLOADING;
    }

    // the user has chosen to delete an object
    // perform that action here...
    void performDelete(int position) {

        // show a progress dialog to the user
        mProgress = ProgressDialog.show(UploadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);

        // get the object to delete based on the index of the row that was
        // tapped
        final UploaderObj o = UploadingFilesActivity.this.mListAdapter.getItem(position);

        if (o.status == STATUS_FINISHED) {
            // remove the object from the list adapter
            UploadingFilesActivity.this.mListAdapter.remove(o);
            mProgress.dismiss();
        }
        else {
            // delete the uploader asynchronously
            o.uploader.terminateAsync(
                    new KiiRTransferCallback()
                    {
                        public void onTerminateCompleted(KiiRTransfer operator,
                                java.lang.Exception e) {
                            if (e == null) {
                                UploadingFilesActivity.this.mListAdapter.remove(o);
                            }
                            else {
                                Toast.makeText(UploadingFilesActivity.this,
                                        R.string.delete_uploader_failed,
                                        Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            mProgress.dismiss();
                        }
                    });
        }
    }

    void performResume(int position) {

        final UploaderObj o = UploadingFilesActivity.this.mListAdapter.getItem(position);
        new UploadFileTask().execute(o);
    }

    void performSuspend(int position) {

        // show a progress dialog to the user
        mProgress = ProgressDialog.show(UploadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);

        final UploaderObj o = UploadingFilesActivity.this.mListAdapter.getItem(position);

        o.uploader.suspendAsync(
                new KiiRTransferCallback()
                {
                    public void onSuspendCompleted(KiiRTransfer operator, java.lang.Exception e) {
                        if (e == null) {
                            o.status = STATUS_SUSPENDED;
                        }
                        else {
                            Toast.makeText(UploadingFilesActivity.this, R.string.suspend_failed,
                                    Toast.LENGTH_SHORT).show();
                            o.status = STATUS_ERROR;
                            e.printStackTrace();
                        }
                        mProgress.dismiss();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void onListItemClick(ListView l, View v, final int position, long id) {

        final UploaderObj o = UploadingFilesActivity.this.mListAdapter.getItem(position);

        if (o.status == STATUS_SUSPENDED || o.status == STATUS_ERROR) {
            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.ask_resume))
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            UploadingFilesActivity.this.performResume(position);
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
        else if (o.status == STATUS_UPLOADING) {
            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_suspend)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            UploadingFilesActivity.this.performSuspend(position);
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
    }

    public void onListItemLongClick(ListView l, View v, final int position, long id) {

        // build the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ask_delete)
                .setCancelable(true)
                .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                    // if the user chooses 'yes',
                    public void onClick(DialogInterface dialog, int id) {

                        // perform the delete action on the tapped object
                        UploadingFilesActivity.this.performDelete(position);
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

    private class UploadFileTask extends AsyncTask<UploaderObj, String, String> {

        private UploaderObj mUploaderObj;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(UploaderObj... params) {
            mUploaderObj = params[0];

            mUploaderObj.uploader.transferAsync(
                    new KiiRTransferCallback()
                    {

                        public void onStart(KiiRTransfer operator)
                        {
                            mUploaderObj.status = STATUS_UPLOADING;
                            mListAdapter.notifyDataSetChanged();
                        }

                        public void onProgress(KiiRTransfer operator, long completedInBytes,
                                long totalSizeinBytes)
                        {
                            mUploaderObj.completedInBytes = completedInBytes;
                            mUploaderObj.totalSizeinBytes = totalSizeinBytes;
                            mListAdapter.notifyDataSetChanged();
                        }

                        public void onTransferCompleted(KiiRTransfer operator, java.lang.Exception e)
                        {
                            if (e == null) {
                                mUploaderObj.status = STATUS_FINISHED;
                            }
                            else {
                                mUploaderObj.status = STATUS_ERROR;
                                e.printStackTrace();
                            }
                            mListAdapter.notifyDataSetChanged();
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
