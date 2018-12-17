package com.speedata.uhf.excel;


import com.speedata.uhf.libutils.excel.Excel;

/**
 * Created by 张明_ on 2018/5/2.
 * Email 741183142@qq.com
 */
public class EPCBean {

    @Excel(ignore = false, name = "EPC")
    String EPC;

    @Excel(ignore = true, name = "TID_USER")
    String TID_USER;


    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getTID_USER() {
        return TID_USER;
    }

    public void setTID_USER(String TID_USER) {
        this.TID_USER = TID_USER;
    }
}
