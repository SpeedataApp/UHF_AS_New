package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.uhf.R;

import java.text.DecimalFormat;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SetModuleDialog extends Dialog implements View.OnClickListener {

    //其他模块频段
    private final String[] freq_area_item = {"840-845", "920-925", "902-928", "865-868", "..."};
    //R2000模块频段
    private final String[] r2k_freq_area_item = {"840-845", "920-925", "902-928",
            "865-868", "当前状态为定频", "..."};
    private Button setf, back;
    private TextView status;
    private Spinner lf;
    private Button setp;
    private EditText pv;
    private IUHFService iuhfService;
    private String model;
    private Context mContext;
    private EditText et_pin_dian;
    private Button button_set_pindian;
    private LinearLayout ll_dianpin;
    private EditText et_zaibo;
    private Button button_zaibo;
    private LinearLayout ll_zaibo;

    private Button btnSetSession, btnGetSession;
    private Spinner session;
    private final String[] sessionItem = {"s0", "s1", "s2", "s3"};


    public SetModuleDialog(Context context, IUHFService iuhfService, String model) {
        super(context);
        this.iuhfService = iuhfService;
        this.model = model;
        this.mContext = context;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setting);
        initView();

        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<>(this.getContext(),
                android.R.layout.simple_spinner_item, sessionItem);
        session.setAdapter(sessionAdapter);

        ArrayAdapter<String> tmp;
        if ("r2k".equals(model)) {
            ll_dianpin.setVisibility(View.VISIBLE);
            tmp = new ArrayAdapter<String>(this.getContext(),
                    android.R.layout.simple_spinner_item, r2k_freq_area_item);
        } else {
            tmp = new ArrayAdapter<String>(this.getContext(),
                    android.R.layout.simple_spinner_item, freq_area_item);
        }
        tmp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lf.setAdapter(tmp);

        int re = iuhfService.get_freq_region();
        if ("r2k".equals(model)) {
            if (re == iuhfService.REGION_CHINA_920_925) {
                lf.setSelection(1, true);
            } else if (re == iuhfService.REGION_CHINA_840_845) {
                lf.setSelection(0, true);
            } else if (re == iuhfService.REGION_CHINA_902_928) {
                lf.setSelection(2, true);
            } else if (re == iuhfService.REGION_EURO_865_868) {
                lf.setSelection(3, true);
            } else if (re == -1) {
                lf.setSelection(5, true);
                status.setText("read region setting read failed");
                Log.e("r2000_kt45", "read region setting read failed");
            } else {
                lf.setSelection(4, true);
                et_pin_dian.setText(String.valueOf(new DecimalFormat("0.000").format(re / 1000.0)));
                ll_zaibo.setVisibility(View.VISIBLE);
            }
        } else {
            if (re == iuhfService.REGION_CHINA_920_925) {
                lf.setSelection(1, true);
            } else if (re == iuhfService.REGION_CHINA_840_845) {
                lf.setSelection(0, true);
            } else if (re == iuhfService.REGION_CHINA_902_928) {
                lf.setSelection(2, true);
            } else if (re == iuhfService.REGION_EURO_865_868) {
                lf.setSelection(3, true);
            } else {
                lf.setSelection(4, true);
                status.setText("read region setting read failed");
                Log.e("r2000_kt45", "read region setting read failed");
            }
        }


        int ivp = iuhfService.get_antenna_power();
        if (ivp > 0) {
            setp.setEnabled(true);
            pv.setText("" + ivp);
        }
        if (model.equals("as3992")) {
            pv.setHint("0关天线1开天线");
            setp.setEnabled(true);
        }

        getSession();
    }

    private void initView() {
        setf = (Button) findViewById(R.id.button_set_region);
        setf.setOnClickListener(this);
        back = (Button) findViewById(R.id.button_set_back);
        back.setOnClickListener(this);
        status = (TextView) findViewById(R.id.textView_set_status);
        setp = (Button) findViewById(R.id.button_set_antenna);
        setp.setOnClickListener(this);
        setp.setEnabled(false);
        pv = (EditText) findViewById(R.id.editText_antenna);
        lf = (Spinner) findViewById(R.id.spinner_region);
        button_set_pindian = (Button) findViewById(R.id.button_set_pindian);
        button_set_pindian.setOnClickListener(this);
        et_pin_dian = (EditText) findViewById(R.id.et_pin_dian);
        ll_dianpin = (LinearLayout) findViewById(R.id.ll_dianpin);
        button_zaibo = (Button) findViewById(R.id.button_zaibo);
        button_zaibo.setOnClickListener(this);
        et_zaibo = (EditText) findViewById(R.id.et_zaibo);
        ll_zaibo = (LinearLayout) findViewById(R.id.ll_zaibo);
        //session
        btnGetSession = (Button) findViewById(R.id.button_get_session);
        btnGetSession.setOnClickListener(this);
        btnSetSession = (Button) findViewById(R.id.button_set_session);
        btnSetSession.setOnClickListener(this);
        session = (Spinner) findViewById(R.id.spinner_session);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == setf) {
            int freq_region = lf.getSelectedItemPosition();
            if (freq_region >= 4) {
                status.setText("Invalid select");
            } else {
                if (iuhfService.set_freq_region(freq_region) < 0) {
                    status.setText("set freq region failed");
                } else {
                    status.setText("set freq region ok");
                    back.setText("update settings");
                    this.setCancelable(false);
                }
            }

        } else if (v == back) {
            new toast_thread().setr("update settings now").start();
            dismiss();
        } else if (v == setp) {
            int ivp = Integer.parseInt(pv.getText().toString());
            if ((ivp < 0) || (ivp > 30)) {
                status.setText("value range is 0 ~ 30");
            } else {
                int rv = iuhfService.set_antenna_power(ivp);
                if (rv < 0) {
                    status.setText("set antenna power failed");
                } else {
                    status.setText("set antenna power ok");
                    back.setText("update settings");
                    this.setCancelable(false);
                }
            }
        } else if (v == button_set_pindian) {
            double parseDouble = Double.parseDouble(et_pin_dian.getText().toString());
            int frequency = iuhfService.setFrequency(parseDouble);
            if (frequency == 0) {
                status.setText("set fixed frequency ok");
                ll_zaibo.setVisibility(View.VISIBLE);
            } else {
                status.setText("set fixed frequency failed");
                ll_zaibo.setVisibility(View.GONE);
            }
        } else if (v == button_zaibo) {
            int zaibo = Integer.parseInt(et_zaibo.getText().toString());
            int engTest = iuhfService.enableEngTest(zaibo);
            if (engTest == 0) {
                status.setText("Set carrier success");
            } else {
                status.setText("Set carrier failed");
            }
        } else if (v == btnGetSession) {
            getSession();
        } else if (v == btnSetSession) {
            int setQueryTagGroup = iuhfService.setQueryTagGroup(0, (int) session.getSelectedItemId(), 0);
            if (setQueryTagGroup == 0) {
                status.setText("Set success");
            } else {
                status.setText("Set failed:" + setQueryTagGroup);
            }
        }
    }

    private void getSession() {
        int queryTagGroup = iuhfService.getQueryTagGroup();
        if (queryTagGroup != -1) {
            session.setSelection(queryTagGroup);
            status.setText("Get success");
        } else {
            status.setText("Get failed");
        }
    }

    private class toast_thread extends Thread {

        String a;

        public toast_thread setr(String m) {
            a = m;
            return this;
        }

        public void run() {
            super.run();
            Looper.prepare();
            Toast.makeText(mContext, a, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}
