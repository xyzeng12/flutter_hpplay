import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:platform/platform.dart';

typedef Future<dynamic> EventHandler(Map<String, dynamic> event);

enum LB_ENUM {
  LBLelinkMediaTypeVideoOnline, // 在线视频媒体类型
  LBLelinkMediaTypeAudioOnline, // 在线音频媒体类型
  LBLelinkMediaTypePhotoOnline, // 在线图片媒体类型
  LBLelinkMediaTypePhotoLocal, // 本地图片媒体类型
  LBLelinkMediaTypeVideoLocal, // 本地视频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地视频的URL
  LBLelinkMediaTypeAudioLocal, // 本地音频媒体类型 注意：需要APP层启动本地的webServer，生成一个本地音频的URL
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

  EventHandler _onLelinkBrowserError;
  EventHandler _onLelinkBrowserDidFindLelinkServices;
  EventHandler _onLelinkConnectionError;
  EventHandler _onLelinkDidConnectionToService;
  EventHandler _onLelinkDisConnectionToService;
  EventHandler _onLelinkPlayerError;
  EventHandler _onLelinkPlayerStatus;
  EventHandler _onLelinkPlayerProgressInfo;

  Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
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
    _channel.invokeMethod('playMedia', {'mediaURLString': mediaURLString, 'mediaType': mediaType});
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
    EventHandler onLelinkBrowserError,

    ///发现设备
    EventHandler onLelinkBrowserDidFindLelinkServices,

    ///连接错误
    EventHandler onLelinkConnectionError,

    ///连接到设备
    EventHandler onLelinkDidConnectionToService,

    ///断开连接
    EventHandler onLelinkDisConnectionToService,

    ///播放错误
    EventHandler onLelinkPlayerError,

    ///播放状态
    EventHandler onLelinkPlayerStatus,

    ///播放进度 总时长、当前播放位置
    EventHandler onLelinkPlayerProgressInfo,
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
        return _onLelinkBrowserError(call.arguments.cast<String, dynamic>());
      case "onLelinkBrowserDidFindLelinkServices":
        return _onLelinkBrowserDidFindLelinkServices(
            call.arguments.cast<String, dynamic>());
      case "onLelinkConnectionError":
        return _onLelinkConnectionError(call.arguments.cast<String, dynamic>());
      case "onLelinkDidConnectionToService":
        return _onLelinkDidConnectionToService(
            call.arguments.cast<String, dynamic>());
      case "onLelinkDisConnectionToService":
        return _onLelinkDisConnectionToService(
            call.arguments.cast<String, dynamic>());
      case "onLelinkPlayerError":
        return _onLelinkPlayerError(call.arguments.cast<String, dynamic>());
      case "onLelinkPlayerStatus":
        return _onLelinkPlayerStatus(call.arguments.cast<String, dynamic>());
      case "onLelinkPlayerProgressInfo":
        return _onLelinkPlayerProgressInfo(
            call.arguments.cast<String, dynamic>());
      default:
        throw new UnsupportedError("Unrecognized Event");
    }
  }
}
