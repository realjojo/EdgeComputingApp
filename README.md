# EdgeComputingApp
---
## 遇到的问题
### 吴姿颍
**1. 建立webSocket长链接一直失败，connect不上**  
**问题原因：** Android模拟器默认的本机ip不是localhost或127.0.0.1，而是10.0.2.2，但是这个ip是Android原生模拟器所默认的本机ip地址，对于genymotion模拟器，所对应的本机ip为10.0.3.2（巨坑。。。。。。。。。坑了我整整一个小时。。。。。）  
**解决方式：** 将webSocket链接地址改为ws://10.0.3.2:8089/ws/webSocket即可

**2. WebSocket参考博客：** [https://blog.csdn.net/beita08/article/details/80162070](https://blog.csdn.net/beita08/article/details/80162070)  

**3. 获取NavigationView里面headerLayout布局的部件出错**  
**问题原因：** activity_main.xml布局文件里引用了NavigationView，因此我直接在Activity中用findViewById的方式获取控件，此时获取到的为null，出错  
**解决方式：** 先获取到navigationView，然后通过navigationView.getHeaderView(0)来获取headView，接着通过headview.findViewById(R.id.xxx)便可获取相应控件

**4. onBackPressed()系统返回键监听**
**问题原因：** 在Activity1中监听系统返回键，当点击返回键回退到主活动界面时，会调用主活动的onActivityResult()方法，且Activity1默认返回的resultCode的值始终为0
**解决方式：** onBackPressed()方法中的super.onBackPressed()是执行系统默认的操作，就是退出当前Activity，所以当我们重写这个方法时，不加super.onBackPressed()，就可以不退出Activity，执行自己的代码
