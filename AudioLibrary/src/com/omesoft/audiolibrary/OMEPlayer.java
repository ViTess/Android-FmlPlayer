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
 * BASS库中的基本都是基于Channel(通道)处理音频的，如解码、播放
 * 一段音频要先加载入通道才能进行处理
 * 每一段音频或通道的标识为一个int类型的Handle(句柄)
 * 通过Handle可以获取或设置足够多的东西
 * 另外，sample(样本)的句柄和Channel的句柄不一样
 * 要将sample的句柄置入Channel中才行
 * 
 * BASS库中的一些设置是基于专业音频的参数进行的
 * 最好先了解一下音频的术语，比如panning
 * 
 * 当前不加插件的情况下，只支持ogg、mp1、mp2、mp3、wav
 */
/*------------------------------------------*/

/**
 * 基于BASS Library<br>
 * gbk
 * 
 * @author WingSun 'ViTe' Tam
 * 
 */
public class OMEPlayer {

	private static final String TAG = "OMEPlayer";
	/**
	 * 默认设备<br>
	 * <li>
	 * -1 = 默认设备<br><li>
	 * 0 = 没有声音<br><li>
	 * 1 = 第一个真正的输出设备
	 */
	private static final int device = -1;
	/**
	 * 输出采样率
	 */
	private static final int freq = 44100;
	/**
	 * 设备标志，通过标志可以实现不同的功能 <br>
	 * 0应该为默认16bit 标志为：<br>
	 * <li><b>BASS_DEVICE_8BITS</b> ：使用8位分辨率，否则默认16位 <br> <li>
	 * <b>BASS_DEVICE_MONO</b> ：使用单声道，否则为立体声 <br> <li><b>BASS_DEVICE_3D</b>
	 * ：使用3d音效功能 <br> <li><b>BASS_DEVICE_LATENCY</b>
	 * ：不清楚，api的说法为计算延迟，因此增加请求音乐至播放音乐的间隔时间 <br> <li><b>BASS_DEVICE_SPEAKERS</b>
	 * ：（不清楚）强制使用扬声器 <br> <li><b>BASS_DEVICE_NOSPEAKER</b> ：（不清楚）忽略扬声器 <br> <li>
	 * <b>BASS_DEVICE_FREQ</b> ：设置设备的采样率<br>
	 */
	private static final int flags = 0;

	/*--------------某些常量--------------*/
	private static Context context;
	/**
	 * 记录是否已经初始化过
	 */
	private static boolean isInitAudioEngine = false;

	/*-----------------------------------*/

	/*-----------------静态（全局）方法------------------*/

	/**
	 * 初始化音乐播放库(不包含录音)
	 * 
	 * @param context
	 *            getApplicationContext
	 * @return true 即初始化成功、已被初始化过
	 */
	public static boolean initAudioEngine(Context context) {
		OMEPlayer.context = context;
		// 保证只初始化一次
		if (!isInitAudioEngine) {
			isInitAudioEngine = BASS.BASS_Init(device, freq, flags);
			printError("initAudioEngine");
		}
		return isInitAudioEngine;
	}

	/**
	 * 释放音乐播放库
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
	 * 全部音频开始播放（在使用overallPause之后）<br>
	 * 使用overallStop后不能再调用此方法<br>
	 * 请务必谨慎使用
	 * 
	 * @return
	 */
	public static boolean overallStart() {
		BASS.BASS_Start();
		return printError("overallStart");
	}

	/**
	 * 所有音频暂停输出
	 * 
	 * @return
	 */
	public static boolean overallPause() {
		BASS.BASS_Pause();
		return printError("overallPause");
	}

	/**
	 * 所有音频停止输出<br>
	 * 停止后无法再播放，所以这个慎用
	 * 
	 * @return
	 */
	public static boolean overallStop() {
		BASS.BASS_Stop();
		return printError("overallStop");
	}

	/**
	 * 设置全局音量，由于可能会影响到其他Channel的单独音量，不建议使用<br>
	 * 传进来的参数若是类似(progress/100)这样的<br>
	 * 请改成(progress/100.0f)
	 * 
	 * @param volume
	 * @return
	 */
	public static boolean overallSetVolume(float volume) {
		BASS.BASS_SetVolume(volume);
		return printError("overallSetVolume");
	}

	/**
	 * 获取当前全局音量
	 * 
	 * @return
	 */
	public static float overallGetVolume() {
		float volume = BASS.BASS_GetVolume();
		printError("overallGetVolume");
		return volume;
	}

