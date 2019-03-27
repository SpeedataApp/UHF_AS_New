# UHF_AS_New
新版R2000库使用，读写盘点数据通过回调返回

##  导入依赖库
**AndroidStudio** build.gradle中的dependencies中添加

```
//最外层build.gradle
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
```
 dependencies {
    compile 'com.github.SpeedataG:UHF:7.7.7'
  }
```
## 低电使用说明
* 芯联模块
  * 电池电量低于20% 禁止使用
* 非芯联模块，非SD60 SC60型号
  * 电池电量低于20% 禁止使用
  * 开始盘点前电压低于3.75v 禁止使用
* 非芯联模块，SD60／SC60型号
  * 电池电量低于20% 禁止使用
  * 开始盘点前电压低于3.75v 禁止使用
  * 盘点过程中电压低于3.35v禁止使用
  
## API文档

	详细的接口说明在showdoc，地址：http://www.showdoc.cc/web/#/79868361520440?page_id=452063154391852

北京思必拓科技股份有限公司

网址 http://www.speedata.cn/

技术支持 电话：155 4266 8023

QQ：2480737278
