package com.example.testaudio;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.omesoft.audiolibrary.OMEPlayer;

public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener,
		OnSeekBarChangeListener, android.widget.RadioGroup.OnCheckedChangeListener {
	Context context;
	OMEPlayer omePlayer;

	Button play, pause, stop;
	TextView name, volume, panning, rotate, eq100, eq600, eq1k, eq8k, eq14k;
	SeekBar volume_sb, panning_sb, rotate_sb, eq100_sb, eq600_sb, eq1k_sb, eq8k_sb, eq14k_sb;
	CheckBox rotate_cb, eq_cb, autowah_cb, phaser_cb, chorus_cb, echo_cb;
	RadioGroup autowah_rg, phaser_rg, chorus_rg, echo_rg;
	RadioButton autowah_slow, phaser_shift, chorus_flanger, echo_small;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		init();
		initView();
		loadView();
		initHandle();
	}

	private void init() {
		OMEPlayer.initAudioEngine(getApplicationContext());
		OMEPlayer.overallSetVolume(1);
//		String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + "Ï²»¶Äã.mp3";
//		omePlayer = new OMEPlayer();
//		omePlayer.setDataSource(filePath);
		 omePlayer = OMEPlayer.Create("sound4.ogg");
		omePlayer.setLooping(true);
		omePlayer.openFX();
	}

	private void initView() {
		play = (Button) findViewById(R.id.play);
		pause = (Button) findViewById(R.id.pause);
		stop = (Button) findViewById(R.id.stop);
		name = (TextView) findViewById(R.id.name);
		volume = (TextView) findViewById(R.id.volume_text);
		panning = (TextView) findViewById(R.id.panning_text);
		rotate = (TextView) findViewById(R.id.rotate_text);
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
		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		stop.setOnClickListener(this);
		volume_sb.setOnSeekBarChangeListener(this);
		panning_sb.setOnSeekBarChangeListener(this);
		rotate_sb.setOnSeekBarChangeListener(this);
		rotate_cb.setOnCheckedChangeListener(this);
		name.setText(omePlayer.getAudioName());
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
	}

	private void initHandle() {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.play:
			omePlayer.Play();
			break;
		case R.id.pause:
			omePlayer.Pause();
			break;
		case R.id.stop:
			omePlayer.Stop();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.rotate_checkbox:
			if (isChecked) {
				omePlayer.openRotate();
				rotate.setText(String.valueOf(omePlayer.getRotate()));
			}
			else
				omePlayer.closeRotate();
			break;
		case R.id.eq_checkbox:
			if (isChecked)
				omePlayer.openEQ();
			else
				omePlayer.closeEQ();
			break;
		case R.id.autowah_checkbox:
			if (isChecked) {
				omePlayer.openAUTOWAH();
				autowah_rg.setEnabled(true);
				autowah_slow.setChecked(true);
			}
			else {
				omePlayer.closeAUTOWAH();
				autowah_rg.clearCheck();
				autowah_rg.setEnabled(false);
			}
			break;
		case R.id.phaser_checkbox:
			if (isChecked) {
				omePlayer.openPhaser();
				phaser_rg.setEnabled(true);
				phaser_shift.setChecked(true);
			}
			else {
				omePlayer.closePhaser();
				phaser_rg.setEnabled(false);
				phaser_rg.clearCheck();
			}
			break;
		case R.id.chorus_checkbox:
			if (isChecked) {
				omePlayer.openChorus();
				chorus_rg.setEnabled(true);
				chorus_flanger.setChecked(true);
			}
			else {
				omePlayer.closeChorus();
				chorus_rg.setEnabled(false);
				chorus_rg.clearCheck();
			}
			break;
		case R.id.echo_checkbox:
			if (isChecked) {
				omePlayer.openEcho();
				echo_small.setChecked(true);
			}
			else {
				omePlayer.closeEcho();
				echo_rg.clearCheck();
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		float[] option;
		String options;
		switch (group.getId()) {
		case R.id.autowah_radiogroup:
			switch (checkedId) {
			case R.id.autowah_rb_slow:
				omePlayer.setAUTOWAH(OMEPlayer.AUTOWAH_SLOW);
				break;
			case R.id.autowah_rb_fast:
				omePlayer.setAUTOWAH(OMEPlayer.AUTOWAH_FAST);
				break;
			case R.id.autowah_rb_hifast:
				omePlayer.setAUTOWAH(OMEPlayer.AUTOWAH_HIFAST);
				break;
			}
			option = omePlayer.getAUTOWAH();
			if (option != null) {
				options = option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4]
						+ "," + option[5];
				Toast.makeText(context, options, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.phaser_radiogroup:
			switch (checkedId) {
			case R.id.phaser_rb_shift:
				omePlayer.setPhaser(OMEPlayer.PHASER_SHIFT);
				break;
			case R.id.phaser_rb_slowshift:
				omePlayer.setPhaser(OMEPlayer.PHASER_SLOWSHIFT);
				break;
			case R.id.phaser_rb_basic:
				omePlayer.setPhaser(OMEPlayer.PHASER_BASIC);
				break;
			case R.id.phaser_rb_med:
				omePlayer.setPhaser(OMEPlayer.PHASER_MED);
				break;
			case R.id.phaser_rb_fast:
				omePlayer.setPhaser(OMEPlayer.PHASER_FAST);
				break;
			case R.id.phaser_rb_wfb:
				omePlayer.setPhaser(OMEPlayer.PHASER_WFB);
				break;
			case R.id.phaser_rb_invert:
				omePlayer.setPhaser(OMEPlayer.PHASER_INVERT);
				break;
			case R.id.phaser_rb_tremolo:
				omePlayer.setPhaser(OMEPlayer.PHASER_TREMOLO);
				break;
			}
			option = omePlayer.getPhaser();
			if (option != null) {
				options = option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4]
						+ "," + option[5];
				Toast.makeText(context, options, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.chorus_radiogroup:
			switch (checkedId) {
			case R.id.chorus_rb_flanger:
				omePlayer.setChorus(OMEPlayer.CHORUS_FLANGER);
				break;
			case R.id.chorus_rb_exaggeration:
				omePlayer.setChorus(OMEPlayer.CHORUS_EXAGGERATION);
				break;
			case R.id.chorus_rb_motocycle:
				omePlayer.setChorus(OMEPlayer.CHORUS_MOTOCYCLE);
				break;
			case R.id.chorus_rb_devil:
				omePlayer.setChorus(OMEPlayer.CHORUS_DEVIL);
				break;
			case R.id.chorus_rb_manyvoice:
				omePlayer.setChorus(OMEPlayer.CHORUS_MANYVOICE);
				break;
			case R.id.chorus_rb_chipmunk:
				omePlayer.setChorus(OMEPlayer.CHORUS_CHIPMUNK);
				break;
			case R.id.chorus_rb_water:
				omePlayer.setChorus(OMEPlayer.CHORUS_WATER);
				break;
			case R.id.chorus_rb_airplane:
				omePlayer.setChorus(OMEPlayer.CHORUS_AIRPLANE);
				break;
			}
			option = omePlayer.getChorus();
			if (option != null) {
				options = option[0] + "," + option[1] + "," + option[2] + "," + option[3] + "," + option[4]
						+ "," + option[5];
				Toast.makeText(context, options, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.echo_radiogroup:
			switch (checkedId) {
			case R.id.echo_rb_small:
				omePlayer.setEcho(OMEPlayer.ECHO_SMALL);
				break;
			case R.id.echo_rb_many:
				omePlayer.setEcho(OMEPlayer.ECHO_MANY);
				break;
			case R.id.echo_rb_reverse:
				omePlayer.setEcho(OMEPlayer.ECHO_REVERSE);
				break;
			case R.id.echo_rb_robotic:
				omePlayer.setEcho(OMEPlayer.ECHO_ROBOTIC);
				break;
			}
			option = omePlayer.getEcho();
			if (option != null) {
				options = option[0] + "," + option[1] + "," + option[2] + "," + option[3];
				Toast.makeText(context, options, Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		case R.id.volume_seekbar:
			omePlayer.setVolume(progress / 100f);
			volume.setText(String.valueOf(omePlayer.getVolume()));
			break;
		case R.id.panning_seekbar:
			float pan = progress * 0.02f - 1f;
			omePlayer.setPanning(pan);
			panning.setText(String.valueOf(omePlayer.getPanning()));
			break;
		case R.id.rotate_seekbar:
			if (rotate_cb.isChecked()) {
				omePlayer.setRotate(progress * 0.01f);
				rotate.setText(String.valueOf(omePlayer.getRotate()));
			}
			break;
		case R.id.eq100_seekbar:
			omePlayer.setEQ100(progress * 0.3f - 15f);
			eq100.setText(String.valueOf(omePlayer.getEQ100()));
			break;
		case R.id.eq600_seekbar:
			omePlayer.setEQ600(progress * 0.3f - 15f);
			eq600.setText(String.valueOf(omePlayer.getEQ600()));
			break;
		case R.id.eq1k_seekbar:
			omePlayer.setEQ1k(progress * 0.3f - 15f);
			eq1k.setText(String.valueOf(omePlayer.getEQ1k()));
			break;
		case R.id.eq8k_seekbar:
			omePlayer.setEQ8k(progress * 0.3f - 15f);
			eq8k.setText(String.valueOf(omePlayer.getEQ8k()));
			break;
		case R.id.eq14k_seekbar:
			omePlayer.setEQ14k(progress * 0.3f - 15f);
			eq14k.setText(String.valueOf(omePlayer.getEQ14k()));
			break;
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
		omePlayer.closeFX();
		omePlayer.release();
		OMEPlayer.releaseAudioEngine();
		super.onDestroy();
	}
}
