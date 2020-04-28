import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:platform/platform.dart';

typedef Future<dynamic> MapHandler(Map<String, dynamic> event);
typedef Future<dynamic> DynamicHandler(dynamic val);

///媒体类型
enum LBLelinkMediaType {
  LBLelinkMediaTypeVideoOnline, // 在线视频媒体类型
  LBLelinkMediaTypeAudioOnline, // 在线音频媒体类型
  LBLelinkMediaTypePhotoOnline, // 在线图片媒体类型
  LBLelinkMediaTypePhotoLocal, // 本地图片媒体类型
  LBLelinkMediaTypeVideoLocal, // 本地视频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地视频的URL
  LBLelinkMediaTypeAudioLocal, // 本地音频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地音频的URL
}

///播放状态
enum LBLelinkPlayStatus {
  LBLelinkPlayStatusUnkown, // 未知状态
  LBLelinkPlayStatusLoading, // 视频正在加载状态
  LBLelinkPlayStatusPlaying, // 正在播放状态
  LBLelinkPlayStatusPause, // 暂停状态
  LBLelinkPlayStatusStopped, // 退出播放状态
  LBLelinkPlayStatusCommpleted, // 播放完成状态
  LBLelinkPlayStatusError, // 播放错误
}

class FlutterHpplay {
  final String flutterLog = "| Hpplay | Flutter | ";
  factory FlutterHpplay() => _instance;

  final MethodChannel _channel;
  final Platform _platform;

  @visibleForTesting
  FlutterHpplay.private(MethodChannel channel, Platform platform)
      : _channel = channel,
        _platform = platform;

  static final FlutterHpplay _instance = FlutterHpplay.private(
      const MethodChannel('flutter_hpplay'), const LocalPlatform());

  DynamicHandler _onLelinkBrowserError;
  DynamicHandler _onLelinkBrowserDidFindLelinkServices;
  DynamicHandler _onLelinkConnectionError;
  DynamicHandler _onLelinkDidConnectionToService;
  DynamicHandler _onLelinkDisConnectionToService;
  DynamicHandler _onLelinkPlayerError;
  DynamicHandler _onLelinkPlayerStatus;
  MapHandler _onLelinkPlayerProgressInfo;

  Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> get isConnected async {
    var val = await _channel.invokeMethod('getIsConnected');
    final bool isConnected = val == '1';
    return isConnected;
  }

  void setup({
    String appid,
    String secretKey,
    String channel = '',
    bool debug = false,
  }) {
    print(flutterLog + "setup:");
    _channel.invokeMethod(
        'setup', {'appid': appid, 'secretKey': secretKey, 'debug': debug});
  }

  void search() {
    _channel.invokeMethod('search');
  }

  void deviceListDidSelectIndex(int index) {
    _channel.invokeMethod('deviceListDidSelectIndex', {'index': index});
  }

  void playMedia(String mediaURLString, int mediaType) {
    _channel.invokeMethod('playMedia',
        {'mediaURLString': mediaURLString, 'mediaType': mediaType});
  }

  void pause() {
    _channel.invokeMethod('pause');
  }

  void resumePlay() {
    _channel.invokeMethod('resumePlay');
  }

  void stop() {
    _channel.invokeMethod('stop');
  }

  ///
  /// 初始化 FlutterHpplay 必须先初始化才能执行其他操作(比如接收事件传递)
  ///
  void addEventHandler({
    ///搜索错误
    DynamicHandler onLelinkBrowserError,

    ///发现设备
    DynamicHandler onLelinkBrowserDidFindLelinkServices,

    ///连接错误
    DynamicHandler onLelinkConnectionError,

    ///连接到设备
    DynamicHandler onLelinkDidConnectionToService,

    ///断开连接
    DynamicHandler onLelinkDisConnectionToService,

    ///播放错误
    DynamicHandler onLelinkPlayerError,

    ///播放状态
    DynamicHandler onLelinkPlayerStatus,

    ///播放进度 总时长、当前播放位置
    MapHandler onLelinkPlayerProgressInfo,
  }) {
    print(flutterLog + "addEventHandler:");

    _onLelinkBrowserError = onLelinkBrowserError;
    _onLelinkBrowserDidFindLelinkServices =
        onLelinkBrowserDidFindLelinkServices;
    _onLelinkConnectionError = onLelinkConnectionError;
    _onLelinkDidConnectionToService = onLelinkDidConnectionToService;
    _onLelinkDisConnectionToService = onLelinkDisConnectionToService;
    _onLelinkPlayerError = onLelinkPlayerError;
    _onLelinkPlayerStatus = onLelinkPlayerStatus;
    _onLelinkPlayerProgressInfo = onLelinkPlayerProgressInfo;
    _channel.setMethodCallHandler(_handleMethod);
  }

  Future<Null> _handleMethod(MethodCall call) async {
    print(flutterLog + "_handleMethod:");

    switch (call.method) {
      case "onLelinkBrowserError":
        return _onLelinkBrowserError(call.arguments);
      case "onLelinkBrowserDidFindLelinkServices":
        return _onLelinkBrowserDidFindLelinkServices(call.arguments);
      case "onLelinkConnectionError":
        return _onLelinkConnectionError(call.arguments);
      case "onLelinkDidConnectionToService":
        return _onLelinkDidConnectionToService(call.arguments);
      case "onLelinkDisConnectionToService":
        return _onLelinkDisConnectionToService(call.arguments);
      case "onLelinkPlayerError":
        return _onLelinkPlayerError(call.arguments);
      case "onLelinkPlayerStatus":
        return _onLelinkPlayerStatus(call.arguments);
      case "onLelinkPlayerProgressInfo":
        return _onLelinkPlayerProgressInfo(
            call.arguments.cast<String, dynamic>());
      default:
        throw new UnsupportedError("Unrecognized Event");
    }
  }
}
