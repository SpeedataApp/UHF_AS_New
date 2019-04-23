package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
 * Created by 张明_ on 2016/12/28.
 */

public class SetEPCDialog extends Dialog implements
        View.OnClickListener {

    private Button ok;
    private Button cancel;
    private TextView EPC;
    private TextView status;
    private EditText passwd;
    private EditText newepc;
    private EditText newepclength;
    private IUHFService iuhfService;
    private String currentTagEpc;
    private boolean isSuccess = false;
    private Context mContext;

    public SetEPCDialog(Context context, IUHFService iuhfService, String currentTagEpc) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.currentTagEpc = currentTagEpc;
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setepc);

        ok = (Button) findViewById(R.id.btn_epc_ok);
        ok.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.btn_epc_cancle);
        cancel.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_epc_epc);
        EPC.setText(currentTagEpc);
        status = (TextView) findViewById(R.id.textView_epc_status);

        passwd = (EditText) findViewById(R.id.editText_epc_passwd);
        newepc = (EditText) findViewById(R.id.editText_epc_newepc);
        newepclength = (EditText) findViewById(R.id.editText_epc_epclength);

        iuhfService.setOnWriteListener(new OnSpdWriteListener() {
            @Override
            public void getWriteData(SpdWriteData var1) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] epcData = var1.getEPCData();
                String hexString = StringUtils.byteToHexString(epcData, var1.getEPCLen());
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：").append(hexString).append("\n");
                }
                if (var1.getStatus() == 0) {
                    //状态判断，已经写卡成功了就不返回错误码了
                    isSuccess = true;
                    stringBuilder.append(mContext.getResources().getString(R.string.Status_Write_Card_Ok)).append("\n");
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder));
                } else {
                    stringBuilder.append(mContext.getResources().getString(R.string.Status_Write_Card_Faild)).append(var1.getStatus()).append("\n");
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
        if (v == ok) {
            final String password = passwd.getText().toString().replace(" ", "");
            final String epc_str = newepc.getText().toString().replace(" ", "");
            String count_str = newepclength.getText().toString();
            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(epc_str) || TextUtils.isEmpty(count_str)) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.param_not_null), Toast.LENGTH_SHORT).show();
                return;
            }
            final byte[] write = StringUtils.stringToByte(epc_str);
            final int epcl;
            try {
                epcl = Integer.parseInt(count_str, 10);
            } catch (NumberFormatException e) {
                return;
            }

            status.setText(mContext.getResources().getString(R.string.writing_card));
            isSuccess = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int writeArea = set_EPC(epcl, password, write);
                    if (writeArea != 0) {
                        handler.sendMessage(handler.obtainMessage(1, mContext.getResources().getString(R.string.param_error)));
                    }
                }
            }).start();

        } else if (v == cancel) {
            dismiss();
        }
    }

    int set_EPC(int epclength, String passwd, byte[] EPC) {
        byte[] res;
        if (epclength > 31) {
            return -3;
        }
        if (epclength * 2 < EPC.length) {
            return -3;
        }
        res = iuhfService.read_area(IUHFService.EPC_A, 1, 1, passwd);
        if (res == null) {
            return -5;
        }
        res[0] = (byte) ((res[0] & 0x7) | (epclength << 3));
        byte[] f = new byte[2 + epclength * 2];
        try {
            System.arraycopy(res, 0, f, 0, 2);
            System.arraycopy(EPC, 0, f, 2, epclength * 2);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        SystemClock.sleep(500);
        return iuhfService.writeArea(IUHFService.EPC_A, 1, f.length / 2, passwd, f);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.what == 1) {
                    status.setText(msg.obj + "");
                }
            }
        }
    };
}
