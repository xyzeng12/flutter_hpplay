package com.nearbychen.flutter_hpplay;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.InteractiveAdListener;
import com.hpplay.sdk.source.api.LelinkPlayer;
import com.hpplay.sdk.source.bean.DanmakuPropertyBean;
import com.hpplay.sdk.source.browse.api.AdInfo;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.IQRCodeListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.nearbychen.flutter_hpplay.bean.MessageDeatail;

import java.util.List;

import static com.nearbychen.flutter_hpplay.utils.LoggerKt.logD;
import static com.nearbychen.flutter_hpplay.utils.LoggerKt.logI;
import static com.nearbychen.flutter_hpplay.utils.LoggerKt.logTest;

/**
 * Created by Zippo on 2018/10/13.
 * Date: 2018/10/13
 * Time: 17:08:24
 */
public class LelinkHelper {

    private static final String TAG = "LelinkHelper";

//    private static final String APP_ID = "14138";
//    private static final String APP_SECRET = "9a7065155a706f76e5f8a1ff9dd93cf2";

    private static final String APP_ID = "13302";
    private static final String APP_SECRET = "69714488dedb5370380a61b91ec1c4f4";

    private static LelinkHelper sLelinkHelper;
    private Context mContext;
    private UIHandler mUIHandler;
    private AllCast mAllCast;
    // 数据
    private List<LelinkServiceInfo> mInfos;
    private AdInfo mAdInfo;

    public static LelinkHelper getInstance(Context context) {
        if (sLelinkHelper == null) {
            sLelinkHelper = new LelinkHelper(context);
        }
        return sLelinkHelper;
    }

    private LelinkHelper(Context context) {
        mContext = context;
        mUIHandler = new UIHandler(Looper.getMainLooper());
    }

    public void lelinkSetting(String appid, String appSecret) {
        mAllCast = new AllCast(mContext.getApplicationContext(), appid, appSecret);
        mAllCast.setOnBrowseListener(mBrowseListener);
        mAllCast.setConnectListener(mConnectListener);
        mAllCast.setPlayerListener(mPlayerListener);
    }

    public void setUIUpdateListener(IUIUpdateListener listener) {
        mUIHandler.setUIUpdateListener(listener);
    }

    public List<LelinkServiceInfo> getInfos() {
        return mInfos;
    }

    public List<LelinkServiceInfo> getConnectInfos() {
        return mAllCast.getConnectInfos();
    }

    public void addQRServiceInfo(String qrCode, IQRCodeListener listener) {
        mAllCast.addQRServiceInfo(qrCode, listener);
    }

    public void addPinCodeServiceInfo(String pinCode) {
        mAllCast.addPinCodeServiceInfo(pinCode);
    }

    public void onBrowseListGone() {
        mAllCast.onBrowseListGone();
    }

    public void onPushButtonClick() {
        mAllCast.onPushButtonClick();
    }

    public void browse(int type) {
        mAllCast.browse(type);
    }

    public void stopBrowse() {
        mAllCast.stopBrowse();
    }

    public void connect(LelinkServiceInfo info) {
        mAllCast.connect(info);
    }

    public void disConnect(LelinkServiceInfo info) {
        mAllCast.disConnect(info);
    }

    public void deleteRemoteServiceInfo(LelinkServiceInfo selectInfo) {
        mAllCast.deleteRemoteServiceInfo(selectInfo);
    }

    public boolean canPlayLocalVideo(LelinkServiceInfo info) {
        return mAllCast.canPlayLocalVideo(info);
    }

    public boolean canPlayLocalPhoto(LelinkServiceInfo info) {
        return mAllCast.canPlayLocalPhoto(info);
    }

    public boolean canPlayLocalAudio(LelinkServiceInfo info) {
        return mAllCast.canPlayLocalAudio(info);
    }

    public boolean canPlayOnlineVideo(LelinkServiceInfo info) {
        return mAllCast.canPlayOnlineVideo(info);
    }

    public boolean canPlayOnlineAudio(LelinkServiceInfo info) {
        return mAllCast.canPlayOnlineAudio(info);
    }

    public boolean canPlayOnlinePhoto(LelinkServiceInfo info) {
        return mAllCast.canPlayOnlinePhoto(info);
    }

    public void playLocalMedia(String url, int mediaType, String screencode) {
        mAllCast.playLocalMedia(url, mediaType, screencode);
    }

    public void playNetMedia(String url, int mediaType, String screencode) {
        mAllCast.playNetMedia(url, mediaType, screencode);
    }

    public void sendDanmaku() {
        mAllCast.sendDanmaku();
    }

    public void sendDanmakuProperty(DanmakuPropertyBean propertyBean) {
        mAllCast.sendDanmakuProperty(propertyBean);
    }

    public void resume() {
        mAllCast.resume();
    }

    public void pause() {
        mAllCast.pause();
    }

    public void stop() {
        mAllCast.stop();
    }

    public void seekTo(int progress) {
        mAllCast.seekTo(progress);
    }

