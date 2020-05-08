import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_hpplay/flutter_hpplay_plus.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  FlutterHpplayPlus hpplay = FlutterHpplayPlus();
  List datas = [];
  int currentIndex = -1;
  LBLelinkPlayStatus playStatus;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      hpplay.setup(
        appid: '14138',
        secretKey: '9a7065155a706f76e5f8a1ff9dd93cf2',
        // debug: bool.fromEnvironment("dart.vm.product"),
        debug: true,
      );
      platformVersion = await hpplay.platformVersion;
      var isConnected = await hpplay.isConnected;
      print('isConnected---$isConnected');
      hpplay.addEventHandler(
        onLelinkBrowserError: (dynamic message) async {
          print('flutter--搜索错误: $message');
        },
        onLelinkBrowserDidFindLelinkServices: (dynamic message) async {
          setState(() {
            datas = message;
          });
          print('flutter--发现设备: $message');
        },
        onLelinkConnectionError: (dynamic message) async {
          print('flutter--连接错误: $message');
        },
        onLelinkDidConnectionToService: (dynamic message) async {
          setState(() {
            currentIndex = int.parse(message);
          });
          print('flutter--连接到设备: $currentIndex');
        },
        onLelinkDisConnectionToService: (dynamic message) async {
          print('flutter--断开连接: $message');
          setState(() {
            currentIndex = -1;
          });
        },
        onLelinkPlayerError: (dynamic message) async {
          print('flutter--播放错误: $message');
        },
        onLelinkPlayerStatus: (dynamic message) async {
          print('flutter--播放状态: $message');
          setState(() {
            playStatus = LBLelinkPlayStatus.values[int.parse(message)];
          });
        },
        onLelinkPlayerProgressInfo: (Map<String, dynamic> message) async {
          print('flutter--播放进度 总时长:${message['duration']}、当前播放位置:${message['currentTime']}');
        },
      );
      hpplay.search();
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> views = [];
    for (var i = 0; i < datas.length; i++) {
      views.add(InkWell(
        onTap: () {
          hpplay.deviceListDidSelectIndex(i);
        },
        child: Text(
          '${datas[i]['name']}',
          style: TextStyle(color: currentIndex == i ? Colors.red : Colors.black),
        ),
      ));
    }
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Column(
                children: views,
              ),
              InkWell(
                onTap: () {
                  hpplay.deviceListDidSelectIndex(0);
                },
                child: Text('链接: $_platformVersion\n'),
              ),
              InkWell(
                onTap: () async {
                  var isConnected = await hpplay.isConnected;
                  print('isConnected---$isConnected');
                  hpplay.playMedia(
                      'http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4',
                      LBLelinkMediaType.LBLelinkMediaTypeVideoOnline.index);
                },
                child: Text('播放: $_platformVersion\n'),
              ),
              InkWell(
                onTap: () {
                  if (playStatus == LBLelinkPlayStatus.LBLelinkPlayStatusPause) {
                    hpplay.resumePlay();
                  } else {
                    hpplay.pause();
                  }
                },
                child: Text(playStatus == LBLelinkPlayStatus.LBLelinkPlayStatusPause ? '继续' : '暂停'),
              ),
              InkWell(
                onTap: () {
                  hpplay.stop();
                },
                child: Text('停止: $_platformVersion\n'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
