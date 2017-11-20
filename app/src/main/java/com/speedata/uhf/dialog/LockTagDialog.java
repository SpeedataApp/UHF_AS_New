package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.uhf.R;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class LockTagDialog extends Dialog implements
        android.view.View.OnClickListener {

    private String[] area_list = {"Kill password", "Access password",
            "EPC", "TID", "USER"};
    private String[] style_list = {"Unlock", "Lock", "Permaunlock",
            "Permalock"};
    private Button Ok;
    private Button Cancle;
    private TextView EPC;
    private TextView Status;
    private Spinner area;
    private Spinner style;
    private EditText passwd;
    private ArrayAdapter<String> setadapter;
    private IUHFService iuhfService;
    private String current_tag_epc;
    private String model;
    private boolean isSuccess = false;

    public LockTagDialog(Context context, IUHFService iuhfService
            , String current_tag_epc, String model) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.current_tag_epc = current_tag_epc;
        this.model = model;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locktag);

        Ok = (Button) findViewById(R.id.btn_lock_ok);
        Ok.setOnClickListener(this);
        Cancle = (Button) findViewById(R.id.btn_lock_cancle);
        Cancle.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_lock_epc);
        EPC.setText(current_tag_epc);
        Status = (TextView) findViewById(R.id.textView_lock_status);

        passwd = (EditText) findViewById(R.id.editText_lock_passwd);

        setadapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_item, area_list);
        setadapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area = (Spinner) findViewById(R.id.spinner_lock_area);
        area.setAdapter(setadapter);

        setadapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_item, style_list);
        setadapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        style = (Spinner) findViewById(R.id.spinner_lock_style);
        style.setAdapter(setadapter);
        iuhfService.setOnWriteListener(new OnSpdWriteListener() {
            @Override
            public void getWriteData(SpdWriteData var1) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] epcData = var1.getEPCData();
                String hexString = StringUtils.byteToHexString(epcData, var1.getEPCLen());
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：" + hexString + "\n");
                }
                if (var1.getStatus() == 0) {
                    //状态判断，已经写卡成功了就不返回错误码了
                    isSuccess = true;
                    stringBuilder.append("WriteSuccess" + "\n");
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder));
                } else {
                    stringBuilder.append("WriteError：" + var1.getStatus() + "\n");
                }
                if (!isSuccess) {
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder));
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == Ok) {
            final int area_nr = area.getSelectedItemPosition();
            final int style_nr = style.getSelectedItemPosition();
            final String ps = passwd.getText().toString();

            Status.setText("正在锁卡中....");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int reval = iuhfService.newSetLock(style_nr, area_nr, ps);
                    if (reval != 0) {
                        handler.sendMessage(handler.obtainMessage(1,"参数不正确"));
                    }
                }
            }).start();
        } else if (v == Cancle) {
            dismiss();
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Status.setText(msg.obj + "");
            }
        }
    };
}