	/**
	 * 设置音频库的一些选项，能在任何时候调用<br>
	 * 但最好还是在Init后调用<br>
	 * 一些网络相关的就不写出来了
	 * 
	 * @param option
	 *            <li><b>BASS_CONFIG_BUFFER</b> <br>
	 *            设置缓冲区大小默认为500ms，只影响stream和music的channel，最大值为5000ms <br>
	 *            <br> <li><b>BASS_CONFIG_UPDATEPERIOD</b> <br>
	 *            设置缓冲区的更新周期，只影响stream和music的channel，0为禁用自动更新，最小更新周期为5ms，
	 *            最大为100ms <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_SAMPLE</b> <br>
	 *            设置所有样本的全局音量，从0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_STREAM</b> <br>
	 *            设置所有音频流的全局音量，从0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_GVOL_MUSIC</b> <br>
	 *            设置所有MOD音频的全局音量，从0-10000 <br>
	 *            <br> <li><b>BASS_CONFIG_CURVE_VOL</b> <br>
	 *            设置音量的增减模式（线性或对数曲线），线形为0%-100%，对数为-100db-0dp <br>
	 *            false = 线性，true = 对数 <br>
	 *            <br> <li><b>BASS_CONFIG_CURVE_PAN</b> <br>
	 *            设置声音平移的增减模式（线性或对数曲线） <br>
	 *            这里的平移可能是指音源的平移（如从左到右） <br>
	 *            <br> <li><b>BASS_CONFIG_FLOATDSP</b> <br>
	 *            设置是否使用(在Android下为24位)浮点运算DSP <br>
	 *            api中说使用该设置能使音频经过DSP后不会降低质量 <br>
	 *            但容易引发错误，所以应该避免使用 <br>
	 *            <br> <li><b>BASS_CONFIG_3DALGORITHM</b> <br>
	 *            设置3D音效的算法，有四个选项： <br>
	 *            BASS_3DALG_DEFAULT <br>
	 *            BASS_3DALG_OFF <br>
	 *            BASS_3DALG_FULL <br>
	 *            BASS_3DALG_LIGHT <br>
	 *            <br> <li><b>BASS_CONFIG_PAUSE_NOPLAY</b> <br>
	 *            跟使用overallPause一样意思 <br>
	 *            <br> <li><b>BASS_CONFIG_REC_BUFFER</b> <br>
	 *            设置录音通道的缓存，1000-5000(ms) <br>
	 *            <br> <li><b>BASS_CONFIG_MUSIC_VIRTUAL</b> <br>
	 *            设置IT格式的MOD音乐的虚拟通道数 <br>
	 *            <br> <li><b>BASS_CONFIG_VERIFY</b> <br>
	 *            设置文件格式的验证长度，应该是根据长度获取一定长度的验证信息来验证是否是这个格式 <br>
	 *            <br> <li><b>BASS_CONFIG_UPDATETHREADS</b> <br>
	 *            更新播放通道的缓冲区的线程 <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_BUFFER</b> <br>
	 *            设置所有播放通道一起播放时，在最终播放前混合在一起所使用的缓存，用于准备播放 <br>
	 *            这个缓存的设置会影响播放和暂停的延迟时间，不能太高，但太低也会导致输出中断 <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_DEFAULT</b> <br>
	 *            这个别碰 <br>
	 *            <br> <li><b>BASS_CONFIG_SRC</b> <br>
	 *            设置默认采样率的转换质量 <br>
	 *            0 =线性插值，1 = 8点SINC插值，2 = 16点SINC插值，3 = 32点SINC插值。 <br>
	 *            <br> <li><b>BASS_CONFIG_ASYNCFILE_BUFFER</b> <br>
	 *            异步读取文件的缓冲区长度。 <br>
	 *            <br> <li><b>BASS_CONFIG_OGG_PRESCAN</b> <br>
	 *            预扫描OGG文件，默认开启 <br>
	 *            <br> <li><b>BASS_CONFIG_DEV_NONSTOP</b> <br>
	 *            不知道，Android新增的，API完全没提
	 * @param value
	 * @return
	 */
	public static boolean setOption(int option, int value) {
		BASS.BASS_SetConfig(option, value);
		return printError("setOption");
	}

