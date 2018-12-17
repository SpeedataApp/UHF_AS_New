package com.speedata.uhf.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.uhf.MsgEvent;
import com.speedata.uhf.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class DirectionalTagDialog extends Dialog implements
        View.OnClickListener, AdapterView.OnItemClickListener {

    private Button Cancle;
    private Button Action;
    private TextView Status;
    private ListView EpcList;
    private boolean inSearch = false;
    private boolean inSeek = false;
    private List<EpcDataBase> firm = new ArrayList<EpcDataBase>();
    private ArrayAdapter<EpcDataBase> adapter;
    private Context cont;
    private SoundPool soundPool;
    private int soundId;
    private int soundId1;
    private int soundId2;
    private long scant = 0;
    private IUHFService iuhfService;
    private Button mBtnSeek;
    private EditText mEtEpc;

    public DirectionalTagDialog(Context context, IUHFService iuhfService) {
        super(context);
        // TODO Auto-generated constructor stub
        cont = context;
        this.iuhfService = iuhfService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_direction);

        initView();

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        if (soundPool == null) {
            Log.e("as3992", "Open sound failed");
        }
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);

        soundId1 = soundPool.load(cont, R.raw.scankey, 0);
        soundId2 = soundPool.load(cont, R.raw.beep, 0);


        //新的Listener回调参考代码

        adapter = new ArrayAdapter<EpcDataBase>(
                cont, android.R.layout.simple_list_item_1, firm);
        EpcList.setAdapter(adapter);

        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {

                if (inSeek) {
                    handler.sendMessage(handler.obtainMessage(2, var1));
                } else {
                    handler.sendMessage(handler.obtainMessage(1, var1));
                }

            }
        });
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
                    if (scant % 1 == 0) {
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
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                    }
                    adapter.notifyDataSetChanged();
                    Status.setText("Total: " + firm.size());
                    break;

                case 2:
                    String epcToStr = mEtEpc.getText().toString();
                    if (!TextUtils.isEmpty(epcToStr)) {
                        SpdInventoryData spdInventoryData = (SpdInventoryData) msg.obj;
                        String epc = spdInventoryData.getEpc();
                        if (epc.equals(epcToStr)) {
                            int rssi = Integer.parseInt(spdInventoryData.getRssi());
                            if (rssi > -60) {
                                if (rssi > -40) {
                                    soundPool.play(soundId1, 1, 1, 0, 0, 3);
                                } else {
                                    soundPool.play(soundId1, 0.6F, 0.6F, 0, 0, 2);
                                }

                            } else {
                                soundPool.play(soundId1, 0.3F, 0.3F, 0, 0, 1);
                            }
                        }
                    } else {
                        if (cn) {
                            Toast.makeText(cont, "请先填写要搜寻卡片的EPC", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(cont, "Please fill out the EPC for the card", Toast.LENGTH_SHORT).show();
                        }
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
            inSeek = false;
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
                mBtnSeek.setEnabled(true);
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
                mBtnSeek.setEnabled(false);
            }
        } else if (v == mBtnSeek) {

            if (inSeek) {
                inSeek = false;
                this.setCancelable(true);
                iuhfService.inventoryStop();
                mBtnSeek.setText(R.string.dialog_dir_seek);
                Cancle.setEnabled(true);
                Action.setEnabled(true);
            } else {
                inSeek = true;
                this.setCancelable(false);
                scant = 0;
                iuhfService.inventoryStart();
                mBtnSeek.setText(R.string.Stop_Search_Btn);
                Cancle.setEnabled(false);
                Action.setEnabled(false);
            }


        }
    }

    private void initView() {
        mEtEpc = (EditText) findViewById(R.id.et_epc);
        Cancle = (Button) findViewById(R.id.btn_search_cancle);
        Cancle.setOnClickListener(this);
        Action = (Button) findViewById(R.id.btn_search_action);
        Action.setOnClickListener(this);

        mBtnSeek = (Button) findViewById(R.id.btn_seek);
        mBtnSeek.setOnClickListener(this);

        Status = (TextView) findViewById(R.id.textView_search_status);
        EpcList = (ListView) findViewById(R.id.listView_search_epclist);
        EpcList.setOnItemClickListener(this);
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
            mEtEpc.setText(epcStr);
        } else {
            Status.setText(R.string.Status_Select_Card_Faild);
        }
    }
}
