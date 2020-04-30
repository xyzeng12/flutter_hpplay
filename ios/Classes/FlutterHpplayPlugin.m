#import "FlutterHpplayPlugin.h"
#import "LBLelinkKitManager/LBLelinkKitManager.h"
#import "LBSystemVolumeKeyObserver.h"
#import <UIKit/UIKit.h>
/**
step 1: 导入头文件
*/
#import <LBLelinkKit/LBLelinkKit.h>
/**
step 2: 到乐播官网（http://cloud.hpplay.cn/dev/）注册账号，并添加APP，获取APPid和密钥
*/
//NSString * const LBAPPID = @"14138";                                    // APP id
//NSString * const LBSECRETKEY = @"9a7065155a706f76e5f8a1ff9dd93cf2";     // 密钥

/**
 视频投屏状态

 - LBVideoCastStateUnCastUnConnect: 未点击投屏，未连接
 - LBVideoCastStateCastedUnConnect: 已点击投屏，未连接
 - LBVideoCastStateCastedConnected: 已点击投屏，已连接
 - LBVideoCastStateUnCastConnected: 未点击投屏（或者点击退出投屏），已连接
 */
typedef NS_ENUM(NSUInteger, LBVideoCastState) {
    LBVideoCastStateUnCastUnConnect,
    LBVideoCastStateCastedUnConnect,
    LBVideoCastStateCastedConnected,
    LBVideoCastStateUnCastConnected,
};

@interface FlutterHpplayPlugin ()

@property (nonatomic, assign) LBVideoCastState castState;
@property (nonatomic, strong) LBSystemVolumeKeyObserver *volumeKeyObserver;

@end

@implementation FlutterHpplayPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_hpplay"
            binaryMessenger:[registrar messenger]];
  FlutterHpplayPlugin* instance = [[FlutterHpplayPlugin alloc] init];
  instance.channel = channel;
//  [registrar addApplicationDelegate:instance];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)dealloc {
    
    [[LBLelinkKitManager sharedManager] reportSerivesListViewDisappear];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    LBLelinkKitManager * manager =[LBLelinkKitManager sharedManager];
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }if ([@"getIsConnected" isEqualToString:call.method]) {
      result(manager.currentConnection.isConnected?@"1":@"0");
  }else if([@"setup" isEqualToString:call.method]) {
    [self setup:call result: result];
  }else if([@"search" isEqualToString:call.method]) {
    [self search:call result: result];
  }else if([@"deviceListDidSelectIndex" isEqualToString:call.method]) {
    [self deviceListDidSelectIndex:call result: result];
  }else if([@"playMedia" isEqualToString:call.method]) {
    [self playMedia:call result: result];
  }else if([@"seekTo" isEqualToString:call.method]) {
    [self seekTo:call result: result];
  }else if([@"pause" isEqualToString:call.method]) {
    [self pause:call result: result];
  }else if([@"resumePlay" isEqualToString:call.method]) {
    [self resumePlay:call result: result];
  }else if([@"stop" isEqualToString:call.method]) {
    [self stop:call result: result];
  }else if([@"addVolume" isEqualToString:call.method]) {
    [self addVolume:call result: result];
  }else if([@"reduceVolume" isEqualToString:call.method]) {
    [self reduceVolume:call result: result];
  }else if([@"isIntoBg" isEqualToString:call.method]) {
      [self isIntoBg:call result: result];
  }else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)search:(FlutterMethodCall*)call result:(FlutterResult)result {
    // 添加通知监听
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(disConnectedNotification:) name:LBLelinkKitManagerConnectionDisConnectedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(connectionDidConnected:) name:LBLelinkKitManagerConnectionDidConnectedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerErrorNotification:) name:LBLelinkKitManagerPlayerErrorNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerStatusNotification:) name:LBLelinkKitManagerPlayerStatusNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerProgressNotification:) name:LBLelinkKitManagerPlayerProgressNotification object:nil];
    [[LBLelinkKitManager sharedManager] search];
}

- (void)deviceListDidSelectIndex:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary *arguments = call.arguments;
    int index = [arguments[@"index"] intValue];
    if(index >= [LBLelinkKitManager sharedManager].lelinkConnections.count){
        return;
    }
    [[LBLelinkKitManager sharedManager] deviceListDidSelectIndex: index];
}

- (void)playMedia:(FlutterMethodCall*)call result:(FlutterResult)result {
    self.volumeKeyObserver.isIntoBg = NO;
    NSDictionary *arguments = call.arguments;
    // 推送媒体
    if (self.castState == LBVideoCastStateCastedConnected) {
        LBLelinkPlayerItem * item = [[LBLelinkPlayerItem alloc] init];
        if ([[LBLelinkKitManager sharedManager].lelinkPlayer canPlayMedia:LBLelinkMediaTypeVideoLocal]) {
            /** 注意，为了适配接收端的bug，播放之前先stop，否则当先推送音乐再推送视频的时候会导致连接被断开 */
            item.mediaURLString = arguments[@"mediaURLString"];
            item.mediaType = (int)arguments[@"mediaType"];
            [[LBLelinkKitManager sharedManager].lelinkPlayer stop];
            [[LBLelinkKitManager sharedManager].lelinkPlayer playWithItem:item];
        }else{
            // 1.创建UIAlertController
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"温馨提示" message:@"当前连接不支持本地视频播放" preferredStyle:UIAlertControllerStyleAlert ];
            UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                self.castState = LBVideoCastStateUnCastConnected;
            }];
            [alertController addAction:okAction];
            [alertController showViewController:[UIApplication sharedApplication].keyWindow.rootViewController sender:nil];
        }
    }else if (self.castState == LBVideoCastStateCastedUnConnect) {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"温馨提示" message:@"当前连接已断开，请重新连接" preferredStyle:UIAlertControllerStyleAlert ];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
