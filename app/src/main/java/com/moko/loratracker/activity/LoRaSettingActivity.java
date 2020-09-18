package com.moko.loratracker.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.loratracker.R;
import com.moko.loratracker.dialog.BottomDialog;
import com.moko.loratracker.dialog.LoadingMessageDialog;
import com.moko.loratracker.dialog.RegionBottomDialog;
import com.moko.loratracker.entity.Region;
import com.moko.loratracker.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoRaSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {


    @Bind(R.id.et_dev_eui)
    EditText etDevEui;
    @Bind(R.id.et_app_eui)
    EditText etAppEui;
    @Bind(R.id.et_app_key)
    EditText etAppKey;
    @Bind(R.id.ll_modem_otaa)
    LinearLayout llModemOtaa;
    @Bind(R.id.et_dev_addr)
    EditText etDevAddr;
    @Bind(R.id.et_nwk_skey)
    EditText etNwkSkey;
    @Bind(R.id.et_app_skey)
    EditText etAppSkey;
    @Bind(R.id.ll_modem_abp)
    LinearLayout llModemAbp;
    @Bind(R.id.et_report_interval)
    EditText etReportInterval;
    @Bind(R.id.tv_ch_1)
    TextView tvCh1;
    @Bind(R.id.tv_ch_2)
    TextView tvCh2;
    @Bind(R.id.tv_dr_1)
    TextView tvDr1;
    @Bind(R.id.tv_connect)
    TextView tvConnect;
    @Bind(R.id.cb_adr)
    CheckBox cbAdr;
    @Bind(R.id.tv_upload_mode)
    TextView tvUploadMode;
    @Bind(R.id.tv_region)
    TextView tvRegion;
    @Bind(R.id.tv_message_type)
    TextView tvMessageType;
    @Bind(R.id.ll_advanced_setting)
    LinearLayout llAdvancedSetting;
    @Bind(R.id.cb_advance_setting)
    CheckBox cbAdvanceSetting;

    private boolean mReceiverTag = false;
    private ArrayList<String> mModeList;
    private ArrayList<Region> mRegionsList;
    private ArrayList<String> mMessageTypeList;
    private String[] mUploadMode;
    private String[] mRegions;
    private String[] mMessageType;
    private int mSelectedMode;
    private int mSelectedRegion;
    private int mSelectedMessageType;
    private int mSelectedCh1;
    private int mSelectedCh2;
    private int mSelectedDr1;
    private int mMaxCH;
    private int mMaxDR;
    private boolean mIsFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lora_setting);
        ButterKnife.bind(this);
        mUploadMode = getResources().getStringArray(R.array.upload_mode);
        mRegions = getResources().getStringArray(R.array.region);
        mMessageType = getResources().getStringArray(R.array.message_type);
        mModeList = new ArrayList<>();
        for (int i = 0, l = mUploadMode.length; i < l; i++) {
            mModeList.add(mUploadMode[i]);
        }
        mRegionsList = new ArrayList<>();
        for (int i = 0; i < mRegions.length; i++) {
            String name = mRegions[i];
            if ("US915HYBRID".equals(name) || "AU915OLD".equals(name)
                    || "CN470PREQUEL".equals(name) || "STE920".equals(name)) {
                continue;
            }
            Region region = new Region();
            region.value = i;
            region.name = name;
            mRegionsList.add(region);
        }
        mMessageTypeList = new ArrayList<>();
        for (int i = 0, l = mMessageType.length; i < l; i++) {
            mMessageTypeList.add(mMessageType[i]);
        }
        cbAdvanceSetting.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getLoraMode());
            orderTasks.add(OrderTaskAssembler.getLoraDevEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppKey());
            orderTasks.add(OrderTaskAssembler.getLoraDevAddr());
            orderTasks.add(OrderTaskAssembler.getLoraAppSKey());
            orderTasks.add(OrderTaskAssembler.getLoraNwkSKey());
            orderTasks.add(OrderTaskAssembler.getLoraRegion());
            orderTasks.add(OrderTaskAssembler.getLoraMessageType());
            orderTasks.add(OrderTaskAssembler.getLoraReportInterval());
            orderTasks.add(OrderTaskAssembler.getLoraCH());
            orderTasks.add(OrderTaskAssembler.getLoraDR());
            orderTasks.add(OrderTaskAssembler.getLoraADR());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;

                switch (orderType) {
                    case WRITE_CONFIG:
                        if (value.length >= 2) {
                            int header = value[0] & 0xFF;
                            int key = value[1] & 0xFF;
                            ConfigKeyEnum configKeyEnum = ConfigKeyEnum.fromConfigKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            if (header == 0xEF && configKeyEnum == ConfigKeyEnum.KEY_DEVICE_MAC) {
                                configKeyEnum = ConfigKeyEnum.KEY_LORA_CONNECT;
                            }
                            int length = value[2] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_LORA_MODE:
                                    if (header == 0xED && length > 0) {
                                        final int mode = value[3];
                                        tvUploadMode.setText(mUploadMode[mode - 1]);
                                        mSelectedMode = mode - 1;
                                        if (mode == 1) {
                                            llModemAbp.setVisibility(View.VISIBLE);
                                            llModemOtaa.setVisibility(View.GONE);
                                        } else {
                                            llModemAbp.setVisibility(View.GONE);
                                            llModemOtaa.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_DEV_EUI:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etDevEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_APP_EUI:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etAppEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_APP_KEY:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etAppKey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_DEV_ADDR:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etDevAddr.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_APP_SKEY:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etAppSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_NWK_SKEY:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        etNwkSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    break;
                                case KEY_LORA_REGION:
                                    if (header == 0xED && length > 0) {
                                        final int region = value[3] & 0xFF;
                                        mSelectedRegion = region;
                                        tvRegion.setText(mRegions[region]);
                                        initCHDRRange();
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_MESSAGE_TYPE:
                                    if (header == 0xED && length > 0) {
                                        final int messageType = value[3] & 0xFF;
                                        mSelectedMessageType = messageType;
                                        tvMessageType.setText(mMessageType[messageType]);
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_REPORT_INTERVAL:
                                    if (header == 0xED && length > 0) {
                                        final int reportInterval = value[3] & 0xFF;
                                        etReportInterval.setText(String.valueOf(reportInterval));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_CH:
                                    if (header == 0xED && length > 1) {
                                        final int ch1 = value[3] & 0xFF;
                                        final int ch2 = value[4] & 0xFF;
                                        mSelectedCh1 = ch1;
                                        mSelectedCh2 = ch2;
                                        tvCh1.setText(String.valueOf(ch1));
                                        tvCh2.setText(String.valueOf(ch2));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_DR:
                                    if (header == 0xED && length > 0) {
                                        final int dr1 = value[3] & 0xFF;
                                        mSelectedDr1 = dr1;
                                        tvDr1.setText(String.format("DR%d", dr1));
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_ADR:
                                    if (header == 0xED && length > 0) {
                                        final int adr = value[3] & 0xFF;
                                        cbAdr.setChecked(adr == 1);
                                    }
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    break;
                                case KEY_LORA_CONNECT:
                                    if (header == 0xEF && (value[3] & 0xff) != 1) {
                                        mIsFailed = true;
                                    }
                                    if (!mIsFailed) {
                                        ToastUtils.showToast(LoRaSettingActivity.this, "Success");
                                    } else {
                                        ToastUtils.showToast(LoRaSettingActivity.this, "Error");
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public void back(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backHome();
    }

    private void backHome() {
        setResult(RESULT_OK);
        finish();
    }

    public void selectMode(View view) {
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mModeList, mSelectedMode);
        bottomDialog.setListener(value -> {
            tvUploadMode.setText(mUploadMode[value]);
            mSelectedMode = value;
            if (value == 0) {
                llModemAbp.setVisibility(View.VISIBLE);
                llModemOtaa.setVisibility(View.GONE);
            } else {
                llModemAbp.setVisibility(View.GONE);
                llModemOtaa.setVisibility(View.VISIBLE);
            }

        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectRegion(View view) {
        RegionBottomDialog bottomDialog = new RegionBottomDialog();
        bottomDialog.setDatas(mRegionsList, mSelectedRegion);
        bottomDialog.setListener(value -> {
            if (mSelectedRegion != value) {
                mSelectedRegion = value;
                tvRegion.setText(mRegions[mSelectedRegion]);
                initCHDRRange();
                updateCHDR();
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectMessageType(View view) {
        {
            BottomDialog bottomDialog = new BottomDialog();
            bottomDialog.setDatas(mMessageTypeList, mSelectedMessageType);
            bottomDialog.setListener(value -> {
                tvMessageType.setText(mMessageType[value]);
                mSelectedMessageType = value;
            });
            bottomDialog.show(getSupportFragmentManager());
        }
    }

    private void updateCHDR() {
        switch (mSelectedRegion) {
            case 0:
            case 4:
            case 9:
            case 10:
                mSelectedCh1 = 0;
                mSelectedCh2 = 2;
                mSelectedDr1 = 0;
                break;
            case 5:
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
                mSelectedDr1 = 2;
                break;
            case 3:
                mSelectedCh1 = 0;
                mSelectedCh2 = 5;
                mSelectedDr1 = 0;
                break;
            case 1:
            case 7:
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
                mSelectedDr1 = 0;
                break;
            case 8:
                mSelectedCh1 = 0;
                mSelectedCh2 = 1;
                mSelectedDr1 = 2;
                break;
        }

        tvCh1.setText(String.valueOf(mSelectedCh1));
        tvCh2.setText(String.valueOf(mSelectedCh2));
        tvDr1.setText(String.format("DR%d", mSelectedDr1));
    }

    private ArrayList<String> mCHList;
    private ArrayList<String> mDRList;

    private void initCHDRRange() {
        mCHList = new ArrayList<>();
        mDRList = new ArrayList<>();
        switch (mSelectedRegion) {
            case 0:
            case 3:
            case 4:
            case 8:
            case 9:
            case 10:
                // EU868、CN779、EU443、AS923、KR920、IN865
                mMaxCH = 15;
                mMaxDR = 5;
                break;
            case 1:
                // US915
                mMaxCH = 63;
                mMaxDR = 4;
                break;
            case 5:
                // AU915
                mMaxCH = 63;
                mMaxDR = 6;
                break;
            case 7:
                // CN470
                mMaxCH = 95;
                mMaxDR = 5;
                break;
        }
        for (int i = 0; i <= mMaxCH; i++) {
            mCHList.add(i + "");
        }
        for (int i = 0; i <= mMaxDR; i++) {
            mDRList.add("DR" + i);
        }
    }

    public void selectCh1(View view) {
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mCHList, mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh1 = value;
            tvCh1.setText(mCHList.get(value));
            if (mSelectedCh1 > mSelectedCh2) {
                mSelectedCh2 = mSelectedCh1;
                tvCh2.setText(mCHList.get(value));
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectCh2(View view) {
        final ArrayList<String> ch2List = new ArrayList<>();
        for (int i = mSelectedCh1; i <= mMaxCH; i++) {
            ch2List.add(i + "");
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(ch2List, mSelectedCh2 - mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh2 = value + mSelectedCh1;
            tvCh2.setText(ch2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr1(View view) {
        if (cbAdr.isChecked()) {
            return;
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mDRList, mSelectedDr1);
        bottomDialog.setListener(value -> {
            mSelectedDr1 = value;
            tvDr1.setText(mDRList.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void onConnect(View view) {
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (mSelectedMode == 0) {
            String devEui = etDevEui.getText().toString();
            String appEui = etAppEui.getText().toString();
            String devAddr = etDevAddr.getText().toString();
            String appSkey = etAppSkey.getText().toString();
            String nwkSkey = etNwkSkey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (devAddr.length() != 8) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (appSkey.length() != 32) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (nwkSkey.length() != 32) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEui(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEui(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraDevAddr(devAddr));
            orderTasks.add(OrderTaskAssembler.setLoraAppSKey(appSkey));
            orderTasks.add(OrderTaskAssembler.setLoraNwkSKey(nwkSkey));
            orderTasks.add(OrderTaskAssembler.setLoraUploadMode(mSelectedMode + 1));
        } else {
            String devEui = etDevEui.getText().toString();
            String appEui = etAppEui.getText().toString();
            String appKey = etAppKey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (appKey.length() != 32) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEui(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEui(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppKey(appKey));
            orderTasks.add(OrderTaskAssembler.setLoraUploadMode(mSelectedMode + 1));
        }
        String reportInterval = etReportInterval.getText().toString();
        if (TextUtils.isEmpty(reportInterval)) {
            ToastUtils.showToast(this, "Reporting Interval is empty");
            return;
        }
        int intervalInt = Integer.parseInt(reportInterval);
        if (intervalInt < 1 || intervalInt > 60) {
            ToastUtils.showToast(this, "Reporting Interval range 1~60");
            return;
        }
        orderTasks.add(OrderTaskAssembler.setLoraUploadInterval(intervalInt));

        orderTasks.add(OrderTaskAssembler.setLoraMessageType(mSelectedMessageType));
        mIsFailed = false;
        // 保存并连接
        orderTasks.add(OrderTaskAssembler.setLoraRegion(mSelectedRegion));
        orderTasks.add(OrderTaskAssembler.setLoraCH(mSelectedCh1, mSelectedCh2));
        orderTasks.add(OrderTaskAssembler.setLoraDR(mSelectedDr1));
        orderTasks.add(OrderTaskAssembler.setLoraADR(cbAdr.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setLoraConnect());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        showSyncingProgressDialog();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        llAdvancedSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }
}
