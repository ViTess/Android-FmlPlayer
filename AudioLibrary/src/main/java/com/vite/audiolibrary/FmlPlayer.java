package com.vite.audiolibrary;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.support.annotation.*;
import android.util.SparseArray;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS.BASS_CHANNELINFO;
import com.un4seen.bass.BASS_FX;
import com.un4seen.bass.BASS_FX.BASS_BFX_AUTOWAH;
import com.un4seen.bass.BASS_FX.BASS_BFX_CHORUS;
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
 * utf-8
 *
 * @author WingSun 'ViTe' Tam
 */
public class FmlPlayer {

    private static final String TAG = "FmlPlayer";
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
    /**
     * 设备相应信息
     */
    private static BASS.BASS_INFO mDeviceInfo = new BASS.BASS_INFO();

    private static WeakReference<Context> wRefContext;
    /**
     * 记录是否已经初始化过
     */
    private static boolean isInitAudioEngine = false;
    /**
     * 是否打印log，可以选择不打印，默认打印
     */
    public static boolean Logout = true;

	/*-----------------------------------*/

	/*-----------------静态（全局）方法------------------*/

    /**
     * 初始化音乐播放库(不包含录音)
     *
     * @param context
     * @param isLogout - 是否打印log
     * @return true-成功 ； false-失败
     */
    public static boolean init(Context context, boolean isLogout) {
        if (wRefContext == null)
            FmlPlayer.wRefContext = new WeakReference<Context>(context.getApplicationContext());
        // 保证只初始化一次
        if (!isInitAudioEngine) {
            FmlPlayer.isInitAudioEngine = BASS.BASS_Init(device, freq, flags);
            printError("init");
            BASS.BASS_GetInfo(mDeviceInfo);
            printError("getDeviceInfo");
            printDeviceInfo(context);
        }
        FmlPlayer.Logout = isLogout;
        return isInitAudioEngine;
    }

    /**
     * 释放音乐播放库
     *
     * @return true-成功释放 ； false-失败
     */
    public static boolean free() {
        boolean state = false;
        if (FmlPlayer.isInitAudioEngine)
            state = BASS.BASS_Free();
        printError("free");
        if (state) {
            FmlPlayer.isInitAudioEngine = false;
            FmlPlayer.Logout = true;
        }
        return state;
    }


    /**
     * 全部音频开始播放（在使用overallPause之后）<br>
     * 使用overallStop后不能再调用此方法<br>
     * 请务必谨慎使用
     *
     * @return
     */
    public static boolean globalStart() {
        BASS.BASS_Start();
        return printError("globalStart");
    }

    /**
     * 所有音频暂停输出
     *
     * @return
     */
    public static boolean globalPause() {
        BASS.BASS_Pause();
        return printError("globalPause");
    }

    /**
     * 所有音频停止输出<br>
     * 停止后无法再播放，所以这个慎用
     *
     * @return
     */
    public static boolean globalStop() {
        BASS.BASS_Stop();
        return printError("globalStop");
    }

    /**
     * 设置全局音量，由于可能会影响到其他Channel的单独音量，不建议使用<br>
     *
     * @param volume
     * @return
     */
    public static boolean setGlobalVolume(float volume) {
        BASS.BASS_SetVolume(volume);
        return printError("setGlobalVolume");
    }

    /**
     * 获取当前全局音量
     *
     * @return For errors , it will return -1
     */
    public static float getGlobalVolume() {
        float volume = BASS.BASS_GetVolume();
        if (printError("getGlobalVolume"))
            return volume;
        else
            return -1;
    }

    /**
     * 设置音频库的一些选项，能在任何时候调用<br>
     * 但最好还是在Init后调用<br>
     * 一些网络相关的就不写出来了
     *
     * @param option <li><b>BASS_CONFIG_BUFFER</b> <br>
     *               设置缓冲区大小默认为500ms，只影响stream和music的channel，最大值为5000ms <br>
     *               <br> <li><b>BASS_CONFIG_UPDATEPERIOD</b> <br>
     *               设置缓冲区的更新周期，只影响stream和music的channel，0为禁用自动更新，最小更新周期为5ms，
     *               最大为100ms <br>
     *               <br> <li><b>BASS_CONFIG_GVOL_SAMPLE</b> <br>
     *               设置所有样本的全局音量，从0-10000 <br>
     *               <br> <li><b>BASS_CONFIG_GVOL_STREAM</b> <br>
     *               设置所有音频流的全局音量，从0-10000 <br>
     *               <br> <li><b>BASS_CONFIG_GVOL_MUSIC</b> <br>
     *               设置所有MOD音频的全局音量，从0-10000 <br>
     *               <br> <li><b>BASS_CONFIG_CURVE_VOL</b> <br>
     *               设置音量的增减模式（线性或对数曲线），线形为0%-100%，对数为-100db-0dp <br>
     *               false = 线性，true = 对数 <br>
     *               <br> <li><b>BASS_CONFIG_CURVE_PAN</b> <br>
     *               设置声音平移的增减模式（线性或对数曲线） <br>
     *               这里的平移可能是指音源的平移（如从左到右） <br>
     *               <br> <li><b>BASS_CONFIG_FLOATDSP</b> <br>
     *               设置是否使用(在Android下为24位)浮点运算DSP <br>
     *               api中说使用该设置能使音频经过DSP后不会降低质量 <br>
     *               但容易引发错误，所以应该避免使用 <br>
     *               <br> <li><b>BASS_CONFIG_3DALGORITHM</b> <br>
     *               设置3D音效的算法，有四个选项： <br>
     *               BASS_3DALG_DEFAULT <br>
     *               BASS_3DALG_OFF <br>
     *               BASS_3DALG_FULL <br>
     *               BASS_3DALG_LIGHT <br>
     *               <br> <li><b>BASS_CONFIG_PAUSE_NOPLAY</b> <br>
     *               跟使用overallPause一样意思 <br>
     *               <br> <li><b>BASS_CONFIG_REC_BUFFER</b> <br>
     *               设置录音通道的缓存，1000-5000(ms) <br>
     *               <br> <li><b>BASS_CONFIG_MUSIC_VIRTUAL</b> <br>
     *               设置IT格式的MOD音乐的虚拟通道数 <br>
     *               <br> <li><b>BASS_CONFIG_VERIFY</b> <br>
     *               设置文件格式的验证长度，应该是根据长度获取一定长度的验证信息来验证是否是这个格式 <br>
     *               <br> <li><b>BASS_CONFIG_UPDATETHREADS</b> <br>
     *               更新播放通道的缓冲区的线程 <br>
     *               <br> <li><b>BASS_CONFIG_DEV_BUFFER</b> <br>
     *               设置所有播放通道一起播放时，在最终播放前混合在一起所使用的缓存，用于准备播放 <br>
     *               这个缓存的设置会影响播放和暂停的延迟时间，不能太高，但太低也会导致输出中断 <br>
     *               <br> <li><b>BASS_CONFIG_DEV_DEFAULT</b> <br>
     *               这个别碰 <br>
     *               <br> <li><b>BASS_CONFIG_SRC</b> <br>
     *               设置默认采样率的转换质量 <br>
     *               0 =线性插值，1 = 8点SINC插值，2 = 16点SINC插值，3 = 32点SINC插值。 <br>
     *               <br> <li><b>BASS_CONFIG_ASYNCFILE_BUFFER</b> <br>
     *               异步读取文件的缓冲区长度。 <br>
     *               <br> <li><b>BASS_CONFIG_OGG_PRESCAN</b> <br>
     *               预扫描OGG文件，默认开启 <br>
     *               <br> <li><b>BASS_CONFIG_DEV_NONSTOP</b> <br>
     *               不知道，Android新增的，API完全没提 <br>
     *               <br> <li><b>BASS_CONFIG_NET_PREBUF</b> <br>
     *               与BASS_CONFIG_NET_BUFFER有关，取值为0~100，表示百分比，默认值为75，表示当缓冲至BASS_CONFIG_NET_BUFFER × 75%时，就开始播放 <br>
     *               <br> <li><b>BASS_CONFIG_NET_SEEK</b> <br>
     *               并没有什么卵用(可能跟流媒体有关) <br>
     *               <br> <li><b>BASS_CONFIG_NET_TIMEOUT</b> <br>
     *               设置网络加载的超时时间，默认5000ms <br>
     *               <br> <li><b>BASS_CONFIG_NET_PASSIVE</b> <br>
     *               Use passive mode in FTP connections , 0 - false , 1 - true ?<br>
     *               <br> <li><b>BASS_CONFIG_NET_BUFFER</b> <br>
     *               设置网络加载缓冲区大小，默认5000，单位ms?增加缓冲区大小可以减小缓冲时因为网络卡顿导致播放卡顿的机会，
     *               但是另一方面也增加了预缓冲的时间。另外，网络缓冲区大小应该比播放缓冲区（BASS_CONFIG_BUFFER）要大，
     *               否则也会造成播放卡顿的情况。注意：该设置只对后来生成的播放流有效，对之前的播放流没有影响<br>
     *               <br> <li><b>BASS_CONFIG_NET_READTIMEOUT</b> <br>
     *               设置读取网络数据的时间，当时间一到，将自动停止缓冲数据<br>
     * @param value
     * @return
     */
    public static boolean setGlobalOption(int option, int value) {
        BASS.BASS_SetConfig(option, value);
        return printError("setGlobalOption");
    }

