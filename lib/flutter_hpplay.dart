import 'package:flutter_hpplay/flutter_hpplay_plus.dart';

///乐播投屏扩展插件
class FlutterHpplay {
  FlutterHpplayPlus _plus;
  FlutterHpplay._();

  static FlutterHpplay _instance;

  static FlutterHpplay getInstance() {
    if (_instance == null) {
      _instance = FlutterHpplay._();
      _instance._plus = FlutterHpplayPlus();
      _instance.registerHandler();
    }
    return _instance;
  }

  List serviceNames = [];
  int currentIndex = -1;
  LBLelinkPlayStatus playStatus;

  DynamicHandler _onLelinkBrowserError;
  DynamicHandler _onLelinkBrowserDidFindLelinkServices;
  DynamicHandler _onLelinkConnectionError;
  DynamicHandler _onLelinkDidConnectionToService;
  DynamicHandler _onLelinkDisConnectionToService;
  DynamicHandler _onLelinkPlayerError;
  DynamicHandler _onLelinkPlayerStatus;
  MapHandler _onLelinkPlayerProgressInfo;

  void setup({
    String appid,
    String secretKey,
    String channel = '',
    bool debug = false,
  })  {
    _plus.setup(
        appid: appid, secretKey: secretKey, channel: channel, debug: debug);
  }
  
  Future<bool> get isConnected async {
    return _plus.isConnected;
  }

  void search() {
    _plus.search();
  }

  void deviceListDidSelectIndex(int index) {
    _plus.deviceListDidSelectIndex(index);
  }

  void playMedia(String mediaURLString, int mediaType) {
    _plus.playMedia(mediaURLString, mediaType);
  }

  void pause() {
    _plus.pause();
  }

  void resumePlay() {
    _plus.resumePlay();
  }

  void stop() {
    _plus.stop();
  }
  
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
    _onLelinkBrowserError = onLelinkBrowserError;
    _onLelinkBrowserDidFindLelinkServices =
        onLelinkBrowserDidFindLelinkServices;
    _onLelinkConnectionError = onLelinkConnectionError;
    _onLelinkDidConnectionToService = onLelinkDidConnectionToService;
    _onLelinkDisConnectionToService = onLelinkDisConnectionToService;
    _onLelinkPlayerError = onLelinkPlayerError;
    _onLelinkPlayerStatus = onLelinkPlayerStatus;
    _onLelinkPlayerProgressInfo = onLelinkPlayerProgressInfo;
  }

  void registerHandler() {
    _plus.addEventHandler(
      onLelinkBrowserError: (dynamic message) async {
        _onLelinkBrowserError(message);
      },
      onLelinkBrowserDidFindLelinkServices: (dynamic message) async {
        serviceNames = message;
        _onLelinkBrowserDidFindLelinkServices(message);
      },
      onLelinkConnectionError: (dynamic message) async {
        _onLelinkConnectionError(message);
      },
      onLelinkDidConnectionToService: (dynamic message) async {
        currentIndex = int.parse(message);
        _onLelinkDidConnectionToService(message);
      },
      onLelinkDisConnectionToService: (dynamic message) async {
        currentIndex = -1;
        _onLelinkDisConnectionToService(message);
      },
      onLelinkPlayerError: (dynamic message) async {
        _onLelinkPlayerError(message);},
      onLelinkPlayerStatus: (dynamic message) async {
        playStatus = LBLelinkPlayStatus.values[int.parse(message)];
        _onLelinkPlayerStatus(message);
      },
      onLelinkPlayerProgressInfo: (Map<String, dynamic> message) async {
        _onLelinkPlayerProgressInfo(message);
        // print(
        //     'flutter--播放进度 总时长:${message['duration']}、当前播放位置:${message['currentTime']}');
      },
    );
  }
}
