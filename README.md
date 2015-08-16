![Logo](https://raw.githubusercontent.com/FinalLody/WelikeAndroid/master/Welike_Android.png)

![license](https://img.shields.io/crates/l/rustc-serialize.svg)
![BuildStatus](https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/bt345.svg)
![Convert](https://img.shields.io/codecov/c/github/codecov/example-python.svg)

##WelikeAndroid 是什么?
WelikeAndroid 是一款引入即用的便捷开发框架,致力于为程序员打造最佳的编程体验,<br>使用WelikeAndroid, 你会觉得写代码是一件很轻松的事情.

##欢迎加入我们的大家庭:
###[142853711](http://jq.qq.com/?_wv=1027&k=cxU1Or)(WelikeAndroid交流群)
<br>
##Welike带来了哪些特征？

WelikeAndroid目前包含五个大模块:

* <b>异常安全隔离模块</b>(实验阶段):当任何线程抛出任何异常,我们的异常隔离机制都会让UI线程继续运行下去.
* <b>Http模块</b>: 一行代码完成POST、GET请求和Download,支持上传, 高度优化Disk的缓存加载机制,<br> 自由设置缓存大小、缓存时间(也支持永久缓存和不缓存).
* <b>Bitmap模块</b>: 一行代码完成异步显示图片,无需考虑OOM问题,支持加载前对图片做自定义处理.
* <b>Database模块</b>: 支持NotNull,Table,ID,Ignore等注解,Bean无需Getter和Setter,一键式部署数据库.
* <b>ui操纵模块</b>: 我们为Activity基类做了完善的封装,继承基类可以让代码更加优雅.
* `另`:请不要认为功能相似,框架就不是原创,源码摆在眼前,何不看一看?

## 使用WelikeAndroid需要以下权限：

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
```

##下文将教你如何圆润的使用WelikeAndroid:<br>
##WelikeGuard(异常安全隔离机制用法):
* 第一步,开启异常隔离机制:

```java
WelikeGuard.enableGuard();
```
* 第二步,注册一个全局异常监听器:

```java

WelikeGuard.registerUnCaughtHandler(new Thread.UncaughtExceptionHandler() {
@Override
public void uncaughtException(Thread thread, Throwable ex) {

 WelikeGuard.newThreadToast("出现异常了: " + ex.getMessage() );

}
});
```
* 你也可以<b>自定义异常</b>:

```java

/**
*
* 自定义的异常,当异常被抛出后,会自动回调onCatchThrowable函数.
*/
@Catch(process = "onCatchThrowable")
public class CustomException extends IllegalAccessError {

   public static void onCatchThrowable(Thread t){
    WeLog.e(t.getName() + "  抛出了一个异常...");
   }
}

```
* 另外,继承自`UncaughtThrowable`的异常我们不会对其进行拦截.


## WeikeHttp入门:
 首先来看看框架的调试信息,是不是一目了然.
![DEBUG](https://raw.githubusercontent.com/FinalLody/WelikeAndroid/master/screenshot/log.png)
![DEBUG2](https://raw.githubusercontent.com/FinalLody/WelikeAndroid/master/screenshot/log2.png)


* 第一步,取得WelikeHttp默认实例.

```java
WelikeHttp welikeHttp = WelikeHttp.getDefault();
```
* 第二步,发送一个Get请求.

```java
HttpParams params = new HttpParams();
params.putParams("app","qr.get",
                 "data","Test");//一次性放入两对 参数 和 值

//发送Get请求
HttpRequest request = welikeHttp.get("http://api.k780.com:88", params, new HttpResultCallback() {
    @Override
    public void onSuccess(String content) {
    super.onSuccess(content);
    WelikeToast.toast("返回的JSON为:" + content);
    }

    @Override
    public void onFailure(HttpResponse response) {
    super.onFailure(response);
    WelikeToast.toast("JSON请求发送失败.");
    }

    @Override
    public void onCancel(HttpRequest request) {
    super.onCancel(request);
    WelikeToast.toast("请求被取消.");
    }
});

    //取消请求,会回调onCancel()
    request.cancel();

```
#### 当然,我们为满足需求提供了多种扩展的Callback,目前我们提供以下Callback供您选择:
* HttpCallback(响应为<b>byte[]数组</b>)
* FileUploadCallback(仅在<b>上传文件时</b>使用)
* HttpBitmapCallback(<b>建议使用Bitmap模块</b>)
* HttpResultCallback(响应为<b>String</b>)
* DownloadCallback(仅在<b>download</b>时使用)

#### 如需自定义Http模块的配置(如缓存时间),请查看HttpConfig.

## WelikeBitmap入门:
* 第一步,取得默认的WelikeBitmap实例:

```java

//取得默认的WelikeBitmap实例
WelikeBitmap welikeBitmap = WelikeBitmap.getDefault();
```
* 第二步,异步加载一张图片:

```java
BitmapRequest request = welikeBitmap.loadBitmap(imageView,
"http://img0.imgtn.bdimg.com/it/u=937075122,1381619862&fm=21&gp=0.jpg",
android.R.drawable.btn_star,//加载中显示的图片
android.R.drawable.ic_delete,//加载失败时显示的图片
new BitmapCallback() {

    @Override
    public Bitmap onProcessBitmap(byte[] data) {
    //如果需要在加载时处理图片,可以在这里处理,
    //如果不需要处理,就返回null或者不复写这个方法.
    return null;
    }

    @Override
    public void onPreStart(String url) {
    super.onPreStart(url);
    //加载前回调
    WeLog.d("===========> onPreStart()");
    }

    @Override
    public void onCancel(String url) {
    super.onCancel(url);
    //请求取消时回调
    WeLog.d("===========> onCancel()");
    }

    @Override
    public void onLoadSuccess(String url, Bitmap bitmap) {
    super.onLoadSuccess(url, bitmap);
    //图片加载成功后回调
    WeLog.d("===========> onLoadSuccess()");
    }

    @Override
    public void onRequestHttp(HttpRequest request) {
    super.onRequestHttp(request);
    //图片需要请求http时回调
    WeLog.d("===========> onRequestHttp()");
    }

    @Override
    public void onLoadFailed(HttpResponse response, String url) {
    super.onLoadFailed(response, url);
    //请求失败时回调
    WeLog.d("===========> onLoadFailed()");
    }
});
```
* 如果需要自定义Config,请看BitmapConfig这个类.

##WelikeDAO入门：
* 首先写一个Bean.

```java

/*表名,可有可无,默认为类名.*/
@Table(name="USER")
public class User{
@ID
public int id;//id可有可无,根据自己是否需要来加.

/*这个注解表示name字段不能为null*/
@NotNull
public String name;

}
```
* 然后将它写入到数据库

```java
WelikeDao db = WelikeDao.instance("Welike.db");
User user = new User();
user.name = "Lody";
db.save(user);
```
* 从数据库取出Bean

```java

User savedUser = db.findBeanByID(1);

```
* 更新指定ID的Bean

```java
User wantoUpdateUser = new User();
wantoUpdateUser.name = "NiHao";
db.updateDbByID(wantoUpdateUser);
```

* 删除指ID定的Bean

```java
db.deleteBeanByID(1);
```

* 更多实例请看DEMO和API文档.

##十秒钟学会WelikeActivity
* 我们将Activity的生命周期划分如下:
```java

=>@initData(所有标有InitData注解的方法都最早在子线程被调用)
=>initGlobalView()
=>@JoinView(将标有此注解的View自动findViewByID和setOnClickListener)
=>onDataLoaded(数据加载完成时回调)
=>点击事件会回调onWidgetClick(View Widget)
```

###关于@JoinView的细节:
* 有以下三种写法:

```java
@JoinView(name = "welike_btn")
Button welikeBtn;
```

```java
@JoinView(id = R.id.welike_btn)
Button welikeBtn;
```
```java
@JoinView(name = "welike_btn",click = false)
Button welikeBtn;
```
* `click`为`true`时会自动调用view的setOnClickListener方法,并在`onWidgetClick`回调.
* 当需要绑定的是一个`Button`的时候, `click属性`默认为`true`,其它的View则默认为`false`.

##有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* QQ: `382816028`
* Email:`382816028@qq.com`
* 来`142853711`群与大家一起讨论


##关于作者

```javascript
var Me = {
nickName  : "Lody"
}
```

`Git`: https://github.com/FinalLody/WelikeAndroid<br>
`Git@OSC`: http://git.oschina.net/lody/WelikeAndroid
