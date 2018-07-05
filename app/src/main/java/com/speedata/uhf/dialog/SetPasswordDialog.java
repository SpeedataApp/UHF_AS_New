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
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.uhf.R;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SetPasswordDialog extends Dialog implements
        android.view.View.OnClickListener {

    private String[] passwd_list = {"Kill Password", "Access Password"};
    private Button Ok;
    private Button Cancle;
    private TextView EPC;
    private TextView Status;
    private Spinner area_select;
    private EditText access_passwd;
    private EditText new_passwd;
    private ArrayAdapter<String> setadapter;
    private IUHFService iuhfService;
    private String current_tag_epc;
    private String model;
    private Context context;
    private boolean isSuccess = false;

    public SetPasswordDialog(Context context, IUHFService iuhfService
            , String current_tag_epc, String model) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.current_tag_epc = current_tag_epc;
        this.model = model;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setpassword);

        Ok = (Button) findViewById(R.id.btn_setpawd_ok);
        Ok.setOnClickListener(this);
        Cancle = (Button) findViewById(R.id.btn_setpawd_cancle);
        Cancle.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_setpawd_epc);
        EPC.setText(current_tag_epc);
        Status = (TextView) findViewById(R.id.textView_setpawd_status);

        access_passwd = (EditText) findViewById(R.id.editText_setpawd_accesspd);
        new_passwd = (EditText) findViewById(R.id.editText_setpawd_newpd);

        setadapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_item, passwd_list);
        setadapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area_select = (Spinner) findViewById(R.id.spinner_setpawd_paswd);
        area_select.setAdapter(setadapter);

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
            final String cur_pass = access_passwd.getText().toString();
            final String new_pass = new_passwd.getText().toString();
            if (TextUtils.isEmpty(cur_pass) || TextUtils.isEmpty(new_pass)) {
                Toast.makeText(context, "参数不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            final int which = area_select.getSelectedItemPosition();
            Status.setText("正在修改密码中....");
            isSuccess = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int setPassword = iuhfService.setPassword(which, cur_pass, new_pass);
                    if (setPassword != 0) {
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
