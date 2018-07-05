package com.speedata.uhf.libutils.excel;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;

/**
 * Created by carl_yang on 2017/5/23.
 * 使用示例
 * ExcelUtils.getInstance()
 * .setSHEET_NAME("测试Sheet")//设置表格名称
 * .setFONT_COLOR(Colour.BLUE)//设置标题字体颜色
 * .setFONT_TIMES(8)//设置标题字体大小
 * .setFONT_BOLD(true)//设置标题字体是否斜体
 * .setBACKGROND_COLOR(Colour.GRAY_25)//设置标题背景颜色
 * .setContent_list_Strings(list)//设置excel内容
 * .createExcel(MenuActivity.this);
 */

public class ExcelUtils<T> implements Serializable {

    private static ExcelUtils mInstance;
    String SHEET_NAME;//建立的表名
    //    String[] title_lsit;//标题栏的内容
    List<String> title_lsit = new ArrayList<>();
    List<String[]> CONTENT_LIST=new ArrayList<>();//主体内容
    String wirteExcelPath = Environment.getExternalStorageDirectory() + "/testExcel.xls";
    boolean FONT_BOLD = false;//设置标题字体是否为斜体，默认不是
    int FONT_TIMES = 10;//设置字体大小，默认为10
    jxl.format.Colour FONT_COLOR = Colour.BLACK;//设置标题字体颜色,默认黑色
    jxl.format.Colour BACKGROND_COLOR = Colour.WHITE;//设置标题背景颜色,默认白色
    jxl.format.Alignment FONT_ALIGNMENT = Alignment.CENTRE;//设置字体左右显示模式,默认左右居中
    jxl.format.VerticalAlignment FONT_VERTICAL = VerticalAlignment.CENTRE;//默认上下居中

    /**
     * 单实例
     * @return ExcelUtils
     */
    public static ExcelUtils getInstance() {
        if (mInstance == null) {
            synchronized (ExcelUtils.class) {
                if (mInstance == null) {
                    mInstance = new ExcelUtils();
                }
            }
        }
        return mInstance;
    }

    public List<String> getTitle_lsit() {
        return title_lsit;
    }

    public String getWirteExcelPath() {
        return wirteExcelPath;
    }

    public ExcelUtils setWirteExcelPath(String wirteExcelPath) {
        this.wirteExcelPath = wirteExcelPath;
        return this;
    }

    public String getSHEET_NAME() {
        return SHEET_NAME;
    }

    /**
     * 设置sheet 名称
     * @param SHEET_NAME
     * @return ExcelUtils
     */
    public ExcelUtils setSHEET_NAME(String SHEET_NAME) {
        this.SHEET_NAME = SHEET_NAME;
        return this;
    }

    /**
     * 标题字体是否斜体
     * @return boolean
     */
    public boolean isFONT_BOLD() {
        return FONT_BOLD;
    }

    /**
     * 设置标题字体是否斜体
     * @param FONT_BOLD
     * @return ExcelUtils
     */
    public ExcelUtils setFONT_BOLD(boolean FONT_BOLD) {
        this.FONT_BOLD = FONT_BOLD;
        return this;
    }


    public int getFONT_TIMES() {
        return FONT_TIMES;
    }

    /**
     * 设置标题字体大小
     * @return ExcelUtils
     */
    public ExcelUtils setFONT_TIMES(int FONT_TIMES) {
        this.FONT_TIMES = FONT_TIMES;
        return this;
    }

    public Colour getFONT_COLOR() {
        return FONT_COLOR;
    }

    public ExcelUtils setFONT_COLOR(Colour FONT_COLOR) {
        this.FONT_COLOR = FONT_COLOR;
        return this;
    }

    public Colour getBACKGROND_COLOR() {
        return BACKGROND_COLOR;
    }

    /**
     * 设置标题背景颜色
     * @param BACKGROND_COLOR
     * @return ExcelUtils
     */
    public ExcelUtils setBACKGROND_COLOR(Colour BACKGROND_COLOR) {
        this.BACKGROND_COLOR = BACKGROND_COLOR;
        return this;
    }

    public Alignment getFONT_ALIGNMENT() {
        return FONT_ALIGNMENT;
    }

    public ExcelUtils setFONT_ALIGNMENT(Alignment FONT_ALIGNMENT) {
        this.FONT_ALIGNMENT = FONT_ALIGNMENT;
        return this;
    }

    public VerticalAlignment getFONT_VERTICAL() {
        return FONT_VERTICAL;
    }

    public ExcelUtils setFONT_VERTICAL(VerticalAlignment FONT_VERTICAL) {
        this.FONT_VERTICAL = FONT_VERTICAL;
        return this;
    }

    public List<String[]> getCONTENT_LIST() {
        return CONTENT_LIST;
    }

    /**
     * 创建表格
     * @param ac
     * @return ExcelUtils
     */
    public ExcelUtils createExcel(Context ac) {
        new ExcelManager(ac, mInstance);
        return this;
    }

    /**
     * 清除缓存
     */
    public void clearData(){
        title_lsit.clear();
        CONTENT_LIST.clear();
    }

    /**
     * 设置excel内容
     * @param content_list_all
     * @return ExcelUtils
     */
    public ExcelUtils setContent_list_Strings(List<T> content_list_all) {
        clearData();
        Class c = content_list_all.get(0).getClass();
        for (int i = 0; i < content_list_all.size(); i++) {
            T current_content_object = content_list_all.get(i);
            Field[] fields = c.getDeclaredFields();
            List<String> single_obj=new ArrayList<String>();
            for (Field temp : fields) {
                boolean hasAnnotation = temp.isAnnotationPresent(Excel.class);
                if (hasAnnotation) {
                    Excel annotation = temp.getAnnotation(Excel.class);
                    System.out.println("-------Field = " + temp.getName()
                            + "  |  ignore = " + annotation.ignore() + "  |  name = "
                            + annotation.name());
                    String type = temp.getGenericType().toString();
                    System.out.println("-----------" + type);
                    String fieldName = temp.getName() + "";
                    if(!title_lsit.contains(annotation.name())) {
                        title_lsit.add(annotation.name());
                    }
                    String getMethodName;
                    if (type.equals("boolean")) {
                        getMethodName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    } else {
                        getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    }
                    try {
                        Method method = current_content_object.getClass().getMethod(getMethodName);
                        method.setAccessible(true);
                        if (type.equals("class java.lang.String")) {
                            String value = (String) method.invoke(current_content_object);
                            single_obj.add(value);
                        } else if (type.equals("int")) {
                            int value = (int) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        } else if (type.equals("double")) {
                            double value = (double) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        } else if (type.equals("long")) {
                            long value = (long) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        } else if (type.equals("boolean")) {
                            boolean value = (boolean) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        } else if (type.equals("float")) {
                            float value = (float) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        }else if (type.equals("class java.lang.Long")) {
                            long value = (long) method.invoke(current_content_object);
                            single_obj.add(value+"");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    System.out.println("-------Field no Annotation= " + temp.getName());
                }
            }
            String[] tt=new String[single_obj.size()];
            for (int i1 = 0; i1 < single_obj.size(); i1++) {
                tt[i1]=single_obj.get(i1);
            }
            CONTENT_LIST.add(tt);
        }
        return this;
    }

}
