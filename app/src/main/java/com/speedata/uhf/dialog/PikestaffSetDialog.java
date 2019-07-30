package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.speedata.uhf.R;
import com.speedata.uhf.libutils.SharedXmlUtil;

/**
 * @author zzc
 */
public class PikestaffSetDialog extends Dialog implements View.OnClickListener {

    private CheckBox cbStartScan;
    private CheckBox cbStartUhf;
    private Button backBtn;
    private Context context;
    private boolean isScan;

    public PikestaffSetDialog(Context context) {
        super(context);
        this.context = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pikestaff_set);

        Log.e("r2000_native", "oncreate");

        cbStartScan = findViewById(R.id.cb_start_scan);
        cbStartUhf = findViewById(R.id.cb_start_uhf);
        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        initData();
    }

    private void initData() {
        cbStartScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbStartUhf.setChecked(false);
                    SystemProperties.set("persist.sys.PistolKey", "scan");
                    SharedXmlUtil.getInstance(context, "uhf_file").write("scan", true);
                }
            }
        });
        cbStartUhf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbStartScan.setChecked(false);
                    SystemProperties.set("persist.sys.PistolKey", "uhf");
                    SharedXmlUtil.getInstance(context, "uhf_file").write("scan", false);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isScan = SharedXmlUtil.getInstance(context,"uhf_file").read("scan",false);
        if (isScan){
            cbStartScan.setChecked(true);
        }else {
            cbStartUhf.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == backBtn) {
            dismiss();
        }
    }
}
