
package com.kii.apis.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.cloud.storage.resumabletransfer.KiiDownloader;
import com.kii.cloud.storage.resumabletransfer.KiiRTransfer;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferCallback;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferManager;
import com.kii.cloud.storage.resumabletransfer.StateStoreAccessException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadingFilesActivity extends Activity {
    private static final String BUCKET_NAME = Constants.FILE_BUCKET;
    private static final String STATUS_SUSPENDED = "Suspended";
    private static final String STATUS_ERROR = "Error";
    private static final String STATUS_DOWNLOADING = "Downloading";
    private static final String STATUS_FINISHED = "Finished";
    private static final String STATUS_KIIFILE = "KiiFile";
    private static final String STATUS_KIIFILE_lIST_TITLE = "ListKiiFile";
    private static final String STATUS_DOWNLOADER_lIST_TITLE = "ListDownloader";

    private ListView mListView;
    private DownloaderAdapter mListAdapter;
    private ProgressDialog mProgress;
    private TextView mTextViewCurrentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloading_files);
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // create an empty uploader adapter
        mListAdapter = new DownloaderAdapter(this, R.layout.kiifile_list_item,
                new ArrayList<DownloaderObj>());
        mListView = (ListView) this.findViewById(R.id.listViewDownloadingFiles);
        mTextViewCurrentPath = (TextView) findViewById(R.id.textViewCurrentPathNotify);
        mTextViewCurrentPath
                .setText(Environment.getExternalStorageDirectory().getPath().toString());
        // set it to our view's list
        mListView.setAdapter(mListAdapter);
        // query for any previously-created downloaderss
        mListAdapter.clear();
        this.loadKiiFiles();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (resultCode) {
            case RESULT_OK:
                File localFile = new File(data.getExtras().getString("data"));
                if (localFile.isDirectory()) {
                    mTextViewCurrentPath.setText(localFile.getAbsolutePath());
                }
                else {
                    mTextViewCurrentPath.setText(Environment.getExternalStorageDirectory()
                            .getPath().toString());
                    Toast.makeText(DownloadingFilesActivity.this, R.string.invalid_folder,
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                mTextViewCurrentPath.setText(Environment.getExternalStorageDirectory().getPath()
                        .toString());
                Toast.makeText(DownloadingFilesActivity.this, R.string.invalid_folder,
                        Toast.LENGTH_LONG).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onSelectFolder(View v) {

        Intent intent = new Intent(this, FileBrowserActivity.class);
        startActivityForResult(intent, 1);
    }

    // define a custom list adapter to handle KiiDownloaders
    public class DownloaderAdapter extends ArrayAdapter<DownloaderObj> {

        private LayoutInflater inflater = null;

        // initialize the adapter
        public DownloaderAdapter(Context context, int resource, List<DownloaderObj> items) {
            super(context, resource, items);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // create the view
            ViewHolder mHolder = null;

            // get a reference to the downloader
            DownloaderObj mDownloaderObj = getItem(position);

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

            if (mDownloaderObj.status == STATUS_KIIFILE_lIST_TITLE) {
                mHolder.progress.setVisibility(View.GONE);
                mHolder.title.setText(R.string.files_in_cloud);
                mHolder.title.setTextColor(android.graphics.Color.BLUE);
                mHolder.title.setTypeface(Typeface.DEFAULT_BOLD, Typeface.ITALIC);
                mHolder.status.setText(R.string.size);
                mHolder.status.setTextColor(android.graphics.Color.BLUE);
                mHolder.status.setTypeface(Typeface.DEFAULT_BOLD, Typeface.ITALIC);
            }
            else if (mDownloaderObj.status == STATUS_DOWNLOADER_lIST_TITLE) {
                mHolder.progress.setVisibility(View.GONE);
                mHolder.title.setText("\r\n\r\n" + R.string.downloading_files);
                mHolder.title.setTextColor(android.graphics.Color.BLUE);
                mHolder.title.setTypeface(Typeface.DEFAULT_BOLD, Typeface.ITALIC);
                mHolder.status.setText("\r\n\r\n" + R.string.status);
                mHolder.status.setTextColor(android.graphics.Color.BLUE);
                mHolder.status.setTypeface(Typeface.DEFAULT_BOLD, Typeface.ITALIC);
            } else if (mDownloaderObj.status == STATUS_KIIFILE) {
                mHolder.title.setTextColor(android.graphics.Color.BLACK);
                mHolder.title.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
                mHolder.status.setTextColor(android.graphics.Color.BLACK);
                mHolder.status.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
                mHolder.progress.setVisibility(View.GONE);
                mHolder.title.setText(mDownloaderObj.kiiFile.getString("title", " "));
                mHolder.status.setText(String.valueOf(mDownloaderObj.kiiFile.getLong("fileSize", 0)));
            }
            else {
                mHolder.title.setTextColor(android.graphics.Color.BLACK);
                mHolder.title.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
                mHolder.status.setTextColor(android.graphics.Color.BLACK);
                mHolder.status.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
                mHolder.title.setText(mDownloaderObj.downloader.getDestFile().getName().toString());
                mHolder.progress.setVisibility(View.VISIBLE);
                mHolder.progress
                        .setProgress((int) ((mDownloaderObj.completedInBytes * 100) / (mDownloaderObj.totalSizeinBytes + 1)));
                if (mDownloaderObj.status == STATUS_SUSPENDED) {
                    mHolder.status.setText(R.string.suspended);
                }
                else if (mDownloaderObj.status == STATUS_ERROR) {
                    mHolder.status.setText(R.string.error);
                }
                else if (mDownloaderObj.status == STATUS_DOWNLOADING) {
                    mHolder.status.setText(R.string.downloading);
                }
                else if (mDownloaderObj.status == STATUS_FINISHED) {
                    mHolder.status.setText(R.string.finished);
                }
            }

            if (mDownloaderObj.status != STATUS_KIIFILE_lIST_TITLE
                    && mDownloaderObj.status != STATUS_DOWNLOADER_lIST_TITLE) {

                mListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
                            long id) {
                        onListItemClick(null, arg1, position, id);
                    }
                });

                mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                            final int position, long id) {
                        onListItemLongClick(null, null, position, id);
                        return true;
                    }
                });
            }
            return convertView;
        }
    }

    public void performDeleteKiiFile(int position)
    {
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);
        // show a progress dialog to the user
        mProgress = ProgressDialog.show(DownloadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);
        o.kiiFile.delete(new KiiObjectCallBack() {
            public void onDeleteCompleted(int token, java.lang.Exception e)
            {
                if (e == null) {
                    DownloadingFilesActivity.this.mListAdapter.remove(o);
                }
                else {
                    Toast.makeText(DownloadingFilesActivity.this, R.string.delete_failed,
                            Toast.LENGTH_SHORT).show();
                }
                mProgress.dismiss();
            }
        });
    }

    void performDelete(int position) {
        // show a progress dialog to the user
        mProgress = ProgressDialog.show(DownloadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);

        // get the object to delete based on the index of the row that was
        // tapped
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);

        if (o.status == STATUS_FINISHED) {
            // remove the object from the list adapter
            DownloadingFilesActivity.this.mListAdapter.remove(o);
            mProgress.dismiss();
        }
        else {
            // delete the object asynchronously
            o.downloader.terminateAsync(
                    new KiiRTransferCallback()
                    {
                        public void onTerminateCompleted(KiiRTransfer operator,
                                java.lang.Exception e)
                        {
                            if (e == null) {
                                DownloadingFilesActivity.this.mListAdapter.remove(o);
                            }
                            else {
                                Toast.makeText(DownloadingFilesActivity.this,
                                        R.string.delete_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                            mProgress.dismiss();
                        }
                    });
        }
    }

    void performResume(int position) {
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);
        new DownloadFileTask().execute(o);
    }

    void performSuspend(int position) {
        // show a progress dialog to the user
        mProgress = ProgressDialog.show(DownloadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);
        o.downloader.suspendAsync(
                new KiiRTransferCallback()
                {
                    public void onSuspendCompleted(KiiRTransfer operator, java.lang.Exception e) {
                        if (e == null) {
                            o.status = STATUS_SUSPENDED;
                        }
                        else {
                            Toast.makeText(DownloadingFilesActivity.this, R.string.suspend_failed,
                                    Toast.LENGTH_SHORT).show();
                            o.status = STATUS_ERROR;
                        }
                        mProgress.dismiss();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
    }

    void performAdd(int position) {
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);
        DownloaderObj mDownloaderObj = null;
        KiiDownloader mDownloader = null;
        String title;

        title = o.kiiFile.getString("title", getString(R.string.default_title));
        try {
            mDownloader = o.kiiFile.downloader(getApplicationContext(),
                    new File(mTextViewCurrentPath.getText().toString(), title));
        } catch (Exception e) {
            Toast.makeText(DownloadingFilesActivity.this, R.string.add_downloader_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mDownloaderObj = new DownloaderObj();
        mDownloaderObj.kiiFile = o.kiiFile;
        mDownloaderObj.downloader = mDownloader;
        mDownloaderObj.status = STATUS_DOWNLOADING;
        mListAdapter.add(mDownloaderObj);
        new DownloadFileTask().execute(mDownloaderObj);
    }

    public void onListItemClick(ListView l, View v, final int position, long id) {
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);

        if (o.status == STATUS_SUSPENDED || o.status == STATUS_ERROR) {
            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_resume)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            DownloadingFilesActivity.this.performResume(position);
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
        else if (o.status == STATUS_DOWNLOADING) {
            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_suspend)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            DownloadingFilesActivity.this.performSuspend(position);
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
        else if (o.status == STATUS_KIIFILE) {
            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_download)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            DownloadingFilesActivity.this.performAdd(position);
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
        final DownloaderObj o = DownloadingFilesActivity.this.mListAdapter.getItem(position);
        if (o.status == STATUS_KIIFILE) {

            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_delete)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            DownloadingFilesActivity.this.performDeleteKiiFile(position);
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
        else {

            // build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ask_delete)
                    .setCancelable(true)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

                        // if the user chooses 'yes',
                        public void onClick(DialogInterface dialog, int id) {

                            // perform the delete action on the tapped object
                            DownloadingFilesActivity.this.performDelete(position);
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

    static class ViewHolder {
        TextView title = null;
        ProgressBar progress = null;
        TextView status = null;
    }

    static class DownloaderObj {
        KiiDownloader downloader = null;
        KiiObject kiiFile = null;
        long completedInBytes = 0;
        long totalSizeinBytes = 0;
        String status = STATUS_DOWNLOADING;
    }

    // load any existing downloaders associated with this user from the server.
    // this is done on view creation
    private void loadDownloaders() {
        boolean isError = false;
        DownloaderObj mDownloaderObj = null;

        // all add downloader list title
        mDownloaderObj = new DownloaderObj();
        mDownloaderObj.kiiFile = null;
        mDownloaderObj.status = STATUS_DOWNLOADER_lIST_TITLE;
        mDownloaderObj.downloader = null;
        mListAdapter.add(mDownloaderObj);

        mProgress = ProgressDialog.show(DownloadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);

        KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        KiiRTransferManager manager = fbucket.getTransferManager();
        List<KiiDownloader> suspended = null;
        try {
            suspended = manager.listDownloadEntries(getApplicationContext());
        } catch (StateStoreAccessException e1) {
            // Failed to access the local storage.
            // This is a rare error; you should be able to safely retry.
            Toast.makeText(DownloadingFilesActivity.this, R.string.load_downloader_failed,
                    Toast.LENGTH_SHORT).show();
            isError = true;
        }
        if (isError == false) {
            for (KiiDownloader downloader : suspended) {
                mDownloaderObj = new DownloaderObj();
                mDownloaderObj.downloader = downloader;
                mDownloaderObj.status = STATUS_DOWNLOADING;
                mListAdapter.add(mDownloaderObj);
                new DownloadFileTask().execute(mDownloaderObj);
            }
        }
        mProgress.dismiss();
    }

    private void loadKiiFiles() {
        DownloaderObj mDownloaderObj = null;
        mProgress = ProgressDialog.show(DownloadingFilesActivity.this, "",
                getString(R.string.in_progressing), true);
        // first of all add kiiFile list title
        mDownloaderObj = new DownloaderObj();
        mDownloaderObj.kiiFile = null;
        mDownloaderObj.status = STATUS_KIIFILE_lIST_TITLE;
        mDownloaderObj.downloader = null;
        mListAdapter.add(mDownloaderObj);

        KiiQuery query = new KiiQuery(null);
        query.sortByAsc("_created");

        KiiBucket fbucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);
        fbucket.query(new KiiQueryCallBack<KiiObject>() {
            DownloaderObj mDownloaderObj = null;

            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result,
                    java.lang.Exception e)
            {
                if (e == null) {
                    // add the KiiFiles to the adapter (adding to the listview)
                    List<KiiObject> kiiFilesLists = result.getResult();
                    for (KiiObject kiiFile : kiiFilesLists) {
                        mDownloaderObj = new DownloaderObj();
                        mDownloaderObj.kiiFile = kiiFile;
                        mDownloaderObj.status = STATUS_KIIFILE;
                        mDownloaderObj.downloader = null;
                        mListAdapter.add(mDownloaderObj);
                    }
                }
                else {
                    Toast.makeText(DownloadingFilesActivity.this, R.string.load_kiifile_failed,
                            Toast.LENGTH_SHORT).show();
                }
                loadDownloaders();
            }
        }, query);
        mProgress.dismiss();
    }

    private class DownloadFileTask extends AsyncTask<DownloaderObj, String, String> {

        private DownloaderObj mDownloaderObj;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(DownloaderObj... params) {
            KiiObject mKiiFile = null;
            mDownloaderObj = params[0];
            try {
                mKiiFile = mDownloaderObj.kiiFile;
                mKiiFile.refresh();
            } catch (Exception e) {
                mDownloaderObj.status = STATUS_ERROR;
                mListAdapter.notifyDataSetChanged();
                e.printStackTrace();
                return null;
            }

            mDownloaderObj.downloader.transferAsync(
                    new KiiRTransferCallback()
                    {
                        public void onStart(KiiRTransfer operator) {
                            mDownloaderObj.status = STATUS_DOWNLOADING;
                            mListAdapter.notifyDataSetChanged();
                        }

                        public void onProgress(KiiRTransfer operator, long completedInBytes,
                                long totalSizeinBytes) {
                            mDownloaderObj.completedInBytes = completedInBytes;
                            mDownloaderObj.totalSizeinBytes = totalSizeinBytes;
                            mListAdapter.notifyDataSetChanged();
                        }

                        public void onTransferCompleted(KiiRTransfer operator, java.lang.Exception e) {
                            if (e == null) {
                                mDownloaderObj.status = STATUS_FINISHED;
                                mListAdapter.notifyDataSetChanged();
                            }
                            else {
                                mDownloaderObj.status = STATUS_ERROR;
                                mListAdapter.notifyDataSetChanged();
                                Toast.makeText(DownloadingFilesActivity.this,
                                        R.string.download_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
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