    public void setVolume(int percent) {
        mAllCast.setVolume(percent);
    }

    public void voulumeUp() {
        mAllCast.voulumeUp();
    }

    public void voulumeDown() {
        mAllCast.voulumeDown();
    }

    public void setInteractiveAdListener() {
        mAllCast.setInteractiveAdListener(mDemoAdListener);
    }

    public void sendRelevantInfo(String appid, boolean isSdk) {
        mAllCast.sendRelevantInfo(appid, isSdk);
    }

    public void sendLeboRelevantInfo(String appid, boolean isSdk) {
        mAllCast.sendLeboRelevantInfo(appid, isSdk);
    }

    public void sendRelevantErrorInfo() {
        mAllCast.sendRelevantErrorInfo();
    }


    public void playNetMediaAndPassthHeader(String url, int type) {
        mAllCast.playNetMediaWithHeader(url, type);
    }

    public void playNetMediaAndPassthMediaAsset(String url, int type) {
        mAllCast.playNetMediaWithAsset(url, type);
    }

    public void startWithLoopMode(String url, boolean isLocalFile) {
        mAllCast.startWithLoopMode(url, isLocalFile);
    }

    public void startNetVideoWith3rdMonitor(String netVideoUrl) {
        mAllCast.startNetVideoWith3rdMonitor(netVideoUrl);
    }

    public void onInteractiveAdShow() {
        mAllCast.onInteractiveAdShow(mAdInfo, LelinkPlayer.STATUS_SUCCESS);
    }

    public void onInteractiveAdClosed() {
        mAllCast.onInteractiveAdClosed(mAdInfo, 10, LelinkPlayer.STATUS_SUCCESS);
    }

    public void setOption(int opt, Object... values) {
        mAllCast.setOption(opt, values);
    }

    public void startMirror(Activity activity, LelinkServiceInfo info, int resolutionLevel,
                            int bitrateLevel, boolean audioEnable, String screencode) {
        mAllCast.startMirror(activity, info, resolutionLevel, bitrateLevel, audioEnable, screencode);
    }

    public void startScreenShot() {
        mAllCast.startScreenShot();
    }

    public void stopMirror() {
        mAllCast.stopMirror();
    }

    public void release() {
        mAllCast.release();
    }

    private Message buildMessageDetail(int state, String text) {
        return buildMessageDetail(state, text, null);
    }

    private Message buildMessageDetail(int state, String text, Object object) {
        MessageDeatail deatail = new MessageDeatail();
        deatail.setText(text);
        deatail.setObj(object);

        Message message = Message.obtain();
        message.what = state;
        message.obj = deatail;
        return message;
    }

