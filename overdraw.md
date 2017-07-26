# 过度绘制分析及解决方案 #
## 过度绘制 ##
### 绘制原理 ###
Android系统要求每一帧都要在 16ms 内绘制完成，平滑的完成一帧意味着任何特殊的帧需要执行所有的渲染代码（包括 framework 发送给 GPU 和CPU 绘制到缓冲区的命令）都要在 16ms 内完成，保持流畅的体验。这个速度允许系统在动画和输入事件的过程中以约 60 帧每秒（ 1秒 / 0.016帧每秒 = 62.5帧/秒 ）的平滑帧率来渲染。
![](http://i.imgur.com/Z7lxjfq.jpg)
如果应用没有在 16ms 内完成这一帧的绘制，假设你花了 24ms 来绘制这一帧，那么就会出现掉帧的情况。
![](http://i.imgur.com/fYtAA9y.jpg)
系统准备将新的一帧绘制到屏幕上，但是这一帧并没有准备好，所有就不会有绘制操作，画面也就不会刷新。反馈到用户身上，就是用户盯着同一张
图看了 32ms 而不是 16ms ，也就是说掉帧发生了。

### 掉帧 ###
掉帧是用户体验中一个非常核心的问题。丢弃了当前帧，并且之后不能够延续之前的帧率，这种不连续的间隔会容易会引起用户的注意，也就是我们
常说的卡顿、不流畅。
掉帧的原因很多，比如：
* ViewTree非常庞大，花了很多时间重新绘制界面中的控件，这样非常浪费CPU周期：
![](http://i.imgur.com/iFw1a5x.png)
* 过度绘制严重，在绘制用户看不到的对象上花费了太多的时间：
![](http://i.imgur.com/d8YElCA.jpg)
* 大量动画多次重复，消耗CPU和GPU
* 频繁地触发GC机制
目前我们的项目的 APP 卡顿现象主要是由于 ViewTree 过于庞大和过度绘制严重造成
## UI绘制机制 ##
在现在的设备上，UI绘制主要由CPU和GPU协作完成，其工作原理如下图：
![](http://i.imgur.com/TgNko9n.jpg)

其实这图我也看得不太懂，但知道那么两个解决问题的方法：
* 利用 Android Studio 自带的 Hierarchy Viewer 去检测各 View 层的绘制时间，删除或合并图层
* 打开手机的 ShowGPUOverdraw去检测Overdraw，移除不必要的background

## Hierarchy Viewer 的使用 ##
### Hierarchy Viewer ###
Hierarchy Viewer工具在Android device monitor中
在Mac的Android Studio中：

![](http://i.imgur.com/lFy2Dix.png)

> 图片来源http://blog.csdn.net/lmj623565791/article/details/45556391/

在windows的Android Studio中：

![](http://i.imgur.com/ytLv69Q.png)

那么如何使用呢？

![](http://i.imgur.com/IHlpqOq.jpg)

> 图片来源http://blog.csdn.net/lmj623565791/article/details/45556391/

### 简单使用 ###
打开ViewTree视图后，点击任意一个view，然后点击Profile Node即可展示每个view在各个阶段的耗时情况，如：
![](http://i.imgur.com/2qK4AXY.png)
图中可以看到，该view在Measure、Layout和Draw阶段都比其它view耗时要多（下面的点变成红色了），图中看出该view节点后有72个子view，还可以读出数据：

阶段|耗时
---|---
Measure |0.028ms
Layout |0.434ms
Draw |9.312ms

在ViewTree中查找可删减或合并的view，找到耗时严重的view加以改良，可以减轻过度绘制现象，例如：

![](http://i.imgur.com/XtYehYD.png)

例如图中的两个LinearLayout只要保留一个就够了，而前面这个RecyclerView的item只有一个，没必要使用RecyclerView，可以考虑用其它view来替代

## 用Show GPU Overdraw方法来检测 ##
### 过度绘制的检测 ###
按照以下步骤打开ShowGPUOverrdraw的选项：
设置 -> 开发者选项 -> 调试GPU过度绘制 -> 显示GPU过度绘制

![](http://i.imgur.com/eB1AUCQ.png)

打开后，屏幕会有多种颜色，切换到需要检测的应用程序，对于各个色块，有一张参考图：

![](http://i.imgur.com/gUFmW0y.png)

其中蓝色部分表示1层过度绘制，红色表示4层过度绘制。

## 解决方案 ##
### 移除不必要的background ###

下面举个简单的例子：

* activity_main 布局文件：
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:card_view="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    android:orientation="vertical"
    tools:context="com.example.erkang.overdraw.MainActivity">

    <android.support.v7.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        card_view:cardBackgroundColor="@color/white">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/white">

            <TextView
                android:id="@+id/title_tv"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center"
                android:paddingBottom="5dp"
                android:paddingTop="5dp"
                android:text="OverDraw展示样式" />

            <ImageView
                android:layout_width="wrap_content"
                android:layout_height="100dp"
                android:layout_below="@+id/title_tv"
                android:layout_marginBottom="5dp"
                android:scaleType="fitCenter"
                android:src="@drawable/infernal_affairs_0" />
        </RelativeLayout>
    </android.support.v7.widget.CardView>

    <View
        android:layout_width="match_parent"
        android:layout_height="20dp" />

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/gray" />
</LinearLayout>
```
* RecyclerView的item的布局文件：
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center">
    <ImageView
        android:layout_marginLeft="10dp"
        android:id="@+id/item_iv"
        android:layout_width="100dp"
        android:layout_height="100dp"
        tools:src="@drawable/infernal_affairs_1" />
    <TextView
        android:layout_marginLeft="10dp"
        android:id="@+id/item_tv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="对唔住，我喺差人。" />
</LinearLayout>
```
* Activity代码：
```
public class MainActivity extends AppCompatActivity {
    private MyAdapter myAdapter;
    private RecyclerView recyclerView;
    private static final int ITEM_COUNT = 20;
    private static final int ITEM_DISTANCE = 40;
    private LinearLayoutManager layoutManager;
    private MyItemDecoration myItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        myAdapter = new MyAdapter(MainActivity.this, ITEM_COUNT);
        layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        myItemDecoration = new MyItemDecoration(ITEM_DISTANCE);
        recyclerView.addItemDecoration(myItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);
    }
}
```
* ItemDecoration代码:
```
public class MyItemDecoration extends RecyclerView.ItemDecoration{
    protected int halfSpace;

    /**
     * @param space item之间的间隙
     */
    public MyItemDecoration(int space){
        setSpace(space);
    }
    public void setSpace(int space) {
        this.halfSpace = space / 2;
    }
  
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = halfSpace;
        outRect.bottom = halfSpace;
        outRect.left = halfSpace;
        outRect.right = halfSpace;
    }
}
```

现在看起来的效果是这样的：
![](http://i.imgur.com/UVIJDgI.png)

图中，我们需要上方展示的部分背景为白色，而下方列表Item之间的颜色为灰色，item的背景为白色。
打开显示过度绘制功能后是这样的：
![](http://i.imgur.com/otr083i.png)

图中可以看到很多区域出现了三重或四重的过度绘制现象。那么我们开始去掉不必要的background。
* 不必要的background 1：
>总布局LinearLayout中的 android:background="@color/white" 可以去掉；
* 不必要的background 2：
> 上方布局RelativeLayout中的android:background="@color/white"可以去掉;

去掉这两个background后，我们重新安装一下应用程序，发现界面上方过度绘制现象明显改善：
![](http://i.imgur.com/jWAaJ8w.png)

但界面下方仍存在过度绘制现象，若把RecyclerView中的background值的灰色去掉，则下方列表Item之间的就会变成白色，显然不是我们想要的效
果：
![](http://i.imgur.com/ua1buk4.png)

于是，我们需要对RecyclerView的ItemDecoration类进行改造，改造成如下：
```
public class MyItemDecoration extends RecyclerView.ItemDecoration{
    protected int halfSpace;
    private Paint paint;
    /**
     * @param space item之间的间隙
     */
    public MyItemDecoration(int space, Context context) {
        setSpace(space);
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿
        paint.setColor(context.getResources().getColor(R.color.gray));//设置背景色
    }
    public void setSpace(int space) {
        this.halfSpace = space / 2;
    }
    /**
     *
     * 重写onDraw 方法以实现recyclerview的item之间的间隙的背景
     * @param c 画布
     * @param parent 使用该 ItemDecoration 的 RecyclerView 对象实例
     * @param state 使用该 ItemDecoration 的 RecyclerView 对象实例的状态
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int outLeft, outTop, outRight, outBottom,viewLeft,viewTop,viewRight,viewBottom;
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            viewLeft = view.getLeft();
            viewTop = view.getTop();
            viewRight = view.getRight();
            viewBottom = view.getBottom();
// item外层的rect在RecyclerView中的坐标
            outLeft = viewLeft - halfSpace;
            outTop = viewTop - halfSpace;
            outRight = viewRight + halfSpace;
            outBottom = viewBottom + halfSpace;
//item 上方的矩形
            c.drawRect(outLeft, outTop, outRight,viewTop, paint);
//item 左边的矩形
            c.drawRect(outLeft,viewTop,viewLeft,viewBottom,paint);
//item 右边的矩形
            c.drawRect(viewRight,viewTop,outRight,viewBottom,paint);
//item 下方的矩形
            c.drawRect(outLeft,viewBottom,outRight,outBottom,paint);
        }
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = halfSpace;
        outRect.bottom = halfSpace;
        outRect.left = halfSpace;
        outRect.right = halfSpace;
    }
}
```
其实就是增加了onDraw方法，在item的间隙画上了带背景色的矩形，于是我们想要的效果又回来了：

![](http://i.imgur.com/NMyDoNi.png)

这时打开“显示过度绘制”功能：
![](http://i.imgur.com/OuzLKTx.png)

已经是可以接受的效果了。

## 总结 ##
解决过度绘制现象，可以从这两个方法入手：
* 利用 Hierarchy Viewer 观察整个界面的ViewTree，删掉无用的图层，找到能合并的view合并，找到红点图层分析原因；
* 查看各图层的background，去掉不必要的background；
* 对于列表中Item之间的间隙颜色，不要在列表的 background 设置，应该在列表应用的 ItemDecoration 中设置

## 后记 ##
本文已上传至GitHub [https://github.com/EKwongChum/OverDraw](https://github.com/EKwongChum/OverDraw "OverDraw")
欢迎指出问题，谢谢。