	/**
	 * 返回设置的值
	 * 
	 * @param option
	 *            <li><b>BASS_CONFIG_HANDLES</b> <br>
	 *            这个只用于该方法，获取当前句柄数 <br>
	 *            <br>
	 * @return
	 */
	public static int getOptions(int option) {
		int value = BASS.BASS_GetConfig(option);
		printError("getOptions");
		return value;
	}

	/**
	 * 在Log打印错误，返回错误代码
	 * 
	 * @param func
	 *            所在的方法
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

	/*-----------------对象（局部）方法------------------*/
	/*-------------局部方法常量------------*/
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

	/*-------------局部方法变量------------*/
	/**
	 * 每个对象对应一个句柄(音频)
	 */
	private int mHandle;
	/**
	 * 每个对象的音频的参数
	 */
	private BASS_CHANNELINFO mInfo;
	/**
	 * 每个对象的音效FX句柄<br>
	 * 用一个ConcurrentHashMap维护<br>
	 * 基于线程安全
	 */
	private ConcurrentHashMap<String, Integer> mFX_Handle;
	/**
	 * rotate音效参数
	 */
	private BASS_BFX_ROTATE mParam_rotate;
	/**
	 * EQ音效参数
	 */
	private BASS_BFX_PEAKEQ mParam_eq;
	/**
	 * 自动哇音音效参数
	 */
	private BASS_BFX_AUTOWAH mParam_autowah;
	/**
	 * 移相音效参数
	 */
	private BASS_BFX_PHASER mParam_phaser;
	/**
	 * 合唱音效参数(包含flanger)
	 */
	private BASS_BFX_CHORUS mParam_chorus;
	/**
	 * 回声声效
	 */
	private BASS_BFX_ECHO2 mParam_echo;

	/*-----------------------------------*/

	/**
	 * 创建对象
	 */
	public OMEPlayer() {
		mInfo = new BASS_CHANNELINFO();
		BASS.BASS_StreamFree(mHandle);
		printError("OMEPlayer()");
		mHandle = 0;
	}

	/**
	 * 获取音频的信息
	 */
	private void getInfo() {
		BASS.BASS_ChannelGetInfo(mHandle, mInfo);
		printError("getInfo");
	}

	/**
	 * 设置对象的句柄值，这个不要乱用
	 * 
	 * @param handle
	 */
	private void setHandle(int handle) {
		mHandle = handle;
	}