    /**
     * 返回设置的值
     *
     * @param option <li><b>BASS_CONFIG_HANDLES</b> <br>
     *               这个只用于该方法，获取当前句柄数 <br>
     *               <br>
     * @return
     */
    public static int getGlobalOptions(int option) {
        int value = BASS.BASS_GetConfig(option);
        printError("getGlobalOptions");
        return value;
    }

    /**
     * 设置加载网络音频超时时间，默认5000ms
     *
     * @param mesc unit(ms)
     * @return
     */
    public static boolean setNetTimeOut(int mesc) {
        return setGlobalOption(BASS.BASS_CONFIG_NET_TIMEOUT, mesc);
    }

    /**
     * 设置网络加载缓冲区大小，默认5000，单位ms?增加缓冲区大小可以减小缓冲时因为网络卡顿导致播放卡顿的机会，
     * 但是另一方面也增加了预缓冲的时间。另外，网络缓冲区大小应该比播放缓冲区（BASS_CONFIG_BUFFER）要大，
     * 否则也会造成播放卡顿的情况。注意：该设置只对后来生成的播放流有效，对之前的播放流没有影响
     *
     * @param mesc
     * @return
     */
    public static boolean setNetBuffer(int mesc) {
        return setGlobalOption(BASS.BASS_CONFIG_NET_BUFFER, mesc);
    }

    /**
     * 与net buffer有关，取值为0~100，表示百分比，默认值为75，表示当缓冲至BASS_CONFIG_NET_BUFFER × 75%时，就开始播放。
     * 如果想要实时获取到缓冲进度，讲其设置为0最好
     *
     * @param percentage
     * @return
     */
    public static boolean setNetPreBufPercentage(@IntRange(from = 0, to = 100) int percentage) {
        return setGlobalOption(BASS.BASS_CONFIG_NET_PREBUF, percentage);
    }

    /**
     * 设置读取网络数据的时间，当时间一到，将自动停止缓冲数据<br>
     * 默认为0, 0即不开启该功能
     *
     * @param mesc
     * @return
     */
    public static boolean setNetReadBufTime(int mesc) {
        return setGlobalOption(BASS.BASS_CONFIG_NET_READTIMEOUT, mesc);
    }

    private static String getFileName(String text) {
        String fileName = null;
        String suffixes = "ogg|mp1|mp2|mp3|wav";
        Pattern mPat = Pattern.compile("[\\w]+[\\.](" + suffixes + ")");
        Matcher mMc = mPat.matcher(text);
        while (mMc.find())
            fileName = mMc.group();
        return fileName;
    }

    /**
     * 在Log打印错误，返回错误代码
     *
     * @param func 所在的方法
     * @return
     */
    private static boolean printError(String func) {
        if (wRefContext == null) {
            Log.e(TAG, "WeakReference is Null!");
            return false;
        }
        Context context = wRefContext.get();
        if (context == null) {
            Log.e(TAG, "Context is Null!");
            return false;
        }
        int error = BASS.BASS_ErrorGetCode();
        if (FmlPlayer.Logout) {
            String errorStr = String.valueOf(error);
            if (error == -1)
                errorStr = "01";
            int resId = context.getResources().getIdentifier("error_" + errorStr, "string",
                    context.getPackageName());
            String print = resId > 0 ? context.getString(resId) : context.getString(R.string.error_01);
            Log.d(TAG, func + "::" + print);
        }
        return (error == 0);
    }

    private static void printDeviceInfo(Context context) {
        if (mDeviceInfo != null) {
            StringBuilder sb = new StringBuilder();
            String version = context.getString(R.string.version);
            sb.append("FmlPlayer Version ").append(version).append(", base on BASS Library ").append(BASS.BASSVERSIONTEXT).append("\n\n");
            sb.append("Device Info :").append("\n");
            sb.append("Device Capabilities Flags - ").append(mDeviceInfo.flags).append("\n");
            sb.append("Total Hardware Memory - ").append(mDeviceInfo.hwsize).append("\n");
            sb.append("Free Hardware Memory - ").append(mDeviceInfo.hwfree).append("\n");
            sb.append("Free Sample Slots - ").append(mDeviceInfo.freesam).append("\n");//数据帧
            sb.append("Free 3D Sample Slots - ").append(mDeviceInfo.free3d).append("\n");
            sb.append("Min Sample Rate - ").append(mDeviceInfo.minrate).append("\n");//支持的最小rate
            sb.append("Max Sample Rate - ").append(mDeviceInfo.maxrate).append("\n");//支持的最大rate
            sb.append("Support EAX - ").append(mDeviceInfo.eax).append("\n");//是否支持eax
            sb.append("Recommended Minimum Buffer(ms) - ").append(mDeviceInfo.minbuf).append("\n");//推荐的最小缓存(单位ms)
            sb.append("DirectSound Version - ").append(mDeviceInfo.dsver).append("\n");
            sb.append("Latency - ").append(mDeviceInfo.latency).append("\n");//playback的延迟..?
            sb.append("BASS Init Flag - ").append(mDeviceInfo.initflags).append("\n");
            sb.append("Number Of Speakers - ").append(mDeviceInfo.speakers).append("\n");
            sb.append("Freq - ").append(mDeviceInfo.freq).append("\n");
            Log.i(TAG, sb.toString());
        }
    }

    /*-----------------对象（局部）方法------------------*/
    /*-------------局部方法变量------------*/

