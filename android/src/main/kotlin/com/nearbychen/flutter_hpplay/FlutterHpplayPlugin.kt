package com.nearbychen.flutter_hpplay;

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.hpplay.sdk.source.browse.api.ILelinkServiceManager
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo
import com.hpplay.sdk.source.common.utils.HapplayUtils.getApplication
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.RELEVANCE_DATA_UNSUPPORT
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_COMPLETION
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_CONNECT_FAILURE
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_CONNECT_SUCCESS
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_DISCONNECT
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_INPUT_SCREENCODE
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_LOADING
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_PAUSE
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_PLAY
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_PLAY_ERROR
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_POSITION_UPDATE
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_SCREENSHOT
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_SEARCH_ERROR
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_SEARCH_NO_RESULT
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_SEARCH_SUCCESS
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_SEEK
import com.nearbychen.flutter_hpplay.IUIUpdateListener.Companion.STATE_STOP
import com.nearbychen.flutter_hpplay.bean.MessageDeatail
import com.nearbychen.flutter_hpplay.bean.MyLelinkServiceInfo
import com.nearbychen.flutter_hpplay.utils.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

public class FlutterHpplayPlugin(private val registrar: Registrar, channel: MethodChannel) : MethodCallHandler {

    private var infos: MutableList<LelinkServiceInfo>? = null
    private var connectInfos: MutableList<LelinkServiceInfo>? = null

    private val REQUEST_CAMERA_PERMISSION = 2
    private val REQUEST_RECORD_AUDIO_PERMISSION = 4

