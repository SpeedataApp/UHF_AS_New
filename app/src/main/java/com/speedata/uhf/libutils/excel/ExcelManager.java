package com.speedata.uhf.libutils.excel;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;
;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by echo on 2017/6/6.
 * Excel管理类
 */

public class ExcelManager {

    private Context context;
    private ExcelUtils excelUtil;


    public ExcelManager(Context ct, ExcelUtils ex) {
        this.context = ct;
        this.excelUtil = ex;
        try {
            //不是主线程
            if(Looper.getMainLooper().getThread() != Thread.currentThread()) {
                System.out.print("---------------非主线程 执行Looper.prepare---------------");
                Looper.prepare();
            }
            writeExcel();
        } catch (Exception e) {
            Toast.makeText(context, "生成Excel发生异常", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 写excel
     * @throws Exception
     */
    public void writeExcel() throws Exception {
        File file;
        file = new File(excelUtil.getWirteExcelPath());
        // 创建Excel工作表
        WritableWorkbook wwb;
        OutputStream os = new FileOutputStream(file);
        wwb = Workbook.createWorkbook(os);
        // 添加第一个工作表并设置第一个Sheet的名字
        WritableSheet sheet = wwb.createSheet(excelUtil.getSHEET_NAME(), 0);
        Label label;
        //填充标题栏
        for (int i = 0; i < excelUtil.getTitle_lsit().size(); i++) {
            // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
            // 在Label对象的子对象中指明单元格的位置和内容
            label = new Label(i, 0, excelUtil.getTitle_lsit().get(i).toString(), getHeader());
            // 将定义好的单元格添加到工作表中
            sheet.addCell(label);
        }

        //向每行填充内容
        for (int i = 0; i < excelUtil.getCONTENT_LIST().size(); i++) {
            String[] content_strs = (String[]) excelUtil.getCONTENT_LIST().get(i);

            for (int m = 0; m < content_strs.length; m++) {
                Label str_label = new Label(m, i + 1, content_strs[m]);
                sheet.addCell(str_label);
            }
            Toast.makeText(context, "写入成功", Toast.LENGTH_LONG).show();

        }
        // 写入数据
        wwb.write();
        // 关闭文件
        wwb.close();
    }

    /**
     * 获取标题格式
     * @return WritableCellFormat
     */
    public WritableCellFormat getHeader() {
        WritableFont font = new WritableFont(WritableFont.TIMES, excelUtil.getFONT_TIMES(),
                WritableFont.BOLD, excelUtil.isFONT_BOLD());
        try {
            font.setColour(excelUtil.getFONT_COLOR());
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(excelUtil.getFONT_ALIGNMENT());// 左右居中
            format.setVerticalAlignment(excelUtil.getFONT_VERTICAL());// 上下居中
            format.setBackground(excelUtil.getBACKGROND_COLOR());
            format.setBorder(Border.ALL, BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }
}
