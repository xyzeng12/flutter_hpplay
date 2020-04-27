import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_hpplay/flutter_hpplay.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  FlutterHpplay hpplay = FlutterHpplay();
  List datas = [];
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
      platformVersion = await hpplay.platformVersion;
      hpplay.addEventHandler(
        onLelinkBrowserDidFindLelinkServices:
            (Map<String, dynamic> message) async {
          List services = message['services'];
          setState(() {
            datas = services;
          });
          print('flutter');
          print(services[0]['name']);
        },
        onLelinkDidConnectionToService: (Map<String, dynamic> message) async {
          1;
        },
      );
      hpplay.search();
      hpplay.setup(
        appid: '14138',
        secretKey: '9a7065155a706f76e5f8a1ff9dd93cf2',
        // debug: bool.fromEnvironment("dart.vm.product"),
        debug: true,
      );
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
        child: Text('${datas[i]['name']}'),
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
                onTap: () {
                  hpplay.playMedia(
                      'http://video.gzk12.com/2cb0accbda1d4f17ae532b1f7f49f571/18e6eda7f5c4a7589a7890584cb7138a-fd-encrypt-stream.m3u8',
                      LB_ENUM.LBLelinkMediaTypeVideoOnline.index);
                },
                child: Text('播放: $_platformVersion\n'),
              ),
              InkWell(
                onTap: () {
                  hpplay.pause();
                },
                child: Text('暂停: $_platformVersion\n'),
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
