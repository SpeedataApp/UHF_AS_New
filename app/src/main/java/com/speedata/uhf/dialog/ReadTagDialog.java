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
import com.speedata.libuhf.bean.SpdReadData;
import com.speedata.libuhf.interfaces.OnSpdReadListener;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.uhf.R;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class ReadTagDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Button Ok;
    private Button Cancle;
    private TextView EPC;
    private TextView Status;
    private EditText Read_Addr;
    private EditText Read_Count;
    private EditText Password;
    private IUHFService iuhfService;
    private String current_tag_epc;
    private int which_choose;
    private String model;
    private Context mContext;

    public ReadTagDialog(Context context, IUHFService iuhfService
            , int which_choose, String current_tag_epc, String model) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.current_tag_epc = current_tag_epc;
        this.which_choose = which_choose;
        this.model = model;
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read);

        Ok = (Button) findViewById(R.id.btn_read_ok);
        Ok.setOnClickListener(this);
        Cancle = (Button) findViewById(R.id.btn_read_cancle);
        Cancle.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_read_epc);
        EPC.setText(current_tag_epc);
        Status = (TextView) findViewById(R.id.textView_read_status);

        Read_Addr = (EditText) findViewById(R.id.editText_read_addr);
        Read_Count = (EditText) findViewById(R.id.editText_read_count);
        Password = (EditText) findViewById(R.id.editText_rp);

        iuhfService.setOnReadListener(new OnSpdReadListener() {
            @Override
            public void getReadData(SpdReadData var1) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] epcData = var1.getEPCData();
                String hexString = StringUtils.byteToHexString(epcData, var1.getEPCLen());
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：" + hexString + "\n");
                }
                if (var1.getStatus() == 0) {
                    byte[] readData = var1.getReadData();
                    String readHexString = StringUtils.byteToHexString(readData, var1.getDataLen());
                    stringBuilder.append("ReadData：" + readHexString + "\n");
                } else {
                    stringBuilder.append("ReadError：" + var1.getStatus() + "\n");
                }
                handler.sendMessage(handler.obtainMessage(1, stringBuilder));
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == Ok) {
            final String str_addr = Read_Addr.getText().toString();
            final String str_count = Read_Count.getText().toString();
            final String str_passwd = Password.getText().toString();
            if (TextUtils.isEmpty(str_addr) || TextUtils.isEmpty(str_count) || TextUtils.isEmpty(str_passwd)) {
                Toast.makeText(mContext, "参数不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            final int addr = Integer.parseInt(str_addr);
            final int count = Integer.parseInt(str_count);
            Status.setText("正在读卡中....");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int readArea = iuhfService.readArea(which_choose, addr, count, str_passwd);
                    if (readArea != 0) {
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
            switch (msg.what) {
                case 1:
                    Status.setText(msg.obj + "");
                    break;
            }
        }
    };
}
