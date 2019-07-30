package com.speedata.uhf.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.uhf.MsgEvent;
import com.speedata.uhf.MyApp;
import com.speedata.uhf.R;
import com.speedata.uhf.excel.EPCBean;
import com.speedata.uhf.libutils.excel.ExcelUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jxl.write.Colour;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SearchTagDialog extends Dialog implements
        View.OnClickListener, AdapterView.OnItemClickListener {

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
    private LinearLayout showLayout;
    private TextView tagNumTv;
    private TextView speedTv;
    private TextView totalTv;
    private TextView totalTime;
    private long startCheckingTime;//盘点命令下发后截取的系统时间
    /**
     * 按设备按键触发的扫描广播
     */
    public static final String START_SCAN = "com.spd.action.start_uhf";
    public static final String STOP_SCAN = "com.spd.action.stop_uhf";
    public static final String SCAN_BARCODE = "com.geomobile.se4500barcode";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                assert action != null;
                switch (action) {
                    case START_SCAN:
                        //启动超高频扫描
                        if (inSearch) {
                            return;
                        }
                        startUhf();
                        break;
                    case STOP_SCAN:
                        if (inSearch) {
                            stopUhf();
                        }
                        break;
                    default:
                        break;
                }
            }
    };

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

        initView();
        initReceive();
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

    /**
     * 注册广播
     */
    private void initReceive() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_SCAN);
        filter.addAction(STOP_SCAN);
        cont.registerReceiver(receiver, filter);
    }

    //新的Listener回调参考代码
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean cn = cont.getApplicationContext().getResources().getConfiguration().locale.getCountry().equals("CN");
            switch (msg.what) {
                case 1:
                    scant++;
                    if (!cbb.isChecked()) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
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
                    UpdateRateCount();
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
                default:
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
        if (receiver != null) {
            cont.unregisterReceiver(receiver);
        }
        super.onStop();
    }

    /**
     * 开始盘点
     */
    private void startUhf() {
        inSearch = true;
        this.setCancelable(false);
        scant = 0;
        firm.clear();
        //取消掩码
        iuhfService.selectCard(1, "", false);
        EventBus.getDefault().post(new MsgEvent("CancelSelectCard", ""));
        SystemClock.sleep(1);
        iuhfService.inventoryStart();
        startCheckingTime = System.currentTimeMillis();
        Action.setText(R.string.Stop_Search_Btn);
        Cancle.setEnabled(false);
        export.setEnabled(false);
    }
    /**
     * 停止盘点
     */
    private void stopUhf() {
        inSearch = false;
        this.setCancelable(true);
        iuhfService.inventoryStop();
        Action.setText(R.string.Start_Search_Btn);
        Cancle.setEnabled(true);
        export.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v == Cancle) {
            soundPool.release();
            dismiss();
        } else if (v == Action) {
            if (inSearch) {
                stopUhf();
            } else {
                startUhf();
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
                        if (epcBeanList.size() > 0) {
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
                        } else {
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

    private void initView() {
        showLayout = (LinearLayout) findViewById(R.id.show_layout);
        tagNumTv = (TextView) findViewById(R.id.tagNum_tv);
        speedTv = (TextView) findViewById(R.id.speed_tv);
        totalTv = (TextView) findViewById(R.id.total_tv);
        totalTime = (TextView) findViewById(R.id.totalTime);
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


    private void UpdateRateCount() {

        long m_lEndTime = System.currentTimeMillis();

        double Rate = Math.ceil((scant * 1.0) * 1000 / (m_lEndTime - startCheckingTime));

        long total_time_count = m_lEndTime - startCheckingTime;

        speedTv.setText(String.format("%s次/秒", String.valueOf(Rate)));

        tagNumTv.setText(String.format("%s个", String.valueOf(firm.size())));

        totalTv.setText(String.format("%s次", String.valueOf(scant)));

        totalTime.setText(String.valueOf(getTimeFromMillisecond(total_time_count)));


    }

    /**
     * 从时间(毫秒)中提取出时间(时:分:秒)
     * 时间格式:  时:分
     *
     * @param millisecond 毫秒
     * @return 时间字符串
     */
    public static String getTimeFromMillisecond(Long millisecond) {
        String milli;
        long hours = millisecond / (60 * 60 * 1000); //根据时间差来计算小时数
        long minutes = (millisecond - hours * (60 * 60 * 1000)) / (60 * 1000);   //根据时间差来计算分钟数
        long second = (millisecond - hours * (60 * 60 * 1000) - minutes * (60 * 1000)) / 1000;   //根据时间差来计算秒数
        long milliSecond = millisecond - hours * (60 * 60 * 1000) - minutes * (60 * 1000) - second * 1000;   //根据时间差来计算秒数
        if (milliSecond < 100) {
            milli = "0" + milliSecond;
        } else {
            milli = "" + milliSecond;
        }

        return hours + ": " + minutes + ": " + second + ":" + milli;
    }
}