//            self.castState = LBVideoCastStateUnCastConnected;
        }];
        [alertController addAction:okAction];
        [alertController showViewController:[UIApplication sharedApplication].keyWindow.rootViewController sender:nil];
    }
}

- (void)seekTo:(FlutterMethodCall*)call result:(FlutterResult)result {
     if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
         NSDictionary *arguments = call.arguments;
         NSInteger seek = [arguments[@"seek"] integerValue];
         [[LBLelinkKitManager sharedManager].lelinkPlayer seekTo:seek];
    }
}

- (void)pause:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
        [[LBLelinkKitManager sharedManager].lelinkPlayer pause];
    }
}

- (void)resumePlay:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
        [[LBLelinkKitManager sharedManager].lelinkPlayer resumePlay];
    }
    
}

- (void)stop:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
        [[LBLelinkKitManager sharedManager].lelinkPlayer stop];
        self.castState = LBVideoCastStateUnCastConnected;
    }else{
        self.castState = LBVideoCastStateUnCastUnConnect;
    }
}

- (void)addVolume:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
        [[LBLelinkKitManager sharedManager].lelinkPlayer addVolume];
    }
}

- (void)reduceVolume:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
        [[LBLelinkKitManager sharedManager].lelinkPlayer reduceVolume];
    }
}

- (void)isIntoBg:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary *arguments = call.arguments;
    self.volumeKeyObserver.isIntoBg = [arguments[@"isIntoBg"] boolValue];
}

- (void)setup:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary *arguments = call.arguments;
    NSNumber *debug = arguments[@"debug"];
    if ([debug boolValue]) {
        [LBLelinkKit enableLog:YES];
    } else {
        [LBLelinkKit enableLog:NO];
    }
    /**
     step 4: 使用APP id 和密钥授权授权SDK
     注意：（1）需要在Info.plist中设置ATS；（2）可以异步执行，不影响APP的启动
     */
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSError * error = nil;
        BOOL result = [LBLelinkKit authWithAppid:arguments[@"appid"] secretKey:arguments[@"secretKey"] error:&error];
        if (result) {
            NSLog(@"授权成功");
        }else{
            NSLog(@"授权失败：error = %@",error);
        }
    });
    [LBLelinkKitManager sharedManager].channel = _channel;
    // 创建音量键监听
    self.volumeKeyObserver = [[LBSystemVolumeKeyObserver alloc] init];
}

#pragma mark - AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    return YES;
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    //  _resumingFromBackground = YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    //  application.applicationIconBadgeNumber = 1;
    //  application.applicationIconBadgeNumber = 0;
}

- (bool)application:(UIApplication *)application
didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler {
    
    return YES;
}

- (void)application:(UIApplication *)application
didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
}

- (void)application:(UIApplication *)application
didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings {
    
}

#pragma mark - notification

- (void)disConnectedNotification:(NSNotification *)notification {
    NSLog(@"%s", __func__);
    if (self.castState == LBVideoCastStateCastedConnected) {
        self.castState = LBVideoCastStateUnCastUnConnect;
    }
}

- (void)connectionDidConnected:(NSNotification *)notification {
    NSLog(@"%s", __func__);
    
    if (self.castState == LBVideoCastStateCastedUnConnect) {
        self.castState = LBVideoCastStateCastedConnected;
//        [self castVideo];
    }else if(self.castState == LBVideoCastStateCastedConnected){
//        [self castVideo];
    }else if (self.castState == LBVideoCastStateUnCastUnConnect){
        self.castState = LBVideoCastStateCastedConnected;
//        [self castVideo];
    }
}

- (void)playerErrorNotification:(NSNotification *)notification {
    NSLog(@"playerErrorNotification");
}

- (void)playerStatusNotification:(NSNotification *)notification {
    NSNumber * status = notification.userInfo[@"playStatus"];
//    [self.videoPlayerView updatePlayStatus:[status integerValue]];
    
    switch ([status integerValue]) {
        case LBLelinkPlayStatusError:
            break;
        case LBLelinkPlayStatusUnkown:
            break;
        case LBLelinkPlayStatusLoading:
            break;
        case LBLelinkPlayStatusPlaying:
            break;
        case LBLelinkPlayStatusPause:
            break;
        case LBLelinkPlayStatusStopped:
            break;
        case LBLelinkPlayStatusCommpleted:
//            if (self.type == LBVideoViewControllerOnlineSerial || self.type == LBVideoViewControllerLocal) {
//                [self autoNext];
//            }
            break;
        default:
            break;
    }
}

- (void)playerProgressNotification:(NSNotification *)notification {
    LBLelinkProgressInfo * progressInfo = notification.userInfo[@"progressInfo"];
//    [self.videoPlayerView updatePlayProgress:progressInfo];
}
@end

