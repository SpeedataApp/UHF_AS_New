package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.speedata.libuhf.FLX;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.uhf.R;

/**
 * Created by 张明_ on 2017/6/26.
 */

public class InvSetDialog extends Dialog implements android.view.View.OnClickListener {

    private Button ok, cancel;
    private Spinner mode;
    private EditText addr, size;
    private TextView status;
    private String[] slist = {"Only EPC", "EPC + TID", "EPC + USER", "EPC+BID", "EPC+BID+TID"};
    private ArrayAdapter<String> iapt;
    private IUHFService iuhfService;
    private Context context;

    public InvSetDialog(Context context, IUHFService iuhfService) {
        super(context);
        this.iuhfService = iuhfService;
        this.context = context;
        // TODO Auto-generated constructor stub
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invset);

        Log.e("r2000_native", "oncreate");

        ok = (Button) findViewById(R.id.btn_invs_set);
        ok.setOnClickListener(this);

        cancel = (Button) findViewById(R.id.btn_invs_cancle);
        cancel.setOnClickListener(this);

        status = (TextView) findViewById(R.id.textView_invs_status);

        addr = (EditText) findViewById(R.id.editText_invs_addr);
        size = (EditText) findViewById(R.id.editText_invs_count);

        iapt = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, slist);
        iapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mode = (Spinner) findViewById(R.id.spinner_invs);
        mode.setAdapter(iapt);
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                if (position == 0 || position == 3 || position == 4) {
                    addr.setEnabled(false);
                    size.setEnabled(false);
                } else {
                    addr.setEnabled(true);
                    size.setEnabled(true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });

        int curmode = iuhfService.GetInvMode(FLX.InvModeType);
        int curaddr = iuhfService.GetInvMode(FLX.InvAddrType);
        int cursize = iuhfService.GetInvMode(FLX.InvSizeType);
        addr.setText(curaddr + "");
        size.setText(cursize + "");
        mode.setSelection(curmode);
        Log.e("r2000_native", "oncreate over");
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == ok) {
            int w = mode.getSelectedItemPosition();
            Log.w("r2000_native", "select item " + w);
            if (w == 3) {
                //读取U8标签代码epc+bid
                iuhfService.Mask(1, 516, 1, StringUtils.stringToByte("80"));
                SharedXmlUtil.getInstance(context).write("U8", true);
            } else if (w == 4) {
                //读取U8标签代码epc+bid+tid
                iuhfService.Mask(1, 516, 1, StringUtils.stringToByte("80"));
                iuhfService.SetInvMode(1, 0, 6);
                SharedXmlUtil.getInstance(context).write("U8", true);
            } else {
                iuhfService.cancelMask();
                SharedXmlUtil.getInstance(context).write("U8", false);
                int caddr = 0, csize = 0;
                String saddr = addr.getText().toString();
                String ssize = size.getText().toString();
                if (w != 0) {
                    try {
                        caddr = Integer.parseInt(saddr);
                        csize = Integer.parseInt(ssize);
                        if (csize == 0)
                            throw new NumberFormatException("size cannot be 0");

                    } catch (NumberFormatException p) {
                        status.setText(R.string.Status_InvalidNumber);
                        status.append("\n" + p.getMessage());
                        return;
                    }
                }
                iuhfService.SetInvMode(w, caddr, csize);
            }

            dismiss();
        } else if (v == cancel) {
            dismiss();
        }
    }
}
