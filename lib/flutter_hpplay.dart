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
  int duration;
  int currentTime;

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
  }) {
    _plus.setup(
        appid: appid, secretKey: secretKey, channel: channel, debug: debug);
  }

  Future<bool> get isConnected async {
    return _plus.isConnected;
  }

  ///搜索设备
  void search() {
    _plus.search();
  }

   ///连接设备 index：索引
  void deviceListDidSelectIndex(int index) {
    _plus.deviceListDidSelectIndex(index);
  }

   ///播放
  void playMedia(String mediaURLString, LBLelinkMediaType mediaType) {
    _plus.playMedia(mediaURLString, mediaType.index);
  }

   ///滚动进度
  void seekTo(int seek) {
    _plus.seekTo(seek);
  }

   ///暂停
  void pause() {
    _plus.pause();
  }

   ///继续播放
  void resumePlay() {
    _plus.resumePlay();
  }

   ///停止投屏
  void stop() {
    _plus.stop();
  }

  ///加音量
  void addVolume() {
    _plus.addVolume();
  }
  
  ///减音量
  void reduceVolume() {
    _plus.reduceVolume();
  }

  ///控制音量物理按键是否有效
  void isIntoBg(int isIntoBg) {
    _plus.isIntoBg(isIntoBg);
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
    _onLelinkBrowserError = onLelinkBrowserError ?? _onLelinkBrowserError;
    _onLelinkBrowserDidFindLelinkServices =
        onLelinkBrowserDidFindLelinkServices ??
            _onLelinkBrowserDidFindLelinkServices;
    _onLelinkConnectionError =
        onLelinkConnectionError ?? _onLelinkConnectionError;
    _onLelinkDidConnectionToService =
        onLelinkDidConnectionToService ?? _onLelinkDidConnectionToService;
    _onLelinkDisConnectionToService =
        onLelinkDisConnectionToService ?? _onLelinkDisConnectionToService;
    _onLelinkPlayerError = onLelinkPlayerError ?? _onLelinkPlayerError;
    _onLelinkPlayerStatus = onLelinkPlayerStatus ?? _onLelinkPlayerStatus;
    _onLelinkPlayerProgressInfo =
        onLelinkPlayerProgressInfo ?? _onLelinkPlayerProgressInfo;
  }

  void registerHandler() {
    _plus.addEventHandler(
      onLelinkBrowserError: (dynamic message) async {
        if (_onLelinkBrowserError != null) {
          _onLelinkBrowserError(message);
        }
      },
      onLelinkBrowserDidFindLelinkServices: (dynamic message) async {
        serviceNames = message;
        if (_onLelinkBrowserDidFindLelinkServices != null) {
          _onLelinkBrowserDidFindLelinkServices(message);
        }
      },
      onLelinkConnectionError: (dynamic message) async {
        if (_onLelinkConnectionError != null) {
          _onLelinkConnectionError(message);
        }
      },
      onLelinkDidConnectionToService: (dynamic message) async {
        currentIndex = int.parse(message);
        if (_onLelinkDidConnectionToService != null) {
          _onLelinkDidConnectionToService(message);
        }
      },
      onLelinkDisConnectionToService: (dynamic message) async {
        currentIndex = -1;
        if (_onLelinkDisConnectionToService != null) {
          _onLelinkDisConnectionToService(message);
        }
      },
      onLelinkPlayerError: (dynamic message) async {
        if (_onLelinkPlayerError != null) {
          _onLelinkPlayerError(message);
        }
      },
      onLelinkPlayerStatus: (dynamic message) async {
        playStatus = LBLelinkPlayStatus.values[int.parse(message)];
        if (_onLelinkPlayerStatus != null) {
          _onLelinkPlayerStatus(message);
        }
      },
      onLelinkPlayerProgressInfo: (Map<String, dynamic> message) async {
        duration = int.parse(message['duration']);
        currentTime = int.parse(message['currentTime']);
        if (_onLelinkPlayerProgressInfo != null) {
          _onLelinkPlayerProgressInfo(message);
        }
        // print(
        //     'flutter--播放进度 总时长:${message['duration']}、当前播放位置:${message['currentTime']}');
      },
    );
  }
}
