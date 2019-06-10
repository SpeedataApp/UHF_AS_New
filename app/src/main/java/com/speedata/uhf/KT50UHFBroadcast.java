package com.speedata.uhf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zzc
 */
public class KT50UHFBroadcast extends Activity {

    /**
     * 按设备按键触发的扫描广播
     */
    public static final String START_SCAN = "com.spd.action.start_uhf";
    public static final String STOP_SCAN = "com.spd.action.stop_uhf";
    private IUHFService iuhfService;
    private SoundPool soundPool;
    private int soundId;
    private boolean isOpen = true;
    private TextView tvEpc;
    private Button btnClear;
    private List<String> epcName = new LinkedList<>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("zzc:", "action:" + action);
            assert action != null;
            if (action.equals(START_SCAN)) {
                if (!openDev()) {
                    iuhfService.inventoryStart();
                }
            }
            if (action.equals(STOP_SCAN)) {
                iuhfService.inventoryStop();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kt50uhf_broadcast);
        initReceive();
        initUHF();
        tvEpc = findViewById(R.id.tv_epc);
        btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEpc.setText("");
            }
        });
        openDev();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (iuhfService != null) {
            isOpen = !openDev();
        }
    }

    private void initUHF() {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);
        iuhfService = MyApp.getInstance().getIuhfService();

        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                epcName.add(var1.epc);
                soundPool.play(soundId, 1, 1, 0, 0, 1);
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
        registerReceiver(receiver, filter);
    }

    /**
     * 上电开串口
     *
     * @return
     */
    private boolean openDev() {
        if (iuhfService.openDev() != 0) {
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    finish();
                }
            }).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iuhfService != null) {
            iuhfService.closeDev();
        }
        unregisterReceiver(receiver);
    }
}
