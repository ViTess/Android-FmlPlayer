# Android-FmlPlayer
version - 0.1.0

###概要
基于BASS、BASS_FX两个库的封装，可直接用于Android

提供最基础的音频播放、部分音效处理，目前仅支持OGG、MP3、WAV格式音频

支持OGG格式音频无缝循环，支持在线音频播放和下载

BASS and BASS_FX Library：*http://www.un4seen.com/*

###如何使用
  
* **导入**

    将AudioLibrary作为module导入，并设置为library
    
* **初始化和销毁**

    在程序的入口处*（如Activity或Application中的onCreate）*处初始化音频库：
    ```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FmlPlayer.init(this, true);
    }
    ```

    在程序的结束处调用释放音频库:
    ```java
    @Override
    protected void onDestroy() {
        FmlPlayer.free();
        super.onDestroy();
    }
    ```
    
    在初始化音频库后，即可像初始化MediaPlayer一样使用FmlPlayer：
    ```java
    FmlPlayer mPlayer = new FmlPlayer();
    ```
    
* **设置播放路径**
    
    目前支持的路径有assets、本地sdcard和网络链接
    
    |               |                        |
    |---------------|------------------------|
    |setAssetFile   |设置assets目录文件路径    |
    |setExternalFile|设置sdcard目录文件路径    |
    |setNetFile     |设置网络链接(http或https) |
    
* **调用prepare方法**

    设置完路径后即可调用`mPlayer.prepare()`方法加载音频，注意设置网络链接后应调用`mPlayer.prepareAsync()`方法异步加载并设置`OnPreparedListener`监听准备事件
    
* **控制播放、暂停、关闭**

    经过prepare后，即可调用`mPlayer.play()`,`mPlayer.pause()`,`mPlayer.stop()`方法操作音频
    
    另外像调节音量、在线音频部分参数设置、下载音频等，可以参看demo中的例子
    
* **重置、释放资源**

    像MediaPlayer一样，在需要重置的地方调用`mPlayer.reset()`
    
    而在不需要再次播放音频后，请使用`mPlayer.release()`释放资源
    
####使用Fx功能
    
Fx功能是通过封装BASS_FX库实现的，目前可支持的音效有：

* Rotate
* AutoWah
* Phaser
* Chorus
* Echo
* PeakEQ

在需要使用时，只需调用相关效果的set方法，最后update到FmlPlayer中即可：

```java
FxController mFxCtrl = new FxController();
mFxCtrl.setRotate().setAutoWah();
mFxCtrl.setPhaser();
...

mFxCtrl.update(mPlayer);
```

在不需要部分音效时，同样只需调用相关效果的reset方法，并update到FmlPlayer即可

如需要取消所有音效或释放FmlPlayer时，请调用`mFxCtrl.release()`

每一项音效对应的参数都有多种，可以自己调♂教；同时部分音效有预设了部分设置在其中提供调用，详细请参考demo中的例子

##TODO

在有时间的前提下，会逐步添加完善其他功能，如增加可支持音频类型的相关库类、增加TAG的读取支持等

##Update Record
    160628：
        1. 增加在线音频播放、下载功能
        2. 增加相关网络参数设置
        3. 更改代码结构、更正原有代码中的bug

    160421：
        1. 修改项目为gradle
        2. 将BASS库打包为jar引用