	/**
	 * 返回对象的句柄<br>
	 * 如果生成音频失败，句柄为0
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
	 * 创建播放文件(assets)
	 * 
	 * @param fileName
	 * @param offSet
	 *            偏移量，从什么地方开始播放，0从文件头开始
	 * @param length
	 *            播放长度，0为播放到文件尾
	 * @param flag
	 *            <li><b>BASS_SAMPLE_FLOAT</b> <br>
	 *            使用32位浮点样本数据<br>
	 *            <br> <li><b>BASS_SAMPLE_MONO</b> <br>
	 *            用单声道播放(只用于MP1、MP2、MP3)，减少CPU的使用<br>
	 *            <br><li><b>BASS_SAMPLE_SOFTWARE</b> <br>
	 *            使用软件混合音频，不使用硬件<br>
	 *            <br><li><b>BASS_SAMPLE_3D</b> <br>
	 *            使用3D功能<br>
	 *            <br><li><b>BASS_SAMPLE_LOOP</b> <br>
	 *            循环，不在这里设置也行<br>
	 *            <br><li><b>BASS_SAMPLE_FX</b> <br>
	 *            如果要使用FX的功能就用这个flag<br>
	 *            <br><li><b>BASS_STREAM_PRESCAN</b> <br>
	 *            预先扫描MP1、MP2、MP3和链状OGG文件，这样会耗时间<br>
	 *            <br><li><b>BASS_STREAM_AUTOFREE</b> <br>
	 *            播放完毕后会自动清空该音频流，或者调用stop都会执行<br>
	 *            <br><li><b>BASS_STREAM_DECODE</b> <br>
	 *            创建一个解码通道，不能用于播放<br>
	 *            <br><li><b>BASS_SPEAKER_xxx</b> <br>
	 *            扬声器的一些配置，不鸟他<br>
	 *            <br><li><b>BASS_ASYNCFILE</b> <br>
	 *            异步读取文件。当启用时，该文件读取和缓冲并行解码<br>
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
	 * 创建播放文件(sdcard)
	 * 
	 * @param filePath
	 * @param offSet
	 *            偏移量，从什么地方开始播放，0从文件头开始
	 * @param length
	 *            播放长度，0为播放到文件尾
	 * @param flag
	 *            <li><b>BASS_SAMPLE_FLOAT</b> <br>
	 *            使用32位浮点样本数据<br>
	 *            <br> <li><b>BASS_SAMPLE_MONO</b> <br>
	 *            用单声道播放(只用于MP1、MP2、MP3)，减少CPU的使用<br>
	 *            <br><li><b>BASS_SAMPLE_SOFTWARE</b> <br>
	 *            使用软件混合音频，不使用硬件<br>
	 *            <br><li><b>BASS_SAMPLE_3D</b> <br>
	 *            使用3D功能<br>
	 *            <br><li><b>BASS_SAMPLE_LOOP</b> <br>
	 *            循环，不在这里设置也行<br>
	 *            <br><li><b>BASS_SAMPLE_FX</b> <br>
	 *            如果要使用FX的功能就用这个flag<br>
	 *            <br><li><b>BASS_STREAM_PRESCAN</b> <br>
	 *            预先扫描MP1、MP2、MP3和链状OGG文件，这样会耗时间<br>
	 *            <br><li><b>BASS_STREAM_AUTOFREE</b> <br>
	 *            播放完毕后会自动清空该音频流，或者调用stop都会执行<br>
	 *            <br><li><b>BASS_STREAM_DECODE</b> <br>
	 *            创建一个解码通道，不能用于播放<br>
	 *            <br><li><b>BASS_SPEAKER_xxx</b> <br>
	 *            扬声器的一些配置，不鸟他<br>
	 *            <br><li><b>BASS_ASYNCFILE</b> <br>
	 *            异步读取文件。当启用时，该文件读取和缓冲并行解码<br>
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
		// 这里的restart参数指是否重新播放
		// false为不重放，在当前暂停处开始，pause和stop一样
		// true为重放，重新开始，pause和stop一样
		BASS.BASS_ChannelPlay(mHandle, false);
		return printError("Play");
	}

	/**
	 * 暂停<br>
	 * Play后会从暂停点开始播放
	 * 
	 * @return
	 */
	public boolean Pause() {
		BASS.BASS_ChannelPause(mHandle);
		return printError("Pause");
	}

	/**
	 * 停止播放<br>
	 * Play后会重新播放
	 * 
	 * @return
	 */
	public boolean Stop() {
		// 由于设置了不重放，stop和pause一样从暂停处开始
		// 所以这里要做处理
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
	 * 传进来的参数若是类似(progress/100)这样的<br>
	 * 请改成(progress/100.0f)
	 * 
	 * @param volume
	 * @return
	 */
	public boolean setVolume(float volume) {
		// Attr的其他选项，有空试一试
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
	 * 设置声源的平移<br>
	 * 在耳机才听得出
	 * 
	 * @param pan
	 * <br>
	 *            -1:完全在左边<br>
	 *            0:中间<br>
	 *            1:完全在右边
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
	 * 获取当前的进度（秒）<br>
	 * 失败返回-1
	 * 
	 * @return
	 */
	public double getCurrentPosition() {
		// BASS.BASS_ChannelGetPosition，获取当前进度的byte数
		// BASS.BASS_ChannelBytes2Seconds，根据该byte返回对应的时间
		double position = BASS.BASS_ChannelBytes2Seconds(mHandle,
				BASS.BASS_ChannelGetPosition(mHandle, BASS.BASS_POS_BYTE));
		printError("getCurrentPosition");
		return (position > 0) ? position : -1;
	}

	public boolean seekTo(double seconds) {
		// BASS.BASS_ChannelSeconds2Bytes，根据时间获取所在的byte
		BASS.BASS_ChannelSetPosition(mHandle, BASS.BASS_ChannelSeconds2Bytes(mHandle, seconds),
				BASS.BASS_POS_BYTE);
		return printError("seekTo");
	}

	/**
	 * 返回整个音频的长度(时间)
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
	 * 返回音频原来的播放频率(Hz)<br>
	 * 没有则返回-1
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
	 * 返回声道数<br>
	 * 没有则返回-1
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
	 * 只有用stream创建的才有名字<br>
	 * 注意记得判空
	 * 
	 * @return
	 */
	public String getAudioName() {
		// 发现直接把传进来的路径直接发出去
		// 这里作处理
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
	 * 返回当前播放文件的格式<br>
	 * 若不支持该文件则会返回null
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
				// 其他wav格式
				if ((mInfo.ctype & BASS.BASS_CTYPE_STREAM_WAV) != 0)
					return "WAVE";
				else
					return null;
			}
		}
		return null;
	}

