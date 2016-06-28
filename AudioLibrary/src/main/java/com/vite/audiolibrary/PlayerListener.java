package com.vite.audiolibrary;

import java.nio.ByteBuffer;

/**
 * Created by trs on 16-4-26.
 */
public class PlayerListener {
    public interface OnCompletionListener {
        /**
         * 当播放网络音频时，如果中间网络中断或其他异常，
         * 播放器在播放完已经缓冲完的数据后，调用onCompletion，
         * 所以请在onCompletion中判断是否网络异常/是否播放到文件末端
         *
         * @param fp
         */
        void onCompletion(FmlPlayer fp);
    }

    public interface OnErrorListener {
        /**
         * @param fp
         * @param errorCode about the error code , you can see the strings_omeplayer.xml
         */
        void onError(FmlPlayer fp, int errorCode);
    }

    public interface OnPreparedListener {
        /**
         * 当设置了OnPreparedListener后，无论调用prepared()或preparedAsync
         *
         * @param fp
         */
        void OnPrepared(FmlPlayer fp);
    }

    public interface OnDownloadedListener {
        void onDownloaded(FmlPlayer fp, ByteBuffer buffer, int length);
    }
}
