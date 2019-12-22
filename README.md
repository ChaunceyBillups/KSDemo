# KSDemo MegSafe软授权说明

#工程架构说明
工程使用AS3.5版本开发，JDK使用1.8版本
1、app工程是demo的主工程
2、multi-image-selector是图片选择的辅助module，不用特殊关注


#demo工程使用说明
1、AuthActivity主要做以下事情

	1.1、generate_fingerprint方法获取设备指纹文件request.c2v；

    1.2、在旷世平台上面下载通讯文件，放在SD卡跟目录下的Download文件夹下面
![](/second.png)

	1.3、修改代码里对应通信文件的名称；
![](/three.png)

	1.4、激活成功后将进入WelcomeActivity；
	

2、SDK初始化方法是WelcomeActivity的initSDK方法，可以修改人脸识别分数，活体检测等各种属性

3、在刷脸界面bottom部分连续点击三次进入调试模式，可以添加人员和删除人员


#相关类说明
	1、AuthActivity主要负责权限申请和软授权

	2、WelcomeActivity主要负责SDK的初始化

	3、MainActivity主要是从摄像头获取Frame数据进行人脸识别逻辑

	4、AddEmployeeActivity是添加人员的逻辑

	5、EmployeeListActivity是人员展示和删除的界面

	6、DebugActivity是调试模式的主界面

	7、摄像头相关的操作封装在camera包中

	8、人员的数据库管理封装在db包中

#3.4.2更新功能
![](/update.png)

#附件文档说明

1、注册账号登录加密授权平台获取认证文件步骤

	了解应用的创建过程以及获取相应的APPkey和秘钥信息。

2、MegSafe软授权方式流程说明

	了解MegSafe软授权的操作流程

3、FacePass Android版开发API使用相关说明3.3

	针对旷世sdk相关API调用说明，了解API的使用，可自定义开发。