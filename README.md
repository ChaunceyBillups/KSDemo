# KSDemo
#工程架构说明
工程使用AS3.5版本开发，JDK使用1.8版本

1、app工程是demo的主工程

2、multi-image-selector是图片选择的辅助module，不用特殊关注


#demo工程使用说明
1、ak和sk的设置，在WelcomeActivity中的成员变量分别填入ak和sk（使用正式的ak和sk）

private static final String ak = "";

private static final String sk = "";

2、SDK初始化方法是WelcomeActivity的initSDK方法，可以修改人脸识别分数，活体检测等各种属性

3、在刷脸界面bottom部分连续点击三次进入调试模式，可以添加人员和删除人员


#相关类说明
1、WelcomeActivity主要负责权限申请和SDK的初始化

2、MainActivity主要是从摄像头获取Frame数据进行人脸识别逻辑

3、DebugActivity是调试模式的主界面

4、AddEmployeeActivity是添加人员的逻辑

5、EmployeeListActivity是人员展示和删除的界面

6、摄像头相关的操作封装在camera包中

7、人员的数据库管理封装在db包中


#附件文档说明
1、FacePass Android 创建应用及获取授权信息步骤
	
	了解应用的创建过程以及获取相应的APPkey和秘钥信息。

2、FacePass Android版SDK接入流程说明

	了解旷世sdk接入的流程。

3、FacePass Android版开发API使用相关说明
	
	针对旷世sdk相关API调用说明，了解API的使用，可自定义开发。