	/**
	 * 释放、清空<br>
	 * 调用该函数后请重新new一个
	 * 
	 * @return
	 */
	public boolean release() {
		BASS.BASS_StreamFree(mHandle);
		return printError("release");
	}

	/*------------------以上为基本功能，已完成-------------------*/
	/*------------------以下为扩展功能-------------------*/

	/**
	 * 开启FX
	 * 
	 * @return
	 */
	public boolean openFX() {
		closeFX();
		mFX_Handle = new ConcurrentHashMap<String, Integer>();
		boolean state = (BASS.BASS_ChannelFlags(mHandle, BASS.BASS_SAMPLE_FX, BASS.BASS_SAMPLE_FX) != -1) ? true
				: false;
		printError("OpenFX");
		// BASS_FX.BASS_FX_GetVersion()返回16进制
		// 如0x02040b01，则版本为2.4.11.1
		// 当前使用版本为2.4.11.1，若不是这个结果，则加载bass_fx库失败
		String vision = Integer.toHexString(BASS_FX.BASS_FX_GetVersion());
		printError("vision:" + vision);
		return (state && (vision.equals("02040b01")));
	}

	/**
	 * 关闭FX
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
	 * <b><u><li>必须使用耳机</u></b><br>
	 * <b><u><li>必须先使用openFX()</u></b><br>
	 * <ul>
	 * 开启Rotate(旋转)效果<br>
	 * 在使用耳机时能听到音乐轮流从左到右发声<br>
	 * 听起来像在旋转一样<br>
	 * 默认好像为0.19997086
	 * </ul>
	 * 
	 * @return
	 */
	public boolean openRotate() {
		if (mFX_Handle == null)
			return false;
		// 当key不存在的时候
		if (!mFX_Handle.containsKey(FX_ROTATE)) {
			// 这里为通道数、参数名、优先级，返回fx通道数
			mFX_Handle.put(FX_ROTATE, BASS.BASS_ChannelSetFX(mHandle, BASS_FX.BASS_FX_BFX_ROTATE, 0));
			printError("openRotate::");
			mParam_rotate = new BASS_BFX_ROTATE();
			mParam_rotate.lChannel = BASS_FX.BASS_BFX_CHANALL;
		}
		return true;
	}

