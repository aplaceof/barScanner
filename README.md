## 说明：

这个项目是在<https://github.com/bertsir/zBarLibary>的基础上做的二次开发， 感谢github用户bertsir的工作。

所以如果想继续改的话， 首先看一下他的github。

## 我主要做了以下修改

1. 在扫描到的二维码/条形码之后，调用相机进行拍照，生成图片。
2. 在图片左上角添加扫描结果以及扫描时间信息，并将保存到手机本地文件中。   

## 具体的：

1. 在QRActivity类中直接获得了camera 对象， 通过在CameraPreview类 以及 CameraManager类中定义的getCamera函数， 获得底层的camera对象。用于拍照操作。
2. 在QRActivity类中定义了 :
   1. 拍照函数 takePicture
   2. 添加文字信息函数  AddTimeWatermark 
3. 在QRActivity类 ScanCallback回调函数中调用takePicture函数， takePicture拍照后，调用AddTimeWatermark添加扫描结果以及扫描时间信息，保存到本地文件中。

## 效果：

## ![image-20200608080449651](xiaoguo.jpg)

### 未来想法：

1. 开一个新的线程，对拍摄的照片进行异步存储
2. 在回调函数ScanCallback中， 添加http请求， 请求电脑拍照，实现快递出库设备的功能，一张拍快递， 一张拍人脸。













 

 
