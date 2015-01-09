package com.omesoft.audiolibrary;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS.BASS_CHANNELINFO;
import com.un4seen.bass.BASS_FX;
import com.un4seen.bass.BASS_FX.BASS_BFX_AUTOWAH;
import com.un4seen.bass.BASS_FX.BASS_BFX_CHORUS;
import com.un4seen.bass.BASS_FX.BASS_BFX_DAMP;
import com.un4seen.bass.BASS_FX.BASS_BFX_DISTORTION;
import com.un4seen.bass.BASS_FX.BASS_BFX_ECHO2;
import com.un4seen.bass.BASS_FX.BASS_BFX_ECHO4;
import com.un4seen.bass.BASS_FX.BASS_BFX_PEAKEQ;
import com.un4seen.bass.BASS_FX.BASS_BFX_PHASER;
import com.un4seen.bass.BASS_FX.BASS_BFX_ROTATE;

/*------------------------------------------*/
/*
 * BASS���еĻ������ǻ���Channel(ͨ��)������Ƶ�ģ�����롢����
 * һ����ƵҪ�ȼ�����ͨ�����ܽ��д���
 * ÿһ����Ƶ��ͨ���ı�ʶΪһ��int���͵�Handle(���)
 * ͨ��Handle���Ի�ȡ�������㹻��Ķ���
 * ���⣬sample(����)�ľ����Channel�ľ����һ��
 * Ҫ��sample�ľ������Channel�в���
 * 
 * BASS���е�һЩ�����ǻ���רҵ��Ƶ�Ĳ������е�
 * ������˽�һ����Ƶ���������panning
 * 
 * ��ǰ���Ӳ��������£�ֻ֧��ogg��mp1��mp2��mp3��wav
 */
/*------------------------------------------*/

/**
 * ����BASS Library<br>
 * gbk
 * 
 * @author WingSun 'ViTe' Tam
 * 
 */
public class OMEPlayer {

	private static final String TAG = "OMEPlayer";
	/**
	 * Ĭ���豸<br>
	 * <li>
	 * -1 = Ĭ���豸<br><li>
	 * 0 = û������<br><li>
	 * 1 = ��һ������������豸
	 */
	private static final int device = -1;
	/**
	 * ���������
	 */
	private static final int freq = 44100;
	/**
	 * �豸��־��ͨ����־����ʵ�ֲ�ͬ�Ĺ��� <br>
	 * 0Ӧ��ΪĬ��16bit ��־Ϊ��<br>
	 * <li><b>BASS_DEVICE_8BITS</b> ��ʹ��8λ�ֱ��ʣ�����Ĭ��16λ <br> <li>
	 * <b>BASS_DEVICE_MONO</b> ��ʹ�õ�����������Ϊ������ <br> <li><b>BASS_DEVICE_3D</b>
	 * ��ʹ��3d��Ч���� <br> <li><b>BASS_DEVICE_LATENCY</b>
	 * ���������api��˵��Ϊ�����ӳ٣�������������������������ֵļ��ʱ�� <br> <li><b>BASS_DEVICE_SPEAKERS</b>
	 * �����������ǿ��ʹ�������� <br> <li><b>BASS_DEVICE_NOSPEAKER</b> ��������������������� <br> <li>
	 * <b>BASS_DEVICE_FREQ</b> �������豸�Ĳ�����<br>
	 */
	private static final int flags = 0;

	/*--------------ĳЩ����--------------*/
	private static Context context;
	/**
	 * ��¼�Ƿ��Ѿ���ʼ����
	 */
	private static boolean isInitAudioEngine = false;

	/*-----------------------------------*/

	/*-----------------��̬��ȫ�֣�����------------------*/

	/**
	 * ��ʼ�����ֲ��ſ�(������¼��)
	 * 
	 * @param context
	 *            getApplicationContext
	 * @return true ����ʼ���ɹ����ѱ���ʼ����
	 */
	public static boolean initAudioEngine(Context context) {
		OMEPlayer.context = context;
		// ��ֻ֤��ʼ��һ��
		if (!isInitAudioEngine) {
			isInitAudioEngine = BASS.BASS_Init(device, freq, flags);
			printError("initAudioEngine");
		}
		return isInitAudioEngine;
	}

	/**
	 * �ͷ����ֲ��ſ�
	 * 
	 * @return
	 */
	public static boolean releaseAudioEngine() {
		boolean state = false;
		if (isInitAudioEngine)
			state = BASS.BASS_Free();
		printError("releaseAudioEngine");
		if (state)
			isInitAudioEngine = false;
		return state;
	}

	/**
	 * ȫ����Ƶ��ʼ���ţ���ʹ��overallPause֮��<br>
	 * ʹ��overallStop�����ٵ��ô˷���<br>
	 * ����ؽ���ʹ��
	 * 
	 * @return
	 */
	public static boolean overallStart() {
		BASS.BASS_Start();
		return printError("overallStart");
	}

	/**
	 * ������Ƶ��ͣ���
	 * 
	 * @return
	 */
	public static boolean overallPause() {
		BASS.BASS_Pause();
		return printError("overallPause");
	}

	/**
	 * ������Ƶֹͣ���<br>
	 * ֹͣ���޷��ٲ��ţ������������
	 * 
	 * @return
	 */
	public static boolean overallStop() {
		BASS.BASS_Stop();
		return printError("overallStop");
	}

	/**
	 * ����ȫ�����������ڿ��ܻ�Ӱ�쵽����Channel�ĵ���������������ʹ��<br>
	 * �������Ĳ�����������(progress/100)������<br>
	 * ��ĳ�(progress/100.0f)
	 * 
	 * @param volume
	 * @return
	 */
	public static boolean overallSetVolume(float volume) {
		BASS.BASS_SetVolume(volume);
		return printError("overallSetVolume");
	}

	/**
	 * ��ȡ��ǰȫ������
	 * 
	 * @return
	 */
	public static float overallGetVolume() {
		float volume = BASS.BASS_GetVolume();
		printError("overallGetVolume");
		return volume;
	}

