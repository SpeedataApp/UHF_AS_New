/*
 *
 * @author yandeqing
 * @created 2016.6.5
 * @email 18612205027@163.com
 * @version v1.0
 *
 */

package com.speedata.uhf.libutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * 本地SharedPreferences数据保存读取工具类
 *
 * @author Jason
 */
public class SharedXmlUtil {
    public static SharedXmlUtil mSharedXmlUtil;
    static byte[] lock = new byte[0];
    private SharedPreferences mShared;
    private Editor mEditor;

    private SharedXmlUtil(Context context, String filename) {
        mShared = context.getSharedPreferences(filename,
                Context.MODE_MULTI_PROCESS);
        mEditor = mShared.edit();
    }

//    private SharedXmlUtil(Context context) {
//        mShared = context.getSharedPreferences(
//                context.getString(R.string.app_name),
//                Context.MODE_MULTI_PROCESS);
//        mEditor = mShared.edit();
//    }
//
//    public static SharedXmlUtil getInstance(Context context) {
//        synchronized (lock) {
//            if (mSharedXmlUtil == null) {
//                mSharedXmlUtil = new SharedXmlUtil(context);
//            }
//            return mSharedXmlUtil;
//        }
//    }

    /**
     * 获取实例
     * @param context 上下文
     * @param filename 文件名称
     * @return SharedXmlUtil对象
     */
    public static SharedXmlUtil getInstance(Context context, String filename) {
        synchronized (lock) {
            if (mSharedXmlUtil == null) {
                mSharedXmlUtil = new SharedXmlUtil(context, filename);
            }
            return mSharedXmlUtil;
        }
    }

    /**
     * 写
     * @param key key
     * @param value String值
     */
    public void write(String key, String value) {
        synchronized (lock) {
            mEditor.putString(key, value);
            mEditor.commit();
            mEditor.apply();
        }
    }

    /**
     * 写
     * @param key key
     * @param value boolean值
     */
    public void write(String key, boolean value) {
        synchronized (lock) {
            mEditor.putBoolean(key, value);
            mEditor.commit();
            mEditor.apply();
        }
    }

    /**
     * 写
     * @param key
     * @param value int值
     */
    public void write(String key, int value) {
        synchronized (lock) {
            mEditor.putInt(key, value);
            mEditor.commit();
            mEditor.apply();
        }
    }

    /**
     * 写
     * @param key
     * @param value float值
     */
    public void write(String key, float value) {
        synchronized (lock) {
            mEditor.putFloat(key, value);
            mEditor.commit();
            mEditor.apply();
        }
    }

    /**
     * 写
     * @param key
     * @param value long值
     */
    public void write(String key, long value) {
        synchronized (lock) {
            mEditor.putLong(key, value);
            mEditor.commit();
            mEditor.apply();
        }
    }

    /**
     * 读
     * @param key
     * @param defValue  String值
     * @return String
     */
    public String read(String key, String defValue) {
//        synchronized (lock) {
            String string = defValue;
            try {
                string = mShared.getString(key, defValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return string;
//        }
    }

    /**
     * 读
     * @param key
     * @param defValue  boolean 值
     * @return boolean
     */
    public boolean read(String key, boolean defValue) {
        synchronized (lock) {
            return mShared.getBoolean(key, defValue);
        }
    }

    /**
     * 读
     * @param key
     * @param defValue int值
     * @return int
     */
    public int read(String key, int defValue) {
        synchronized (lock) {
            return mShared.getInt(key, defValue);
        }
    }
    /**
     * 读
     * @param key
     * @param defValue float值
     * @return float
     */
    public float read(String key, float defValue) {
        synchronized (lock) {
            return mShared.getFloat(key, defValue);
        }
    }

    /**
     * 读
     * @param key
     * @param defValue long值
     * @return long
     */
    public long read(String key, long defValue) {
        synchronized (lock) {
            return mShared.getLong(key, defValue);
        }
    }

    /**
     * 删除
     * @param key key
     */
    public void delete(String key) {
        synchronized (lock) {
            mEditor.remove(key);
            mEditor.commit();
            mEditor.apply();
        }
    }
}
