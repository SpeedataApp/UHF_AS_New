package com.speedata.uhf.libutils;

import android.content.Context;

import com.alibaba.fastjson.JSON;

/**
 * Created by brxu on 2017/3/15.
 * 除了uhf  配置文件不存在的话还需要单独判断是哪个模块
 * 其他模块  配置文件不存在  就直接返回标准表格里的参数
 * 模块配置工具类
 */

public class ConfigUtils {
//    public static String RELEASE_4_4_2 = "4.4.2";
//    public static String RELEASE_5_1 = "5.1";

    /**
     * 判断配置文件是否存在
     *
     * @return  boolean
     */
    public static boolean isConfigFileExists() {
        return FileUtils.fileExists();
    }

    public static ReadBean readConfig(Context mContext) {
        ReadBean mRead;
        if (!CommonUtils.isExists()) {
            mRead = JSON.parseObject(CommonUtils.getFromAssets(mContext), ReadBean.class);
        } else {
            mRead = JSON.parseObject(CommonUtils.readTxtFile(), ReadBean.class);
        }
        return mRead;
    }
}
