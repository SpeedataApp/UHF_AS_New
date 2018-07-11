package com.speedata.uhf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.uhf.MsgEvent;
import com.speedata.uhf.R;
import com.speedata.uhf.excel.EPCBean;
import com.speedata.uhf.libutils.excel.ExcelUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jxl.write.Colour;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SearchTagDialog extends Dialog implements
        android.view.View.OnClickListener, AdapterView.OnItemClickListener {

    private Button Cancle;
    private Button Action;
    private TextView Status;
    private ListView EpcList;
    private boolean inSearch = false;
    private List<EpcDataBase> firm = new ArrayList<EpcDataBase>();
    private ArrayAdapter<EpcDataBase> adapter;
    private Context cont;
    private SoundPool soundPool;
    private int soundId;
    private long scant = 0;
    private CheckBox cbb;
    private IUHFService iuhfService;
    private String model;
    private Button export;
    private KProgressHUD kProgressHUD;

    public SearchTagDialog(Context context, IUHFService iuhfService, String model) {
        super(context);
        // TODO Auto-generated constructor stub
        cont = context;
        this.iuhfService = iuhfService;
        this.model = model;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setreader);

        Cancle = (Button) findViewById(R.id.btn_search_cancle);
        Cancle.setOnClickListener(this);
        Action = (Button) findViewById(R.id.btn_search_action);
        Action.setOnClickListener(this);

        export = (Button) findViewById(R.id.btn_export);
        export.setOnClickListener(this);
        cbb = (CheckBox) findViewById(R.id.checkBox_beep);

        Status = (TextView) findViewById(R.id.textView_search_status);
        EpcList = (ListView) findViewById(R.id.listView_search_epclist);
        EpcList.setOnItemClickListener(this);

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        if (soundPool == null) {
            Log.e("as3992", "Open sound failed");
        }
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);


        //新的Listener回调参考代码

        adapter = new ArrayAdapter<EpcDataBase>(
                cont, android.R.layout.simple_list_item_1, firm);
        EpcList.setAdapter(adapter);

        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                handler.sendMessage(handler.obtainMessage(1, var1));
            }
        });
    }

    //新的Listener回调参考代码
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean cn = cont.getApplicationContext().getResources().getConfiguration().locale.getCountry().equals("CN");
            switch (msg.what) {
                case 1:
                    scant++;
                    if (!cbb.isChecked()) {
                        if (scant % 10 == 0) {
                            soundPool.play(soundId, 1, 1, 0, 0, 1);
                        }
                    }
                    SpdInventoryData var1 = (SpdInventoryData) msg.obj;
                    int j;
                    for (j = 0; j < firm.size(); j++) {
                        if (var1.epc.equals(firm.get(j).epc)) {
                            firm.get(j).valid++;
                            firm.get(j).setRssi(var1.rssi);
                            break;
                        }
                    }
                    if (j == firm.size()) {
                        firm.add(new EpcDataBase(var1.epc, 1,
                                var1.rssi, var1.tid));
                        if (cbb.isChecked()) {
                            soundPool.play(soundId, 1, 1, 0, 0, 1);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Status.setText("Total: " + firm.size());
                    break;

                case 2:
                    kProgressHUD.dismiss();
                    if (cn) {
                        Toast.makeText(cont, "导出完成", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(cont, "Export the complete", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 3:
                    kProgressHUD.dismiss();
                    if (cn) {
                        Toast.makeText(cont, "导出过程中出现问题！请重试", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(cont, "There is a problem in exporting! Please try again", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onStop() {
        Log.w("stop", "im stopping");
        if (inSearch) {
            iuhfService.inventoryStop();
            inSearch = false;
        }
        soundPool.release();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if (v == Cancle) {
            soundPool.release();
            dismiss();
        } else if (v == Action) {
            if (inSearch) {
                inSearch = false;
                this.setCancelable(true);
                iuhfService.inventoryStop();

                Action.setText(R.string.Start_Search_Btn);
                Cancle.setEnabled(true);
                export.setEnabled(true);
            } else {
                inSearch = true;
                this.setCancelable(false);
                scant = 0;
                //取消掩码
                iuhfService.selectCard(1, "", false);
                EventBus.getDefault().post(new MsgEvent("CancelSelectCard", ""));
                iuhfService.inventoryStart();
                Action.setText(R.string.Stop_Search_Btn);
                Cancle.setEnabled(false);
                export.setEnabled(false);
            }
        } else if (v == export) {
            kProgressHUD = KProgressHUD.create(cont)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
            if (firm.size() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<EPCBean> epcBeanList = new ArrayList<EPCBean>();
                        for (EpcDataBase epcDataBase : firm) {
                            EPCBean epcBean = new EPCBean();
                            epcBean.setEPC(epcDataBase.epc);
                            epcBean.setTID_USER(epcDataBase.tid_user);
                            epcBeanList.add(epcBean);
                        }
                        try {
                            ExcelUtils.getInstance()
                                    .setSHEET_NAME("UHFMsg")//设置表格名称
                                    .setFONT_COLOR(Colour.BLUE)//设置标题字体颜色
                                    .setFONT_TIMES(8)//设置标题字体大小
                                    .setFONT_BOLD(true)//设置标题字体是否斜体
                                    .setBACKGROND_COLOR(Colour.GRAY_25)//设置标题背景颜色
                                    .setContent_list_Strings(epcBeanList)//设置excel内容
                                    .setWirteExcelPath(Environment.getExternalStorageDirectory() + File.separator + "UHFMsg.xls")
                                    .createExcel(cont);
                            handler.sendMessage(handler.obtainMessage(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendMessage(handler.obtainMessage(3));
                        }


                    }
                }).start();
            } else {
                kProgressHUD.dismiss();
                boolean cn = cont.getApplicationContext().getResources().getConfiguration().locale.getCountry().equals("CN");
                if (cn) {
                    Toast.makeText(cont, "没有数据，请先盘点", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(cont, "No data, please take stock", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    class EpcDataBase {
        String epc;
        int valid;
        String rssi;
        String tid_user;

        public EpcDataBase(String e, int v, String rssi, String tid_user) {
            // TODO Auto-generated constructor stub
            epc = e;
            valid = v;
            this.rssi = rssi;
            this.tid_user = tid_user;
        }

        public String getRssi() {
            return rssi;
        }

        public void setRssi(String rssi) {
            this.rssi = rssi;
        }

        @Override
        public String toString() {
            if (TextUtils.isEmpty(tid_user)) {
                return "EPC:" + epc + "\n"
                        + "(" + "COUNT:" + valid + ")" + " RSSI:" + rssi + "\n";
            } else {
                return "EPC:" + epc + "\n"
                        + "T/U:" + tid_user + "\n"
                        + "(" + "COUNT:" + valid + ")" + " RSSI:" + rssi + "\n";
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
        // TODO Auto-generated method stub
        if (inSearch) {
            return;
        }

        String epcStr = firm.get(arg2).epc;
        boolean u8 = SharedXmlUtil.getInstance(cont).read("U8", false);
        if (u8) {
            epcStr = epcStr.substring(0, 24);
        }
        int res = iuhfService.selectCard(1, epcStr, true);
        if (res == 0) {
            EventBus.getDefault().post(new MsgEvent("set_current_tag_epc", epcStr));
            dismiss();
        } else {
            Status.setText(R.string.Status_Select_Card_Faild);
        }
    }
}