    //设置搜索监听
    //onBrowse是在子线程工作
    private IBrowseListener mBrowseListener = new IBrowseListener() {

        @Override
        public void onBrowse(int resultCode, List<LelinkServiceInfo> list) {
            logD("onSuccess size:" + (list == null ? 0 : list.size()));
            mInfos = list;
            if (resultCode == IBrowseListener.BROWSE_SUCCESS) {//搜索成功
                logD("browse success");
                StringBuffer buffer = new StringBuffer();
                if (null != mInfos) {
                    for (LelinkServiceInfo info : mInfos) {
                        buffer.append("name：").append(info.getName())
                                .append(" uid: ").append(info.getUid())
                                .append(" type:").append(info.getTypes()).append("\n");
                    }
                    buffer.append("---------------------------\n");
                    if (null != mUIHandler) {
                        // 发送文本信息
                        String text = buffer.toString();
                        if (mInfos.isEmpty()) {
                            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_NO_RESULT, text));
                        } else {
                            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_SUCCESS, text));
                        }
                    }
                }
            } else {//搜索失败，**Auth错误，请检查您的网络设置或AppId和AppSecret**
                if (null != mUIHandler) {
                    // 发送文本信息
                    logTest("browse error:Auth error");
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_ERROR, "搜索错误：Auth错误"));
                }
            }

        }

    };

    private IConnectListener mConnectListener = new IConnectListener() {

        @Override
        public void onConnect(final LelinkServiceInfo serviceInfo, final int extra) {
            logD("onConnect:" + serviceInfo.getName());
            if (null != mUIHandler) {
                String type = extra == TYPE_LELINK ? "Lelink" : extra == TYPE_DLNA ? "DLNA" : extra == TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                String text;
                if (TextUtils.isEmpty(serviceInfo.getName())) {
                    text = "pin码连接" + type + "成功";
                } else {
                    text = serviceInfo.getName() + "连接" + type + "成功";
                }
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_SUCCESS, text, serviceInfo));
            }
        }

        @Override
        public void onDisconnect(LelinkServiceInfo serviceInfo, int what, int extra) {
            logD("onDisconnect:" + serviceInfo.getName() + " disConnectType:" + what + " extra:" + extra);
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {//连接断开
                if (null != mUIHandler) {
                    String text;
                    if (TextUtils.isEmpty(serviceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = serviceInfo.getName() + "连接断开";
                    }
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_DISCONNECT, text));
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                String text = null;
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = serviceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_WAITTING) {
                    text = serviceInfo.getName() + "等待确认";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_REJECT) {
                    text = serviceInfo.getName() + "连接拒绝";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_TIMEOUT) {
                    text = serviceInfo.getName() + "连接超时";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_BLACKLIST) {
                    text = serviceInfo.getName() + "连接黑名单";
                }
                if (null != mUIHandler) {
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_FAILURE, text));
                }
            }
        }

    };
    //设置播控监听
    private ILelinkPlayerListener mPlayerListener = new ILelinkPlayerListener() {

        @Override
        public void onLoading() {
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_LOADING, "开始加载"));
            }
        }

        @Override
        public void onStart() {
            logD("onStart:");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_PLAY, "开始播放"));
            }
        }

        @Override
        public void onPause() {
            logD("onPause");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_PAUSE, "暂停播放"));
            }
        }

        @Override
        public void onCompletion() {
            logD("onCompletion");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_COMPLETION, "播放完成"));
            }
        }

        @Override
        public void onStop() {
            logD("onStop");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_STOP, "播放结束"));
            }
        }

        @Override
        public void onSeekComplete(int pPosition) {
            logD("onSeekComplete position:" + pPosition);
            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEEK, "设置进度"));
        }

        @Override
        public void onInfo(int what, int extra) {
            logD("onInfo what:" + what + " extra:" + extra);
            String text = null;
            if (what == ILelinkPlayerListener.INFO_SCREENSHOT) {
                if (extra == ILelinkPlayerListener.INFO_SCREENSHOT_COMPLATION) {
                    text = "截图完成";
                } else {
                    text = "截图失败";
                }
                if (null != mUIHandler) {
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SCREENSHOT, text));
                }
            }
        }

        @Override
        public void onError(int what, int extra) {
            logD("onError what:" + what + " extra:" + extra);
            String text = null;
            if (what == ILelinkPlayerListener.PUSH_ERROR_INIT) {//推送初始化错误
                if (extra == ILelinkPlayerListener.PUSH_ERRROR_FILE_NOT_EXISTED) {
                    text = "文件不存在";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_OFFLINE) {
                    text = "IM TV不在线";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IMAGE) {

                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_UNSUPPORTED_MIMETYPE) {
                    text = "IM不支持的媒体类型";
                } else {
                    text = "未知";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_INIT) {//镜像初始化错误
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_UNSUPPORTED) {
                    text = "不支持镜像";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_REJECT_PERMISSION) {
                    text = "镜像权限拒绝";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_DEVICE_UNSUPPORTED) {
                    text = "设备不支持镜像";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_PREPARE) {//镜像准备错误
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_INFO) {
                    text = "获取镜像信息出错";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_PORT) {
                    text = "获取镜像端口出错";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    if (null != mUIHandler) {
                        mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_INPUT_SCREENCODE, text));
                    }
                    return;
                } else if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                    text = "投屏码模式不支持抢占";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PLAY) {//推送播放错误
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "播放无响应";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    if (null != mUIHandler) {
                        mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_INPUT_SCREENCODE, text));
                    }
                    return;
                } else if (extra == ILelinkPlayerListener.RELEVANCE_DATA_UNSUPPORTED) {
                    text = "老乐联不支持数据透传,请升级接收端的版本！";
                    if (null != mUIHandler) {
                        mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.RELEVANCE_DATA_UNSUPPORT, text));
                    }
                    return;
                } else if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                    text = "投屏码模式不支持抢占";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_STOP) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "退出 播放无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PAUSE) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "暂停无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_RESUME) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "恢复无响应";
                }
            }
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_PLAY_ERROR, text));
            }
        }

        /**
         * 音量变化回调
         *
         * @param percent 当前音量
         */
        @Override
        public void onVolumeChanged(float percent) {
            logD("onVolumeChanged percent:" + percent);
        }

        /**
         * 进度更新回调
         *
         * @param duration 媒体资源总长度
         * @param position 当前进度
         */
        @Override
        public void onPositionUpdate(long duration, long position) {
            logD("onPositionUpdate duration:" + duration + " position:" + position);
            long[] arr = new long[]{duration, position};
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_POSITION_UPDATE, "进度更新", arr));
            }
        }

    };

    private InteractiveAdListener mDemoAdListener = new InteractiveAdListener() {

        @Override
        public void onAdLoaded(AdInfo adInfo) {
            logI("onAdLoaded:" + adInfo);
            mAdInfo = adInfo;
        }

    };

    private static class UIHandler extends Handler {

        private IUIUpdateListener mUIUpdateListener;

        private UIHandler(Looper looper) {
            super(looper);
        }

        private void setUIUpdateListener(IUIUpdateListener pUIUpdateListener) {
            mUIUpdateListener = pUIUpdateListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MessageDeatail detail = (MessageDeatail) msg.obj;
            if (null != mUIUpdateListener) {
                mUIUpdateListener.onUpdate(msg.what, detail);
            }
        }
    }

}