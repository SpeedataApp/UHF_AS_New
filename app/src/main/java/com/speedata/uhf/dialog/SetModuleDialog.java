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
    private boolean isSet = false;




    public SetModuleDialog(Context context, IUHFService iuhfService, String model) {
        super(context);
        this.iuhfService = iuhfService;
        this.model = model;
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setting);
        initView();

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

        int re = iuhfService.getFreqRegion();
        if ("r2k".equals(model)) {
            if (re == IUHFService.REGION_CHINA_920_925) {
                lf.setSelection(1, true);
            } else if (re == IUHFService.REGION_CHINA_840_845) {
                lf.setSelection(0, true);
            } else if (re == IUHFService.REGION_CHINA_902_928) {
                lf.setSelection(2, true);
            } else if (re == IUHFService.REGION_EURO_865_868) {
                lf.setSelection(3, true);
            } else if (re == -1) {
                lf.setSelection(5, true);
                status.setText(R.string.set_read_fail);
                Log.e("r2000_kt45", "read region setting read failed");
            } else {
                lf.setSelection(4, true);
                et_pin_dian.setText(String.valueOf(new DecimalFormat("0.000").format(re / 1000.0)));
                ll_zaibo.setVisibility(View.VISIBLE);
            }
        } else {
            if (re == IUHFService.REGION_CHINA_920_925) {
                lf.setSelection(1, true);
            } else if (re == IUHFService.REGION_CHINA_840_845) {
                lf.setSelection(0, true);
            } else if (re == IUHFService.REGION_CHINA_902_928) {
                lf.setSelection(2, true);
            } else if (re == IUHFService.REGION_EURO_865_868) {
                lf.setSelection(3, true);
            } else {
                lf.setSelection(4, true);
                status.setText(R.string.set_read_fail);
                Log.e("r2000_kt45", "read region setting read failed");
            }
        }


        int ivp = iuhfService.getAntennaPower();
        if (ivp > 0) {
            setp.setEnabled(true);
            pv.setText("" + ivp);
        }
        if (model.equals("as3992")) {
            pv.setHint("0关天线1开天线");
            setp.setEnabled(true);
        }

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
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == setf) {
            int freq_region = lf.getSelectedItemPosition();
            if (freq_region >= 4) {
                status.setText(R.string.invalid_select);
            } else {
                if (iuhfService.setFreqRegion(freq_region) < 0) {
                    status.setText(R.string.set_freq_fail);
                } else {
                    status.setText(R.string.set_freq_ok);
                    back.setText(R.string.update_set);
                    isSet = true;
                    this.setCancelable(false);
                }
            }

        } else if (v == back) {
            if (isSet){
                new toast_thread().setr(mContext.getResources().getString(R.string.toast_update_now)).start();
            }
            dismiss();
        } else if (v == setp) {
            String power = pv.getText().toString();
            if (power.isEmpty()){
                status.setText(R.string.param_not_null);
                return;
            }
            int ivp = Integer.parseInt(power);
            if ((ivp < 0) || (ivp > 33)) {
                status.setText(R.string.power_range);
            } else {
                int rv = iuhfService.setAntennaPower(ivp);
                if (rv < 0) {
                    status.setText(R.string.set_power_fail );
                } else {
                    status.setText(R.string.set_power_ok);
                    back.setText(R.string.update_set);
                    isSet = true;
                    this.setCancelable(false);
                }
            }
        } else if (v == button_set_pindian) {
            String parse = et_pin_dian.getText().toString();
            if (parse.isEmpty()){
                status.setText(R.string.param_not_null);
                return;
            }
            double parseDouble = Double.parseDouble(parse);
            int frequency = iuhfService.setFrequency(parseDouble);
            if (frequency == 0) {
                status.setText(R.string.set_fix_ok);
                ll_zaibo.setVisibility(View.VISIBLE);
            } else {
                status.setText(R.string.set_fix_fail);
                ll_zaibo.setVisibility(View.GONE);
            }
        } else if (v == button_zaibo) {
            String parse = et_zaibo.getText().toString();
            if (parse.isEmpty()){
                status.setText(R.string.param_not_null);
                return;
            }
            int zaibo = Integer.parseInt(parse);
            int engTest = iuhfService.enableEngTest(zaibo);
            if (engTest == 0) {
                status.setText(R.string.set_carrier_ok);
            } else {
                status.setText(R.string.set_carrier_fail);
            }
        }
    }

    private class toast_thread extends Thread {

        String a;

        public toast_thread setr(String m) {
            a = m;
            return this;
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            Toast.makeText(mContext, a, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}