	/**
	 * ������Ƶ���һЩѡ������κ�ʱ�����<br>
	 * ����û�����Init�����<br>
	 * һЩ������صľͲ�д������
	 * 
	 * @param option
	 *            <li><b>BASS_CONFIG_BUFFER</b> <br>
	 *            ���û�������СĬ��Ϊ500ms��ֻӰ��stream��music��channel�����ֵΪ5000ms <br>
	 *            <br> <li><b>BASS_CONFIG_UPDATEPERIOD</b> <br>
	 *            ���û������ĸ������ڣ�ֻӰ��stream��music��channel��0Ϊ�����Զ����£���С��������Ϊ5ms��
	 *            ���Ϊ100ms <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_SAMPLE</b> <br>
	 *            ��������������ȫ����������0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_STREAM</b> <br>
	 *            ����������Ƶ����ȫ����������0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_MUSIC</b> <br>
	 *            ��������MOD��Ƶ��ȫ����������0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_CURVE_VOL</b> <br>
	 *            ��������������ģʽ�����Ի�������ߣ�������Ϊ0%-100%������Ϊ-100db-0dp <br>
	 *            false = ���ԣ�true = ���� <br>
	 *            <br> <li><b>BASS_CONFIG_CURVE_PAN</b> <br>
	 *            ��������ƽ�Ƶ�����ģʽ�����Ի�������ߣ� <br>
	 *            �����ƽ�ƿ�����ָ��Դ��ƽ�ƣ�������ң� <br>
	 *            <br> <li><b>BASS_CONFIG_FLOATDSP</b> <br>
	 *            �����Ƿ�ʹ��(��Android��Ϊ24λ)��������DSP <br>
	 *            api��˵ʹ�ø�������ʹ��Ƶ����DSP�󲻻ή������ <br>
	 *            ������������������Ӧ�ñ���ʹ�� <br>
	 *            <br> <li><b>BASS_CONFIG_3DALGORITHM</b> <br>
	 *            ����3D��Ч���㷨�����ĸ�ѡ� <br>
	 *            BASS_3DALG_DEFAULT <br>
	 *            BASS_3DALG_OFF <br>
	 *            BASS_3DALG_FULL <br>
	 *            BASS_3DALG_LIGHT <br>
	 *            <br> <li><b>BASS_CONFIG_PAUSE_NOPLAY</b> <br>
	 *            ��ʹ��overallPauseһ����˼ <br>
	 *            <br> <li><b>BASS_CONFIG_REC_BUFFER</b> <br>
	 *            ����¼��ͨ���Ļ��棬1000-5000(ms) <br>
	 *            <br> <li><b>BASS_CONFIG_MUSIC_VIRTUAL</b> <br>
	 *            ����IT��ʽ��MOD���ֵ�����ͨ���� <br>
	 *            <br> <li><b>BASS_CONFIG_VERIFY</b> <br>
	 *            �����ļ���ʽ����֤���ȣ�Ӧ���Ǹ��ݳ��Ȼ�ȡһ�����ȵ���֤��Ϣ����֤�Ƿ��������ʽ <br>
	 *            <br> <li><b>BASS_CONFIG_UPDATETHREADS</b> <br>
	 *            ���²���ͨ���Ļ��������߳� <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_BUFFER</b> <br>
	 *            �������в���ͨ��һ�𲥷�ʱ�������ղ���ǰ�����һ����ʹ�õĻ��棬����׼������ <br>
	 *            �����������û�Ӱ�첥�ź���ͣ���ӳ�ʱ�䣬����̫�ߣ���̫��Ҳ�ᵼ������ж� <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_DEFAULT</b> <br>
	 *            ������� <br>
	 *            <br> <li><b>BASS_CONFIG_SRC</b> <br>
	 *            ����Ĭ�ϲ����ʵ�ת������ <br>
	 *            0 =���Բ�ֵ��1 = 8��SINC��ֵ��2 = 16��SINC��ֵ��3 = 32��SINC��ֵ�� <br>
	 *            <br> <li><b>BASS_CONFIG_ASYNCFILE_BUFFER</b> <br>
	 *            �첽��ȡ�ļ��Ļ��������ȡ� <br>
	 *            <br> <li><b>BASS_CONFIG_OGG_PRESCAN</b> <br>
	 *            Ԥɨ��OGG�ļ���Ĭ�Ͽ��� <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_NONSTOP</b> <br>
	 *            ��֪����Android�����ģ�API��ȫû��
	 * @param value
	 * @return
	 */
	public static boolean setOption(int option, int value) {
		BASS.BASS_SetConfig(option, value);
		return printError("setOption");
	}

	/**
	 * �������õ�ֵ
	 * 
	 * @param option
	 *            <li><b>BASS_CONFIG_HANDLES</b> <br>
	 *            ���ֻ���ڸ÷�������ȡ��ǰ����� <br>
	 *            <br>
	 * @return
	 */
	public static int getOptions(int option) {
		int value = BASS.BASS_GetConfig(option);
		printError("getOptions");
		return value;
	}

	/**
	 * ��Log��ӡ���󣬷��ش������
	 * 
	 * @param func
	 *            ���ڵķ���
	 * @return
	 */
	public static boolean printError(String func) {
		int error = BASS.BASS_ErrorGetCode();
		String errorStr = String.valueOf(error);
		if (error == -1)
			errorStr = "01";
		int resId = context.getResources().getIdentifier("error_" + errorStr, "string",
				context.getPackageName());
		String print = resId > 0 ? context.getString(resId) : context.getString(R.string.error_01);
		Log.d(TAG, func + "::" + print);
		return (error == 0);
	}

	/*-----------------���󣨾ֲ�������------------------*/
	/*-------------�ֲ���������------------*/
	private static final String FX_ROTATE = "rotate";
	private static final String FX_EQ_100 = "eq100";
	private static final String FX_EQ_600 = "eq600";
	private static final String FX_EQ_1k = "eq1k";
	private static final String FX_EQ_8k = "eq8k";
	private static final String FX_EQ_14k = "eq14k";
	private static final String FX_AUTOWAH = "autowah";
	private static final String FX_PHASER = "phaser";
	private static final String FX_CHORUS = "chorus";
	private static final String FX_ECHO = "echo";

	public static final byte AUTOWAH_SLOW = 0x01;
	public static final byte AUTOWAH_FAST = 0x02;
	public static final byte AUTOWAH_HIFAST = 0x03;

	public static final byte PHASER_SHIFT = AUTOWAH_SLOW;
	public static final byte PHASER_SLOWSHIFT = AUTOWAH_FAST;
	public static final byte PHASER_BASIC = AUTOWAH_HIFAST;
	public static final byte PHASER_WFB = 0x04;
	public static final byte PHASER_MED = 0x05;
	public static final byte PHASER_FAST = 0x06;
	public static final byte PHASER_INVERT = 0x07;
	public static final byte PHASER_TREMOLO = 0x08;

	public static final byte CHORUS_FLANGER = AUTOWAH_SLOW;
	public static final byte CHORUS_EXAGGERATION = AUTOWAH_FAST;
	public static final byte CHORUS_MOTOCYCLE = AUTOWAH_HIFAST;
	public static final byte CHORUS_DEVIL = PHASER_WFB;
	public static final byte CHORUS_MANYVOICE = PHASER_MED;
	public static final byte CHORUS_CHIPMUNK = PHASER_FAST;
	public static final byte CHORUS_WATER = PHASER_INVERT;
	public static final byte CHORUS_AIRPLANE = PHASER_TREMOLO;

	public static final byte ECHO_SMALL = AUTOWAH_SLOW;
	public static final byte ECHO_MANY = AUTOWAH_FAST;
	public static final byte ECHO_REVERSE = AUTOWAH_HIFAST;
	public static final byte ECHO_ROBOTIC = PHASER_WFB;
	/*-----------------------------------*/

