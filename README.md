# WheelView

适用 Android 开发，提供类 iOS 的滚动选择功能。

## 1. 特性

* 支持多级联动，级数自定义。
* 支持自定义 item 布局。
* 支持自定义绑定数据的方式，即不限定数据类型。
* 支持自定义同时显示的 item 数量。
* 支持目标数据变化的实时监听。
* 支持获取和设定当前选中项。

## 2. 文件列表

| 文件名         | 说明                   |
| -------------- | ---------------------- |
| WheelView      | 单个滚动选择控件。     |
| MultiWheelView | 多级联动滚动选择控件。 |

## 3. xml 属性与方法说明

（1）WheelView 说明：

| xml 属性          | 类型      | 方法                                | 功能                                                         |
| :---------------- | --------- | ----------------------------------- | ------------------------------------------------------------ |
| app:radicalNotify | boolean   | 无                                  | 影响 TargetDataObserver 被调用频率，如果为 true 则不论当前是否滚动，只要目标位置的数据发生变化就会被调用，否则只在没有滚动时被调用。 |
| app:displayCount  | int       | getDisplayCount()/setDisplayCount() | 同时显示的 item 数量，必须为大于 0 的奇数，否则会抛异常或被修正为奇数，大小向 5 靠拢。 |
| app:itemLayout    | reference | 无                                  | 自定义 item 的布局，如果不设定 ItemAdapter 则要求布局中必须有 TextView 且 id 设定为 android.R.id.text1 |
| app:maskColor     | color     | setMaskBackground()                 | 设置遮罩层颜色，遮罩层用于实现渐变突出正中间项的效果。       |

（2）MultiWheelView 说明：

| xml 属性            | 类型      | 方法                                | 功能                                                         |
| ------------------- | --------- | ----------------------------------- | ------------------------------------------------------------ |
| app:radicalNotify   | boolean   | 无                                  | 影响联动数据更新频率，如果为 true 则不论滚动与否，数据均实时更新，否则仅在滚动停止时更新。 |
| app:displayCount    | int       | getDisplayCount()/setDisplayCount() | 每个联动的 WheelView 同时显示的 item 数量，必须为大于 0 的奇数，否则会抛出异常或被修正为奇数，大小向 5 靠拢。 |
| app:wheelViewLayout | reference | 无                                  | 自定义 WheelView 布局，要求根布局为 WheelView，即不能嵌套或被嵌套于其它 View. |
| app:wheelViewCount  | int       | 无                                  | WheelView 的数量，即联动的级数。                             |

## 4. 使用效果

（1）年、月、日三级联动效果

![MultiWheelView_01](https://raw.githubusercontent.com/ccolorcat/Sample/master/multi_wheel_view_date.gif)

（2）省、市、县三级联动效果

![MultiWheelView_02](https://raw.githubusercontent.com/ccolorcat/Sample/master/multi_wheel_view_region.gif)

（3）设置选定项

![WheelView](https://raw.githubusercontent.com/ccolorcat/Sample/master/wheel_view.gif)

## 5. 使用方法

(1) 在项目的 build.gradle 中配置仓库地址：

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

(2) 添加项目依赖：

```groovy
dependencies {
    implementation 'com.github.ccolorcat:WheelView:v1.0.0'
}
```

## 5. 版本历史

v1.0.0

> 首次正式发布。