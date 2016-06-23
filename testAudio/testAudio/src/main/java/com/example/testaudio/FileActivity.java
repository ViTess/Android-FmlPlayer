package com.example.testaudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * File search activity
 * Created by vite on 16-6-21.
 */
public class FileActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
    final int HANDLER_MSG_SELECTED = 0x01;

    Context context;
    Button btn_back, btn_close;
    TextView tv_path;
    ListView lv;

    FileAdapter mAdapter;

    File mFile = null;
    File[] mFileArr = null;
    String mSelectedPath = null;

    FileSearchTask mFSTask = null;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        init();
        initView();
        loadView();
        initHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFSTask != null) {
            mFSTask.cancel(true);
            mFSTask = null;
        }
        mFileArr = null;
        mFile = null;
    }

    private void init() {
        context = this;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SDCard isn't mounted", Toast.LENGTH_SHORT);
            finish();
        }
    }

    private void initView() {
        btn_back = (Button) findViewById(R.id.activity_file_btn_back);
        btn_close = (Button) findViewById(R.id.activity_file_btn_close);
        tv_path = (TextView) findViewById(R.id.activity_file_tv_title);
        lv = (ListView) findViewById(R.id.activity_file_lv);
    }

    private void loadView() {
        mAdapter = new FileAdapter();
        lv.setAdapter(mAdapter);

        btn_back.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        lv.setOnItemClickListener(this);

        mFSTask = new FileSearchTask();
        String path = getIntent().getStringExtra("musicpath");
        if (!TextUtils.isEmpty(path)) {
            mFSTask.execute(new File(path).getParentFile());
        } else
            mFSTask.execute(Environment.getExternalStorageDirectory());
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_MSG_SELECTED:
                        if (mSelectedPath != null) {
                            btn_close.setBackgroundResource(R.drawable.ok);
                            btn_back.setVisibility(View.GONE);
                            tv_path.setText("已选择：" + mSelectedPath);
                        } else {
                            btn_close.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
                            btn_back.setVisibility(View.VISIBLE);
                            tv_path.setText(mFile.getAbsolutePath());
                        }
                        break;
                }
            }
        };
    }

    private void loadFile(File file) {
        final File mTempFile = file;
        if (mTempFile != null && mTempFile.exists() && mTempFile.isDirectory()) {
            mFile = mTempFile;
            mFileArr = mTempFile.listFiles();
            if (mFileArr != null && mFileArr.length > 0) {
                Arrays.sort(mFileArr, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        int result;
                        boolean f1 = lhs.isDirectory();
                        boolean f2 = rhs.isDirectory();
                        if (f1 && !f2) {
                            result = -1;
                        } else if (!f1 && f2) {
                            result = 1;
                        } else {
                            result = lhs.getName().toUpperCase().compareTo(rhs.getName().toUpperCase());
                        }
                        return result;
                    }
                });
            }
        }
    }

    private boolean isMusicFile(String filePath) {
        if (filePath == null)
            return false;
        final String mTemp = filePath.toUpperCase();
        return mTemp.endsWith("MP3") || mTemp.endsWith("WAV") || mTemp.endsWith("OGG");
    }

    private boolean isMusicFile(File file) {
        if (file == null)
            return false;
        return isMusicFile(file.getName());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mFileArr != null) {
            File mTempFile = mFileArr[position];
            if (mTempFile.isDirectory()) {
                if (mFSTask != null)
                    mFSTask.cancel(true);

                if (mTempFile.list() == null) {
                    Toast.makeText(context, "need root", Toast.LENGTH_SHORT).show();
                } else {
                    mFSTask = new FileSearchTask();
                    mFSTask.execute(mTempFile);
                }
            } else {
                if (isMusicFile(mTempFile.getAbsolutePath())) {
                    mSelectedPath = mSelectedPath == null ? mTempFile.getAbsolutePath() : null;
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "please selected mp3/wav/ogg", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mFSTask != null)
            mFSTask.cancel(true);
        switch (v.getId()) {
            case R.id.activity_file_btn_back:
                if (mSelectedPath != null) {
                    mSelectedPath = null;
                    mAdapter.notifyDataSetChanged();
                    return;
                }
                if (mFileArr != null) {
                    File mTempFile = mFile.getParentFile();
                    if (mTempFile != null) {
                        mFSTask = new FileSearchTask();
                        mFSTask.execute(mTempFile);
                    } else {
                        Toast.makeText(this, "No upper directory", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.activity_file_btn_close:
                if (mSelectedPath == null)
                    setResult(RESULT_CANCELED);
                else {
                    Intent data = new Intent();
                    data.putExtra("path", mSelectedPath);
                    setResult(RESULT_OK, data);
                }
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onClick(btn_back);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class FileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFileArr == null ? 0 : mFileArr.length;
        }

        @Override
        public Object getItem(int position) {
            return mFileArr[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            if (convertView == null) {
                mHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_file, null);
                mHolder.iv_icon = (ImageView) convertView.findViewById(R.id.item_file_iv_icon);
                mHolder.tv_fileName = (TextView) convertView.findViewById(R.id.item_file_tv_filename);
                mHolder.cb_selected = (CheckBox) convertView.findViewById(R.id.item_file_cb_selected);
                mHolder.cb_selected.setOnCheckedChangeListener(MyCheckListener);
                convertView.setTag(mHolder);
            } else
                mHolder = (ViewHolder) convertView.getTag();

            File mTempFile = mFileArr[position];
            if (mTempFile.isDirectory()) {
                mHolder.iv_icon.setImageResource(R.drawable.folder);
                mHolder.cb_selected.setVisibility(View.GONE);
                mHolder.cb_selected.setTag(null);
            } else {
                mHolder.iv_icon.setImageResource(R.drawable.file);
                mHolder.cb_selected.setVisibility(View.VISIBLE);
                mHolder.cb_selected.setTag(position);
                if (mSelectedPath != null && mTempFile.getAbsolutePath().equals(mSelectedPath)) {
                    mHolder.cb_selected.setChecked(true);
                } else {
                    mHolder.cb_selected.setChecked(false);
                }
            }

            mHolder.tv_fileName.setText(mTempFile.getName());

            return convertView;
        }

        CompoundButton.OnCheckedChangeListener MyCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getTag() != null) {
                    int position = (int) buttonView.getTag();
                    File mTempFile = mFileArr[position];
                    if (isChecked) {
                        if (mSelectedPath == null || !mTempFile.getAbsolutePath().equals(mSelectedPath)) {
                            if (isMusicFile(mTempFile.getAbsolutePath())) {
                                mSelectedPath = mTempFile.getAbsolutePath();
                            } else {
                                Toast.makeText(context, "please selected mp3/wav/ogg", Toast.LENGTH_SHORT).show();
                                buttonView.setChecked(false);
                                return;
                            }
                        }
                    } else {
                        if (mSelectedPath != null && mTempFile.getAbsolutePath().equals(mSelectedPath))
                            mSelectedPath = null;
                    }
                    mHandler.sendEmptyMessage(HANDLER_MSG_SELECTED);
                    FileAdapter.this.notifyDataSetChanged();
                }
            }
        };
    }

    private class ViewHolder {
        ImageView iv_icon;
        TextView tv_fileName;
        CheckBox cb_selected;
    }

    private class FileSearchTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFileArr = null;
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                loadFile((File) params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                mFileArr = null;
            }
            return mFileArr;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            tv_path.setText(mFile.getAbsolutePath());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mAdapter.notifyDataSetChanged();
        }
    }
}