    init {
        registrar.addRequestPermissionsResultListener { requestCode, permissions, grantResults ->
            if (requestCode == REQUEST_MUST_PERMISSION) {
                var denied = false
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        denied = true
                    }
                }
                if (denied) { // 拒绝
                    ToastUtil.show(registrar.context(), "您拒绝了权限")
                } else { // 允许
                    initLelinkHelper(registrar)
                }
            } else if (requestCode == CameraPermissionCompat.REQUEST_CODE_CAMERA) {
                var denied = false
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        denied = true
                    }
                }
                if (denied) { // 拒绝
                    ToastUtil.show(registrar.context(), "请打开此应用的摄像头权限！")
                } else {
//                    startCaptureActivity()//todo
                }
            } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
                var denied = false
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        denied = true
                    }
                }
                if (denied) { // 拒绝
                    ToastUtil.show(registrar.context(), "您录制音频的权限")
                } else { // 允许
//                    startMirror() todo
                }
            }
            false
        }

    }

    companion object {
        private var mLelinkHelper: LelinkHelper? = null
        private var mDelayHandler: UIHandler? = null
        private var channel: MethodChannel? = null
        private var isFirstBrowse = true
        private val REQUEST_MUST_PERMISSION = 1
        private var isPlayMirror = false

        private val mScreencode: String? = null
        private var isPause = false

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            if (ContextCompat.checkSelfPermission(getApplication(),
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(registrar.context(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(registrar.context(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                initLelinkHelper(registrar)
            } else {          // 若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
                ActivityCompat.requestPermissions(registrar.activity(), arrayOf(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_MUST_PERMISSION)
            }

            channel = MethodChannel(registrar.messenger(), "flutter_hpplay")
            val flutterHpplayPlugin = FlutterHpplayPlugin(registrar, channel!!)
            channel?.setMethodCallHandler(flutterHpplayPlugin)
            mDelayHandler = UIHandler(flutterHpplayPlugin)


        }

        private fun initLelinkHelper(registrar: Registrar) {
            mLelinkHelper = LelinkHelper.getInstance(registrar.context());
            val mUIUpdateListener = object : IUIUpdateListener {
                override fun onUpdate(what: Int, deatail: MessageDeatail?) {
//                    Log.i("xyzeng", "initLelinkHelper——onUpdate:${what}")
                    when (what) {
                        STATE_SEARCH_SUCCESS -> {
                            if (isFirstBrowse) {
                                isFirstBrowse = false
                                ToastUtil.show(registrar.context(), "搜索成功")
                            }
                            if (mDelayHandler != null) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_SEARCH_SUCCESS,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_SEARCH_ERROR -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                val message = Message()
                                message.what = STATE_SEARCH_ERROR
                                message.obj = deatail
                                mDelayHandler!!.sendMessageDelayed(message,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_SEARCH_NO_RESULT -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_SEARCH_SUCCESS,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_CONNECT_SUCCESS -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_CONNECT_SUCCESS,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_DISCONNECT -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                val message = Message()
                                message.what = STATE_DISCONNECT
                                message.obj = deatail
                                mDelayHandler!!.sendMessageDelayed(message,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_CONNECT_FAILURE -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                val message = Message()
                                message.what = STATE_CONNECT_FAILURE
                                message.obj = deatail
                                mDelayHandler!!.sendMessageDelayed(message,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_PLAY -> {
                            isPause = false
                            logD("ToastUtil 开始播放")
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_PLAY,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_LOADING -> {
                            isPause = false;
                            logD("ToastUtil 开始加载"); if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_LOADING,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_PAUSE -> {
                            isPause = true;
                            logD("ToastUtil 暂停播放");
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_PAUSE,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_STOP -> {
                            isPause = false
                            logD("ToastUtil 播放结束")
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_STOP,
                                        TimeUnit.SECONDS.toMillis(1))
                            }

                        }
                        STATE_SEEK -> {
                            logTest("callback seek:" + deatail!!.text)
                            logD("ToastUtil seek完成:" + deatail.text)
                            ToastUtil.show(registrar.context(), "seek完成" + deatail.text)

                        }
                        STATE_PLAY_ERROR -> {
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                val message = Message()
                                message.what = STATE_PLAY_ERROR
                                message.obj = deatail
                                mDelayHandler!!.sendMessageDelayed(message,
                                        TimeUnit.SECONDS.toMillis(1))
                            }
                        }
                        STATE_POSITION_UPDATE -> {
                            isPause = false
                            val arr = deatail?.obj as LongArray?
                            val duration = arr!![0]
                            val position = arr[1]
//                            logD("ToastUtil 总长度：$duration 当前进度:$position")
                            val message = mapOf<String, Any>(
                                    Pair("duration", duration),
                                    Pair("currentTime", position)
                            )
                            channel?.invokeMethod("onLelinkPlayerProgressInfo",
                                    message)
                            channel?.invokeMethod("onLelinkPlayerStatus", 2)

//                                    """{"max":${duration.toInt()},"progress":${position.toInt()}}""")
                        }
                        STATE_COMPLETION -> {
                            isPause = false
                            logD("播放完成")
                            if (null != mDelayHandler) {
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                mDelayHandler!!.sendEmptyMessageDelayed(STATE_COMPLETION,
                                        TimeUnit.SECONDS.toMillis(1))
                            }

                        }
                        STATE_INPUT_SCREENCODE -> {

                        }
                        RELEVANCE_DATA_UNSUPPORT -> {
                            if (null != mDelayHandler) {
                                ToastUtil.show(registrar.context(), deatail!!.text!!)
                                mDelayHandler!!.removeCallbacksAndMessages(null)
                                val message = Message()
                                message.what = RELEVANCE_DATA_UNSUPPORT
                                message.obj = deatail
                                mDelayHandler!!.sendMessageDelayed(message,
                                        TimeUnit.SECONDS.toMillis(1))
                            }

                        }
                        STATE_SCREENSHOT -> {

                        }
                    }
                }
            }
            mLelinkHelper?.setUIUpdateListener(mUIUpdateListener)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "setup") {
            setup(call, result)
            return
        }

        var connectInfos: List<LelinkServiceInfo?>? = null
        if (null != mLelinkHelper) {
            connectInfos = mLelinkHelper!!.connectInfos
        }
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "getIsConnected" -> {//是否已连接
                getIsConnected(call, result)
            }
            "deviceListDidSelectIndex" -> {//选中索要连接的设备
                deviceListDidSelectIndex(call, result)
            }
            "search" -> {//搜索
                search(call, result)
            }
            "stopBrowse" -> {//停止搜索
                stopBrowse(call, result)
            }
            "connect" -> {//连接
                connect(call, result)
            }
            "playMedia" -> {//播放
                println("播放")
                play(call, result)
            }
            "resumePlay" -> {//继续播放
                play(call, result)
            }
            "pause" -> {//暂停
                pause(call, result, connectInfos)
            }
            "stop" -> {//停止
                stop(call, result, connectInfos)
            }
            "addVolume" -> {//音量+
                addVolume(call, result, connectInfos)
            }
            "reduceVolume" -> {//音量-
                reduceVolume(call, result, connectInfos)
            }
            "isIntoBg" -> {// todo
            }
            "volumProgress" -> {//音量条
                volumProgress(call, result)
            }
            "seekTo" -> {//进度条
                seekTo(call, result)
            }
            else -> {
                result.notImplemented()

            }
        }
    }

    private fun setup(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val appid = map["appid"] as String
        val secretKey = map["secretKey"] as String
        val debug = map["debug"] as Boolean
        Log.i("xyzeng", "appid=$appid***secretKey=$secretKey***debug=$debug")
        if (null != mLelinkHelper) {
            mLelinkHelper!!.lelinkSetting(appid, secretKey)
        } else {
            ToastUtil.show(registrar.context(), "未初始化")
        }
        inProduction = debug
    }

    private fun search(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val isLelinkOpen =
                if (map == null || map["isLelinkOpen"] == null) {
                    true
                } else {
                    map["isLelinkOpen"] as Boolean
                }
        val isDLNAOpen =
                if (map == null || map["isDLNAOpen"] == null) {
                    true
                } else {
                    map["isDLNAOpen"] as Boolean
                }


        if (null != mLelinkHelper) {
            val type: Int
            val text: String
            if (isLelinkOpen && isDLNAOpen) { //可以搜索到乐联和DLNA协议
                text = "All"
                type = ILelinkServiceManager.TYPE_ALL
            } else if (isLelinkOpen) { //仅搜索乐联协议
                text = "Lelink"
                type = ILelinkServiceManager.TYPE_LELINK
            } else if (isDLNAOpen) {
                text = "DLNA"
                type = ILelinkServiceManager.TYPE_DLNA
            } else {
                text = "All"
                type = ILelinkServiceManager.TYPE_ALL
            }
            logTest("browse type:$text")
            if (!isFirstBrowse) {
                isFirstBrowse = true
            }
            mLelinkHelper!!.browse(type)
        } else {
            ToastUtil.show(registrar.context(), "权限不够")
        }
    }

    //搜索成功
    fun updateBrowseAdapter() {
        logE("updateBrowseAdapter")
        if (null != mLelinkHelper) {
            infos = mLelinkHelper!!.infos
            val myInfos: MutableList<MyLelinkServiceInfo> = mutableListOf()
            infos?.forEach {
                myInfos.add(MyLelinkServiceInfo(it.name, it.uid, it.types))
            }
            val myInfosJson = Gson().toJson(myInfos)
            logE("myInfosJson:$myInfosJson")
            //发送myInfosJson给flutter
            channel?.invokeMethod("onLelinkBrowserDidFindLelinkServices", myInfosJson)
        }

    }

    //搜索出错
    fun searchError(any: Any) {
        channel?.invokeMethod("onLelinkBrowserError", any)
    }

    //连接成功
    fun updateConnectAdapter() {
        if (null != mLelinkHelper) {
            connectInfos = mLelinkHelper!!.connectInfos
            val index = infos?.indexOf(connectInfos?.get(0))
            channel?.invokeMethod("onLelinkDidConnectionToService", "$index")
        }
    }

    //连接出错
    fun connectError(any: Any) {
        channel?.invokeMethod("onLelinkConnectionError", any)
    }

    //断开连接
    fun disConnect(any: Any) {
        channel?.invokeMethod("onLelinkDisConnectionToService", any)
    }

    //播放错误
    fun playError(any: Any) {
        channel?.invokeMethod("onLelinkPlayerError", any)
    }

    //播放错误
    fun onLelinkPlayerStatus(status: Int) {
        channel?.invokeMethod("onLelinkPlayerStatus", status)
    }

    private fun deviceListDidSelectIndex(call: MethodCall, result: Result) {
        if (null != mLelinkHelper) {
            val map = call.arguments<HashMap<String, Any>>()
            val index = map["index"] as Int
//            var infoUid: String = call.arguments<String>()
            var info = infos?.get(index);
//            logI("开始连接" + infoUid)
//            var info: LelinkServiceInfo?
//            info = infos?.find { it.uid == infoUid }
            logI("开始连接" + info?.name)
            if (info != null) {
                if (null != mLelinkHelper) {
                    logI("选中了:" + info.name + " type:" + info.types)
                    ToastUtil.show(registrar.context(), "选中了:" + info.name
                            + " type:" + info.types)
                    mLelinkHelper!!.connect(info)
                } else {
                    ToastUtil.show(registrar.context(), "未初始化或未选择设备")
                }
            } else {
                ToastUtil.show(registrar.context(), "连接异常，请重新搜索")
            }
        } else {
            ToastUtil.show(registrar.context(), "未初始化")
        }
    }

    private fun getIsConnected(call: MethodCall, result: Result) {
        if (null != mLelinkHelper) {
            result.success(mLelinkHelper!!.connectInfos.isNotEmpty())
        } else {
            ToastUtil.show(registrar.context(), "未初始化")
        }
    }

    private fun stopBrowse(call: MethodCall, result: Result) {
        if (null != mLelinkHelper) {
            isFirstBrowse = false
            mLelinkHelper!!.stopBrowse()
        } else {
            ToastUtil.show(registrar.context(), "未初始化")
        }
    }

    private fun connect(call: MethodCall, result: Result) {
        var infoUid: String = call.arguments<String>()
        logI("开始连接" + infoUid)
        var info: LelinkServiceInfo?
        info = infos?.find { it.uid == infoUid }
        logI("开始连接" + info?.name)
        if (info != null) {
            if (null != mLelinkHelper) {
                ToastUtil.show(registrar.context(), "选中了:" + info.name
                        + " type:" + info.types)
                mLelinkHelper!!.connect(info)
            } else {
                ToastUtil.show(registrar.context(), "未初始化或未选择设备")
            }
        } else {
            ToastUtil.show(registrar.context(), "连接异常，请重新搜索")
        }
    }

    var playUrl: String? = null
    var playMediaType: Int = 0
    private fun play(call: MethodCall, result: Result) {
        if (null == mLelinkHelper) {
            ToastUtil.show(registrar.context(), "未初始化或未选择设备")
            return
        }
        println("isPause:$isPause")

        val map = call.arguments<HashMap<String, Any>>()
        if (isPause) {
            isPause = false
            // 暂停中
            mLelinkHelper!!.resume()
            return
        }

        var isLocalFile =
                if (map == null || map["isLocalFile"] == null) {
                    false
                } else {
                    map["isLocalFile"] as Boolean
                }
        //101音乐102视频103图片
        playMediaType =
                if (map == null || map["mediaType"] == null) {
                    playMediaType
                } else {
                    map["mediaType"] as Int
                }
        playUrl =
                if (map == null || map["mediaURLString"] == null) {
                    playUrl
                } else {
                    map["mediaURLString"] as String
                }




        when (playMediaType) {
            0 -> {//在线视频媒体类型
                playMediaType = 102
                isLocalFile = false
            }
            1 -> {//在线音频媒体类型
                playMediaType = 101
                isLocalFile = false
            }
            2 -> {//在线图片媒体类型
                playMediaType = 103
                isLocalFile = false
            }
            3 -> {//本地图片媒体类型
                playMediaType = 103
                isLocalFile = true
            }
            4 -> {//本地视频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地视频的URL
                playMediaType = 102
                isLocalFile = true
            }
            5 -> {//本地音频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地音频的URL
                playMediaType = 101
                isLocalFile = true
            }
        }

        isPlayMirror = false

        val connectInfos = mLelinkHelper!!.connectInfos

        if (null == connectInfos || connectInfos.isEmpty()) {
            ToastUtil.show(registrar.context(), "请先连接设备")
            return
        }
        logTest("start play url:$playUrl type:$playMediaType")
        if (isLocalFile) { // 本地media
            mLelinkHelper!!.playLocalMedia(playUrl, playMediaType, mScreencode)
        } else { // 网络media
            mLelinkHelper!!.playNetMedia(playUrl, playMediaType, mScreencode)
        }
    }

    private fun pause(call: MethodCall, result: Result, connectInfos: List<LelinkServiceInfo?>?) {
        if (null != mLelinkHelper && null != connectInfos && !connectInfos.isEmpty()) {
            isPause = true
            mLelinkHelper!!.pause()
        }
    }

    private fun stop(call: MethodCall, result: Result, connectInfos: List<LelinkServiceInfo?>?) {
        if (null != mLelinkHelper && null != connectInfos && !connectInfos.isEmpty()) {
            logE("结束投屏")
            mLelinkHelper!!.stop()
        } else {
            ToastUtil.show(registrar.context(), "请先连接设备")
        }
    }

    private fun addVolume(call: MethodCall, result: Result, connectInfos: List<LelinkServiceInfo?>?) {
        if (null != mLelinkHelper && null != connectInfos && !connectInfos.isEmpty()) {
            logTest("addVolume click")
            mLelinkHelper!!.voulumeUp()
        } else {
            ToastUtil.show(registrar.context(), "请先连接设备")
        }
    }

    private fun reduceVolume(call: MethodCall, result: Result, connectInfos: List<LelinkServiceInfo?>?) {
        if (null != mLelinkHelper && null != connectInfos && !connectInfos.isEmpty()) {
            logTest("volumeDown click")
            mLelinkHelper!!.voulumeDown()
        } else {
            ToastUtil.show(registrar.context(), "请先连接设备")
        }
    }

    private fun volumProgress(call: MethodCall, result: Result) {
        var progress: Int = call.arguments<Int>()
        logTest("set volume:$progress")
        mLelinkHelper!!.setVolume(progress)
    }

    private fun seekTo(call: MethodCall, result: Result) {
        val map = call.arguments<HashMap<String, Any>>()
        val progress = map["seek"] as Int
        Log.i("xyzeng", "progres$progress")
        mLelinkHelper!!.seekTo(progress)
    }

    private class UIHandler internal constructor(reference: FlutterHpplayPlugin) : Handler() {
        private val mReference: WeakReference<FlutterHpplayPlugin>
        override fun handleMessage(msg: Message) {
            Log.i("xyzeng", "handleMessage:${msg.what}")
            val flutterHpplayPlugin: FlutterHpplayPlugin = mReference.get() ?: return
            when (msg.what) {
                STATE_SEARCH_SUCCESS -> flutterHpplayPlugin.updateBrowseAdapter()
                STATE_SEARCH_ERROR -> flutterHpplayPlugin.searchError(msg.obj)
                STATE_CONNECT_SUCCESS -> flutterHpplayPlugin.updateConnectAdapter()
                STATE_CONNECT_FAILURE -> flutterHpplayPlugin.connectError(msg.obj)
                STATE_DISCONNECT -> flutterHpplayPlugin.disConnect(msg.obj)
                STATE_PLAY_ERROR -> {
                    flutterHpplayPlugin.playError(msg.obj)
                    flutterHpplayPlugin.onLelinkPlayerStatus(6)//播放错误
                }
                STATE_LOADING -> flutterHpplayPlugin.onLelinkPlayerStatus(1)//正在加载
                STATE_PLAY -> flutterHpplayPlugin.onLelinkPlayerStatus(2)//正在播放
                STATE_PAUSE -> flutterHpplayPlugin.onLelinkPlayerStatus(3)//暂停状态
                STATE_COMPLETION -> flutterHpplayPlugin.onLelinkPlayerStatus(5)//播放完成
                STATE_STOP -> flutterHpplayPlugin.onLelinkPlayerStatus(4)//播放结束
                RELEVANCE_DATA_UNSUPPORT ->
                    flutterHpplayPlugin.onLelinkPlayerStatus(6)//播放错误
            }
            super.handleMessage(msg)
        }

        init {
            mReference = WeakReference<FlutterHpplayPlugin>(reference)
        }
    }
}