	/*-------------�ֲ���������------------*/
	/**
	 * ÿ�������Ӧһ�����(��Ƶ)
	 */
	private int mHandle;
	/**
	 * ÿ���������Ƶ�Ĳ���
	 */
	private BASS_CHANNELINFO mInfo;
	/**
	 * ÿ���������ЧFX���<br>
	 * ��һ��ConcurrentHashMapά��<br>
	 * �����̰߳�ȫ
	 */
	private ConcurrentHashMap<String, Integer> mFX_Handle;
	/**
	 * rotate��Ч����
	 */
	private BASS_BFX_ROTATE mParam_rotate;
	/**
	 * EQ��Ч����
	 */
	private BASS_BFX_PEAKEQ mParam_eq;
	/**
	 * �Զ�������Ч����
	 */
	private BASS_BFX_AUTOWAH mParam_autowah;
	/**
	 * ������Ч����
	 */
	private BASS_BFX_PHASER mParam_phaser;
	/**
	 * �ϳ���Ч����(����flanger)
	 */
	private BASS_BFX_CHORUS mParam_chorus;
	/**
	 * ������Ч
	 */
	private BASS_BFX_ECHO2 mParam_echo;

	/*-----------------------------------*/

	/**
	 * ��������
	 */
	public OMEPlayer() {
		mInfo = new BASS_CHANNELINFO();
		BASS.BASS_StreamFree(mHandle);
		printError("OMEPlayer()");
		mHandle = 0;
	}

	/**
	 * ��ȡ��Ƶ����Ϣ
	 */
	private void getInfo() {
		BASS.BASS_ChannelGetInfo(mHandle, mInfo);
		printError("getInfo");
	}

	/**
	 * ���ö���ľ��ֵ�������Ҫ����
	 * 
	 * @param handle
	 */
	private void setHandle(int handle) {
		mHandle = handle;
	}

	/**
	 * ���ض���ľ��<br>
	 * ���������Ƶʧ�ܣ����Ϊ0
	 * 
	 * @return
	 */
	public int getHandle() {
		return mHandle;
	}

	public static OMEPlayer Create(String fileName) {
		return Create(fileName, 0);
	}

	public static OMEPlayer Create(String fileName, int offSet) {
		return Create(fileName, offSet, 0);
	}

	public static OMEPlayer Create(String fileName, int offSet, int length) {
		return Create(fileName, offSet, length, 0);
	}

	/**
	 * ���������ļ�(assets)
	 * 
	 * @param fileName
	 * @param offSet
	 *            ƫ��������ʲô�ط���ʼ���ţ�0���ļ�ͷ��ʼ
	 * @param length
	 *            ���ų��ȣ�0Ϊ���ŵ��ļ�β
	 * @param flag
	 *            <li><b>BASS_SAMPLE_FLOAT</b> <br>
	 *            ʹ��32λ������������<br>
	 *            <br> <li><b>BASS_SAMPLE_MONO</b> <br>
	 *            �õ���������(ֻ����MP1��MP2��MP3)������CPU��ʹ��<br>
	 *            <br><li><b>BASS_SAMPLE_SOFTWARE</b> <br>
	 *            ʹ����������Ƶ����ʹ��Ӳ��<br>
	 *            <br><li><b>BASS_SAMPLE_3D</b> <br>
	 *            ʹ��3D����<br>
	 *            <br><li><b>BASS_SAMPLE_LOOP</b> <br>
	 *            ѭ����������������Ҳ��<br>
	 *            <br><li><b>BASS_SAMPLE_FX</b> <br>
	 *            ���Ҫʹ��FX�Ĺ��ܾ������flag<br>
	 *            <br><li><b>BASS_STREAM_PRESCAN</b> <br>
	 *            Ԥ��ɨ��MP1��MP2��MP3����״OGG�ļ����������ʱ��<br>
	 *            <br><li><b>BASS_STREAM_AUTOFREE</b> <br>
	 *            ������Ϻ���Զ���ո���Ƶ�������ߵ���stop����ִ��<br>
	 *            <br><li><b>BASS_STREAM_DECODE</b> <br>
	 *            ����һ������ͨ�����������ڲ���<br>
	 *            <br><li><b>BASS_SPEAKER_xxx</b> <br>
	 *            ��������һЩ���ã�������<br>
	 *            <br><li><b>BASS_ASYNCFILE</b> <br>
	 *            �첽��ȡ�ļ���������ʱ�����ļ���ȡ�ͻ��岢�н���<br>
	 * @return
	 */
	public static OMEPlayer Create(String fileName, int offSet, int length, int flag) {
		OMEPlayer op = new OMEPlayer();
		int handle = BASS.BASS_StreamCreateFile(new BASS.Asset(context.getAssets(), fileName), offSet,
				length, flag);
		printError("Create(" + fileName + "," + offSet + "," + length + "," + flag + ")");
		if (handle == 0)
			op.release();
		op.setHandle(handle);
		op.getInfo();
		return op;
	}

	public void setDataSource(String filePath) {
		setDataSource(filePath, 0);
	}

	public void setDataSource(String filePath, int offSet) {
		setDataSource(filePath, offSet, 0);
	}

	public void setDataSource(String filePath, int offSet, int length) {
		setDataSource(filePath, offSet, length, 0);
	}

	/**
	 * ���������ļ�(sdcard)
	 * 
	 * @param filePath
	 * @param offSet
	 *            ƫ��������ʲô�ط���ʼ���ţ�0���ļ�ͷ��ʼ
	 * @param length
	 *            ���ų��ȣ�0Ϊ���ŵ��ļ�β
	 * @param flag
	 *            <li><b>BASS_SAMPLE_FLOAT</b> <br>
	 *            ʹ��32λ������������<br>
	 *            <br> <li><b>BASS_SAMPLE_MONO</b> <br>
	 *            �õ���������(ֻ����MP1��MP2��MP3)������CPU��ʹ��<br>
	 *            <br><li><b>BASS_SAMPLE_SOFTWARE</b> <br>
	 *            ʹ����������Ƶ����ʹ��Ӳ��<br>
	 *            <br><li><b>BASS_SAMPLE_3D</b> <br>
	 *            ʹ��3D����<br>
	 *            <br><li><b>BASS_SAMPLE_LOOP</b> <br>
	 *            ѭ����������������Ҳ��<br>
	 *            <br><li><b>BASS_SAMPLE_FX</b> <br>
	 *            ���Ҫʹ��FX�Ĺ��ܾ������flag<br>
	 *            <br><li><b>BASS_STREAM_PRESCAN</b> <br>
	 *            Ԥ��ɨ��MP1��MP2��MP3����״OGG�ļ����������ʱ��<br>
	 *            <br><li><b>BASS_STREAM_AUTOFREE</b> <br>
	 *            ������Ϻ���Զ���ո���Ƶ�������ߵ���stop����ִ��<br>
	 *            <br><li><b>BASS_STREAM_DECODE</b> <br>
	 *            ����һ������ͨ�����������ڲ���<br>
	 *            <br><li><b>BASS_SPEAKER_xxx</b> <br>
	 *            ��������һЩ���ã�������<br>
	 *            <br><li><b>BASS_ASYNCFILE</b> <br>
	 *            �첽��ȡ�ļ���������ʱ�����ļ���ȡ�ͻ��岢�н���<br>
	 * @return
	 */
	public void setDataSource(String filePath, int offSet, int length, int flag) {
		mHandle = BASS.BASS_StreamCreateFile(filePath, offSet, length, flag);
		printError("setDataSource(" + filePath + "," + offSet + "," + length + "," + flag + ")");
		if (mHandle == 0)
			release();
		getInfo();
	}

