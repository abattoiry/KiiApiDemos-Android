package com.kii.apis.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.kii.apis.R;

public class FileBrowserActivity extends Activity{
    private ListView mListView;	
    private File[] mFiles;  
    private String mCurrentpath;  
    private SimpleAdapter mSimpleAdapter; 
    private TextView mTextViewCurrentPath;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.file_browser);
        mTextViewCurrentPath = (TextView) findViewById(R.id.textViewCurrentPath);
        mListView = (ListView) findViewById(R.id.listViewFileBrowser);  
        init(Environment.getExternalStorageDirectory());  
        mListView.setOnItemClickListener(new OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
                // TODO Auto-generated method stub  
                String folder = ((TextView) arg1.findViewById(R.id.textViewFile))  
                        .getText().toString();  
                try {  
                    File filef = new File(mCurrentpath + '/'  
                            + folder);  
                    init(filef);  
  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
  
            }  
        });  
    }          
        
    public void init(File f) {  
        if (Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED)) {
            mTextViewCurrentPath.setText(f.getAbsolutePath());
            if (f.isDirectory())        		
        	{
                mFiles = f.listFiles(); 
                mTextViewCurrentPath.setText(f.getAbsolutePath());
                mCurrentpath=f.getPath();  
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
                for (int i = 0; i < mFiles.length; i++) {  
                    Map<String, Object> maps = new HashMap<String, Object>();  
                    if (mFiles[i].isFile()) {  
                        maps.put("image", R.drawable.file);
                    }
                    else {  
                        maps.put("image", R.drawable.folder);
                    }
                    maps.put("filenames", mFiles[i].getName());  
                    list.add(maps);  
                }  
                mSimpleAdapter = new SimpleAdapter(this, list,  
                        R.layout.file_browser_list_item, new String[] { "image",  
                                "filenames" }, new int[] { R.id.imageViewFile,  
                                R.id.textViewFile });  
                mListView.setAdapter(mSimpleAdapter);  
            }
        }          
    }
    
    public void onBackButtonClick(View v) {
        
        try {
            File filef = new File(mTextViewCurrentPath.getText().toString());
            init(filef.getParentFile());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }

    public void onOKButtonClick(View v) {
		Intent intent = new Intent(this, UploadingFilesActivity.class);
        Bundle b = new Bundle();  
        b.putString("data", mTextViewCurrentPath.getText().toString());  
        intent.putExtras(b);  
        this.setResult(RESULT_OK, intent);  
        this.finish();
    }
}
