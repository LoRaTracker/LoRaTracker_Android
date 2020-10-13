package com.moko.loratracker.activity;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moko.loratracker.AppConstants;
import com.moko.loratracker.R;
import com.moko.loratracker.dialog.AlertMessageDialog;
import com.moko.loratracker.dialog.LoadingMessageDialog;
import com.moko.loratracker.fragment.AdvFragment;
import com.moko.loratracker.fragment.DeviceFragment;
import com.moko.loratracker.fragment.ScannerFragment;
import com.moko.loratracker.fragment.SettingFragment;
import com.moko.loratracker.service.DfuService;
import com.moko.loratracker.utils.FileUtils;
import com.moko.loratracker.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @Bind(R.id.frame_container)
    FrameLayout frameContainer;
    @Bind(R.id.radioBtn_adv)
    RadioButton radioBtnAdv;
    @Bind(R.id.radioBtn_scanner)
    RadioButton radioBtnScanner;
    @Bind(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @Bind(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @Bind(R.id.rg_options)
    RadioGroup rgOptions;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_save)
    ImageView ivSave;
    private FragmentManager fragmentManager;
    private AdvFragment advFragment;
    private ScannerFragment scannerFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mReceiverTag = false;
    private int disConnectType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        fragmentManager = getFragmentManager();
        initFragment();
        radioBtnAdv.setChecked(true);
        tvTitle.setText(R.string.title_advertiser);
        rgOptions.setOnCheckedChangeListener(this);
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
            // sync time after connect success;
            orderTasks.add(OrderTaskAssembler.setTime());
            // get adv params
            orderTasks.add(OrderTaskAssembler.getAdvName());
            orderTasks.add(OrderTaskAssembler.getiBeaconUUID());
            orderTasks.add(OrderTaskAssembler.getiBeaconMajor());
            orderTasks.add(OrderTaskAssembler.getIBeaconMinor());
            orderTasks.add(OrderTaskAssembler.getAdvInterval());
            orderTasks.add(OrderTaskAssembler.getTransmission());
            orderTasks.add(OrderTaskAssembler.getMeasurePower());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    private void initFragment() {
        advFragment = AdvFragment.newInstance();
        scannerFragment = ScannerFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, advFragment)
                .add(R.id.frame_container, scannerFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(advFragment)
                .hide(scannerFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                showDisconnectDialog();
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case DISCONNECTED_NOTIFY:
                        int type = value[1] & 0xFF;
                        disConnectType = type;
                        if (type == 1) {
                            // valid password timeout
                        } else if (type == 2) {
                            // change password success
                        } else if (type == 3) {
                            // reset success
                        } else if (type == 4) {
                            // no data exchange timeout
                        } else if (type == 5) {
                            // close device
                        }
                        break;
                }
            }
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
                    case DEVICE_MODEL:
                        String productModel = new String(value);
                        deviceFragment.setProductModel(productModel);
                        break;
                    case SOFTWARE_VERSION:
                        String softwareVersion = new String(value);
                        deviceFragment.setSoftwareVersion(softwareVersion);
                        break;
                    case FIRMWARE_VERSION:
                        String firmwareVersion = new String(value);
                        deviceFragment.setFirmwareVersion(firmwareVersion);
                        break;
                    case HARDWARE_VERSION:
                        String hardwareVersion = new String(value);
                        deviceFragment.setHardwareVersion(hardwareVersion);
                        break;
                    case MANUFACTURER:
                        String manufacture = new String(value);
                        deviceFragment.setManufacture(manufacture);
                        break;
                    case WRITE_CONFIG:
                        if (value.length >= 2) {
                            int header = value[0] & 0xFF;
                            int key = value[1] & 0xFF;
                            ConfigKeyEnum configKeyEnum = ConfigKeyEnum.fromConfigKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[2] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final String deviceName = new String(rawDataBytes);
                                        mDeviceName = deviceName;
                                        advFragment.setDeviceName(deviceName);
                                    }
                                    break;
                                case KEY_IBEACON_UUID:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final String uuid = MokoUtils.bytesToHexString(rawDataBytes);
                                        advFragment.setUUID(uuid);
                                    }
                                    break;
                                case KEY_IBEACON_MAJOR:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final int major = MokoUtils.toInt(rawDataBytes);
                                        advFragment.setMajor(major);
                                    }
                                    break;
                                case KEY_IBEACON_MINOR:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final int minor = MokoUtils.toInt(rawDataBytes);
                                        advFragment.setMinor(minor);
                                    }
                                    break;
                                case KEY_ADV_INTERVAL:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final int advInterval = MokoUtils.toInt(rawDataBytes);
                                        advFragment.setAdvInterval(advInterval);
                                    }
                                    break;
                                case KEY_MEASURE_POWER:
                                    if (header == 0xED && length > 0) {
                                        int rssi_1m = value[3];
                                        advFragment.setMeasurePower(rssi_1m);
                                    }
                                    break;
                                case KEY_TRANSMISSION:
                                    if (header == 0xED && length > 0) {
                                        int txPower = value[3];
                                        advFragment.setTransmission(txPower);
                                    }
                                    if (header == 0xEF && length > 0) {
                                        int result = value[3];
                                        if (result == 0)
                                            return;
                                        AlertMessageDialog dialog = new AlertMessageDialog();
                                        dialog.setMessage("Saved Successfully！");
                                        dialog.setConfirm("OK");
                                        dialog.setCancelGone();
                                        dialog.show(getSupportFragmentManager());
                                    }
                                    break;
                                case KEY_SCAN_INTERVAL:
                                    if (header == 0xED && length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        final int scannInterval = MokoUtils.toInt(rawDataBytes);
                                        scannerFragment.setScanInterval(scannInterval);
                                    }
                                    break;
                                case KEY_ALARM_NOTIFY:
                                    if (header == 0xED && length > 0) {
                                        int notify = value[3] & 0xFF;
                                        scannerFragment.setAlarmNotify(notify);
                                    }
                                    break;
                                case KEY_ALARM_RSSI:
                                    if (header == 0xED && length > 0) {
                                        int rssi = value[3];
                                        scannerFragment.setAlarmTriggerRssi(rssi);
                                    }
                                    if (header == 0xEF && length > 0) {
                                        int result = value[3];
                                        if (result == 0)
                                            return;
                                        AlertMessageDialog dialog = new AlertMessageDialog();
                                        dialog.setMessage("Saved Successfully！");
                                        dialog.setConfirm("OK");
                                        dialog.setCancelGone();
                                        dialog.show(getSupportFragmentManager());
                                    }
                                    break;
                                case KEY_LORA_CONNECTABLE:
                                    if (header == 0xED && length > 0) {
                                        int connectable = value[3];
                                        settingFragment.setLoRaConnectable(connectable);
                                    }
                                    break;
                                case KEY_SCAN_WINDOW:
                                    if (header == 0xED && length > 0) {
                                        int scannerState = value[3] & 0xFF;
                                        int startTime = value[4] & 0xFF;
                                        settingFragment.setScanWindow(scannerState, startTime);
                                    }
                                    break;
                                case KEY_CONNECTABLE:
                                    if (header == 0xED && length > 0) {
                                        int connectable = value[3] & 0xFF;
                                        settingFragment.setConnectable(connectable);
                                    }
                                    break;
                                case KEY_DEVICE_MAC:
                                    if (header == 0xED && length > 0) {
                                        byte[] macBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        StringBuffer stringBuffer = new StringBuffer();
                                        for (int i = 0, l = macBytes.length; i < l; i++) {
                                            stringBuffer.append(MokoUtils.byte2HexString(macBytes[i]));
                                            if (i < (l - 1))
                                                stringBuffer.append(":");
                                        }
                                        mDeviceMac = stringBuffer.toString();
                                        deviceFragment.setMacAddress(stringBuffer.toString());
                                    }
                                    break;
                                case KEY_BATTERY:
                                    if (header == 0xED && length > 0) {
                                        int battery = value[3] & 0xFF;
                                        deviceFragment.setBatteryValtage(battery);
                                    }
                                    break;
                                case KEY_VIBRATION_INTENSITY:
                                    if (header == 0xED && length > 0) {
                                        int intansity = value[3] & 0xFF;
                                        scannerFragment.setVibrationIntansity(intansity);
                                    }
                                    break;
                                case KEY_VIBRATION_DURATION:
                                    if (header == 0xED && length > 0) {
                                        int duration = value[3] & 0xFF;
                                        scannerFragment.setVibrationDuration(duration);
                                    }
                                    break;
                                case KEY_VIBRATION_CYCLE:
                                    if (header == 0xED && length > 0) {
                                        byte[] cycleBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                        int cycle = MokoUtils.toInt(cycleBytes);
                                        scannerFragment.setVibrationCycle(cycle);
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        });
    }

    private void showDisconnectDialog() {
        if (disConnectType == 2) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Change Password");
            dialog.setMessage("Password changed successfully!Please reconnect the device.");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                setResult(RESULT_OK);
                finish();
            });
            dialog.show(getSupportFragmentManager());
        } else if (disConnectType == 3) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Factory Reset");
            dialog.setMessage("Factory reset successfully!Please reconnect the device.");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                setResult(RESULT_OK);
                finish();
            });
            dialog.show(getSupportFragmentManager());
        } else if (disConnectType == 4) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage("No data communication for 2 minutes, the device is disconnected.");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                setResult(RESULT_OK);
                finish();
            });
            dialog.show(getSupportFragmentManager());
        } else if (disConnectType == 1) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage("The Beacon is disconnected.");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                setResult(RESULT_OK);
                finish();
            });
            dialog.show(getSupportFragmentManager());
        } else {
            if (MokoSupport.getInstance().isBluetoothOpen() && !isUpgrade) {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Dismiss");
                dialog.setMessage("The Beacon disconnected!");
                dialog.setConfirm("Exit");
                dialog.setCancelGone();
                dialog.setOnAlertConfirmListener(() -> {
                    setResult(RESULT_OK);
                    finish();
                });
                dialog.show(getSupportFragmentManager());
            }
        }
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
                            builder.setTitle("Dismiss");
                            builder.setCancelable(false);
                            builder.setMessage("The current system of bluetooth is not available!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DeviceInfoActivity.this.setResult(RESULT_OK);
                                    finish();
                                }
                            });
                            builder.show();
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath))
                    return;
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuService.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == AppConstants.REQUEST_CODE_LORA_SETTING) {
            if (resultCode == RESULT_OK) {
                ivSave.postDelayed(() -> {
                    showSyncingProgressDialog();
                    List<OrderTask> orderTasks = new ArrayList<>();
                    // setting
                    orderTasks.add(OrderTaskAssembler.getLoRaConnectable());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }, 500);
            }
        }
    }

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

    @OnClick({R.id.tv_back, R.id.iv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.iv_save:
                if (radioBtnAdv.isChecked()) {
                    if (advFragment.isValid()) {
                        showSyncingProgressDialog();
                        advFragment.saveParams();
                    } else {
                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                    }
                }
                if (radioBtnScanner.isChecked()) {
                    if (scannerFragment.isValid()) {
                        if (scannerFragment.isDurationLessThanCycle()) {
                            showSyncingProgressDialog();
                            scannerFragment.saveParams();
                        }
                    } else {
                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                    }
                }
                break;
        }
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
//        mIsClose = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_adv:
                showAdvAndGetData();
                break;
            case R.id.radioBtn_scanner:
                showScannerAndGetData();
                break;
            case R.id.radioBtn_setting:
                showSettingAndGetData();
                break;
            case R.id.radioBtn_device:
                showDeviceAndGetData();
                break;
        }
    }

    private void showDeviceAndGetData() {
        tvTitle.setText(R.string.title_device);
        ivSave.setVisibility(View.GONE);
        fragmentManager.beginTransaction()
                .hide(advFragment)
                .hide(scannerFragment)
                .hide(settingFragment)
                .show(deviceFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // device
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getMacAddress());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showSettingAndGetData() {
        tvTitle.setText(R.string.title_setting);
        ivSave.setVisibility(View.GONE);
        fragmentManager.beginTransaction()
                .hide(advFragment)
                .hide(scannerFragment)
                .show(settingFragment)
                .hide(deviceFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // setting
        orderTasks.add(OrderTaskAssembler.getLoRaConnectable());
        orderTasks.add(OrderTaskAssembler.getScanWindow());
        orderTasks.add(OrderTaskAssembler.getConnectable());
        orderTasks.add(OrderTaskAssembler.getMacAddress());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showScannerAndGetData() {
        tvTitle.setText(R.string.title_scanner);
        ivSave.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction()
                .hide(advFragment)
                .show(scannerFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // scanner
        orderTasks.add(OrderTaskAssembler.getScanInterval());
        orderTasks.add(OrderTaskAssembler.getAlarmNotify());
        orderTasks.add(OrderTaskAssembler.getAlarmRssi());
        orderTasks.add(OrderTaskAssembler.getVibrationIntansity());
        orderTasks.add(OrderTaskAssembler.getVibrationCycle());
        orderTasks.add(OrderTaskAssembler.getVibrationDuration());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showAdvAndGetData() {
        tvTitle.setText(R.string.title_advertiser);
        ivSave.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction()
                .show(advFragment)
                .hide(scannerFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // get adv params
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getiBeaconUUID());
        orderTasks.add(OrderTaskAssembler.getiBeaconMajor());
        orderTasks.add(OrderTaskAssembler.getIBeaconMinor());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        orderTasks.add(OrderTaskAssembler.getTransmission());
        orderTasks.add(OrderTaskAssembler.getMeasurePower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    public void changePassword(String password) {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.changePassword(password));
    }

    public void reset() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setReset());
    }

    public void setScanWindow(int scannerState, int startTime) {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setScanWindow(scannerState, startTime), OrderTaskAssembler.getScanWindow());
    }

    public void changeConnectState(int connectState) {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setConnectionMode(connectState), OrderTaskAssembler.getConnectable());
    }

    public void powerOff() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.closePower());
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
        builder.setTitle("Dismiss");
        builder.setCancelable(false);
        builder.setMessage("The device disconnected!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isUpgrade = false;
                DeviceInfoActivity.this.setResult(RESULT_OK);
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrade;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            LogModule.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                Toast.makeText(DeviceInfoActivity.this, "Error:DFU Failed", Toast.LENGTH_SHORT).show();
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }

        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            LogModule.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            isUpgrade = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            ToastUtils.showToast(DeviceInfoActivity.this, "DFU Successfully!");
            dismissDFUProgressDialog();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            mDFUDialog.setMessage("Progress:" + percent + "%");
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            ToastUtils.showToast(DeviceInfoActivity.this, "Opps!DFU Failed. Please try again!");
            LogModule.i("Error:" + message);
            dismissDFUProgressDialog();
        }
    };
}