	public boolean Play() {
		// �����restart����ָ�Ƿ����²���
		// falseΪ���طţ��ڵ�ǰ��ͣ����ʼ��pause��stopһ��
		// trueΪ�طţ����¿�ʼ��pause��stopһ��
		BASS.BASS_ChannelPlay(mHandle, false);
		return printError("Play");
	}

	/**
	 * ��ͣ<br>
	 * Play������ͣ�㿪ʼ����
	 * 
	 * @return
	 */
	public boolean Pause() {
		BASS.BASS_ChannelPause(mHandle);
		return printError("Pause");
	}

	/**
	 * ֹͣ����<br>
	 * Play������²���
	 * 
	 * @return
	 */
	public boolean Stop() {
		// ���������˲��طţ�stop��pauseһ������ͣ����ʼ
		// ��������Ҫ������
		BASS.BASS_ChannelStop(mHandle);
		seekTo(0d);
		return (printError("Stop") == seekTo(0d));
	}

	public boolean isPlaying() {
		boolean state = (BASS.BASS_ChannelIsActive(mHandle) == BASS.BASS_ACTIVE_PLAYING) ? true : false;
		printError("isPlaying");
		return state;
	}

	public boolean isPausing() {
		boolean state = (BASS.BASS_ChannelIsActive(mHandle) == BASS.BASS_ACTIVE_PAUSED) ? true : false;
		printError("isPausing");
		return state;
	}

	public boolean isStoping() {
		boolean state = (BASS.BASS_ChannelIsActive(mHandle) == BASS.BASS_ACTIVE_STOPPED) ? true : false;
		printError("isStoping");
		return state;
	}

	public boolean setLooping(boolean isLoop) {
		boolean state = false;
		if (isLoop)
			state = (BASS.BASS_ChannelFlags(mHandle, BASS.BASS_SAMPLE_LOOP, BASS.BASS_SAMPLE_LOOP) != -1) ? true
					: false;
		else
			state = (BASS.BASS_ChannelFlags(mHandle, 0, BASS.BASS_SAMPLE_LOOP) != -1) ? true : false;
		printError("setLooping");
		return state;
	}

	/**
	 * �������Ĳ�����������(progress/100)������<br>
	 * ��ĳ�(progress/100.0f)
	 * 
	 * @param volume
	 * @return
	 */
	public boolean setVolume(float volume) {
		// Attr������ѡ��п���һ��
		BASS.BASS_ChannelSetAttribute(mHandle, BASS.BASS_ATTRIB_VOL, volume);
		return printError("setVolume");
	}

	public float getVolume() {
		Float volume = new Float(0);
		BASS.BASS_ChannelGetAttribute(mHandle, BASS.BASS_ATTRIB_VOL, volume);
		printError("getVolume");
		return volume.floatValue();
	}

	/**
	 * ������Դ��ƽ��<br>
	 * �ڶ��������ó�
	 * 
	 * @param pan
	 * <br>
	 *            -1:��ȫ�����<br>
	 *            0:�м�<br>
	 *            1:��ȫ���ұ�
	 * @return
	 */
	public boolean setPanning(float pan) {
		BASS.BASS_ChannelSetAttribute(mHandle, BASS.BASS_ATTRIB_PAN, pan);
		return printError("setPanning");
	}

	public float getPanning() {
		Float pan = new Float(0);
		BASS.BASS_ChannelGetAttribute(mHandle, BASS.BASS_ATTRIB_PAN, pan);
		printError("getPanning");
		return pan.floatValue();
	}

	/**
	 * ��ȡ��ǰ�Ľ��ȣ��룩<br>
	 * ʧ�ܷ���-1
	 * 
	 * @return
	 */
	public double getCurrentPosition() {
		// BASS.BASS_ChannelGetPosition����ȡ��ǰ���ȵ�byte��
		// BASS.BASS_ChannelBytes2Seconds�����ݸ�byte���ض�Ӧ��ʱ��
		double position = BASS.BASS_ChannelBytes2Seconds(mHandle,
				BASS.BASS_ChannelGetPosition(mHandle, BASS.BASS_POS_BYTE));
		printError("getCurrentPosition");
		return (position > 0) ? position : -1;
	}

	public boolean seekTo(double seconds) {
		// BASS.BASS_ChannelSeconds2Bytes������ʱ���ȡ���ڵ�byte
		BASS.BASS_ChannelSetPosition(mHandle, BASS.BASS_ChannelSeconds2Bytes(mHandle, seconds),
				BASS.BASS_POS_BYTE);
		return printError("seekTo");
	}

	/**
	 * ����������Ƶ�ĳ���(ʱ��)
	 * 
	 * @return
	 */
	public double getDuration() {
		double time = BASS.BASS_ChannelBytes2Seconds(mHandle,
				BASS.BASS_ChannelGetLength(mHandle, BASS.BASS_POS_BYTE));
		printError("getDuration");
		return time;
	}

	/**
	 * ������Ƶԭ���Ĳ���Ƶ��(Hz)<br>
	 * û���򷵻�-1
	 * 
	 * @return
	 */
	public int getRate() {
		if (mInfo != null) {
			return mInfo.freq;
		}
		return -1;
	}

	/**
	 * ����������<br>
	 * û���򷵻�-1
	 * 
	 * @return
	 */
	public int getChannels() {
		if (mInfo != null) {
			return mInfo.chans;
		}
		return -1;
	}

