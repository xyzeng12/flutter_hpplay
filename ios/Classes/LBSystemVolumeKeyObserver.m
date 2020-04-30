//
//  LBSystemVolumeKeyObserver.m
//  LBLelinkKitSample
//
//  Created by 刘明星 on 2018/8/31.
//  Copyright © 2018 深圳乐播科技有限公司. All rights reserved.
//

#import "LBSystemVolumeKeyObserver.h"

#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>

@interface LBSystemVolumeKeyObserver ()

@property (nonatomic, assign) BOOL isIntoBg;

@end

@implementation LBSystemVolumeKeyObserver

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setObserver];
    }
    return self;
}

- (void)setObserver {
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setActive:YES error:nil];
    [session setCategory:AVAudioSessionCategoryPlayback error:nil];
    NSError *error;
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    //注，ios9上不加这一句会无效
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
    
    //监听音量调节
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(volumeChanged:) name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
    
    // 如果进入后台也可用音量键控制声音，则注释掉下面两个通知
//    //进入后台
//    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(intoBackg) name:UIApplicationDidBecomeActiveNotification object:nil];
//    //进入前台
//    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(returnBackg) name:UIApplicationWillResignActiveNotification object:nil];
    
    
    // 监听音量键不显示音量条, 如果想显示音量条则注释掉以下代码
//    MPVolumeView *volumeView = [[MPVolumeView alloc] initWithFrame:CGRectMake(0, 0, 200, 20)];
//    volumeView.center = CGPointMake(-550,370);//设置中心点，让音量视图不显示在屏幕中
//    [volumeView sizeToFit];
//    UIWindow * window = [UIApplication sharedApplication].keyWindow;
//    [window addSubview:volumeView];
}

- (void)volumeChanged:(NSNotification *)noti {
    float volume =
    [[[noti userInfo] objectForKey:@"AVSystemController_AudioVolumeNotificationParameter"] floatValue];
    NSLog(@"volumn is %f", volume);
    NSString *str1 = [[noti userInfo]objectForKey:@"AVSystemController_AudioCategoryNotificationParameter"];
    NSString *str2 = [[noti userInfo]objectForKey:@"AVSystemController_AudioVolumeChangeReasonNotificationParameter"];
    if (([str1 isEqualToString:@"Audio/Video"] || [str1 isEqualToString:@"Ringtone"]) && ([str2 isEqualToString:@"ExplicitVolumeChange"])) {
        if(_isIntoBg == NO){
            //这里做你想要的进行的操作
            if ([LBLelinkKitManager sharedManager].currentConnection.isConnected) {
                [[LBLelinkKitManager sharedManager].lelinkPlayer setVolume:(NSInteger)(volume * 100)];
            }
            
        }
    }
}

- (void)intoBackg {
    NSLog(@"***************后台出来*****************");
    _isIntoBg = NO;
}

- (void)returnBackg {
    NSLog(@"***************进入后台*****************");
    _isIntoBg = YES;
}





@end
