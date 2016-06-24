package com.example.testaudio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.un4seen.bass.BASS;
import com.vite.audiolibrary.FmlPlayer;
import com.vite.audiolibrary.PlayerListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener,
        OnSeekBarChangeListener, android.widget.RadioGroup.OnCheckedChangeListener {
    private final int HANDLER_MSG_UPDATE_PROGRESS = 0x01;

    private final String ASSETS_FILE = "example.mp3";

    Context context;
    FmlPlayer fmlPlayer;
    FmlPlayer.FxController fxController;

    EditText url;
    Button playLocal, playAsset, playOnline, openFile, pause, stop, restart;
    TextView name, localFilePath, volume, panning, rotate, eq100, eq600, eq1k, eq8k, eq14k, tv_progress;
    SeekBar volume_sb, panning_sb, rotate_sb, eq100_sb, eq600_sb, eq1k_sb, eq8k_sb, eq14k_sb, sb_progress;
    CheckBox rotate_cb, eq_cb, autowah_cb, phaser_cb, chorus_cb, echo_cb;
    RadioGroup autowah_rg, phaser_rg, chorus_rg, echo_rg;
    RadioButton autowah_slow, phaser_shift, chorus_flanger, echo_small;
    ProgressDialog loadingDialog;

    PlayerListener.OnCompletionListener mCompletionListener;
    PlayerListener.OnPreparedListener mPreparedListener;
    PlayerListener.OnErrorListener mErrorListener;

    Timer timer;
    TimerTask timerTask;
    MainHandler mMianHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        init();
        initView();
        loadView();
    }

    private void init() {
        mMianHandler = new MainHandler(this);

        FmlPlayer.init(this, true);
        FmlPlayer.setGlobalVolume(1f);

        FmlPlayer.setNetTimeOut(6000);
        FmlPlayer.setNetBuffer(8000);
        FmlPlayer.setNetPreBufPercentage(0);

//        Log.v("MainActivity","BASS_CONFIG_NET_BUFFER::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_BUFFER));
//        Log.v("MainActivity","BASS_CONFIG_NET_PASSIVE::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_PASSIVE));
//        Log.v("MainActivity","BASS_CONFIG_NET_PREBUF::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_PREBUF));
//        Log.v("MainActivity","BASS_CONFIG_NET_PLAYLIST::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_PLAYLIST));
//        Log.v("MainActivity","BASS_CONFIG_NET_READTIMEOUT::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_READTIMEOUT));
//        Log.v("MainActivity","BASS_CONFIG_NET_AGENT::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_AGENT));
//        Log.v("MainActivity","BASS_CONFIG_NET_PROXY::"+FmlPlayer.getGlobalOptions(BASS.BASS_CONFIG_NET_PROXY));

        fxController = new FmlPlayer.FxController();

        mCompletionListener = new PlayerListener.OnCompletionListener() {
            @Override
            public void onCompletion(FmlPlayer fp) {
                dismissLoadingDialog();

                Toast.makeText(context, "onCompletion", Toast.LENGTH_LONG).show();

                if (fmlPlayer != null) {
                    fmlPlayer.stop();
                    fmlPlayer.release();
                    fmlPlayer = null;
                }

                stopTimerTask();
            }
        };

        mPreparedListener = new PlayerListener.OnPreparedListener() {
            @Override
            public void OnPrepared(FmlPlayer fp) {
                dismissLoadingDialog();

                Toast.makeText(context, "OnPrepared", Toast.LENGTH_LONG).show();
                if (fmlPlayer != null) {
                    name.setText(fmlPlayer.getAudioName());
                    fmlPlayer.play();
                    sb_progress.setMax((int) Math.round(fmlPlayer.getTotalTime()));
                    startTimerTask();
                }

            }
        };

        mErrorListener = new PlayerListener.OnErrorListener() {
            @Override
            public void onError(FmlPlayer fp, int errorCode) {

                dismissLoadingDialog();

                String errorStr = String.valueOf(errorCode);
                if (errorCode == -1)
                    errorStr = "01";
                int resId = context.getResources().getIdentifier("error_" + errorStr, "string",
                        context.getPackageName());
                String print = resId > 0 ? context.getString(resId) : context.getString(com.vite.audiolibrary.R.string.error_01);
                Toast.makeText(context, "onError , " + print, Toast.LENGTH_LONG).show();

                stopTimerTask();
            }
        };
    }

    private void initView() {
        url = (EditText) findViewById(R.id.url);
        playLocal = (Button) findViewById(R.id.play);
        playAsset = (Button) findViewById(R.id.playasset);
        playOnline = (Button) findViewById(R.id.playonline);
        openFile = (Button) findViewById(R.id.openfile);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);
        restart = (Button) findViewById(R.id.restart);
        name = (TextView) findViewById(R.id.name);
        localFilePath = (TextView) findViewById(R.id.filepath);
        volume = (TextView) findViewById(R.id.volume_text);
        panning = (TextView) findViewById(R.id.panning_text);
        rotate = (TextView) findViewById(R.id.rotate_text);
        tv_progress = (TextView) findViewById(R.id.progress_text);
        sb_progress = (SeekBar) findViewById(R.id.progress_seekbar);
        volume_sb = (SeekBar) findViewById(R.id.volume_seekbar);
        panning_sb = (SeekBar) findViewById(R.id.panning_seekbar);
        rotate_sb = (SeekBar) findViewById(R.id.rotate_seekbar);
        rotate_cb = (CheckBox) findViewById(R.id.rotate_checkbox);
        //
        eq100 = (TextView) findViewById(R.id.eq100_text);
        eq600 = (TextView) findViewById(R.id.eq600_text);
        eq1k = (TextView) findViewById(R.id.eq1k_text);
        eq8k = (TextView) findViewById(R.id.eq8k_text);
        eq14k = (TextView) findViewById(R.id.eq14k_text);
        eq100_sb = (SeekBar) findViewById(R.id.eq100_seekbar);
        eq600_sb = (SeekBar) findViewById(R.id.eq600_seekbar);
        eq1k_sb = (SeekBar) findViewById(R.id.eq1k_seekbar);
        eq8k_sb = (SeekBar) findViewById(R.id.eq8k_seekbar);
        eq14k_sb = (SeekBar) findViewById(R.id.eq14k_seekbar);
        eq_cb = (CheckBox) findViewById(R.id.eq_checkbox);
        //
        autowah_cb = (CheckBox) findViewById(R.id.autowah_checkbox);
        autowah_rg = (RadioGroup) findViewById(R.id.autowah_radiogroup);
        autowah_slow = (RadioButton) findViewById(R.id.autowah_rb_slow);
        //
        phaser_cb = (CheckBox) findViewById(R.id.phaser_checkbox);
        phaser_rg = (RadioGroup) findViewById(R.id.phaser_radiogroup);
        phaser_shift = (RadioButton) findViewById(R.id.phaser_rb_shift);
        //
        chorus_cb = (CheckBox) findViewById(R.id.chorus_checkbox);
        chorus_rg = (RadioGroup) findViewById(R.id.chorus_radiogroup);
        chorus_flanger = (RadioButton) findViewById(R.id.chorus_rb_flanger);
        //
        echo_cb = (CheckBox) findViewById(R.id.echo_checkbox);
        echo_rg = (RadioGroup) findViewById(R.id.echo_radiogroup);
        echo_small = (RadioButton) findViewById(R.id.echo_rb_small);
    }

    private void loadView() {
        playLocal.setOnClickListener(this);
        playAsset.setOnClickListener(this);
        playOnline.setOnClickListener(this);
        openFile.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        restart.setOnClickListener(this);
        sb_progress.setOnSeekBarChangeListener(this);
        volume_sb.setOnSeekBarChangeListener(this);
        panning_sb.setOnSeekBarChangeListener(this);
        rotate_sb.setOnSeekBarChangeListener(this);
        rotate_cb.setOnCheckedChangeListener(this);
        volume_sb.setProgress(100);
        panning_sb.setProgress(50);
        //
        eq100_sb.setOnSeekBarChangeListener(this);
        eq600_sb.setOnSeekBarChangeListener(this);
        eq1k_sb.setOnSeekBarChangeListener(this);
        eq8k_sb.setOnSeekBarChangeListener(this);
        eq14k_sb.setOnSeekBarChangeListener(this);
        eq_cb.setOnCheckedChangeListener(this);
        eq100_sb.setProgress(50);
        eq600_sb.setProgress(50);
        eq1k_sb.setProgress(50);
        eq8k_sb.setProgress(50);
        eq14k_sb.setProgress(50);
        //
        autowah_cb.setOnCheckedChangeListener(this);
        autowah_rg.setOnCheckedChangeListener(this);
        //
        phaser_cb.setOnCheckedChangeListener(this);
        phaser_rg.setOnCheckedChangeListener(this);
        //
        chorus_cb.setOnCheckedChangeListener(this);
        chorus_rg.setOnCheckedChangeListener(this);
        //
        echo_cb.setOnCheckedChangeListener(this);
        echo_rg.setOnCheckedChangeListener(this);

        url.setText("http://wl.baidu190.com/1449198945/d52fd8cd9e5bc41d8adc3a55db9b231d.mp3");
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        stopTimerTask();
        switch (v.getId()) {
            case R.id.play://play local music file
                playLocalFile();
                break;
            case R.id.playasset:
                playAssetFile();
                break;
            case R.id.playonline:
                playOnlineUrl();
                break;
            case R.id.openfile:
                openFile();
                break;
            case R.id.pause:
                stopTimerTask();
                if (fmlPlayer != null)
                    fmlPlayer.pause();
                break;
            case R.id.stop:
                stopTimerTask();
                if (fmlPlayer != null)
                    fmlPlayer.stop();
                break;
            case R.id.restart:
                startTimerTask();
                if (fmlPlayer != null && fmlPlayer.isPausing())
                    fmlPlayer.play();
                break;
        }
    }

    private void playLocalFile() {
        final String path = localFilePath.getText().toString().trim();
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(MainActivity.this, "please select music file", Toast.LENGTH_SHORT).show();
            return;
        }
        closeFX();
        new Thread() {
            @Override
            public void run() {
                if (fmlPlayer != null) {
                    fmlPlayer.stop();
                    fmlPlayer.release();
                    fmlPlayer = null;
                }

                try {
                    fmlPlayer = new FmlPlayer();
                    fmlPlayer.setExternalFile(localFilePath.getText().toString().trim());
                    fmlPlayer.setLooping(false);

                    fmlPlayer.setOnCompletionListener(mCompletionListener);
                    fmlPlayer.setOnPreparedListener(mPreparedListener);
                    fmlPlayer.setOnErrorListener(mErrorListener);

                    fmlPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    mErrorListener.onError(fmlPlayer, -1);
                }
            }
        }.start();
    }

    private void playAssetFile() {
        closeFX();
        new Thread() {
            @Override
            public void run() {
                if (fmlPlayer != null) {
                    fmlPlayer.stop();
                    fmlPlayer.release();
                    fmlPlayer = null;
                }

                fmlPlayer = new FmlPlayer();
                fmlPlayer.setAssetFile(ASSETS_FILE);
                fmlPlayer.setLooping(false);

                fmlPlayer.setOnCompletionListener(mCompletionListener);
                fmlPlayer.setOnPreparedListener(mPreparedListener);
                fmlPlayer.setOnErrorListener(mErrorListener);

                fmlPlayer.prepare();
            }
        }.start();
    }

    private void playOnlineUrl() {
        final String path = url.getText().toString().trim();
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(MainActivity.this, "please enter music url", Toast.LENGTH_SHORT).show();
            return;
        }
        closeFX();
        showLoadingDialog();
        new Thread() {
            @Override
            public void run() {
                if (fmlPlayer != null) {
                    fmlPlayer.stop();
                    fmlPlayer.release();
                    fmlPlayer = null;
                }

                try {
                    fmlPlayer = new FmlPlayer();
                    fmlPlayer.setNetFile(path);
                    fmlPlayer.setLooping(false);

                    fmlPlayer.setOnCompletionListener(mCompletionListener);
                    fmlPlayer.setOnPreparedListener(mPreparedListener);
                    fmlPlayer.setOnErrorListener(mErrorListener);

                    fmlPlayer.prepareSync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void openFile() {
        Intent intent = new Intent(this, FileActivity.class);
        intent.putExtra("musicpath", localFilePath.getText().toString().trim());
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                localFilePath.setText(data.getStringExtra("path"));
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    private void showLoadingDialog() {
        dismissLoadingDialog();
        loadingDialog = ProgressDialog.show(this, null, "waiting", false, false);
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    private void startTimerTask() {
        if (timer != null && timerTask != null) {
            timerTask.cancel();
        }

        if (timer == null)
            timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (fmlPlayer != null) {
//                    if (fmlPlayer.isStoping()) {
//                        timerTask.cancel();
//                        return;
//                    }

                    double cTime = fmlPlayer.getCurrentPosition();
                    double aTime = fmlPlayer.getTotalTime();
                    StringBuilder sb = new StringBuilder();
                    int cMin = (int) cTime / 60;//don't use Math.round()
                    int cSec = (int) cTime % 60;
                    int aMin = (int) aTime / 60;
                    int aSec = (int) aTime % 60;
                    sb.append(cMin).append(":").append(cSec < 10 ? "0" + cSec : cSec).append("/");
                    sb.append(aMin).append(":").append(aSec);

                    int buf = (int) (fmlPlayer.getBufferPercentage() / 100 * aTime);
                    mMianHandler.obtainMessage(HANDLER_MSG_UPDATE_PROGRESS, (int) cTime, buf, sb.toString()).sendToTarget();
                }
            }
        };

        timer.schedule(timerTask, 0, 1000);
    }

    private void stopTimerTask() {
        if (timer != null && timerTask != null) {
            timerTask.cancel();
        }
    }

    private void closeFX() {
        rotate_cb.setChecked(false);
        rotate_sb.setProgress(0);

        eq_cb.setChecked(false);
        eq100_sb.setProgress(0);
        eq600_sb.setProgress(0);
        eq1k_sb.setProgress(0);
        eq8k_sb.setProgress(0);
        eq14k_sb.setProgress(0);

        autowah_cb.setChecked(false);
        autowah_rg.clearCheck();
        autowah_rg.setEnabled(false);

        phaser_cb.setChecked(false);
        phaser_rg.clearCheck();
        phaser_rg.setEnabled(false);

        chorus_cb.setChecked(false);
        chorus_rg.clearCheck();
        chorus_rg.setEnabled(false);

        echo_cb.setChecked(false);
        echo_rg.clearCheck();
        echo_rg.setEnabled(false);

        if (fxController != null) {
            fxController.release();
            fxController = new FmlPlayer.FxController();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        switch (buttonView.getId()) {
            case R.id.rotate_checkbox:
                if (!isChecked)
                    fxController.resetRotate();
                break;
            case R.id.eq_checkbox:
                if (!isChecked)
                    fxController.resetPeakEQ();

                //fxController.resetPeakEQ_100();
                //fxController.resetPeakEQ_600();
                //fxController.resetPeakEQ_1k();
                //fxController.resetPeakEQ_8k();
                //fxController.resetPeakEQ_14k();
                break;
            case R.id.autowah_checkbox:
                if (isChecked) {
                    autowah_rg.setEnabled(true);
                    autowah_slow.setChecked(true);
                } else {
                    fxController.resetAutoWah();

                    autowah_rg.clearCheck();
                    autowah_rg.setEnabled(false);
                }
                break;
            case R.id.phaser_checkbox:
                if (isChecked) {
                    phaser_rg.setEnabled(true);
                    phaser_shift.setChecked(true);
                } else {
                    fxController.resetPhaser();

                    phaser_rg.setEnabled(false);
                    phaser_rg.clearCheck();
                }
                break;
            case R.id.chorus_checkbox:
                if (isChecked) {
                    chorus_rg.setEnabled(true);
                    chorus_flanger.setChecked(true);
                } else {
                    fxController.resetChorus();

                    chorus_rg.setEnabled(false);
                    chorus_rg.clearCheck();
                }
                break;
            case R.id.echo_checkbox:
                if (isChecked) {
                    echo_rg.setEnabled(true);
                    echo_small.setChecked(true);
                } else {
                    fxController.resetEcho();

                    echo_rg.setEnabled(false);
                    echo_rg.clearCheck();
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (group.getId()) {
            case R.id.autowah_radiogroup:
                if (!autowah_cb.isChecked()) {
                    return;
                }
                switch (checkedId) {
                    case R.id.autowah_rb_slow:
                        fxController.setAutoWah(FmlPlayer.FxController.AUTOWAH_SLOW);
                        break;
                    case R.id.autowah_rb_fast:
                        fxController.setAutoWah(FmlPlayer.FxController.AUTOWAH_FAST);
                        break;
                    case R.id.autowah_rb_hifast:
                        fxController.setAutoWah(FmlPlayer.FxController.AUTOWAH_HIFAST);
                        break;
                }
                break;
            case R.id.phaser_radiogroup:
                if (!phaser_cb.isChecked()) {
                    return;
                }
                switch (checkedId) {
                    case R.id.phaser_rb_shift:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_SHIFT);
                        break;
                    case R.id.phaser_rb_slowshift:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_SLOWSHIFT);
                        break;
                    case R.id.phaser_rb_basic:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_BASIC);
                        break;
                    case R.id.phaser_rb_med:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_MED);
                        break;
                    case R.id.phaser_rb_fast:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_FAST);
                        break;
                    case R.id.phaser_rb_wfb:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_WFB);
                        break;
                    case R.id.phaser_rb_invert:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_INVERT);
                        break;
                    case R.id.phaser_rb_tremolo:
                        fxController.setPhaser(FmlPlayer.FxController.PHASER_TREMOLO);
                        break;
                }
                break;
            case R.id.chorus_radiogroup:
                if (!chorus_cb.isChecked()) {
                    return;
                }
                switch (checkedId) {
                    case R.id.chorus_rb_flanger:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_FLANGER);
                        break;
                    case R.id.chorus_rb_exaggeration:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_EXAGGERATION);
                        break;
                    case R.id.chorus_rb_motocycle:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_MOTOCYCLE);
                        break;
                    case R.id.chorus_rb_devil:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_DEVIL);
                        break;
                    case R.id.chorus_rb_manyvoice:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_MANYVOICE);
                        break;
                    case R.id.chorus_rb_chipmunk:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_CHIPMUNK);
                        break;
                    case R.id.chorus_rb_water:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_WATER);
                        break;
                    case R.id.chorus_rb_airplane:
                        fxController.setChorus(FmlPlayer.FxController.CHORUS_AIRPLANE);
                        break;
                }
                break;
            case R.id.echo_radiogroup:
                if (!echo_cb.isChecked()) {
                    return;
                }
                switch (checkedId) {
                    case R.id.echo_rb_small:
                        fxController.setEcho(FmlPlayer.FxController.ECHO_SMALL);
                        break;
                    case R.id.echo_rb_many:
                        fxController.setEcho(FmlPlayer.FxController.ECHO_MANY);
                        break;
                    case R.id.echo_rb_reverse:
                        fxController.setEcho(FmlPlayer.FxController.ECHO_REVERSE);
                        break;
                    case R.id.echo_rb_robotic:
                        fxController.setEcho(FmlPlayer.FxController.ECHO_ROBOTIC);
                        break;
                }
                break;
        }
        if (fmlPlayer != null)
            fxController.update(fmlPlayer);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        if (fmlPlayer == null)
            return;

        if (seekBar.getId() == R.id.progress_seekbar) {
            if (fromUser) {
                fmlPlayer.seekTo(progress);
            }
        } else {
            if (!fromUser)
                return;
        }

        boolean isChange = false;
        switch (seekBar.getId()) {
            case R.id.volume_seekbar:
                fmlPlayer.setVolume(progress / 100f);
                volume.setText(String.valueOf(fmlPlayer.getVolume()));
                break;
            case R.id.panning_seekbar:
                float pan = progress * 0.02f - 1f;
                fmlPlayer.setPanning(pan);
                panning.setText(String.valueOf(fmlPlayer.getPanning()));
                break;
            case R.id.rotate_seekbar:
                if (rotate_cb.isChecked()) {
                    fxController.setRotate(progress * 0.01f);
                    isChange = true;
                }
                break;
            case R.id.eq100_seekbar:
                if (eq_cb.isChecked()) {
                    fxController.setPeakEQ_100(progress * 0.3f - 15f);
                    isChange = true;
                }
                break;
            case R.id.eq600_seekbar:
                if (eq_cb.isChecked()) {
                    fxController.setPeakEQ_600(progress * 0.3f - 15f);
                    isChange = true;
                }
                break;
            case R.id.eq1k_seekbar:
                if (eq_cb.isChecked()) {
                    fxController.setPeakEQ_1k(progress * 0.3f - 15f);
                    isChange = true;
                }
                break;
            case R.id.eq8k_seekbar:
                if (eq_cb.isChecked()) {
                    fxController.setPeakEQ_8k(progress * 0.3f - 15f);
                    isChange = true;
                }
                break;
            case R.id.eq14k_seekbar:
                if (eq_cb.isChecked()) {
                    fxController.setPeakEQ_14k(progress * 0.3f - 15f);
                    isChange = true;
                }
                break;
        }

        if (isChange) {
            fxController.update(fmlPlayer);
            rotate.setText(String.valueOf(fxController.getRotate()));
            eq100.setText(String.valueOf(fxController.getGain4PeakEQ_100()));
            eq600.setText(String.valueOf(fxController.getGain4PeakEQ_600()));
            eq1k.setText(String.valueOf(fxController.getGain4PeakEQ_1k()));
            eq8k.setText(String.valueOf(fxController.getGain4PeakEQ_8k()));
            eq14k.setText(String.valueOf(fxController.getGain4PeakEQ_14k()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        stopTimerTask();
        fxController.release();
        if (fmlPlayer != null) {
            fmlPlayer.release();
        }
        FmlPlayer.free();
        super.onDestroy();
    }

    private class MainHandler extends Handler {
        private final WeakReference<Context> mRef;

        public MainHandler(Context context) {
            mRef = new WeakReference<Context>(context.getApplicationContext());
        }

        @Override
        public void handleMessage(Message msg) {
            if (mRef == null)
                return;

            Context context = mRef.get();
            if (context == null)
                return;

            switch (msg.what) {
                case HANDLER_MSG_UPDATE_PROGRESS:
                    tv_progress.setText(msg.obj.toString());
                    sb_progress.setProgress(msg.arg1);
                    sb_progress.setSecondaryProgress(msg.arg2);
                    break;
            }
        }
    }
}
