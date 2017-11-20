package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.uhf.R;

/**
 * Created by 张明_ on 2016/12/27.
 */

public class WriteTagDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Button Ok;
    private Button Cancle;
    private TextView EPC;
    private TextView Status;
    private EditText Write_Addr;
    private EditText Write_Count;
    private EditText Write_Passwd;
    private IUHFService iuhfService;
    private Context mContext;
    private int which_choose;
    private String current_tag_epc;
    private String model;
    private EditText Write_Content;
    private boolean isSuccess = false;

    public WriteTagDialog(Context context, IUHFService iuhfService,
                          int which_choose, String current_tag_epc, String model) {
        super(context);
        this.iuhfService = iuhfService;
        this.mContext = context;
        this.which_choose = which_choose;
        this.current_tag_epc = current_tag_epc;
        this.model = model;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write);

        Ok = (Button) findViewById(R.id.btn_write_ok);
        Ok.setOnClickListener(this);
        Cancle = (Button) findViewById(R.id.btn_write_cancle);
        Cancle.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_write_epc);
        EPC.setText(current_tag_epc);
        Status = (TextView) findViewById(R.id.textView_write_status);

        Write_Addr = (EditText) findViewById(R.id.editText_write_addr);
        Write_Count = (EditText) findViewById(R.id.editText_write_count);
        Write_Passwd = (EditText) findViewById(R.id.editText_write_passwd);
        Write_Content = (EditText) findViewById(R.id.et_content);

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
            final String str_addr = Write_Addr.getText().toString();
            final String str_count = Write_Count.getText().toString();
            final String str_passwd = Write_Passwd.getText().toString();
            final String str_content = Write_Content.getText().toString();
            if (TextUtils.isEmpty(str_addr) || TextUtils.isEmpty(str_count) || TextUtils.isEmpty(str_passwd)
                    || TextUtils.isEmpty(str_content)) {
                Toast.makeText(mContext, "参数不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            final byte[] write = StringUtils.stringToByte(str_content);
            final int addr = Integer.parseInt(str_addr);
            final int count = Integer.parseInt(str_count);
            Status.setText("正在写卡中....");
            isSuccess = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int writeArea = iuhfService.newWriteArea(which_choose, addr, count, str_passwd, write);
                    if (writeArea != 0) {
                        handler.sendMessage(handler.obtainMessage(1,"参数不正确"));
                    }
                }
            }).start();

//            int rev = iuhfService.write_area(which_choose, str_addr, str_passwd, str_count
//                    , str_content);
//            Status.setText(rev + "");


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