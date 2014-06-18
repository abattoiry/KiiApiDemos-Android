
package com.kii.apis.object;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.Utils;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.resumabletransfer.KiiRTransfer;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferCallback;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;

import java.io.File;

public class NoteEditor extends Activity implements OnClickListener {

    public static final String EXTRA_URI = "uri";

    EditText titleField, contentField;
    Uri uri = null;
    Button uploadImage, deleteImage, downloadImage;
    ImageView image;
    File bodyFile;
    ProgressDialog dialog = null;
    boolean hasBody = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        titleField = (EditText) findViewById(R.id.title_field);
        contentField = (EditText) findViewById(R.id.content_field);
        String title = getIntent().getStringExtra(Note.KEY_TITLE);
        titleField.setText(title);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        contentField.setText(getIntent().getStringExtra(Note.KEY_CONTENT));
        String u = getIntent().getStringExtra(EXTRA_URI);
        if (!TextUtils.isEmpty(u)) {
            uri = Uri.parse(u);
            downloadBody();
        }
        View v = findViewById(R.id.save);
        v.setOnClickListener(this);
        v = findViewById(R.id.delete);
        v.setOnClickListener(this);
        image = (ImageView) findViewById(R.id.image);
        image.setOnClickListener(this);

        initBodyButtons();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Utils.TAKE_OR_CHOOSE_PHOTO:
                bodyFile = Utils.getPhotoFromResult(this, data);
                image.setImageDrawable(new BitmapDrawable(getResources(), bodyFile
                        .getAbsolutePath()));
                break;
        }
    }

    private void initBodyButtons() {
        uploadImage = (Button) findViewById(R.id.upload_image);
        downloadImage = (Button) findViewById(R.id.download_image);
        deleteImage = (Button) findViewById(R.id.delete_image);
        uploadImage.setOnClickListener(this);
        downloadImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        if (hasBody) {
            downloadImage.setVisibility(View.INVISIBLE);
            deleteImage.setVisibility(View.VISIBLE);
            uploadImage.setText(R.string.update_image);
        } else {
            downloadImage.setVisibility(View.INVISIBLE);
            deleteImage.setVisibility(View.INVISIBLE);
            uploadImage.setText(R.string.upload_image);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                save(false);
                break;
            case R.id.delete:
                KiiObject object = null;
                if (uri != null) {
                    object = KiiObject.createByUri(uri);
                    new DeleteTask().execute(object);
                } else {
                    Toast.makeText(this, "Cannot delete unsaved note", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.image:
                Utils.takeOrChoosePhoto(this, Utils.TAKE_OR_CHOOSE_PHOTO);
                break;
            case R.id.upload_image:
                save(true);
                break;
            case R.id.delete_image:
                deleteBody();
                break;
            case R.id.download_image:
                downloadBody();
                break;
        }
    }

    private void downloadBody() {
        KiiObject object = KiiObject.createByUri(uri);
        new DownloadBodyTask().execute(object);
    }

    private void deleteBody() {
        KiiObject object = KiiObject.createByUri(uri);
        new DeleteBodyTask().execute(object);
    }

    protected void save(boolean withBody) {
        String title = titleField.getText().toString();
        String content = contentField.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "All fields cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        KiiObject object = null;
        if (uri != null) {
            object = KiiObject.createByUri(uri);
        } else {
            object = NotesList.kUser.bucket(Note.BUCKET).object();
        }
        object.set(Note.KEY_TITLE, title);
        object.set(Note.KEY_CONTENT, content);
        if (withBody) {
            new SaveBodyTask().execute(object);
        } else {
            new SaveTask().execute(object);
        }
    }

    class SaveBodyTask extends SaveTask {
        @Override
        protected void onPostExecute(Void result) {
            KiiUploader uploader = KiiObject.createByUri(uri).uploader(NoteEditor.this, bodyFile);
            try {
                uploader.transferAsync(new KiiRTransferCallback() {
                    @Override
                    public void onTransferCompleted(KiiRTransfer operator, Exception e) {
                        String content;
                        if (e != null) {
                            e.printStackTrace();
                            content = e.getLocalizedMessage();
                        } else {
                            content = "Upload body success!";
                        }
                        hasBody = true;
                        dialog.dismiss();
                        initBodyButtons();
                        Toast.makeText(NoteEditor.this, content, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SaveTask extends AsyncTask<KiiObject, Void, Void> {

        boolean saveSuccess = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                KiiObject o = params[0];
                o.save();
                o.refresh();
                uri = o.toUri();
                saveSuccess = true;
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NoteEditor.this);
            dialog.setCancelable(false);
            dialog.setMessage("Saving note…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (saveSuccess) {
                Toast.makeText(NoteEditor.this, "Save successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(NoteEditor.this, "Save failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    class DeleteTask extends AsyncTask<KiiObject, Void, Void> {

        boolean delSuccess = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                params[0].delete();
                delSuccess = true;
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NoteEditor.this);
            dialog.setCancelable(false);
            dialog.setMessage("Deleting note…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (delSuccess) {
                Toast.makeText(NoteEditor.this, "Deleted successfully", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(NoteEditor.this, "Deleted failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    class DeleteBodyTask extends AsyncTask<KiiObject, Void, Void> {
        boolean delSuccess = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                params[0].deleteBody();
                delSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NoteEditor.this);
            dialog.setCancelable(false);
            dialog.setMessage("Deleting body…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (delSuccess) {
                Toast.makeText(NoteEditor.this, "Deleted successfully", Toast.LENGTH_LONG).show();
                image.setImageDrawable(getResources().getDrawable(R.drawable.pic_load));
                hasBody = false;
            } else {
                Toast.makeText(NoteEditor.this, "Deleted failed, please try again later",
                        Toast.LENGTH_LONG).show();
            }
            initBodyButtons();
        }
    }

    class DownloadBodyTask extends AsyncTask<KiiObject, Void, Void> {
        boolean downloadSuccess = false;
        boolean noBody = false;

        @Override
        protected Void doInBackground(KiiObject... params) {
            try {
                Context context = NoteEditor.this;
                bodyFile = new File(context.getCacheDir(), uri.getLastPathSegment());
                params[0].downloader(NoteEditor.this, bodyFile).transfer(null);
                downloadSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof NotFoundException) {
                    noBody = true;
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NoteEditor.this);
            dialog.setCancelable(false);
            dialog.setMessage("Downloading body…");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (downloadSuccess) {
                hasBody = true;
                Toast.makeText(NoteEditor.this, "Download body successfully", Toast.LENGTH_LONG)
                        .show();
                if (bodyFile.exists()) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 3;
                    Bitmap bmp = BitmapFactory.decodeFile(bodyFile.getAbsolutePath(), opts);
                    image.setImageBitmap(bmp);
                }
            }
            initBodyButtons();
        }
    }
}
