# Android-FmlPlayer

###概要：
基于BASS、BASS_FX两个库的封装，可直接用于Android

提供最基础的音频播放、音效处理，目前仅支持OGG、MP3、WAV格式音频
支持OGG格式音频无缝循环，支持在线音频播放和下载

BASS and BASS_FX Library：http://www.un4seen.com/

###目录说明：

AudioLibrary为库类工程

##Update Record：
    160628：
        1. 增加在线音频播放功能
        2. 增加相关网络参数设置
        3. 更改代码结构、更正原有代码中的bug

    160421：
        1. 修改项目为gradle
        2. 将BASS库打包为jar引用