	/**
	 * ֻ����stream�����Ĳ�������<br>
	 * ע��ǵ��п�
	 * 
	 * @return
	 */
	public String getAudioName() {
		// ����ֱ�ӰѴ�������·��ֱ�ӷ���ȥ
		// ����������
		try {
			if (mInfo != null && mInfo.filename != null) {
				String[] str = mInfo.filename.split("/");
				if (str.length != 1)
					return str[str.length - 1];
				else
					return mInfo.filename;
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���ص�ǰ�����ļ��ĸ�ʽ<br>
	 * ����֧�ָ��ļ���᷵��null
	 * 
	 * @return
	 */
	public String getAudioType() {
		if (mInfo != null) {
			switch (mInfo.ctype) {
			case BASS.BASS_CTYPE_STREAM_OGG:
				return "Ogg Vorbis";
			case BASS.BASS_CTYPE_STREAM_MP1:
				return "MPEG layer 1";
			case BASS.BASS_CTYPE_STREAM_MP2:
				return "MPEG layer 2";
			case BASS.BASS_CTYPE_STREAM_MP3:
				return "MPEG layer 3";
			case BASS.BASS_CTYPE_STREAM_AIFF:
				return "Audio IFF";
			case BASS.BASS_CTYPE_STREAM_WAV_PCM:
				return "PCM WAVE";
			case BASS.BASS_CTYPE_STREAM_WAV_FLOAT:
				return "Floating-point WAVE";
			default:
				// ����wav��ʽ
				if ((mInfo.ctype & BASS.BASS_CTYPE_STREAM_WAV) != 0)
					return "WAVE";
				else
					return null;
			}
		}
		return null;
	}

	/**
	 * �ͷš����<br>
	 * ���øú�����������newһ��
	 * 
	 * @return
	 */
	public boolean release() {
		BASS.BASS_StreamFree(mHandle);
		return printError("release");
	}

	/*------------------����Ϊ�������ܣ������-------------------*/
	/*------------------����Ϊ��չ����-------------------*/

	/**
	 * ����FX
	 * 
	 * @return
	 */
	public boolean openFX() {
		closeFX();
		mFX_Handle = new ConcurrentHashMap<String, Integer>();
		boolean state = (BASS.BASS_ChannelFlags(mHandle, BASS.BASS_SAMPLE_FX, BASS.BASS_SAMPLE_FX) != -1) ? true
				: false;
		printError("OpenFX");
		// BASS_FX.BASS_FX_GetVersion()����16����
		// ��0x02040b01����汾Ϊ2.4.11.1
		// ��ǰʹ�ð汾Ϊ2.4.11.1���������������������bass_fx��ʧ��
		String vision = Integer.toHexString(BASS_FX.BASS_FX_GetVersion());
		printError("vision:" + vision);
		return (state && (vision.equals("02040b01")));
	}

	/**
	 * �ر�FX
	 * 
	 * @return
	 */
	public boolean closeFX() {
		BASS.BASS_FXReset(mHandle);
		printError("closeFX::Reset");
		if (mFX_Handle != null) {
			mFX_Handle.clear();
			mFX_Handle = null;
		}
		boolean state = (BASS.BASS_ChannelFlags(mHandle, 0, BASS.BASS_SAMPLE_FX) != -1) ? true : false;
		printError("closeFX");
		return state;
	}

	/**
	 * <b><u><li>����ʹ�ö���</u></b><br>
	 * <b><u><li>������ʹ��openFX()</u></b><br>
	 * <ul>
	 * ����Rotate(��ת)Ч��<br>
	 * ��ʹ�ö���ʱ�������������������ҷ���<br>
	 * ������������תһ��<br>
	 * Ĭ�Ϻ���Ϊ0.19997086
	 * </ul>
	 * 
	 * @return
	 */
	public boolean openRotate() {
		if (mFX_Handle == null)
			return false;
		// ��key�����ڵ�ʱ��
		if (!mFX_Handle.containsKey(FX_ROTATE)) {
			// ����Ϊͨ�����������������ȼ�������fxͨ����
			mFX_Handle.put(FX_ROTATE, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_ROTATE, 0));
			printError("openRotate::");
			mParam_rotate = new BASS_BFX_ROTATE();
			mParam_rotate.lChannel = BASS_FX.BASS_BFX_CHANALL;
		}
		return true;
	}

	/**
	 * ������ת�ٶ�<br>
	 * ������֤��0.00-1.00�������Ч�����
	 * 
	 * @param rate
	 *            0.00-1.00
	 * @return
	 */
	public boolean setRotate(float rate) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_ROTATE))
			return false;
		if (mParam_rotate == null)
			return false;
		mParam_rotate.fRate = rate;
		BASS.BASS_FXSetParameters(mFX_Handle.get(FX_ROTATE), mParam_rotate);
		return printError("setRotate");
	}

	public float getRotate() {
		if (mFX_Handle == null)
			return 0;
		if (!mFX_Handle.containsKey(FX_ROTATE))
			return 0;
		if (mParam_rotate == null)
			return 0;
		BASS.BASS_FXGetParameters(mFX_Handle.get(FX_ROTATE), mParam_rotate);
		printError("getRotate");
		return mParam_rotate.fRate;
	}

	public boolean closeRotate() {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(FX_ROTATE)) {
			// FXResetû����
			// boolean state = BASS.BASS_FXReset(mFX_Handle.get(FX_ROTATE));
			// ����remove֮��fx��Ӧ��handle�ͱ������
			// Ҫ����Open
			boolean state = BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(FX_ROTATE));
			printError("closeRotate");
			mFX_Handle.remove(FX_ROTATE);
			mParam_rotate = null;
			return state;
		}
		return true;
	}

	private boolean openEQ(String key) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(key)) {
			mFX_Handle.put(key, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_PEAKEQ, 0));
			printError("openEQ," + key);
		}
		if (mParam_eq == null) {
			mParam_eq = new BASS_BFX_PEAKEQ();
			mParam_eq.lChannel = BASS_FX.BASS_BFX_CHANALL;
			mParam_eq.fQ = 0.35f;
			mParam_eq.fGain = 0f;
		}
		return true;
	}

	private boolean closeEQ(String key) {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(key)) {
			BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(key));
			printError("closeEQ," + key);
			mFX_Handle.remove(key);
		}
		return true;
	}

	/**
	 * ����EQ
	 * 
	 * @param band
	 *            Ƶ���ţ����ڱ��ÿһ��Ƶ���������ظ����Լ�����
	 * @param center
	 *            ����Ƶ�ʣ�1HZ~freq/2HZ
	 * @param gain
	 *            �����С��-15db~15db
	 * @param key
	 *            ��¼ÿ��EQ�ľ����key
	 * @return
	 */
	private boolean setEQ(int band, float center, float gain, String key) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(key))
			return false;
		if (mParam_eq == null)
			return false;
		mParam_eq.lBand = band;
		mParam_eq.fCenter = center;
		mParam_eq.fGain = gain;
		BASS.BASS_FXSetParameters(mFX_Handle.get(key), mParam_eq);
		return printError("setEQ," + key);
	}

	/**
	 * ���ص�ǰEQ������
	 * 
	 * @param band
	 * @param key
	 * @return
	 */
	private float getEQGain(int band, String key) {
		if (mFX_Handle == null)
			return 0;
		if (!mFX_Handle.containsKey(key))
			return 0;
		if (mParam_eq == null)
			return 0;
		mParam_eq.lBand = band;
		BASS.BASS_FXGetParameters(mFX_Handle.get(key), mParam_eq);
		printError("getEQ," + key);
		return mParam_eq.fGain;
	}

	/**
	 * <b><u><li>������ʹ��openFX()</u></b><br>
	 * �򿪾�����(EQ)����<br>
	 * �����EQΪ����������<br>
	 * ĿǰEQ�ĵ���Ƶ��Ϊ100,600,1k,8k,14k<br>
	 * QֵΪ0.35<br>
	 * ֮���ٳ��Ը���Ĳ���
	 * <ul>
	 * "Qֵ������Ϊ��ȷ�����Ƕ��ź��о�ȷ������޶���������˥���������ߵĿ�խ�ȡ������ĳ
	 * һƵ��Ծ�������������˥������ʱ���ܵ�Ӱ��Ĳ����Ǳ�ѡ��������Ƶ�ʣ����Ҹ�Ƶ�㸽�� Ƶ�ʷ�Χ������Ҳ����һͬ������������˥������
	 * Qֵ���ɾ��������Ƶ�ʳ��Դ���õ��ġ�������������Ƶ��Ϊ��׼����������������������
	 * ��3dBʱ����֮��ľ��롣���Դ���������Ƶ��֮��Ĺ�ϵQ���Ա�ʾΪ��Q=Fc/bw������FcΪ
	 * ����Ƶ�ʣ�bwΪ���������ڸ��ʽ��������ߵ�ͨ�˲�������������������ˮƽ��Ϊ��
	 * ׼���������С3dBʱ��Ƶ�㿪ʼ������Ƶ��֮��ľ��룬��ʱQֵ���Եò���Ҫ�ˡ�"
	 * </ul>
	 * 
	 * @return
	 */
	public boolean openEQ() {
		if (openEQ(FX_EQ_100) && openEQ(FX_EQ_600) && openEQ(FX_EQ_1k) && openEQ(FX_EQ_8k)
				&& openEQ(FX_EQ_14k))
			return true;
		return false;
	}

	public boolean closeEQ() {
		if (closeEQ(FX_EQ_100) && closeEQ(FX_EQ_600) && closeEQ(FX_EQ_1k) && closeEQ(FX_EQ_8k)
				&& closeEQ(FX_EQ_14k)) {
			mParam_eq = null;
			return true;
		}
		return false;
	}

	/**
	 * @param gain
	 *            -15~15
	 * @return
	 */
	public boolean setEQ100(float gain) {
		return setEQ(1, 100, gain, FX_EQ_100);
	}

	public float getEQ100() {
		return getEQGain(1, FX_EQ_100);
	}

	/**
	 * @param gain
	 *            -15~15
	 * @return
	 */
	public boolean setEQ600(float gain) {
		return setEQ(2, 600, gain, FX_EQ_600);
	}

	public float getEQ600() {
		return getEQGain(2, FX_EQ_600);
	}

	/**
	 * @param gain
	 *            -15~15
	 * @return
	 */
	public boolean setEQ1k(float gain) {
		return setEQ(3, 1000, gain, FX_EQ_1k);
	}

	public float getEQ1k() {
		return getEQGain(3, FX_EQ_1k);
	}

	/**
	 * @param gain
	 *            -15~15
	 * @return
	 */
	public boolean setEQ8k(float gain) {
		return setEQ(4, 8000, gain, FX_EQ_8k);
	}

	public float getEQ8k() {
		return getEQGain(4, FX_EQ_8k);
	}

	/**
	 * @param gain
	 *            -15~15
	 * @return
	 */
	public boolean setEQ14k(float gain) {
		return setEQ(5, 14000, gain, FX_EQ_14k);
	}

	public float getEQ14k() {
		return getEQGain(5, FX_EQ_14k);
	}

	/**
	 * <b><u><li>������ʹ��openFX()</u></b><br>
	 * �Զ�������ò���Ǹ�����ʱ���������Ĳ����Զ����������۵�����<br>
	 * ע��ֻ��ĳЩ��Ƶ��Ч���������������ĸ質��Ƶ��������������Ӧ���ǶԵ缪����Ч<br>
	 * ������һЩ����Ƶ�������������������������� �����ĸ���ʹ�ñȽϸ��ӣ�ֻ�ܰ����Ӹ�������<br>
	 * ������Ĭ��ʹ��AUTOWAH_SLOW<br>
	 * ע��Ҫ������freq������������������
	 * 
	 * @return
	 */
	public boolean openAUTOWAH() {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_AUTOWAH)) {
			mFX_Handle.put(FX_AUTOWAH, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_AUTOWAH, 0));
			printError("openAUTOWAH");
			mParam_autowah = new BASS_BFX_AUTOWAH();
			mParam_autowah.lChannel = BASS_FX.BASS_BFX_CHANALL;
			setAUTOWAH(AUTOWAH_SLOW);
		}
		return true;
	}

	public boolean closeAUTOWAH() {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(FX_AUTOWAH)) {
			boolean state = BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(FX_AUTOWAH));
			printError("closeAUTOWAH");
			mFX_Handle.remove(FX_AUTOWAH);
			mParam_autowah = null;
			return state;
		}
		return true;
	}

	/**
	 * ��������Ч��
	 * 
	 * @param flag
	 * <br>
	 *            AUTOWAH_SLOW<br>
	 *            AUTOWAH_FAST<br>
	 *            AUTOWAH_HIFAST
	 * @return
	 */
	public boolean setAUTOWAH(byte flag) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_AUTOWAH))
			return false;
		if (mParam_autowah == null)
			return false;
		switch (flag) {
		case AUTOWAH_SLOW:
			mParam_autowah.fDryMix = 0.500f;
			mParam_autowah.fWetMix = 1.500f;
			mParam_autowah.fFeedback = 0.5f;
			mParam_autowah.fRate = 2.0f;
			mParam_autowah.fRange = 4.3f;
			mParam_autowah.fFreq = 50.0f;
			break;
		case AUTOWAH_FAST:
			mParam_autowah.fDryMix = 0.500f;
			mParam_autowah.fWetMix = 1.500f;
			mParam_autowah.fFeedback = 0.5f;
			mParam_autowah.fRate = 5.0f;
			mParam_autowah.fRange = 5.3f;
			mParam_autowah.fFreq = 50.0f;
			break;
		case AUTOWAH_HIFAST:
			mParam_autowah.fDryMix = 0.500f;
			mParam_autowah.fWetMix = 1.500f;
			mParam_autowah.fFeedback = 0.5f;
			mParam_autowah.fRate = 5.0f;
			mParam_autowah.fRange = 4.3f;
			mParam_autowah.fFreq = 500.0f;
			break;
		default:
			return false;
		}
		BASS.BASS_FXSetParameters(mFX_Handle.get(FX_AUTOWAH), mParam_autowah);
		return printError("setAUTOWAH," + flag);
	}

	/**
	 * ���ص�ǰauto wah�Ĳ���<br>
	 * 
	 * @return ����һ������Ϊ6��float����<br>
	 *         �ֱ�Ϊ��<br>
	 *         <ul>
	 *         0��DryMix<br>
	 *         1��WetMix<br>
	 *         2��Feedback<br>
	 *         3��Rate<br>
	 *         4��Range<br>
	 *         5��Freq
	 *         </ul>
	 */
	public float[] getAUTOWAH() {
		if (mFX_Handle == null)
			return null;
		if (!mFX_Handle.containsKey(FX_AUTOWAH))
			return null;
		if (mParam_autowah == null)
			return null;
		float[] arrayList = new float[6];
		BASS.BASS_FXGetParameters(mFX_Handle.get(FX_AUTOWAH), mParam_autowah);
		printError("getAUTOWAH");
		arrayList[0] = mParam_autowah.fDryMix;
		arrayList[1] = mParam_autowah.fWetMix;
		arrayList[2] = mParam_autowah.fFeedback;
		arrayList[3] = mParam_autowah.fRate;
		arrayList[4] = mParam_autowah.fRange;
		arrayList[5] = mParam_autowah.fFreq;
		return arrayList;
	}

	/**
	 * <b><u><li>������ʹ��openFX()</u></b><br>
	 * ����Ч��������������<br>
	 * ��ͬ���ǶԸ�����Ƶ����Ч��<br>
	 * ���ǻ᲻���ظ������Ǻ���Ȼ
	 * 
	 * @return
	 */
	public boolean openPhaser() {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_PHASER)) {
			mFX_Handle.put(FX_PHASER, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_PHASER, 0));
			printError("openPhaser");
			mParam_phaser = new BASS_BFX_PHASER();
			mParam_phaser.lChannel = BASS_FX.BASS_BFX_CHANALL;
			setPhaser(PHASER_SHIFT);
		}
		return true;
	}

	public boolean closePhaser() {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(FX_PHASER)) {
			boolean state = BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(FX_PHASER));
			printError("closePhaser");
			mFX_Handle.remove(FX_PHASER);
			mParam_phaser = null;
			return state;
		}
		return true;
	}

	/**
	 * @param flag
	 * <br>
	 *            PHASER_SHIFT<br>
	 *            PHASER_SLOWSHIFT<br>
	 *            PHASER_BASIC<br>
	 *            PHASER_WFB<br>
	 *            PHASER_MED<br>
	 *            PHASER_FAST<br>
	 *            PHASER_INVERT<br>
	 *            PHASER_TREMOLO<br>
	 * @return
	 */
	public boolean setPhaser(byte flag) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_PHASER))
			return false;
		if (mParam_phaser == null)
			return false;
		switch (flag) {
		case PHASER_SHIFT:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.0f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 4.0f;
			mParam_phaser.fFreq = 100.0f;
			break;
		case PHASER_SLOWSHIFT:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = -0.999f;
			mParam_phaser.fFeedback = -0.6f;
			mParam_phaser.fRate = 0.2f;
			mParam_phaser.fRange = 6.0f;
			mParam_phaser.fFreq = 100.0f;
			break;
		case PHASER_BASIC:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.0f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 4.3f;
			mParam_phaser.fFreq = 50.0f;
			break;
		case PHASER_WFB:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.6f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 4.0f;
			mParam_phaser.fFreq = 40.0f;
			break;
		case PHASER_MED:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.0f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 7.0f;
			mParam_phaser.fFreq = 100.0f;
			break;
		case PHASER_FAST:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.0f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 7.0f;
			mParam_phaser.fFreq = 400.0f;
			break;
		case PHASER_INVERT:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = -0.999f;
			mParam_phaser.fFeedback = -0.2f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 7.0f;
			mParam_phaser.fFreq = 200.0f;
			break;
		case PHASER_TREMOLO:
			mParam_phaser.fDryMix = 0.999f;
			mParam_phaser.fWetMix = 0.999f;
			mParam_phaser.fFeedback = 0.6f;
			mParam_phaser.fRate = 1.0f;
			mParam_phaser.fRange = 4.0f;
			mParam_phaser.fFreq = 60.0f;
			break;
		default:
			return false;
		}
		BASS.BASS_FXSetParameters(mFX_Handle.get(FX_PHASER), mParam_phaser);
		return printError("setPhaser" + flag);
	}

	/**
	 * ���ص�ǰphaser�Ĳ���<br>
	 * 
	 * @return ����һ������Ϊ6��float����<br>
	 *         �ֱ�Ϊ��<br>
	 *         <ul>
	 *         0��DryMix<br>
	 *         1��WetMix<br>
	 *         2��Feedback<br>
	 *         3��Rate<br>
	 *         4��Range<br>
	 *         5��Freq
	 *         </ul>
	 */
	public float[] getPhaser() {
		if (mFX_Handle == null)
			return null;
		if (!mFX_Handle.containsKey(FX_PHASER))
			return null;
		if (mParam_phaser == null)
			return null;
		float[] arrayList = new float[6];
		BASS.BASS_FXGetParameters(mFX_Handle.get(FX_PHASER), mParam_phaser);
		printError("getPhaser");
		arrayList[0] = mParam_phaser.fDryMix;
		arrayList[1] = mParam_phaser.fWetMix;
		arrayList[2] = mParam_phaser.fFeedback;
		arrayList[3] = mParam_phaser.fRate;
		arrayList[4] = mParam_phaser.fRange;
		arrayList[5] = mParam_phaser.fFreq;
		return arrayList;
	}

	/**
	 * <b><u><li>������ʹ��openFX()</u></b><br>
	 * �ϳ���Ч��Ӧ����������ʱ�õ�����Ч<br>
	 * ������������������һ������
	 * 
	 * @return
	 */
	public boolean openChorus() {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_CHORUS)) {
			mFX_Handle.put(FX_CHORUS, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_CHORUS, 0));
			printError("openChorus");
			mParam_chorus = new BASS_BFX_CHORUS();
			mParam_chorus.lChannel = BASS_FX.BASS_BFX_CHANALL;
		}
		return true;
	}

	public boolean closeChorus() {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(FX_CHORUS)) {
			boolean state = BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(FX_CHORUS));
			printError("closeChorus");
			mFX_Handle.remove(FX_CHORUS);
			mParam_chorus = null;
			return state;
		}
		return true;
	}

	/**
	 * @param flag
	 * <br>
	 *            CHORUS_FLANGER�����ʸ�<br>
	 *            CHORUS_EXAGGERATION�����ŵĺϳ���Ч<br>
	 *            CHORUS_MOTOCYCLE��ģ��Ħ�г�����<br>
	 *            CHORUS_DEVIL��һ�㶼����ħ������ĳЩʱ�������������е��Ȼ<br>
	 *            CHORUS_MANYVOICE��˵�Ǻܶ�����<br>
	 *            CHORUS_CHIPMUNK�����������������Ƶ����������һ��װ�۵�����<br>
	 *            CHORUS_WATER�����ư�ˮ���������������������ĳЩ��Ƶ�²�����<br>
	 *            CHORUS_AIRPLANE��һ�㶼������<br>
	 * @return
	 */
	public boolean setChorus(byte flag) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_CHORUS))
			return false;
		if (mParam_chorus == null)
			return false;
		switch (flag) {
		case CHORUS_FLANGER:
			mParam_chorus.fDryMix = 1.0f;
			mParam_chorus.fWetMix = 0.35f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 5.0f;
			mParam_chorus.fRate = 1.0f;
			break;
		case CHORUS_EXAGGERATION:
			mParam_chorus.fDryMix = 0.7f;
			mParam_chorus.fWetMix = 0.25f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 200.0f;
			mParam_chorus.fRate = 50.0f;
			break;
		case CHORUS_MOTOCYCLE:
			mParam_chorus.fDryMix = 0.9f;
			mParam_chorus.fWetMix = 0.45f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 100.0f;
			mParam_chorus.fRate = 25.0f;
			break;
		case CHORUS_DEVIL:
			mParam_chorus.fDryMix = 0.9f;
			mParam_chorus.fWetMix = 0.35f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 50.0f;
			mParam_chorus.fRate = 200.0f;
			break;
		case CHORUS_MANYVOICE:
			mParam_chorus.fDryMix = 0.9f;
			mParam_chorus.fWetMix = 0.35f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 400.0f;
			mParam_chorus.fRate = 200.0f;
			break;
		case CHORUS_CHIPMUNK:
			mParam_chorus.fDryMix = 0.9f;
			mParam_chorus.fWetMix = -0.2f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 400.0f;
			mParam_chorus.fRate = 400.0f;
			break;
		case CHORUS_WATER:
			mParam_chorus.fDryMix = 0.9f;
			mParam_chorus.fWetMix = -0.4f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 2.0f;
			mParam_chorus.fRate = 1.0f;
			break;
		case CHORUS_AIRPLANE:
			mParam_chorus.fDryMix = 0.3f;
			mParam_chorus.fWetMix = 0.4f;
			mParam_chorus.fFeedback = 0.5f;
			mParam_chorus.fMinSweep = 1.0f;
			mParam_chorus.fMaxSweep = 10.0f;
			mParam_chorus.fRate = 5.0f;
			break;
		default:
			return false;
		}
		BASS.BASS_FXSetParameters(mFX_Handle.get(FX_CHORUS), mParam_chorus);
		return printError("setChorus" + flag);
	}

	/**
	 * ���ص�ǰChorus�Ĳ���<br>
	 * 
	 * @return ����һ������Ϊ6��float����<br>
	 *         �ֱ�Ϊ��<br>
	 *         <ul>
	 *         0��DryMix<br>
	 *         1��WetMix<br>
	 *         2��Feedback<br>
	 *         3��MinSweep<br>
	 *         4��MaxSweep<br>
	 *         5��Rate
	 *         </ul>
	 */
	public float[] getChorus() {
		if (mFX_Handle == null)
			return null;
		if (!mFX_Handle.containsKey(FX_CHORUS))
			return null;
		if (mParam_chorus == null)
			return null;
		float[] arrayList = new float[6];
		BASS.BASS_FXGetParameters(mFX_Handle.get(FX_CHORUS), mParam_chorus);
		printError("getChorus");
		arrayList[0] = mParam_chorus.fDryMix;
		arrayList[1] = mParam_chorus.fWetMix;
		arrayList[2] = mParam_chorus.fFeedback;
		arrayList[3] = mParam_chorus.fMinSweep;
		arrayList[4] = mParam_chorus.fMaxSweep;
		arrayList[5] = mParam_chorus.fRate;
		return arrayList;
	}

	//������echo4�ò��ˣ���̳�Ǳ�˵�������⣬���Ǹ������°汾��so�ò���
	//����������echo2�����Ǳ���������
	public boolean openEcho() {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_ECHO)) {
			mFX_Handle.put(FX_ECHO, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_ECHO2, 0));
			printError("openEcho");
			mParam_echo = new BASS_BFX_ECHO2();
			mParam_echo.lChannel = BASS_FX.BASS_BFX_CHANALL;
		}
		return true;
	}

	public boolean closeEcho() {
		if (mFX_Handle == null)
			return false;
		if (mFX_Handle.containsKey(FX_ECHO)) {
			boolean state = BASS.BASS_ChannelRemoveFX(mHandle, mFX_Handle.get(FX_ECHO));
			printError("closeEcho");
			mFX_Handle.remove(FX_ECHO);
			mParam_echo = null;
			return state;
		}
		return true;
	}

	public boolean setEcho(byte flag) {
		if (mFX_Handle == null)
			return false;
		if (!mFX_Handle.containsKey(FX_ECHO))
			return false;
		if (mParam_echo == null)
			return false;
		switch (flag) {
		case ECHO_SMALL:
			mParam_echo.fDryMix = 0.999f;
			mParam_echo.fWetMix = 0.999f;
			mParam_echo.fFeedback = 0.0f;
			mParam_echo.fDelay = 0.20f;
			break;
		case ECHO_MANY:
			mParam_echo.fDryMix = 0.999f;
			mParam_echo.fWetMix = 0.999f;
			mParam_echo.fFeedback = 0.7f;
			mParam_echo.fDelay = 0.50f;
			break;
		case ECHO_REVERSE:
			mParam_echo.fDryMix = 0.999f;
			mParam_echo.fWetMix = 0.999f;
			mParam_echo.fFeedback = -0.7f;
			mParam_echo.fDelay = 0.80f;
			break;
		case ECHO_ROBOTIC:
			mParam_echo.fDryMix = 0.500f;
			mParam_echo.fWetMix = 0.800f;
			mParam_echo.fFeedback = 0.5f;
			mParam_echo.fDelay = 0.10f;
			break;
		default:
			return false;
		}
//		mParam_echo.bStereo = false;
		BASS.BASS_FXSetParameters(mFX_Handle.get(FX_ECHO), mParam_echo);
		return printError("setEcho" + flag);
	}

	public float[] getEcho() {
		if (mFX_Handle == null)
			return null;
		if (!mFX_Handle.containsKey(FX_ECHO))
			return null;
		if (mParam_echo == null)
			return null;
		float[] arrayList = new float[6];
		BASS.BASS_FXGetParameters(mFX_Handle.get(FX_ECHO), mParam_echo);
		printError("getEcho");
		arrayList[0] = mParam_echo.fDryMix;
		arrayList[1] = mParam_echo.fWetMix;
		arrayList[2] = mParam_echo.fFeedback;
		arrayList[3] = mParam_echo.fDelay;
		return arrayList;
	}
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/
	/*--------------    �������Ч��֪Ϊʲô      ---------------*/
	/*--------------       ����Ϊ������              ---------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/
	/*--------------�ڵ�ǰƽ̨����Ҫ�õĿ��ຯ��---------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	// BASS_SetDevice
	// ���ò�ͬ�豸����������Android���豸��һ����Ӧ�ò���Ҫ

	// BASS_GetCPU
	// ��ȡ��ǰ���иÿ��CPU��ʹ����

	// BASS_GetVersion
	// ��ȡ��ǰ��İ汾

	// BASS_GetDevice
	// ��ȡ�豸�ĺ���

	// BASS_GetDeviceInfo
	// ��ȡ�豸�������Ϣ

	// BASS_GetInfo
	// ��ȡ�豸�ľ�����Ϣ��֮��Ӧ���õ���

	// BASS_SetConfigPtr
	// ��ò���������߲����йصġ����ӷ��������õ�

	// BASS_GetConfigPtr
	// ͬ��

	// ���������ķ���(BASS_Sample��ͷ��)����д����Ϊ�ò���

	// BASS_ChannelGetTags
	// ���Ի�ȡ��Ƶ�ļ��еı�ǩ��Ϣ�����������ҡ�����ʲô�ģ�
	// �����Ժ����

	// BASS_FX_BFX_DAMP
	// ���Ч����ʱû����Ч��

	// BASS_FX_BFX_DISTORTION
	// ������������
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/
	/*--------------------�����������;---------------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	// BASS_Update
	// ָ����channel�Ļ��棬��������»������;
}
