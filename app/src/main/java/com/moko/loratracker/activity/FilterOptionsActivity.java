package com.moko.loratracker.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.loratracker.R;
import com.moko.loratracker.dialog.AlertMessageDialog;
import com.moko.loratracker.dialog.LoadingMessageDialog;
import com.moko.loratracker.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.DataTypeEnum;
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
import butterknife.OnClick;

public class FilterOptionsActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";
    @Bind(R.id.sb_rssi_filter)
    SeekBar sbRssiFilter;
    @Bind(R.id.tv_rssi_filter_value)
    TextView tvRssiFilterValue;
    @Bind(R.id.tv_rssi_filter_tips)
    TextView tvRssiFilterTips;
    @Bind(R.id.iv_mac_address)
    ImageView ivMacAddress;
    @Bind(R.id.et_mac_address)
    EditText etMacAddress;
    @Bind(R.id.iv_adv_name)
    ImageView ivAdvName;
    @Bind(R.id.et_adv_name)
    EditText etAdvName;
    @Bind(R.id.iv_ibeacon_major)
    ImageView ivIbeaconMajor;
    @Bind(R.id.iv_ibeacon_minor)
    ImageView ivIbeaconMinor;
    @Bind(R.id.iv_raw_adv_data)
    ImageView ivRawAdvData;
    @Bind(R.id.cl_adv_data_filter)
    ConstraintLayout clAdvDataFilter;
    @Bind(R.id.et_ibeacon_major_min)
    EditText etIbeaconMajorMin;
    @Bind(R.id.et_ibeacon_major_max)
    EditText etIbeaconMajorMax;
    @Bind(R.id.ll_ibeacon_major)
    LinearLayout llIbeaconMajor;
    @Bind(R.id.et_ibeacon_minor_min)
    EditText etIbeaconMinorMin;
    @Bind(R.id.et_ibeacon_minor_max)
    EditText etIbeaconMinorMax;
    @Bind(R.id.ll_ibeacon_minor)
    LinearLayout llIbeaconMinor;
    @Bind(R.id.iv_raw_data_del)
    ImageView ivRawDataDel;
    @Bind(R.id.iv_raw_data_add)
    ImageView ivRawDataAdd;
    @Bind(R.id.ll_raw_data_filter)
    LinearLayout llRawDataFilter;
    private boolean mReceiverTag = false;

    private boolean savedParamsError;


    private ArrayList<String> filterRawDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        sbRssiFilter.setOnSeekBarChangeListener(this);

        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }

            return null;
        };
        etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), inputFilter});
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
            orderTasks.add(OrderTaskAssembler.getFilterRssi());
            orderTasks.add(OrderTaskAssembler.getFilterMac());
            orderTasks.add(OrderTaskAssembler.getFilterName());
            orderTasks.add(OrderTaskAssembler.getFilterMajorRange());
            orderTasks.add(OrderTaskAssembler.getFilterMinorRange());
            orderTasks.add(OrderTaskAssembler.getFilterAdvRawData());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                setResult(RESULT_OK);
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
                            int length = value[2] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_RSSI:
                                    if (header == 0xED && length > 0) {
                                        final int rssi = value[3];
                                        int progress = rssi + 127;
                                        sbRssiFilter.setProgress(progress);
                                        tvRssiFilterValue.setText(String.format("%ddBm", rssi));
                                        tvRssiFilterTips.setText(getString(R.string.rssi_filter, rssi));
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_MAC:
                                    if (header == 0xED) {
                                        filterMacEnable = length > 0;
                                        ivMacAddress.setImageResource(filterMacEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        etMacAddress.setVisibility(filterMacEnable ? View.VISIBLE : View.GONE);
                                        if (filterMacEnable) {
                                            byte[] macBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            String filterMac = MokoUtils.bytesToHexString(macBytes).toUpperCase();
                                            etMacAddress.setText(filterMac);
                                        }
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_ADV_NAME:
                                    if (header == 0xED) {
                                        filterNameEnable = length > 0;
                                        ivAdvName.setImageResource(filterNameEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        etAdvName.setVisibility(filterNameEnable ? View.VISIBLE : View.GONE);
                                        if (filterNameEnable) {
                                            byte[] nameBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            String filterName = new String(nameBytes);
                                            etAdvName.setText(filterName);
                                        }
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_MAJOR_RANGE:
                                    if (header == 0xED) {
                                        filterMajorEnable = length > 0;
                                        ivIbeaconMajor.setImageResource(filterMajorEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        llIbeaconMajor.setVisibility(filterMajorEnable ? View.VISIBLE : View.GONE);
                                        if (filterMajorEnable) {
                                            byte[] majorMinBytes = Arrays.copyOfRange(value, 3, 5);
                                            int majorMin = MokoUtils.toInt(majorMinBytes);
                                            etIbeaconMajorMin.setText(String.valueOf(majorMin));
                                            byte[] majorMaxBytes = Arrays.copyOfRange(value, 5, 7);
                                            int majorMax = MokoUtils.toInt(majorMaxBytes);
                                            etIbeaconMajorMax.setText(String.valueOf(majorMax));
                                        }
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_MINOR_RANGE:
                                    if (header == 0xED) {
                                        filterMinorEnable = length > 0;
                                        ivIbeaconMinor.setImageResource(filterMinorEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        llIbeaconMinor.setVisibility(filterMinorEnable ? View.VISIBLE : View.GONE);
                                        if (length > 1) {
                                            byte[] minorMinBytes = Arrays.copyOfRange(value, 3, 5);
                                            int minorMin = MokoUtils.toInt(minorMinBytes);
                                            etIbeaconMinorMin.setText(String.valueOf(minorMin));
                                            byte[] minorMaxBytes = Arrays.copyOfRange(value, 5, 7);
                                            int minorMax = MokoUtils.toInt(minorMaxBytes);
                                            etIbeaconMinorMax.setText(String.valueOf(minorMax));
                                        }
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_ADV_RAW_DATA:
                                    if (header == 0xED) {
                                        filterRawAdvDataEnable = value != null && value.length > 5;
                                        ivRawAdvData.setImageResource(filterRawAdvDataEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        llRawDataFilter.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                                        ivRawDataAdd.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                                        ivRawDataDel.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                                        if (filterRawAdvDataEnable) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 3, 3 + length);
                                            for (int i = 0, l = rawDataBytes.length; i < l; ) {
                                                View v = LayoutInflater.from(FilterOptionsActivity.this).inflate(R.layout.item_raw_data_filter, llRawDataFilter, false);
                                                EditText etDataType = ButterKnife.findById(v, R.id.et_data_type);
                                                EditText etMin = ButterKnife.findById(v, R.id.et_min);
                                                EditText etMax = ButterKnife.findById(v, R.id.et_max);
                                                EditText etRawData = ButterKnife.findById(v, R.id.et_raw_data);
                                                int filterLength = rawDataBytes[i] & 0xFF;
                                                i++;
                                                String type = MokoUtils.byte2HexString(rawDataBytes[i]);
                                                i++;
                                                String min = String.valueOf((rawDataBytes[i] & 0xFF));
                                                i++;
                                                String max = String.valueOf((rawDataBytes[i] & 0xFF));
                                                i++;
                                                String data = MokoUtils.bytesToHexString(Arrays.copyOfRange(rawDataBytes, i, i + filterLength - 3));
                                                i += filterLength - 3;
                                                etDataType.setText(type);
                                                etMin.setText(min);
                                                etMax.setText(max);
                                                etRawData.setText(data);
                                                llRawDataFilter.addView(v);
                                            }
                                        }
                                        return;
                                    }
                                    if (header == 0xEF && (value[3] & 0xFF) != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(FilterOptionsActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        AlertMessageDialog dialog = new AlertMessageDialog();
                                        dialog.setMessage("Saved Successfully！");
                                        dialog.setConfirm("OK");
                                        dialog.setCancelGone();
                                        dialog.show(getSupportFragmentManager());
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
                            FilterOptionsActivity.this.setResult(RESULT_OK);
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

    private boolean filterMacEnable;
    private boolean filterNameEnable;
    private boolean filterMajorEnable;
    private boolean filterMinorEnable;
    private boolean filterRawAdvDataEnable;

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.iv_mac_address, R.id.iv_adv_name,
            R.id.iv_ibeacon_major, R.id.iv_ibeacon_minor, R.id.iv_raw_adv_data,
            R.id.iv_raw_data_add, R.id.iv_raw_data_del})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_save:
                if (isValid()) {
                    showSyncingProgressDialog();
                    saveParams();
                } else {
                    ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                }
                break;
            case R.id.iv_mac_address:
                filterMacEnable = !filterMacEnable;
                ivMacAddress.setImageResource(filterMacEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                etMacAddress.setVisibility(filterMacEnable ? View.VISIBLE : View.GONE);
                break;
            case R.id.iv_adv_name:
                filterNameEnable = !filterNameEnable;
                ivAdvName.setImageResource(filterNameEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                etAdvName.setVisibility(filterNameEnable ? View.VISIBLE : View.GONE);
                break;
            case R.id.iv_ibeacon_major:
                filterMajorEnable = !filterMajorEnable;
                ivIbeaconMajor.setImageResource(filterMajorEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                llIbeaconMajor.setVisibility(filterMajorEnable ? View.VISIBLE : View.GONE);
                break;
            case R.id.iv_ibeacon_minor:
                filterMinorEnable = !filterMinorEnable;
                ivIbeaconMinor.setImageResource(filterMinorEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                llIbeaconMinor.setVisibility(filterMinorEnable ? View.VISIBLE : View.GONE);
                break;
            case R.id.iv_raw_adv_data:
                filterRawAdvDataEnable = !filterRawAdvDataEnable;
                ivRawAdvData.setImageResource(filterRawAdvDataEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                llRawDataFilter.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                ivRawDataAdd.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                ivRawDataDel.setVisibility(filterRawAdvDataEnable ? View.VISIBLE : View.GONE);
                break;
            case R.id.iv_raw_data_add:
                int count = llRawDataFilter.getChildCount();
                if (count > 4) {
                    ToastUtils.showToast(this, "You can set up to 5 filters!");
                    return;
                }
                View v = LayoutInflater.from(this).inflate(R.layout.item_raw_data_filter, llRawDataFilter, false);
                llRawDataFilter.addView(v);
                break;
            case R.id.iv_raw_data_del:
                final int c = llRawDataFilter.getChildCount();
                if (c == 0) {
                    ToastUtils.showToast(this, "There are currently no filters to delete");
                    return;
                }
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning");
                dialog.setMessage("Please confirm whether to delete  a filter option，If yes，the last option will be deleted. ");
                dialog.setOnAlertConfirmListener(() -> {
                    if (c > 0) {
                        llRawDataFilter.removeViewAt(c - 1);
                    }
                });
                dialog.show(getSupportFragmentManager());
                break;
        }
    }

    private void saveParams() {
        final int progress = sbRssiFilter.getProgress();
        int filterRssi = progress - 127;
        List<OrderTask> orderTasks = new ArrayList<>();
        final String mac = etMacAddress.getText().toString();
        final String name = etAdvName.getText().toString();
        final String majorMin = etIbeaconMajorMin.getText().toString();
        final String majorMax = etIbeaconMajorMax.getText().toString();
        final String minorMin = etIbeaconMinorMin.getText().toString();
        final String minorMax = etIbeaconMinorMax.getText().toString();

        orderTasks.add(OrderTaskAssembler.setFilterRssi(filterRssi));
        orderTasks.add(OrderTaskAssembler.setFilterMac(filterMacEnable ? mac : ""));
        orderTasks.add(OrderTaskAssembler.setFilterName(filterNameEnable ? name : ""));
        orderTasks.add(OrderTaskAssembler.setFilterMajorRange(
                filterMajorEnable ? 1 : 0,
                filterMajorEnable ? Integer.parseInt(majorMin) : 0,
                filterMajorEnable ? Integer.parseInt(majorMax) : 0));
        orderTasks.add(OrderTaskAssembler.setFilterMinorRange(
                filterMinorEnable ? 1 : 0,
                filterMinorEnable ? Integer.parseInt(minorMin) : 0,
                filterMinorEnable ? Integer.parseInt(minorMax) : 0));
        orderTasks.add(OrderTaskAssembler.setFilterAdvRawData(filterRawAdvDataEnable ? filterRawDatas : null));

        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        final String mac = etMacAddress.getText().toString();
        final String name = etAdvName.getText().toString();
        final String majorMin = etIbeaconMajorMin.getText().toString();
        final String majorMax = etIbeaconMajorMax.getText().toString();
        final String minorMin = etIbeaconMinorMin.getText().toString();
        final String minorMax = etIbeaconMinorMax.getText().toString();
        if (filterMacEnable) {
            if (TextUtils.isEmpty(mac))
                return false;
            int length = mac.length();
            if (length % 2 != 0)
                return false;
        }
        if (filterNameEnable) {
            if (TextUtils.isEmpty(name))
                return false;
        }
        if (filterMajorEnable) {
            if (TextUtils.isEmpty(majorMin))
                return false;
            if (Integer.parseInt(majorMin) > 65535)
                return false;
            if (TextUtils.isEmpty(majorMax))
                return false;
            if (Integer.parseInt(majorMax) > 65535)
                return false;
            if (Integer.parseInt(majorMin) > Integer.parseInt(majorMax))
                return false;

        }
        if (filterMinorEnable) {
            if (TextUtils.isEmpty(minorMin))
                return false;
            if (Integer.parseInt(minorMin) > 65535)
                return false;
            if (TextUtils.isEmpty(minorMax))
                return false;
            if (Integer.parseInt(minorMax) > 65535)
                return false;
            if (Integer.parseInt(minorMin) > Integer.parseInt(minorMax))
                return false;
        }
        filterRawDatas = new ArrayList<>();
        if (filterRawAdvDataEnable) {
            // 发送设置的过滤RawData
            int count = llRawDataFilter.getChildCount();
            if (count == 0)
                return false;

            for (int i = 0; i < count; i++) {
                View v = llRawDataFilter.getChildAt(i);
                EditText etDataType = ButterKnife.findById(v, R.id.et_data_type);
                EditText etMin = ButterKnife.findById(v, R.id.et_min);
                EditText etMax = ButterKnife.findById(v, R.id.et_max);
                EditText etRawData = ButterKnife.findById(v, R.id.et_raw_data);
                final String dataTypeStr = etDataType.getText().toString();
                final String minStr = etMin.getText().toString();
                final String maxStr = etMax.getText().toString();
                final String rawDataStr = etRawData.getText().toString();

                if (TextUtils.isEmpty(dataTypeStr))
                    return false;

                final int dataType = Integer.parseInt(dataTypeStr, 16);
                final DataTypeEnum dataTypeEnum = DataTypeEnum.fromDataType(dataType);
                if (dataTypeEnum == null)
                    return false;
                if (TextUtils.isEmpty(rawDataStr))
                    return false;
                int length = rawDataStr.length();
                if (length % 2 != 0)
                    return false;
                int min = 0;
                if (!TextUtils.isEmpty(minStr))
                    min = Integer.parseInt(minStr);
                int max = 0;
                if (!TextUtils.isEmpty(maxStr))
                    max = Integer.parseInt(maxStr);
                if (min == 0 && max != 0)
                    return false;
                if (min > 29)
                    return false;
                if (max > 29)
                    return false;
                if (max < min)
                    return false;
                if (min > 0) {
                    int interval = max - min;
                    if (length != ((interval + 1) * 2))
                        return false;
                }
                int rawDataLength = 3 + length / 2;
                StringBuffer rawData = new StringBuffer();
                rawData.append(MokoUtils.int2HexString(rawDataLength));
                rawData.append(MokoUtils.int2HexString(dataType));
                rawData.append(MokoUtils.int2HexString(min));
                rawData.append(MokoUtils.int2HexString(max));
                rawData.append(rawDataStr);
                filterRawDatas.add(rawData.toString());
            }
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int rssi = progress - 127;
        tvRssiFilterValue.setText(String.format("%ddBm", rssi));
        tvRssiFilterTips.setText(getString(R.string.rssi_filter, rssi));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