	/**
	 * 设置旋转速度<br>
	 * 经测试证明0.00-1.00这个区间效果最好
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
			// FXReset没作用
			// boolean state = BASS.BASS_FXReset(mFX_Handle.get(FX_ROTATE));
			// 这里remove之后，fx对应的handle就被清空了
			// 要重新Open
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
	 * 设置EQ
	 * 
	 * @param band
	 *            频带号，用于标记每一个频带，不能重复、自己定义
	 * @param center
	 *            中心频率，1HZ~freq/2HZ
	 * @param gain
	 *            增益大小，-15db~15db
	 * @param key
	 *            记录每个EQ的句柄的key
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
	 * 返回当前EQ增益数
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
	 * <b><u><li>必须先使用openFX()</u></b><br>
	 * 打开均衡器(EQ)功能<br>
	 * 这里的EQ为参数均衡器<br>
	 * 目前EQ的调整频率为100,600,1k,8k,14k<br>
	 * Q值为0.35<br>
	 * 之后再尝试更多的测试
	 * <ul>
	 * "Q值，就是为了确保我们对信号有精确处理而限定被提升或衰减均衡曲线的宽窄度。当针对某
	 * 一频点对均衡曲线提升或衰减处理时，受到影响的不仅是被选定的中心频率，而且该频点附近 频率范围的声音也都会一同被提升起来或衰减掉。
	 * Q值是由均衡的中心频率除以带宽得到的。带宽是以中心频率为基准，向两边延伸至其增益下
	 * 降3dB时两点之间的距离。所以带宽与中心频率之间的关系Q可以表示为：Q=Fc/bw，其中Fc为
	 * 中心频率，bw为带宽。而对于搁架式均衡器或高低通滤波器来讲，带宽则是以水平轴为基
	 * 准，从增益减小3dB时的频点开始到操作频点之间的距离，此时Q值就显得不重要了。"
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
	 * <b><u><li>必须先使用openFX()</u></b><br>
	 * 自动哇音，貌似是根据延时，按给定的参数自动发出类似哇的声音<br>
	 * 注意只对某些音频有效，比如在有人声的歌唱音频中能听出哇音，应该是对电吉他有效<br>
	 * 但是在一些纯音频（如雨声），是听不出哇音的 参数的各种使用比较复杂，只能按例子给出三种<br>
	 * 开启后默认使用AUTOWAH_SLOW<br>
	 * 注意要先设置freq，其他参数才能设置
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
	 * 设置哇音效果
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
	 * 返回当前auto wah的参数<br>
	 * 
	 * @return 返回一个长度为6的float数组<br>
	 *         分别为：<br>
	 *         <ul>
	 *         0：DryMix<br>
	 *         1：WetMix<br>
	 *         2：Feedback<br>
	 *         3：Rate<br>
	 *         4：Range<br>
	 *         5：Freq
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
	 * <b><u><li>必须先使用openFX()</u></b><br>
	 * 移相效果，类似于哇音<br>
	 * 不同的是对各种音频均有效果<br>
	 * 但是会不断重复，不是很自然
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
	 * 返回当前phaser的参数<br>
	 * 
	 * @return 返回一个长度为6的float数组<br>
	 *         分别为：<br>
	 *         <ul>
	 *         0：DryMix<br>
	 *         1：WetMix<br>
	 *         2：Feedback<br>
	 *         3：Rate<br>
	 *         4：Range<br>
	 *         5：Freq
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
	 * <b><u><li>必须先使用openFX()</u></b><br>
	 * 合唱声效，应该是利用延时得到的声效<br>
	 * 好听不好听就是另外一回事了
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
	 *            CHORUS_FLANGER：弗朗格<br>
	 *            CHORUS_EXAGGERATION：夸张的合唱音效<br>
	 *            CHORUS_MOTOCYCLE：模拟摩托车声音<br>
	 *            CHORUS_DEVIL：一点都不像魔鬼声，某些时候听起来倒是有点悚然<br>
	 *            CHORUS_MANYVOICE：说是很多声音<br>
	 *            CHORUS_CHIPMUNK：如果是有人声的音频，会听到另一个装嫩的声音<br>
	 *            CHORUS_WATER：类似把水慢慢倒进壶子里的声音，某些音频下不明显<br>
	 *            CHORUS_AIRPLANE：一点都不明显<br>
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
	 * 返回当前Chorus的参数<br>
	 * 
	 * @return 返回一个长度为6的float数组<br>
	 *         分别为：<br>
	 *         <ul>
	 *         0：DryMix<br>
	 *         1：WetMix<br>
	 *         2：Feedback<br>
	 *         3：MinSweep<br>
	 *         4：MaxSweep<br>
	 *         5：Rate
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

	//这里用echo4用不了，论坛那边说是有问题，但是给出的新版本的so用不了
	//所以这里用echo2，这是本来废弃的
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
	/*--------------    有许多音效不知为什么      ---------------*/
	/*--------------       以下为测试用              ---------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/
	/*--------------在当前平台不需要用的库类函数---------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	// BASS_SetDevice
	// 设置不同设备播放声音，Android中设备就一个，应该不需要

	// BASS_GetCPU
	// 获取当前运行该库的CPU的使用率

	// BASS_GetVersion
	// 获取当前库的版本

	// BASS_GetDevice
	// 获取设备的号码

	// BASS_GetDeviceInfo
	// 获取设备的相关信息

	// BASS_GetInfo
	// 获取设备的具体信息，之后应该用得上

	// BASS_SetConfigPtr
	// 这貌似是与在线播放有关的、连接服务器配置的

	// BASS_GetConfigPtr
	// 同上

	// 关于样本的方法(BASS_Sample开头的)都不写，因为用不上

	// BASS_ChannelGetTags
	// 可以获取音频文件中的标签信息（比如艺术家、长度什么的）
	// 考虑以后加入

	// BASS_FX_BFX_DAMP
	// 这个效果暂时没听出效果

	// BASS_FX_BFX_DISTORTION
	// 根本就是噪音
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/
	/*--------------------不清楚具体用途---------------------*/
	/*-----------------------------------------------------*/
	/*-----------------------------------------------------*/

	// BASS_Update
	// 指更新channel的缓存，不清楚更新换存的用途
}
