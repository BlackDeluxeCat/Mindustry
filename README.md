# MI2 Custom Client | MI2端

主要修改原版显示和界面。

Mainly modify vanilla display and UI.

**kimi违背本仓库要求（传播了自行编译端），严重影响了作者的热情。决定放弃更新本仓库，已有代码保留供参考，126.2最终编译版本已公开于蓝奏云。**

本端仅是作者练习jvav（笑）的产物之一，已基本完成作者喜好的功能，127及以后的跟进版本仅在学术交流群进行测试。如果你有意有能力基于本端开发新功能，遵守[协议](https://github.com/BlackDeluxeCat/Mindustry/blob/master/LICENSE)自取便是。

[126.2蓝奏云链接[Windows/Android]](https://wwr.lanzoui.com/b02c69sha)
密码:crvz

请避免作为新人入门端使用，祝游玩愉快。安卓端未做界面适应，谨慎使用。

## Update & Release

~~自行构建最新测试版本（可体验最新特性），可能存在大量bug。请勿传播自行编译端。~~

~~Release版在原版的更新发布后发布（需要从原版同步内容）。所具有的特性经过测试，应该比较稳定。~~

~~_第一个release预计在v127后发布。_~~

~~Compile the lastest alpha build on your own, may with many potential bugs.~~

~~Release build follows the tag in origin repositoriy. Its new features has been tested for times and should be much more stable.~~

~~_First release build will be done when Mindustry v127 is available._~~

## Features
* 界面
  * 左侧：全队伍资源和单位统计
  * 左上：地图信息+波次信息栏，其他调整
  * 右上：小地图尺寸可调，可点击跳转视角，其他调整
  * 右上：快捷设置界面，有部分常用设置
  * 右下：信息更多更详细，可同时显示方块和单位，其他调整
  * 玩家列表：舒适的布局，增加跳转视角按钮
  * 聊天栏：记录发言玩家临时id和位置
  * 逻辑编辑器：显示指令地址
* 画面
  * 可设置的大量辅助线
  * 桥带和连接器的物品显示
  * 单位血条（及更多）和状态显示
  * 建筑血条（及更多）
  * 钍反应堆贴图修改，渲染燃料棒
  * 可设置单位透明度和单位腿部透明度
  * 可设置的平移视角移动速度
  * 可设置让跟随视角的玩家不在屏幕中央
* 游戏性
  * 蓝图大小限制提高
  * 解除跳波限制
  * 解除敌对信息的屏蔽
* 其他
  * 地图编辑器
    * 解除对实验特性的限制
    * 新增过滤器Point Symmetry（中心对称）：便于快速制作中心对称地图

## Changelog

See [Changelog](core/assets/changelog) (cn)

En is not available bcus I'm lazy.

# ANUKEN!

![Logo](core/assets-raw/sprites/ui/logo.png)

[![Build Status](https://github.com/Anuken/Mindustry/workflows/Tests/badge.svg?event=push)](https://github.com/Anuken/Mindustry/actions)
[![Discord](https://img.shields.io/discord/391020510269669376.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/mindustry)  

A sandbox tower defense game written in Java.

_[Trello Board](https://trello.com/b/aE2tcUwF/mindustry-40-plans)_  
_[Wiki](https://mindustrygame.github.io/wiki)_  
_[Javadoc](https://mindustrygame.github.io/docs/)_ 

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md).

## Building

Bleeding-edge builds are generated automatically for every commit. You can see them [here](https://github.com/Anuken/MindustryBuilds/releases).

If you'd rather compile on your own, follow these instructions.
First, make sure you have [JDK 14](https://adoptopenjdk.net/) installed. Open a terminal in the root directory, `cd` to the Mindustry folder and run the following commands:

### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`  
_Sprite Packing:_ `gradlew tools:pack`

### Linux/Mac OS

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`  
_Sprite Packing:_ `./gradlew tools:pack`

### Server

Server builds are bundled with each released build (in Releases). If you'd rather compile on your own, replace 'desktop' with 'server', e.g. `gradlew server:dist`.

### Android

1. Install the Android SDK [here.](https://developer.android.com/studio#downloads) Make sure you're downloading the "Command line tools only", as Android Studio is not required.
2. Set the `ANDROID_HOME` environment variable to point to your unzipped Android SDK directory.
3. Run `gradlew android:assembleDebug` (or `./gradlew` if on linux/mac). This will create an unsigned APK in `android/build/outputs/apk`.

To debug the application on a connected phone, run `gradlew android:installDebug android:run`.

### Troubleshooting

#### Permission Denied

If the terminal returns `Permission denied` or `Command not found` on Mac/Linux, run `chmod +x ./gradlew` before running `./gradlew`. *This is a one-time procedure.*

---

Gradle may take up to several minutes to download files. Be patient. <br>
After building, the output .JAR file should be in `/desktop/build/libs/Mindustry.jar` for desktop builds, and in `/server/build/libs/server-release.jar` for server builds.

## Feature Requests

Post feature requests and feedback [here](https://github.com/Anuken/Mindustry-Suggestions/issues/new/choose).

## Downloads

| [![](https://static.itch.io/images/badge.svg)](https://anuke.itch.io/mindustry)    |    [![](https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png)](https://play.google.com/store/apps/details?id=io.anuke.mindustry)   |    [![](https://fdroid.gitlab.io/artwork/badge/get-it-on.png)](https://f-droid.org/packages/io.anuke.mindustry)	| [![](https://flathub.org/assets/badges/flathub-badge-en.svg)](https://flathub.org/apps/details/com.github.Anuken.Mindustry)  
|---	|---	|---	|---	|