    /**
     * 每个对象对应一个句柄(音频)
     */
    private int mHandle;
    /**
     * 音频总长度
     */
    private double mTotalTime;
    /**
     * 记录音频文件被设置时的参数
     */
    private DataParam mDataParam;
    /**
     * 是否循环
     */
    private boolean isLoop = false;
    /**
     * 音量
     */
    private float mVolume = 1.0f;
    /**
     * 平移
     */
    private float mPan = 0.0f;
    /**
     * 播放/解码完成监听
     */
    private PlayerListener.OnCompletionListener mCompletionListener;
    /**
     * 错误监听
     */
    private PlayerListener.OnErrorListener mErrorListener;
    /**
     * 加载完毕监听
     */
    private PlayerListener.OnPreparedListener mPreparedListener;
    /**
     * 下载监听事件
     */
    private PlayerListener.OnDownloadedListener mDownloadedListener;
    /**
     * Handler
     */
    private FmlHandler mFmlHandler;
    /**
     * 创建一个单线程-线程池，用于网络异步加载
     */
    private ExecutorService mSingleExecutor;
    /**
     * 每个对象的音频的参数
     */
    private BASS_CHANNELINFO mInfo;
    /**
     * 同步调用，channel到了播放（解码）末端时调起
     */
    private final BASS.SYNCPROC mEndSync = new BASS.SYNCPROC() {
        @Override
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            if (mCompletionListener != null)
                mFmlHandler.sendEmptyMessage(FmlHandler.MSG_COMPLETION);
        }
    };
    /**
     * 设置EndSync后得到的对应的句柄，0为设置失败
     */
    private int mEndSyncHandle = 0;

    private final BASS.DOWNLOADPROC mDownloadSync = new BASS.DOWNLOADPROC() {
        /**
         * @param byteBuffer
         * @param length
         * @param user 此处的user是在StreamCreateURL时设置的用于标记
         */
        @Override
        public void DOWNLOADPROC(ByteBuffer byteBuffer, int length, Object user) {
            //由于多个handle都可以对应同一个sync
            //所以用到Object user这个变量来分辨
            if (mDownloadedListener != null)
                mDownloadedListener.onDownloaded(FmlPlayer.this, byteBuffer, length);
        }
    };
    /*-----------------------------------*/

    /**
     * 创建对象
     */
    public FmlPlayer() {
        if (wRefContext == null) {
            Log.e(TAG, "WeakReference is Null!");
            return;
        }
        Context context = wRefContext.get();
        if (context == null) {
            Log.e(TAG, "Context is Null!");
            return;
        }
        if (mFmlHandler == null)
            mFmlHandler = new FmlHandler(context.getMainLooper());
        if (mSingleExecutor == null)
            mSingleExecutor = Executors.newSingleThreadExecutor();
        if (mHandle != 0) {
            BASS.BASS_StreamFree(mHandle);
            printError("FmlPlayer()");
        }

        mInfo = new BASS_CHANNELINFO();

        mHandle = 0;
        isLoop = false;
        mTotalTime = 0;
        mDataParam = null;
        mVolume = 1.0f;
        mPan = 0f;
        mCompletionListener = null;
        mErrorListener = null;
        mPreparedListener = null;
        mDownloadedListener = null;
    }

    /**
     * 获取音频的信息
     */
    private void setInfo() {
        BASS.BASS_ChannelGetInfo(mHandle, mInfo);
        printError("getInfo");
        callErrorListener();
    }

    /**
     * 获取总时长
     */
    private void setTotalTime() {
        mTotalTime = BASS.BASS_ChannelBytes2Seconds(mHandle, BASS.BASS_ChannelGetLength(mHandle, BASS.BASS_POS_BYTE));
        printError("setTotalTime");
        callErrorListener();
    }

    /**
     * 设置参数
     *
     * @param fileType
     * @param fileName
     * @param filePath
     * @param offSet
     * @param length
     * @param flag
     */
    private void setDataParam(@DataParam.type byte fileType, String fileName, String filePath, int offSet, int length, int flag) {
        mDataParam = new DataParam();
        mDataParam.fileType = fileType;
        mDataParam.filePath = filePath;
        mDataParam.fileName = fileName;
        mDataParam.offSet = offSet;
        mDataParam.length = length;
        mDataParam.flag = flag;
    }

    /**
     * 调用错误监听
     */
    private void callErrorListener() {
        if (mErrorListener != null) {
            int errorCode = BASS.BASS_ErrorGetCode();
            if (errorCode != 0) {//不成功
                mFmlHandler.obtainMessage(FmlHandler.MSG_ERROR, errorCode, 0).sendToTarget();
            }
        }
    }

    /**
     * 调用预备完毕监听
     */
    private void callPrepareListener() {
        if (mPreparedListener != null) {
            mFmlHandler.obtainMessage(FmlHandler.MSG_PREPARED).sendToTarget();
        }
    }

    /**
     * 设置播放结束监听
     */
    private void setCompletionListener() {
        if (mCompletionListener != null) {
            mEndSyncHandle = BASS.BASS_ChannelSetSync(mHandle, BASS.BASS_SYNC_END, 0, mEndSync, 0);
            printError("setCompletionListener");
            if (mEndSyncHandle == 0)
                callErrorListener();
        }
    }

    /**
     * @param listener
     */
    public void setOnCompletionListener(PlayerListener.OnCompletionListener listener) {
        this.mCompletionListener = listener;
    }

    public void setOnErrorListener(PlayerListener.OnErrorListener listener) {
        this.mErrorListener = listener;
    }

    public void setOnPreparedListener(PlayerListener.OnPreparedListener listener) {
        this.mPreparedListener = listener;
    }

    public void setOnDownloadedListener(PlayerListener.OnDownloadedListener listener) {
        this.mDownloadedListener = listener;
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

    /**
     * @param fileName the file name in assets folder
     * @see #setAssetFile(String fileName, int offSet, int length, int flag)
     */
    public void setAssetFile(@NonNull String fileName) {
        setAssetFile(fileName, 0);
    }

    /**
     * @param fileName the file name in assets folder
     * @param offSet
     * @see #setAssetFile(String fileName, int offSet, int length, int flag)
     */
    public void setAssetFile(@NonNull String fileName, int offSet) {
        setAssetFile(fileName, offSet, 0);
    }

    /**
     * @param fileName the file name in assets folder
     * @param offSet
     * @param length
     * @see #setAssetFile(String fileName, int offSet, int length, int flag)
     */
    public void setAssetFile(@NonNull String fileName, int offSet, int length) {
        setAssetFile(fileName, offSet, length, 0);
    }

    /**
     * Create music file in assets folder
     *
     * @param fileName the file name in assets folder
     * @param offSet   偏移量，从什么地方开始播放，0从文件头开始
     * @param length   播放长度，0为播放到文件尾
     * @param flag     <p><li><b>BASS_SAMPLE_FLOAT</b></li>
     *                 使用32位浮点样本数据
     *                 <li><b>BASS_SAMPLE_MONO</b></li>
     *                 用单声道播放(只用于MP1、MP2、MP3)，减少CPU的使用
     *                 <li><b>BASS_SAMPLE_SOFTWARE</b></li>
     *                 使用软件混合音频，不使用硬件
     *                 <li><b>BASS_SAMPLE_3D</b></li>
     *                 使用3D功能
     *                 <li><b>BASS_SAMPLE_LOOP</b></li>
     *                 循环，不在这里设置也行
     *                 <li><b>BASS_SAMPLE_FX</b></li>
     *                 如果要使用FX的功能就用这个flag
     *                 <li><b>BASS_STREAM_PRESCAN</b></li>
     *                 预先扫描MP1、MP2、MP3和链状OGG文件，这样会耗时间
     *                 <li><b>BASS_STREAM_AUTOFREE</b></li>
     *                 播放完毕后会自动清空该音频流，或者调用stop都会执行
     *                 <li><b>BASS_STREAM_DECODE</b></li>
     *                 创建一个解码通道，不能用于播放
     *                 <li><b>BASS_SPEAKER_xxx</b></li>
     *                 扬声器的一些配置，不鸟他
     *                 <li><b>BASS_ASYNCFILE</b></li>
     *                 异步读取文件。当启用时，该文件读取和缓冲并行解码</p>
     */
    public void setAssetFile(@NonNull String fileName, int offSet, int length, int flag) {
        setDataParam(DataParam.FILE_TYPE_ASSETS, fileName, null, offSet, length, flag);
    }

    /**
     * @param filePath the file path in External Storage Directory
     */
    public void setExternalFile(@NonNull String filePath) throws IOException {
        setExternalFile(filePath, 0);
    }

    /**
     * @param filePath the file path in External Storage Directory
     * @param offSet
     */
    public void setExternalFile(@NonNull String filePath, int offSet) throws IOException {
        setExternalFile(filePath, offSet, 0);
    }

    /**
     * @param filePath the file path in External Storage Directory
     * @param offSet
     * @param length
     */
    public void setExternalFile(@NonNull String filePath, int offSet, int length) throws IOException {
        setExternalFile(filePath, offSet, length, 0);
    }

    /**
     * Create music file in External Storage Directory
     *
     * @param filePath
     * @param offSet   偏移量，从什么地方开始播放，0从文件头开始
     * @param length   播放长度，0为播放到文件尾
     * @param flag     <p><li><b>BASS_SAMPLE_FLOAT</b></li>
     *                 使用32位浮点样本数据
     *                 <li><b>BASS_SAMPLE_MONO</b></li>
     *                 用单声道播放(只用于MP1、MP2、MP3)，减少CPU的使用
     *                 <li><b>BASS_SAMPLE_SOFTWARE</b></li>
     *                 使用软件混合音频，不使用硬件
     *                 <li><b>BASS_SAMPLE_3D</b></li>
     *                 使用3D功能
     *                 <li><b>BASS_SAMPLE_LOOP</b></li>
     *                 循环，不在这里设置也行
     *                 <li><b>BASS_SAMPLE_FX</b></li>
     *                 如果要使用FX的功能就用这个flag
     *                 <li><b>BASS_STREAM_PRESCAN</b></li>
     *                 预先扫描MP1、MP2、MP3和链状OGG文件，这样会耗时间
     *                 <li><b>BASS_STREAM_AUTOFREE</b></li>
     *                 播放完毕后会自动清空该音频流，或者调用stop都会执行
     *                 <li><b>BASS_STREAM_DECODE</b></li>
     *                 创建一个解码通道，不能用于播放
     *                 <li><b>BASS_SPEAKER_xxx</b></li>
     *                 扬声器的一些配置，不鸟他
     *                 <li><b>BASS_ASYNCFILE</b></li>
     *                 异步读取文件。当启用时，该文件读取和缓冲并行解码</p>
     * @throws IOException if file is not exist
     */
    public void setExternalFile(@NonNull String filePath, int offSet, int length, int flag) throws IOException {
        String mFileName = null;
        File mFile = new File(filePath);
        if (!mFile.isDirectory() && mFile.exists()) {
            mFileName = getFileName(filePath);
            if (mFileName != null) {
                setDataParam(DataParam.FILE_TYPE_EXTERNAL, mFileName, filePath, offSet, length, flag);
                return;
            }
        }
        throw new IOException(filePath + " is not found");
    }

    public void setNetFile(@NonNull String url) throws IOException {
        setNetFile(url, 0);
    }

    public void setNetFile(@NonNull String url, int offSet) throws IOException {
        setNetFile(url, offSet, 0);
    }

    /**
     * @param url
     * @param offSet File position to start streaming from. This is ignored by some servers, specifically when the length is unknown/undefined.
     * @param flag   <p><li><b>BASS_SAMPLE_FLOAT</b></li>
     *               Use 32-bit floating-point sample data. See Floating-point channels for info.
     *               <li><b>BASS_SAMPLE_MONO</b></li>
     *               Decode/play the stream (MP3/MP2/MP1 only) in mono, reducing the CPU usage (if it was originally stereo).
     *               This flag is automatically applied if BASS_DEVICE_MONO was specified when calling BASS_Init.
     *               <li><b>BASS_SAMPLE_SOFTWARE</b></li>
     *               Force the stream to not use hardware mixing.
     *               <li><b>BASS_SAMPLE_3D</b></li>
     *               Enable 3D functionality. This requires that the BASS_DEVICE_3D flag was specified when calling BASS_Init,
     *               and the stream must be mono. The SPEAKER flags cannot be used together with this flag.
     *               <li><b>BASS_SAMPLE_LOOP</b></li>
     *               Loop the file. This flag can be toggled at any time using BASS_ChannelFlags. This flag is ignored when
     *               streaming in blocks (BASS_STREAM_BLOCK).
     *               <li><b>BASS_SAMPLE_FX</b></li>
     *               Enable the old implementation of DirectX 8 effects. See the DX8 effect implementations section for details.
     *               Use BASS_ChannelSetFX to add effects to the stream.
     *               <li><b>BASS_STREAM_RESTRATE</b></li>
     *               Restrict the download rate of the file to the rate required to sustain playback. If this flag is not
     *               used, then the file will be downloaded as quickly as the user's internet connection allows.
     *               <li><b>BASS_STREAM_BLOCK</b></li>
     *               Download and play the file in smaller chunks, instead of downloading the entire file to memory. Uses a
     *               lot less memory than otherwise, but it is not possible to seek or loop the stream; once it has ended, the file must be
     *               opened again to play it again. This flag will automatically be applied when the file length is unknown, for example with
     *               Shout/Icecast streams. This flag also has the effect of restricting the download rate.
     *               <li><b>BASS_STREAM_STATUS</b></li>
     *               Pass status info (HTTP/ICY tags) from the server to the DOWNLOADPROC callback during connection. This
     *               can be useful to determine the reason for a failure.
     *               <li><b>BASS_STREAM_AUTOFREE</b></li>
     *               Automatically free the stream when playback ends.
     *               <li><b>BASS_STREAM_DECODE</b></li>
     *               Decode the sample data, without playing it. Use BASS_ChannelGetData to retrieve decoded sample data. The
     *               BASS_SAMPLE_3D, BASS_STREAM_AUTOFREE and SPEAKER flags cannot be used together with this flag. The BASS_SAMPLE_SOFTWARE
     *               and BASS_SAMPLE_FX flags are also ignored.
     *               <li><b>BASS_SPEAKER_xxx</b></li>
     *               Speaker assignment flags. These flags have no effect when the stream is more than stereo.
     *               <li><b>BASS_UNICODE</b></li>
     *               url is in UTF-16 form. Otherwise it is ANSI on Windows or Windows CE, and UTF-8 on other platforms.</p>
     */
    public void setNetFile(@NonNull String url, int offSet, int flag) throws IOException {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int mFlag = flag;
            if (mFlag != BASS.BASS_STREAM_STATUS)
                mFlag |= BASS.BASS_STREAM_STATUS;
            setDataParam(DataParam.FILE_TYPE_URL, getFileName(url), url, offSet, 0, mFlag);
            return;
        }
        throw new IOException("Url is not WebSite");
    }

    public void prepare() {
        if (wRefContext == null) {
            Log.e(TAG, "WeakReference is Null!");
            return;
        }
        Context context = wRefContext.get();
        if (context == null) {
            Log.e(TAG, "Context is Null!");
            return;
        }
        synchronized (this) {
            boolean isPreparedSuccess = false;
            final DataParam mParam = mDataParam;
            switch (mParam.fileType) {
                case DataParam.FILE_TYPE_ASSETS:
                    mHandle = BASS.BASS_StreamCreateFile(new BASS.Asset(context.getAssets(), mParam.fileName), mParam.offSet,
                            mParam.length, mParam.flag);
                    isPreparedSuccess = printError("Assets (" + mParam.fileName + "," + mParam.offSet + "," + mParam.length + "," + mParam.flag +
                            ")");
                    break;
                case DataParam.FILE_TYPE_EXTERNAL:
                    mHandle = BASS.BASS_StreamCreateFile(mParam.filePath, mParam.offSet, mParam.length, mParam.flag);
                    isPreparedSuccess = printError("External (" + mParam.filePath + "," + mParam.offSet + "," + mParam.length + "," + mParam.flag +
                            ")");
                    break;
                case DataParam.FILE_TYPE_URL:
                    mHandle = BASS.BASS_StreamCreateURL(mParam.filePath, mParam.offSet, mParam.flag, mDownloadSync, null);
                    isPreparedSuccess = printError("Url (" + mParam.filePath + "," + mParam.offSet + "," + mParam.flag + ")");
                    break;
            }
            callErrorListener();
            if (isPreparedSuccess) {
                setTotalTime();
                setInfo();
                setCompletionListener();
                callPrepareListener();
                setSync();
            }
        }
    }

    public void prepareAsync() {
        if (mSingleExecutor != null && !mSingleExecutor.isShutdown()) {
            mSingleExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    prepare();
                }
            });
        }
    }

    /**
     * add setting in prepareSync
     */
    private void setSync() {
        //由于在加载网络文件时存在异步
        //handle不会立刻生成
        //所以需要在异步加载成功后再设置一次
        if (mDataParam != null && mDataParam.fileType == DataParam.FILE_TYPE_URL) {
            setLooping(isLoop);
            setVolume(mVolume);
            setPanning(mPan);
        }
    }

    public boolean play() {
        synchronized (this) {
            // 这里的restart参数指是否重新播放
            // false为不重放，在当前暂停处开始，pause和stop一样
            // true为重放，重新开始，pause和stop一样
            BASS.BASS_ChannelPlay(mHandle, false);
            return printError("Play");
        }
    }

    /**
     * 暂停<br>
     * Play后会从暂停点开始播放
     *
     * @return
     */
    public boolean pause() {
        synchronized (this) {
            BASS.BASS_ChannelPause(mHandle);
            return printError("Pause");
        }
    }

    /**
     * 停止播放<br>
     * Play后会重新播放
     *
     * @return
     */
    public boolean stop() {
        synchronized (this) {
            // 由于设置了不重放，stop和pause一样从暂停处开始
            // 所以这里要做处理
            BASS.BASS_ChannelStop(mHandle);
            seekTo(0d);
            return (printError("Stop") == seekTo(0d));
        }
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
        this.isLoop = isLoop;
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
        mVolume = volume;
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
     * @param pan <br>
     *            -1:完全在左边<br>
     *            0:中间<br>
     *            1:完全在右边
     * @return
     */
    public boolean setPanning(float pan) {
        mPan = pan;
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

    /**
     * 播放网络音频时，使用该方法获取当前缓冲百分比
     * 注意：在播放音频时如果设置了offSet，方法中调用的获取文件长度的方法也会相应改变
     * <p/>
     * get the percentage of buffer when you play online audio
     *
     * @return [0.0, 100.0]
     */
    @FloatRange(from = 0.0, to = 100.0)
    public float getBufferPercentage() {
        float pre = 0.0f;
        if (mDataParam != null && mDataParam.fileType == DataParam.FILE_TYPE_URL) {
            long buf = BASS.BASS_StreamGetFilePosition(mHandle, BASS.BASS_FILEPOS_BUFFER);
            long len = BASS.BASS_StreamGetFilePosition(mHandle, BASS.BASS_FILEPOS_END);
            pre = 100.0f * buf / len;
            printError("getBufferPercentage");
        }
        return pre;
    }

    /**
     * 如果播放的是网络音频，
     * 无法seekTo到未缓冲到的地方，会提示Invaild Handle
     * <p/>
     * if play the music on net ,
     * seekTo can't move to positon where the buffer is not loading.
     *
     * @param seconds
     * @return
     */
    public boolean seekTo(double seconds) {
        // BASS.BASS_ChannelSeconds2Bytes，根据时间获取所在的byte
        BASS.BASS_ChannelSetPosition(mHandle, BASS.BASS_ChannelSeconds2Bytes(mHandle, seconds),
                BASS.BASS_POS_BYTE);
        return printError("seekTo");
    }

    /**
     * 返回整个音频的长度(秒)
     * 注意：如果在调用播放时设置了offset，返回的长度是去掉offset之前的时间长度
     * <p/>
     * return audio total length(unit:s)
     * Attention: if set offset when play audio , it will return the (total length - offset)
     *
     * @return
     */
    public double getTotalTime() {
        return mTotalTime;
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
        if (mDataParam != null)
            return mDataParam.fileName;
        return null;
    }

    /**
     * 该方法有bug。
     * 首先，如果播放的文件是mp3，就要使用id3v2来获取meta数据。
     * 但是返回是乱码。
     * 解决方法是使用tag库来解析。
     * 另外，当播放不同的音频时，要根据音频类型设置不同的tag标志来获取数据
     * <p/>
     * It has bug,
     * if play mp3 , the meta data is id3v2,
     * but the data is messy code
     *
     * @return
     */
    @Deprecated
    public String getMetaData() {
        String data = null;
        Object obj = BASS.BASS_ChannelGetTags(mHandle, BASS.BASS_TAG_ID3V2);
        printError("getMetaData");
        if (obj != null) {
            ByteBuffer bb = (ByteBuffer) obj;
            CharBuffer charBuffer = null;
            try {
                Charset charset = Charset.forName("ISO-8859-1");//ISO-8859-1
                CharsetDecoder decoder = charset.newDecoder();
                charBuffer = decoder.decode(bb);
                data = charBuffer.toString();
//                Log.v("getMetaData iso",charBuffer.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
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

    private void reset_() {
        isLoop = false;
        mTotalTime = 0;
        mDataParam = null;
        mVolume = 1.0f;
        mPan = 0f;
        mInfo = null;
        mCompletionListener = null;
        mErrorListener = null;
        mPreparedListener = null;
        mDownloadedListener = null;
        BASS.BASS_StreamFree(mHandle);
        mHandle = 0;
    }

    /**
     * reset 重置状态
     */
    public void reset() {
        stop();
        synchronized (this) {
            reset_();
        }
    }

    /**
     * 释放、清空<br>
     * 调用该函数后请重新new一个
     *
     * @return
     */
    public boolean release() {
        synchronized (this) {
            mFmlHandler = null;
            if (mSingleExecutor != null && !mSingleExecutor.isShutdown()) {
                mSingleExecutor.shutdownNow();
                mSingleExecutor = null;
            }
            if (mHandle != 0 && mEndSyncHandle != 0)
                BASS.BASS_ChannelRemoveSync(mHandle, mEndSyncHandle);
            reset_();
            return printError("release");
        }
    }

    private static class DataParam {
        static final byte FILE_TYPE_ASSETS = 0x01;
        static final byte FILE_TYPE_EXTERNAL = FILE_TYPE_ASSETS + 0x02;
        static final byte FILE_TYPE_URL = FILE_TYPE_ASSETS + 0x04;

        @IntDef({FILE_TYPE_ASSETS, FILE_TYPE_EXTERNAL, FILE_TYPE_URL})
        @interface type {
        }

        @type
        byte fileType;
        String fileName;
        String filePath;
        int offSet;
        int length;
        int flag;
    }

	/*------------------以上为基本功能，已完成-------------------*/
    /*------------------以下为扩展功能-------------------*/

    /**
     * @param <V> the handle with FX
     * @param <T> the param about FX
     */
    private static class HandlePair<V, T> {
        public V handle;
        public T param;

        public HandlePair() {
        }

        public HandlePair(V handle, T param) {
            this.handle = handle;
            this.param = param;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = false;

            if (!(o instanceof HandlePair))
                return result;

            HandlePair<V, T> mPair = (HandlePair<V, T>) o;

            if (mPair.param == null)
                return result;

            if ((param instanceof BASS_BFX_ROTATE) && (mPair.param instanceof BASS_BFX_ROTATE)) {//rotate
                BASS_BFX_ROTATE p1 = (BASS_BFX_ROTATE) this.param;
                BASS_BFX_ROTATE p2 = (BASS_BFX_ROTATE) mPair.param;
                result = (p1.fRate == p2.fRate);
            } else if ((param instanceof BASS_BFX_AUTOWAH) && (mPair.param instanceof BASS_BFX_AUTOWAH)) {//autowah
                BASS_BFX_AUTOWAH p1 = (BASS_BFX_AUTOWAH) param;
                BASS_BFX_AUTOWAH p2 = (BASS_BFX_AUTOWAH) mPair.param;
                result = ((p1.fDryMix == p2.fDryMix) &&
                        (p1.fWetMix == p2.fWetMix) &&
                        (p1.fFeedback == p2.fFeedback) &&
                        (p1.fRate == p2.fRate) &&
                        (p1.fRange == p2.fRange) &&
                        (p1.fFreq == p2.fFreq));
            } else if ((param instanceof BASS_BFX_PHASER) && (mPair.param instanceof BASS_BFX_PHASER)) {//phaser
                BASS_BFX_PHASER p1 = (BASS_BFX_PHASER) param;
                BASS_BFX_PHASER p2 = (BASS_BFX_PHASER) mPair.param;
                result = ((p1.fDryMix == p2.fDryMix) &&
                        (p1.fWetMix == p2.fWetMix) &&
                        (p1.fFeedback == p2.fFeedback) &&
                        (p1.fRate == p2.fRate) &&
                        (p1.fRange == p2.fRange) &&
                        (p1.fFreq == p2.fFreq));
            } else if ((param instanceof BASS_BFX_CHORUS) && (mPair.param instanceof BASS_BFX_CHORUS)) {//chorus
                BASS_BFX_CHORUS p1 = (BASS_BFX_CHORUS) param;
                BASS_BFX_CHORUS p2 = (BASS_BFX_CHORUS) mPair.param;
                result = ((p1.fDryMix == p2.fDryMix) &&
                        (p1.fWetMix == p2.fWetMix) &&
                        (p1.fFeedback == p2.fFeedback) &&
                        (p1.fRate == p2.fRate) &&
                        (p1.fMaxSweep == p2.fMaxSweep) &&
                        (p1.fMinSweep == p2.fMinSweep));
            } else if ((param instanceof BASS_BFX_ECHO4) && (mPair.param instanceof BASS_BFX_ECHO4)) {//echo
                BASS_BFX_ECHO4 p1 = (BASS_BFX_ECHO4) param;
                BASS_BFX_ECHO4 p2 = (BASS_BFX_ECHO4) mPair.param;
                result = ((p1.fDryMix == p2.fDryMix) &&
                        (p1.fWetMix == p2.fWetMix) &&
                        (p1.fFeedback == p2.fFeedback) &&
                        (p1.fDelay == p2.fDelay) &&
                        (p1.bStereo == p2.bStereo));
            } else if ((param instanceof BASS_BFX_PEAKEQ) && (mPair.param instanceof BASS_BFX_PEAKEQ)) {//BASS_BFX_PEAKEQ
                BASS_BFX_PEAKEQ p1 = (BASS_BFX_PEAKEQ) param;
                BASS_BFX_PEAKEQ p2 = (BASS_BFX_PEAKEQ) mPair.param;
                result = ((p1.lBand == p2.lBand) &&
                        (p1.fGain == p2.fGain) &&
                        (p1.fCenter == p2.fCenter) &&
                        (p1.fBandwidth == p2.fBandwidth) &&
                        (p1.fQ == p2.fQ));
            }

            return result;
        }

        /**
         * @return the param class type
         */
        public int getParamType() {
            if (param == null)
                return 0;

            if (param instanceof BASS_BFX_ROTATE)
                return BASS_FX.BASS_FX_BFX_ROTATE;

            if (param instanceof BASS_BFX_AUTOWAH)
                return BASS_FX.BASS_FX_BFX_AUTOWAH;

            if (param instanceof BASS_BFX_PHASER)
                return BASS_FX.BASS_FX_BFX_PHASER;

            if (param instanceof BASS_BFX_CHORUS)
                return BASS_FX.BASS_FX_BFX_CHORUS;

            if (param instanceof BASS_BFX_ECHO4)
                return BASS_FX.BASS_FX_BFX_ECHO4;

            if (param instanceof BASS_BFX_PEAKEQ)
                return BASS_FX.BASS_FX_BFX_PEAKEQ;

            return 0;
        }
    }

    /**
     * Control audioFX
     * base on BASS_FX , because Android unsupport dx8(i guess)
     */
    public static class FxController {
        private static final int BAND_FX_EQ_100 = 0x02;
        private static final int BAND_FX_EQ_600 = 0x04;
        private static final int BAND_FX_EQ_1k = 0x06;
        private static final int BAND_FX_EQ_8k = 0x08;
        private static final int BAND_FX_EQ_14k = 0x0a;

        private static final int BAND_FX_EQ = 0x80;

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
        /**
         * rotate音效参数
         */
        private HandlePair<Integer, BASS_BFX_ROTATE> mParam_rotate;
        /**
         * 自动哇音音效参数
         */
        private HandlePair<Integer, BASS_BFX_AUTOWAH> mParam_autowah;
        /**
         * 移相音效参数
         */
        private HandlePair<Integer, BASS_BFX_PHASER> mParam_phaser;
        /**
         * 合唱音效参数(包含flanger)
         */
        private HandlePair<Integer, BASS_BFX_CHORUS> mParam_chorus;
        /**
         * 回声声效
         */
        private HandlePair<Integer, BASS_BFX_ECHO4> mParam_echo;

        /**
         * 记录peak eq 的参数
         * key - band
         * valve - HandlePair<Integer, BASS_BFX_PEAKEQ>
         */
        private SparseArray<HandlePair<Integer, BASS_BFX_PEAKEQ>> mPeakEQList;
        /**
         * player对应的handle句柄
         */
        private int mPlayerHandle = 0;

        private boolean isFXOpen = false;

        public FxController() {
        }

        /**
         * 开启FX
         *
         * @return
         */
        private boolean openFX() {
            boolean state = (BASS.BASS_ChannelFlags(mPlayerHandle, BASS.BASS_SAMPLE_FX, BASS.BASS_SAMPLE_FX) != -1) ? true
                    : false;
            printError("OpenFX");
            // BASS_FX.BASS_FX_GetVersion()返回16进制
            // 如0x02040b01，则版本为2.4.11.1
            // 当前使用版本为2.4.11.1，若不是这个结果，则加载bass_fx库失败
            String vision = Integer.toHexString(BASS_FX.BASS_FX_GetVersion());
            printError("vision:" + vision);
            return (state && (vision.equals("2040b01")));
        }

        /**
         * 关闭FX
         *
         * @return
         */
        private boolean closeFX() {
            BASS.BASS_FXReset(mPlayerHandle);
            printError("closeFX::Reset");
            boolean state = (BASS.BASS_ChannelFlags(mPlayerHandle, 0, BASS.BASS_SAMPLE_FX) != -1) ? true : false;
            printError("closeFX");
            mPlayerHandle = 0;
            return state;
        }

        /**
         * release controller
         *
         * @return
         */
        public FxController release() {
            closeFX();
            return this;
        }

        /**
         * When you set up some FX, you should call update
         *
         * @return
         */
        public FxController update(FmlPlayer player) {

            synchronized (player) {
                mPlayerHandle = player.getHandle();
                if (!isFXOpen)
                    isFXOpen = openFX();

                update(mPlayerHandle, mParam_rotate);//rotate
                update(mPlayerHandle, mParam_autowah);//autowah
                update(mPlayerHandle, mParam_phaser);//phaser
                update(mPlayerHandle, mParam_chorus);//chorus
                update(mPlayerHandle, mParam_echo);//echo
                if (mPeakEQList != null) {
                    int size = mPeakEQList.size();
                    for (int i = 0; i < size; i++) {
                        HandlePair<Integer, BASS_BFX_PEAKEQ> mPeakEQ = mPeakEQList.valueAt(i);
                        update(mPlayerHandle, mPeakEQ);
                    }
                }
            }
            return this;
        }

        private void update(int playerHandle, Object obj) {
            if (obj == null)
                return;

            HandlePair mParam = (HandlePair) obj;
            boolean isUpdate = true;
            int mFxHandle = mParam.handle == null ? 0 : (int) mParam.handle;
            if (mFxHandle == 0) {
                mFxHandle = BASS.BASS_ChannelSetFX(playerHandle, mParam.getParamType(), 0);
                mParam.handle = mFxHandle;
            } else {
                Object o = new Object();
                switch (mParam.getParamType()) {
                    case BASS_FX.BASS_FX_BFX_ROTATE:
                        o = new BASS_BFX_ROTATE();
                        break;
                    case BASS_FX.BASS_FX_BFX_AUTOWAH:
                        o = new BASS_BFX_AUTOWAH();
                        break;
                    case BASS_FX.BASS_FX_BFX_PHASER:
                        o = new BASS_BFX_PHASER();
                        break;
                    case BASS_FX.BASS_FX_BFX_CHORUS:
                        o = new BASS_BFX_CHORUS();
                        break;
                    case BASS_FX.BASS_FX_BFX_ECHO4:
                        o = new BASS_BFX_ECHO4();
                        break;
                    case BASS_FX.BASS_FX_BFX_PEAKEQ:
                        o = new BASS_BFX_PEAKEQ();
                        break;
                }
                //BASS_FX_BFX_ECHO4 has bug，when use BASS_FXGetParameters
                //jni will crash
                if (mParam.getParamType() != BASS_FX.BASS_FX_BFX_ECHO4) {
                    BASS.BASS_FXGetParameters(mFxHandle, o);
                    HandlePair hp = new HandlePair(0, o);
                    if (mParam.equals(hp))
                        isUpdate = false;
                }
            }

            if (isUpdate) {
                BASS.BASS_FXSetParameters(mFxHandle, mParam.param);
                printError("update type : " + mParam.getParamType());
            }
        }

        /**
         * <b><u><li>必须使用耳机</li></u></b>
         * <ul>
         * 开启Rotate(旋转)效果<br>
         * 在使用耳机时能听到音乐轮流从左到右发声<br>
         * 听起来像在旋转一样<br>
         * 默认好像为0.19997086
         * </ul>
         *
         * @param rate [0.00-1.00] 经测试证明0.00-1.00这个区间效果最好
         * @return
         */
        public FxController setRotate(float rate) {
            if (mParam_rotate == null) {
                BASS_BFX_ROTATE mRotate = new BASS_BFX_ROTATE();
                mParam_rotate = new HandlePair<>();
                mParam_rotate.param = mRotate;
            }
            mParam_rotate.param.fRate = rate;
            mParam_rotate.param.lChannel = BASS_FX.BASS_BFX_CHANALL;
            return this;
        }

        public float getRotate() {
            if (mParam_rotate == null)
                return 0;

            int handle = mParam_rotate.handle == null ? 0 : mParam_rotate.handle;

            if (handle == 0)
                return 0;

            BASS_BFX_ROTATE mRotate = new BASS_BFX_ROTATE();
            BASS.BASS_FXGetParameters(handle, mRotate);
            printError("getRotate");
            return mRotate.fRate;
        }

        public FxController resetRotate() {
            if (mParam_rotate == null)
                return this;

            int handle = mParam_rotate.handle == null ? 0 : mParam_rotate.handle;
            if (handle != 0) {
                // FXReset没作用
                // boolean state = BASS.BASS_FXReset(mFX_Handle.get(FX_ROTATE));
                // 这里remove之后，fx对应的handle就被清空了
                // 要重新Open
                boolean state = BASS.BASS_ChannelRemoveFX(mPlayerHandle, handle);
                printError("closeRotate");
            }
            mParam_rotate = null;
            return this;
        }

        /**
         * It used to private
         *
         * @param band
         * @param gain
         * @return
         */
        private FxController setPeakEQInner(int band, float q, float bandWidth, float center, float gain) {
            if (mPeakEQList == null)
                mPeakEQList = new SparseArray<>();

            HandlePair<Integer, BASS_BFX_PEAKEQ> mPeakEQ = mPeakEQList.get(band);
            if (mPeakEQ == null) {
                mPeakEQ = new HandlePair<>(0, new BASS_BFX_PEAKEQ());
            }
            mPeakEQ.param.lBand = band;
            mPeakEQ.param.fQ = q;
            mPeakEQ.param.fBandwidth = bandWidth;//default : 0.1
            mPeakEQ.param.fCenter = center;//default : 1.0
            mPeakEQ.param.fGain = gain;
            mPeakEQ.param.lChannel = BASS_FX.BASS_BFX_CHANALL;

            mPeakEQList.put(band, mPeakEQ);
            return this;
        }

        /**
         * @param band
         * @return
         */
        private BASS_BFX_PEAKEQ getPeakEQInner(int band) {
            if (mPeakEQList == null)
                return null;

            HandlePair<Integer, BASS_BFX_PEAKEQ> mPeakEQ = mPeakEQList.get(band);
            if (mPeakEQ == null)
                return null;

            int handle = mPeakEQ.handle == null ? 0 : mPeakEQ.handle;
            if (handle == 0)
                return null;

            BASS_BFX_PEAKEQ eq = new BASS_BFX_PEAKEQ();
            eq.lBand = band;//need band number to get that params
            BASS.BASS_FXGetParameters(handle, eq);
            printError("getPeakEQ4Band");

            return eq;
        }

        /**
         * It used to private
         *
         * @param band
         * @return
         */
        private FxController resetPeakEQInner(int band) {
            if (mPeakEQList == null)
                return null;

            HandlePair<Integer, BASS_BFX_PEAKEQ> mPeakEQ = mPeakEQList.get(band);
            int handle = mPeakEQ.handle == null ? 0 : mPeakEQ.handle;
            if (mPeakEQ == null || handle <= 0)
                return null;

            BASS.BASS_ChannelRemoveFX(mPlayerHandle, handle);
            printError("resetPeakEQ4Inner," + band);
            mPeakEQList.remove(band);
            return this;
        }

        /**
         * <br>A Peak equalizer</br>
         * 峰值均衡器
         * <p>detail: http://www.sengpielaudio.com/calculator-cutoffFrequencies.htm</p>
         * <p>chinese introduce : http://www.wtoutiao.com/p/Qc8b4s.html</p>
         *
         * @param band      It used to mark the current band(频带号，用于标记每一个频带，不能重复、自己定义)
         * @param q         quality factor (品质因数，和带宽相互影响，存在计算公式)
         * @param bandWidth the current band's width (带宽，受q值影响，并不会完全固定)
         * @param center    Center frequency , 1HZ~(freq/2)HZ (中心频率)
         * @param gain      gain 增益大小，-15db~15db
         * @return <br>
         * <ul>
         * "Q值，就是为了确保我们对信号有精确处理而限定被提升或衰减均衡曲线的宽窄度。当针对某
         * 一频点对均衡曲线提升或衰减处理时，受到影响的不仅是被选定的中心频率，而且该频点附近 频率范围的声音也都会一同被提升起来或衰减掉。
         * Q值是由均衡的中心频率除以带宽得到的。带宽是以中心频率为基准，向两边延伸至其增益下
         * 降3dB时两点之间的距离。所以带宽与中心频率之间的关系Q可以表示为：Q=Fc/bw，其中Fc为
         * 中心频率，bw为带宽。而对于搁架式均衡器或高低通滤波器来讲，带宽则是以水平轴为基
         * 准，从增益减小3dB时的频点开始到操作频点之间的距离，此时Q值就显得不重要了。"
         * </ul>
         */
        public FxController setPeakEQ(@IntRange(from = 0) int band, float q, float bandWidth, float center, float gain) {
            return setPeakEQInner(BAND_FX_EQ + band, q, bandWidth, center, gain);
        }

        /**
         * @param band
         * @return the peak eq param
         */
        public BASS_BFX_PEAKEQ getPeakEQ4Band(int band) {
            return getPeakEQInner(BAND_FX_EQ + band);
        }

        public FxController resetPeakEQ4Band(int band) {
            return resetPeakEQInner(BAND_FX_EQ + band);
        }

        /**
         * reset all eq
         *
         * @return
         */
        public FxController resetPeakEQ() {
            if (mPeakEQList != null) {
                int size = mPeakEQList.size();
                for (int i = 0; i < size; i++) {
                    HandlePair<Integer, BASS_BFX_PEAKEQ> mPeakEQ = mPeakEQList.valueAt(i);
                    BASS.BASS_ChannelRemoveFX(mPlayerHandle, mPeakEQ.handle);
                    printError("resetPeakEQ");
                }
                mPeakEQList.clear();
                mPeakEQList = null;
            }
            return this;
        }

        /**
         * a example peak eq
         * q = 0.35
         * center = 100
         *
         * @param gain
         * @return
         */
        public FxController setPeakEQ_100(float gain) {
            return setPeakEQInner(BAND_FX_EQ_100, 0.35f, 0, 100, gain);
        }

        public float getGain4PeakEQ_100() {
            BASS_BFX_PEAKEQ eq = getPeakEQInner(BAND_FX_EQ_100);
            if (eq != null)
                return eq.fGain;
            return 0;
        }

        public FxController resetPeakEQ_100() {
            return resetPeakEQInner(BAND_FX_EQ_100);
        }

        /**
         * a example peak eq
         * q = 0.35
         * center = 600
         *
         * @param gain
         * @return
         */
        public FxController setPeakEQ_600(float gain) {
            return setPeakEQInner(BAND_FX_EQ_600, 0.35f, 0, 600, gain);
        }

        public float getGain4PeakEQ_600() {
            BASS_BFX_PEAKEQ eq = getPeakEQInner(BAND_FX_EQ_600);
            if (eq != null)
                return eq.fGain;
            return 0;
        }

        public FxController resetPeakEQ_600() {
            return resetPeakEQInner(BAND_FX_EQ_600);
        }

        /**
         * a example peak eq
         * q = 0.35
         * center = 1000
         *
         * @param gain
         * @return
         */
        public FxController setPeakEQ_1k(float gain) {
            return setPeakEQInner(BAND_FX_EQ_1k, 0.35f, 0, 1000, gain);
        }

        public float getGain4PeakEQ_1k() {
            BASS_BFX_PEAKEQ eq = getPeakEQInner(BAND_FX_EQ_1k);
            if (eq != null)
                return eq.fGain;
            return 0;
        }

        public FxController resetPeakEQ_1k() {
            return resetPeakEQInner(BAND_FX_EQ_1k);
        }

        /**
         * a example peak eq
         * q = 0.35
         * center = 8000
         *
         * @param gain
         * @return
         */
        public FxController setPeakEQ_8k(float gain) {
            return setPeakEQInner(BAND_FX_EQ_8k, 0.35f, 0, 8000, gain);
        }

        public float getGain4PeakEQ_8k() {
            BASS_BFX_PEAKEQ eq = getPeakEQInner(BAND_FX_EQ_8k);
            if (eq != null)
                return eq.fGain;
            return 0;
        }

        public FxController resetPeakEQ_8k() {
            return resetPeakEQInner(BAND_FX_EQ_8k);
        }

        /**
         * a example peak eq
         * q = 0.35
         * center = 14000
         *
         * @param gain
         * @return
         */
        public FxController setPeakEQ_14k(float gain) {
            return setPeakEQInner(BAND_FX_EQ_14k, 0.35f, 0, 14000, gain);
        }

        public float getGain4PeakEQ_14k() {
            BASS_BFX_PEAKEQ eq = getPeakEQInner(BAND_FX_EQ_14k);
            if (eq != null)
                return eq.fGain;
            return 0;
        }

        public FxController resetPeakEQ_14k() {
            return resetPeakEQInner(BAND_FX_EQ_14k);
        }

        /**
         * 自动哇音，貌似是根据延时，按给定的参数自动发出类似哇的声音<br>
         * 注意只对某些音频有效，比如在有人声的歌唱音频中能听出哇音，应该是对电吉他有效<br>
         * 但是在一些纯音频（如雨声），是听不出哇音的 参数的各种使用比较复杂，只能按例子给出三种<br>
         * 注意要先设置freq，其他参数才能设置
         *
         * @param dryMix
         * @param wetMix
         * @param feedBack
         * @param rate
         * @param range
         * @param freq
         * @return
         */
        public FxController setAutoWah(float dryMix, float wetMix, float feedBack, float rate, float range, float freq) {
            if (mParam_autowah == null)
                mParam_autowah = new HandlePair<>(0, new BASS_BFX_AUTOWAH());

            mParam_autowah.param.fDryMix = dryMix;
            mParam_autowah.param.fWetMix = wetMix;
            mParam_autowah.param.fFeedback = feedBack;
            mParam_autowah.param.fRate = rate;
            mParam_autowah.param.fRange = range;
            mParam_autowah.param.fFreq = freq;
            mParam_autowah.param.lChannel = BASS_FX.BASS_BFX_CHANALL;
            return this;
        }

        /**
         * 设置哇音效果
         *
         * @param type <br>
         *             AUTOWAH_SLOW<br>
         *             AUTOWAH_FAST<br>
         *             AUTOWAH_HIFAST
         * @return
         */
        public FxController setAutoWah(byte type) {
            Log.v("FxControler", "setAutoWah");
            switch (type) {
                case AUTOWAH_SLOW:
                    setAutoWah(0.500f, 1.500f, 0.5f, 2.0f, 4.3f, 50.0f);
                    break;
                case AUTOWAH_FAST:
                    setAutoWah(0.500f, 1.500f, 0.5f, 5.0f, 5.3f, 50.0f);
                    break;
                case AUTOWAH_HIFAST:
                    setAutoWah(0.500f, 1.500f, 0.5f, 5.0f, 4.3f, 500.0f);
                    break;
            }
            return this;
        }

        /**
         * @return
         */
        public BASS_BFX_AUTOWAH getAutoWah() {
            if (mParam_autowah == null)
                return null;
            int handle = mParam_autowah.handle == null ? 0 : mParam_autowah.handle;
            if (handle == 0)
                return null;
            BASS_BFX_AUTOWAH mParam = new BASS_BFX_AUTOWAH();
            BASS.BASS_FXGetParameters(mParam_autowah.handle, mParam);
            printError("getAUTOWAH");
            return mParam;
        }

        public FxController resetAutoWah() {
            if (mParam_autowah == null)
                return this;

            int handle = mParam_autowah.handle == null ? 0 : mParam_autowah.handle;
            if (handle != 0) {
                boolean state = BASS.BASS_ChannelRemoveFX(mPlayerHandle, mParam_autowah.handle);
                printError("resetAutoWah");
            }
            mParam_autowah = null;
            return this;
        }

        /**
         * 移相效果，类似于哇音<br>
         * 不同的是对各种音频均有效果<br>
         * 但是会不断重复，不是很自然
         *
         * @param dryMix
         * @param wetMix
         * @param feedBack
         * @param rate
         * @param range
         * @param freq
         * @return
         */
        public FxController setPhaser(float dryMix, float wetMix, float feedBack, float rate, float range, float freq) {
            if (mParam_phaser == null)
                mParam_phaser = new HandlePair<>(0, new BASS_BFX_PHASER());

            mParam_phaser.param.fDryMix = dryMix;
            mParam_phaser.param.fWetMix = wetMix;
            mParam_phaser.param.fFeedback = feedBack;
            mParam_phaser.param.fRate = rate;
            mParam_phaser.param.fRange = range;
            mParam_phaser.param.fFreq = freq;
            mParam_phaser.param.lChannel = BASS_FX.BASS_BFX_CHANALL;
            return this;
        }

        public BASS_BFX_PHASER getPhaser() {
            if (mParam_phaser == null)
                return null;
            int handle = mParam_phaser.handle == null ? 0 : mParam_phaser.handle;
            if (handle == 0)
                return null;
            BASS_BFX_PHASER mParam = new BASS_BFX_PHASER();
            BASS.BASS_FXGetParameters(mParam_phaser.handle, mParam);
            printError("getPhaser");
            return mParam;
        }

        public FxController resetPhaser() {
            if (mParam_phaser == null)
                return this;

            int handle = mParam_phaser.handle == null ? 0 : mParam_phaser.handle;

            if (handle != 0) {
                boolean state = BASS.BASS_ChannelRemoveFX(mPlayerHandle, mParam_phaser.handle);
                printError("resetAutoWah");
            }
            mParam_phaser = null;
            return this;
        }

        /**
         * @param type <br>
         *             PHASER_SHIFT<br>
         *             PHASER_SLOWSHIFT<br>
         *             PHASER_BASIC<br>
         *             PHASER_WFB<br>
         *             PHASER_MED<br>
         *             PHASER_FAST<br>
         *             PHASER_INVERT<br>
         *             PHASER_TREMOLO<br>
         * @return
         */
        public FxController setPhaser(byte type) {
            switch (type) {
                case PHASER_SHIFT:
                    setPhaser(0.999f, 0.999f, 0.0f, 1.0f, 4.0f, 100.0f);
                    break;
                case PHASER_SLOWSHIFT:
                    setPhaser(0.999f, -0.999f, -0.6f, 0.2f, 6.0f, 100.0f);
                    break;
                case PHASER_BASIC:
                    setPhaser(0.999f, 0.999f, 0.0f, 1.0f, 4.3f, 50.0f);
                    break;
                case PHASER_WFB:
                    setPhaser(0.999f, 0.999f, 0.6f, 1.0f, 4.0f, 40.0f);
                    break;
                case PHASER_MED:
                    setPhaser(0.999f, 0.999f, 0.0f, 1.0f, 7.0f, 100.0f);
                    break;
                case PHASER_FAST:
                    setPhaser(0.999f, 0.999f, 0.0f, 1.0f, 7.0f, 400.0f);
                    break;
                case PHASER_INVERT:
                    setPhaser(0.999f, -0.999f, -0.2f, 1.0f, 7.0f, 200.0f);
                    break;
                case PHASER_TREMOLO:
                    setPhaser(0.999f, 0.999f, 0.6f, 1.0f, 4.0f, 60.0f);
                    break;
            }
            return this;
        }

        /**
         * 合唱声效，应该是利用延时得到的声效<br>
         * 好听不好听就是另外一回事了
         *
         * @param dryMix
         * @param wetMix
         * @param feedBack
         * @param minSweep
         * @param maxSweep
         * @param rate
         * @return
         */
        public FxController setChorus(float dryMix, float wetMix, float feedBack, float minSweep, float maxSweep, float rate) {
            if (mParam_chorus == null)
                mParam_chorus = new HandlePair<>(0, new BASS_BFX_CHORUS());

            mParam_chorus.param.fDryMix = dryMix;
            mParam_chorus.param.fWetMix = wetMix;
            mParam_chorus.param.fFeedback = feedBack;
            mParam_chorus.param.fMinSweep = minSweep;
            mParam_chorus.param.fMaxSweep = maxSweep;
            mParam_chorus.param.fRate = rate;
            mParam_chorus.param.lChannel = BASS_FX.BASS_BFX_CHANALL;
            return this;
        }

        public BASS_BFX_CHORUS getChorus() {
            if (mParam_chorus == null)
                return null;
            int handle = mParam_chorus.handle == null ? 0 : mParam_chorus.handle;
            if (handle == 0)
                return null;
            BASS_BFX_CHORUS mParam = new BASS_BFX_CHORUS();
            BASS.BASS_FXGetParameters(mParam_chorus.handle, mParam);
            printError("getChorus");
            return mParam;
        }

        public FxController resetChorus() {
            if (mParam_chorus == null)
                return this;

            int handle = mParam_chorus.handle == null ? 0 : mParam_chorus.handle;
            if (handle != 0) {
                boolean state = BASS.BASS_ChannelRemoveFX(mPlayerHandle, mParam_chorus.handle);
                printError("resetChorus");
            }
            mParam_chorus = null;
            return this;
        }

        /**
         * @param type <br>
         *             CHORUS_FLANGER：弗朗格<br>
         *             CHORUS_EXAGGERATION：夸张的合唱音效<br>
         *             CHORUS_MOTOCYCLE：模拟摩托车声音<br>
         *             CHORUS_DEVIL：一点都不像魔鬼声，某些时候听起来倒是有点悚然<br>
         *             CHORUS_MANYVOICE：说是很多声音<br>
         *             CHORUS_CHIPMUNK：如果是有人声的音频，会听到另一个装嫩的声音<br>
         *             CHORUS_WATER：类似把水慢慢倒进壶子里的声音，某些音频下不明显<br>
         *             CHORUS_AIRPLANE：一点都不明显<br>
         * @return
         */
        public FxController setChorus(byte type) {
            switch (type) {
                case CHORUS_FLANGER:
                    setChorus(1.0f, 0.35f, 0.5f, 1.0f, 5.0f, 1.0f);
                    break;
                case CHORUS_EXAGGERATION:
                    setChorus(0.7f, 0.25f, 0.5f, 1.0f, 200.0f, 50.0f);
                    break;
                case CHORUS_MOTOCYCLE:
                    setChorus(0.9f, 0.45f, 0.5f, 1.0f, 100.0f, 25.0f);
                    break;
                case CHORUS_DEVIL:
                    setChorus(0.9f, 0.35f, 0.5f, 1.0f, 50.0f, 200.0f);
                    break;
                case CHORUS_MANYVOICE:
                    setChorus(0.9f, 0.35f, 0.5f, 1.0f, 400.0f, 200.0f);
                    break;
                case CHORUS_CHIPMUNK:
                    setChorus(0.9f, -0.2f, 0.5f, 1.0f, 400.0f, 400.0f);
                    break;
                case CHORUS_WATER:
                    setChorus(0.9f, -0.4f, 0.5f, 1.0f, 2.0f, 1.0f);
                    break;
                case CHORUS_AIRPLANE:
                    setChorus(0.3f, 0.4f, 0.5f, 1.0f, 10.0f, 5.0f);
                    break;
            }
            return this;
        }

        /**
         * @param dryMix
         * @param wetMix
         * @param feedBack
         * @param delay
         * @param stereo   立体声
         * @return
         */
        public FxController setEcho(float dryMix, float wetMix, float feedBack, float delay, boolean stereo) {
            if (mParam_echo == null)
                mParam_echo = new HandlePair<>(0, new BASS_BFX_ECHO4());

            mParam_echo.param.fDryMix = dryMix;
            mParam_echo.param.fWetMix = wetMix;
            mParam_echo.param.fFeedback = feedBack;
            mParam_echo.param.fDelay = delay;
            mParam_echo.param.bStereo = stereo;
            mParam_echo.param.lChannel = BASS_FX.BASS_BFX_CHANALL;
            return this;
        }

        /**
         * it has bug,
         * don't use the BASS_FXGetParameters when you use echo4
         *
         * @return
         */
        public BASS_BFX_ECHO4 getEcho() {
//            if (mParam_echo == null)
//                return null;
//
//            int handle = mParam_echo.handle == null ? 0 : mParam_echo.handle;
//            if (handle == 0)
//                return null;
//            BASS_BFX_ECHO4 mParam = new BASS_BFX_ECHO4();
//            BASS.BASS_FXGetParameters(mParam_echo.handle, mParam);
//            printError("getEcho");
//            return mParam;

            return null;
        }

        public FxController resetEcho() {
            if (mParam_echo == null)
                return this;

            int handle = mParam_echo.handle == null ? 0 : mParam_echo.handle;
            if (handle != 0) {
                boolean state = BASS.BASS_ChannelRemoveFX(mPlayerHandle, mParam_echo.handle);
                printError("resetEcho");
            }
            mParam_echo = null;
            return this;
        }

        /**
         * @param type <br>
         *             ECHO_SMALL<br>
         *             ECHO_MANY<br>
         *             ECHO_REVERSE<br>
         *             ECHO_ROBOTIC<br>
         * @return
         */
        public FxController setEcho(byte type) {
            switch (type) {
                case ECHO_SMALL:
                    setEcho(0.999f, 0.999f, 0.0f, 0.20f, false);
                    break;
                case ECHO_MANY:
                    setEcho(0.999f, 0.999f, 0.0f, 0.50f, false);
                    break;
                case ECHO_REVERSE:
                    setEcho(0.999f, 0.999f, -0.7f, 0.80f, false);
                    break;
                case ECHO_ROBOTIC:
                    setEcho(0.500f, 0.800f, 0.5f, 0.10f, false);
                    break;
            }
            return this;
        }
    }

    private class FmlHandler extends Handler {
        public static final int MSG_ERROR = 0x01;
        public static final int MSG_PREPARED = MSG_ERROR + 0x02;
        public static final int MSG_COMPLETION = MSG_PREPARED + 0x02;

        public FmlHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (wRefContext == null) {
                Log.e(TAG, "WeakReference is Null!");
                return;
            }
            Context context = wRefContext.get();
            if (context == null) {
                Log.e(TAG, "Context is Null!");
                return;
            }

            switch (msg.what) {
                case MSG_ERROR:
                    if (mErrorListener != null)
                        mErrorListener.onError(FmlPlayer.this, msg.arg1);
                    break;
                case MSG_PREPARED:
                    if (mPreparedListener != null)
                        mPreparedListener.OnPrepared(FmlPlayer.this);
                    break;
                case MSG_COMPLETION:
                    if (mCompletionListener != null)
                        mCompletionListener.onCompletion(FmlPlayer.this);
                    break;
            }
        }
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

    // BASS_SYNC_META
    // 用BASS_ChannelSetSync设置，用于获取meta信息，前提是需要有shoutcast服务

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
