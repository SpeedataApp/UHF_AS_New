package com.speedata.uhf.dialog;

import android.annotation.SuppressLint;
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
        View.OnClickListener {

    private Button ok;
    private Button cancel;
    private TextView status;
    private EditText readAddr;
    private EditText readCount;
    private EditText password;
    private IUHFService iuhfService;
    private String currentTagEpc;
    private int whichChoose;
    private Context mContext;

    public ReadTagDialog(Context context, IUHFService iuhfService
            , int whichChoose, String currentTagEpc, String model) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.currentTagEpc = currentTagEpc;
        this.whichChoose = whichChoose;
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read);

        ok = findViewById(R.id.btn_read_ok);
        ok.setOnClickListener(this);
        cancel = findViewById(R.id.btn_read_cancle);
        cancel.setOnClickListener(this);

        TextView ePC = findViewById(R.id.textView_read_epc);
        ePC.setText(currentTagEpc);
        status = findViewById(R.id.textView_read_status);

        readAddr = findViewById(R.id.editText_read_addr);
        readCount = findViewById(R.id.editText_read_count);
        password = findViewById(R.id.editText_rp);

        iuhfService.setOnReadListener(new OnSpdReadListener() {
            @Override
            public void getReadData(SpdReadData var1) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] epcData = var1.getEPCData();
                String hexString = StringUtils.byteToHexString(epcData, var1.getEPCLen());
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：").append(hexString).append("\n");
                }
                if (var1.getStatus() == 0) {
                    byte[] readData = var1.getReadData();
                    String readHexString = StringUtils.byteToHexString(readData, var1.getDataLen());
                    stringBuilder.append("ReadData：").append(readHexString).append("\n");
                } else {
                    stringBuilder.append(mContext.getResources().getString(R.string.Status_Read_Card_Faild)).append(var1.getStatus()).append("\n");
                }
                handler.sendMessage(handler.obtainMessage(1, stringBuilder));
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == ok) {
            final String strAddr = readAddr.getText().toString();
            final String strCount = readCount.getText().toString();
            final String strPasswd = password.getText().toString();
            if (TextUtils.isEmpty(strAddr) || TextUtils.isEmpty(strCount) || TextUtils.isEmpty(strPasswd)) {
                Toast.makeText(mContext, R.string.param_not_null, Toast.LENGTH_SHORT).show();
                return;
            }
            final int addr = Integer.parseInt(strAddr);
            final int count = Integer.parseInt(strCount);
            status.setText(R.string.reading_card);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int readArea = iuhfService.readArea(whichChoose, addr, count, strPasswd);
                    if (readArea != 0) {
                        handler.sendMessage(handler.obtainMessage(1, mContext.getResources().getString(R.string.param_error)));
                    }
                }
            }).start();

        } else if (v == cancel) {
            dismiss();
        }
    }

    @SuppressLint("HandlerLeak")
    private
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    status.setText(msg.obj + "");
                    break;
                default:
                    break;
            }
        }
    };
}
