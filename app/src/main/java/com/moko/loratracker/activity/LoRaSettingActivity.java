package com.moko.loratracker.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.loratracker.R;
import com.moko.loratracker.dialog.AlertMessageDialog;
import com.moko.loratracker.dialog.BottomDialog;
import com.moko.loratracker.dialog.LoadingMessageDialog;
import com.moko.loratracker.dialog.RegionBottomDialog;
import com.moko.loratracker.entity.Region;
import com.moko.loratracker.service.MokoService;
import com.moko.loratracker.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
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

public class LoRaSettingActivity extends BaseActivity {


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
    @Bind(R.id.tv_dr_2)
    TextView tvDr2;
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


    private MokoService mMokoService;
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
    private int mSelectedDr2;
    private boolean mIsFailed;
    private boolean mReadCHDR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lora_setting);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
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
        EventBus.getDefault().register(this);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(300);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                MokoSupport.getInstance().enableBluetooth();
            } else {
                if (mMokoService == null) {
                    finish();
                    return;
                }
                showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(mMokoService.getLoraMode());
                orderTasks.add(mMokoService.getLoraDevEUI());
                orderTasks.add(mMokoService.getLoraAppEUI());
                orderTasks.add(mMokoService.getLoraAppKey());
                orderTasks.add(mMokoService.getLoraDevAddr());
                orderTasks.add(mMokoService.getLoraAppSKey());
                orderTasks.add(mMokoService.getLoraNwkSKey());
                orderTasks.add(mMokoService.getLoraRegion());
                orderTasks.add(mMokoService.getLoraMessageType());
                orderTasks.add(mMokoService.getLoraReportInterval());
                orderTasks.add(mMokoService.getLoraCH());
                orderTasks.add(mMokoService.getLoraDR());
                orderTasks.add(mMokoService.getLoraADR());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
            // 设备断开
            finish();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    if (mReadCHDR) {
                        mReadCHDR = false;
                        return;
                    }
                    dismissSyncProgressDialog();
                    if (!mIsFailed) {
                        ToastUtils.showToast(LoRaSettingActivity.this, "Success");
                    } else {
                        ToastUtils.showToast(LoRaSettingActivity.this, "Error");
                    }
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    abortBroadcast();
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    int responseType = response.responseType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case WRITE_CONFIG:
                            if (value.length >= 2) {
                                int key = value[1] & 0xFF;
                                ConfigKeyEnum configKeyEnum = ConfigKeyEnum.fromConfigKey(key);
                                if (configKeyEnum == null) {
                                    return;
                                }
                                int length = value[2] & 0xFF;
                                switch (configKeyEnum) {
                                    case GET_LORA_MODE:
                                        if (length > 0) {
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
                                        break;
                                    case GET_LORA_DEV_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etDevEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_APP_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etAppEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_APP_KEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etAppKey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_DEV_ADDR:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etDevAddr.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_APP_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etAppSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_NWK_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            etNwkSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case GET_LORA_REGION:
                                        if (length > 0) {
                                            final int region = value[3] & 0xFF;
                                            mSelectedRegion = region;
                                            tvRegion.setText(mRegions[region]);
                                        }
                                        break;
                                    case GET_LORA_MESSAGE_TYPE:
                                        if (length > 0) {
                                            final int messageType = value[3] & 0xFF;
                                            mSelectedMessageType = messageType;
                                            tvMessageType.setText(mMessageType[messageType]);
                                        }
                                        break;
                                    case GET_LORA_REPORT_INTERVAL:
                                        if (length > 0) {
                                            final int reportInterval = value[3] & 0xFF;
                                            etReportInterval.setText(String.valueOf(reportInterval));
                                        }
                                        break;
                                    case GET_LORA_CH:
                                        if (length > 1) {
                                            final int ch1 = value[3] & 0xFF;
                                            final int ch2 = value[4] & 0xFF;
                                            mSelectedCh1 = ch1;
                                            mSelectedCh2 = ch2;
                                            tvCh1.setText(String.valueOf(ch1));
                                            tvCh1.setText(String.valueOf(ch2));
                                        }
                                        break;
                                    case GET_LORA_DR:
                                        if (length > 1) {
                                            final int dr1 = value[3] & 0xFF;
                                            final int dr2 = value[4] & 0xFF;
                                            mSelectedDr1 = dr1;
                                            mSelectedDr2 = dr2;
                                            tvDr1.setText(String.format("DR%d", dr1));
                                            tvDr2.setText(String.format("DR%d", dr2));
                                        }
                                        break;
                                    case GET_LORA_ADR:
                                        if (length > 0) {
                                            final int adr = value[3] & 0xFF;
                                            cbAdr.setChecked(adr == 1);
                                        }
                                        break;
                                    case SET_LORA_DEV_ADDR:
                                    case SET_LORA_NWK_SKEY:
                                    case SET_LORA_APP_SKEY:
                                    case SET_LORA_DEV_EUI:
                                    case SET_LORA_APP_EUI:
                                    case SET_LORA_APP_KEY:
                                    case SET_LORA_MESSAGE_TYPE:
                                    case SET_LORA_MODE:
                                    case SET_LORA_REPORT_INTERVAL:
                                    case SET_LORA_CH:
                                    case SET_LORA_DR:
                                    case SET_LORA_ADR:
                                    case SET_LORA_CONNECT:
                                        if ((value[3] & 0xff) != 1) {
                                            mIsFailed = true;
                                        }
                                        break;
                                    case SET_LORA_REGION:
                                        if ((value[3] & 0xff) != 1) {
                                            mIsFailed = true;
                                        } else {
                                            if (mReadCHDR) {
                                                List<OrderTask> orderTasks = new ArrayList<>();
                                                orderTasks.add(mMokoService.getLoraCH());
                                                orderTasks.add(mMokoService.getLoraDR());
                                                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                                            }
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            LoRaSettingActivity.this.setResult(RESULT_OK);
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
        unbindService(mServiceConnection);
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
            mReadCHDR = true;
            mSelectedRegion = value;
            tvRegion.setText(mRegions[mSelectedRegion]);
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(mMokoService.setLoraRegion(mSelectedRegion));
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

    private static ArrayList<String> mCHList;
    private static ArrayList<String> mDRList;

    static {
        mCHList = new ArrayList<>();
        for (int i = 0; i <= 95; i++) {
            mCHList.add(i + "");
        }
        mDRList = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
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
        for (int i = mSelectedCh1; i <= 95; i++) {
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
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mDRList, mSelectedDr1);
        bottomDialog.setListener(value -> {
            mSelectedDr1 = value;
            tvDr1.setText(mDRList.get(value));
            if (mSelectedDr1 > mSelectedDr2) {
                mSelectedDr2 = mSelectedDr1;
                tvDr2.setText(mDRList.get(value));
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr2(View view) {
        final ArrayList<String> dr2List = new ArrayList<>();
        for (int i = mSelectedDr1; i <= 15; i++) {
            dr2List.add("DR" + i);
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(dr2List, mSelectedDr2 - mSelectedDr1);
        bottomDialog.setListener(value -> {
            mSelectedDr2 = value + mSelectedDr1;
            tvDr2.setText(dr2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void onConnect(View view) {
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (mSelectedMode == 0) {
            String devAddr = etDevAddr.getText().toString();
            String nwkSkey = etNwkSkey.getText().toString();
            String appSkey = etAppSkey.getText().toString();
            if (devAddr.length() != 8) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (nwkSkey.length() != 32) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            if (appSkey.length() != 32) {
                ToastUtils.showToast(this, "data length error");
                return;
            }
            orderTasks.add(mMokoService.setLoraDevAddr(devAddr));
            orderTasks.add(mMokoService.setLoraNwkSKey(nwkSkey));
            orderTasks.add(mMokoService.setLoraAppSKey(appSkey));
            orderTasks.add(mMokoService.setLoraUploadMode(mSelectedMode + 1));
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
            orderTasks.add(mMokoService.setLoraDevEui(devEui));
            orderTasks.add(mMokoService.setLoraAppEui(appEui));
            orderTasks.add(mMokoService.setLoraAppKey(appKey));
            orderTasks.add(mMokoService.setLoraUploadMode(mSelectedMode + 1));
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
        orderTasks.add(mMokoService.setLoraUploadInterval(intervalInt));

        orderTasks.add(mMokoService.setLoraMessageType(mSelectedMessageType));
        mIsFailed = false;
        // 保存并连接
        orderTasks.add(mMokoService.setLoraRegion(mSelectedRegion));
        orderTasks.add(mMokoService.setLoraCH(mSelectedCh1, mSelectedCh2));
        orderTasks.add(mMokoService.setLoraDR(mSelectedDr1, mSelectedDr2));
        orderTasks.add(mMokoService.setLoraADR(cbAdr.isChecked() ? 1 : 0));
        orderTasks.add(mMokoService.setLoraConnect());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        showSyncingProgressDialog();
    }
}
