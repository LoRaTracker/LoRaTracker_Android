package com.moko.loratracker.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.loratracker.R;
import com.moko.loratracker.activity.DeviceInfoActivity;
import com.moko.loratracker.activity.FilterOptionsActivity;
import com.moko.loratracker.dialog.AlertMessageDialog;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.task.OrderTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class ScannerFragment extends Fragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    @Bind(R.id.sb_scan_interval)
    SeekBar sbScanInterval;
    @Bind(R.id.tv_scan_interval_value)
    TextView tvScanIntervalValue;
    @Bind(R.id.tv_scan_interval_tips)
    TextView tvScanIntervalTips;
    @Bind(R.id.npv_alarm_notify)
    NumberPickerView npvAlarmNotify;
    @Bind(R.id.sb_alarm_trigger_rssi)
    SeekBar sbAlarmTriggerRssi;
    @Bind(R.id.tv_alarm_trigger_rssi_value)
    TextView tvAlarmTriggerRssiValue;
    @Bind(R.id.tv_alarm_trigger_rssi_tips)
    TextView tvAlarmTriggerRssiTips;
    @Bind(R.id.et_vibration_cycle)
    EditText etVibrationCycle;
    @Bind(R.id.et_vibration_duration)
    EditText etVibrationDuration;
    @Bind(R.id.npv_vibration_intensity)
    NumberPickerView npvVibrationIntensity;

    private DeviceInfoActivity activity;

    public ScannerFragment() {
    }


    public static ScannerFragment newInstance() {
        ScannerFragment fragment = new ScannerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        sbScanInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvScanIntervalValue.setText(String.format("%ds", progress));
                tvScanIntervalTips.setText(getString(R.string.storage_interval, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbAlarmTriggerRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress - 127;
                tvAlarmTriggerRssiValue.setText(String.format("%dBm", value));
                tvAlarmTriggerRssiTips.setText(getString(R.string.alarm_trigger_rssi, value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        npvAlarmNotify.setDisplayedValues(getResources().getStringArray(R.array.tracking_notify));
        npvAlarmNotify.setMaxValue(3);
        npvAlarmNotify.setMinValue(0);
        npvAlarmNotify.setValue(0);

        npvVibrationIntensity.setDisplayedValues(getResources().getStringArray(R.array.vibration_intensity));
        npvVibrationIntensity.setMaxValue(2);
        npvVibrationIntensity.setMinValue(0);
        npvVibrationIntensity.setValue(0);
        return view;
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }


    @OnClick({R.id.tv_filter_options})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_filter_options:
                startActivity(new Intent(getActivity(), FilterOptionsActivity.class));
                break;
        }
    }

    public boolean isValid() {
        final String durationStr = etVibrationDuration.getText().toString();
        final String cycleStr = etVibrationCycle.getText().toString();
        if (TextUtils.isEmpty(durationStr))
            return false;
        if (TextUtils.isEmpty(cycleStr))
            return false;
        int duration = Integer.parseInt(durationStr);
        if (duration > 10)
            return false;
        if (TextUtils.isEmpty(cycleStr))
            return false;
        int cycle = Integer.parseInt(cycleStr);
        if (cycle < 1 || cycle > 600)
            return false;
        return true;
    }

    public boolean isDurationLessThanCycle() {
        final String durationStr = etVibrationDuration.getText().toString();
        final String cycleStr = etVibrationCycle.getText().toString();
        int duration = Integer.parseInt(durationStr);
        int cycle = Integer.parseInt(cycleStr);
        if (duration > cycle) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setCancelGone();
            dialog.setMessage("Vibration Cycle should be no less than Duration of  Vibration");
            dialog.setConfirm("OK");
            dialog.show(activity.getSupportFragmentManager());
            return false;
        }
        return true;
    }

    public void saveParams() {
        final int scanIntervalProgress = sbScanInterval.getProgress();
        final int alarmNotify = npvAlarmNotify.getValue();
        final int alarmTriggerRssiProgress = sbAlarmTriggerRssi.getProgress();
        final int intensityValue = npvVibrationIntensity.getValue();
        int intensity = 0;
        switch (intensityValue) {
            case 0:
                intensity = 10;
                break;
            case 1:
                intensity = 50;
                break;
            case 2:
                intensity = 100;
                break;
        }
        final String durationStr = etVibrationDuration.getText().toString();
        final String cycleStr = etVibrationCycle.getText().toString();
        int duration = Integer.parseInt(durationStr);
        int cycle = Integer.parseInt(cycleStr);
        int rssi = alarmTriggerRssiProgress - 127;
        List<OrderTask> orderTasks = new ArrayList<>();

        orderTasks.add(OrderTaskAssembler.setScanInterval(scanIntervalProgress));
        orderTasks.add(OrderTaskAssembler.setAlarmNotify(alarmNotify));
        orderTasks.add(OrderTaskAssembler.setVibrationIntensity(intensity));
        orderTasks.add(OrderTaskAssembler.setVibrationDuration(duration));
        orderTasks.add(OrderTaskAssembler.setVibrationCycle(cycle));
        orderTasks.add(OrderTaskAssembler.setAlarmTriggerRssi(rssi));

        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setScanInterval(int time) {
        if (time <= 600)
            sbScanInterval.setProgress(time);
    }

    public void setVibrationIntansity(int intansity) {
        switch (intansity) {
            case 10:
                npvVibrationIntensity.setValue(0);
                break;
            case 50:
                npvVibrationIntensity.setValue(1);
                break;
            case 100:
                npvVibrationIntensity.setValue(2);
                break;
        }
    }

    public void setAlarmTriggerRssi(int rssi) {
        int progress = rssi + 127;
        if (progress >= 0 && rssi <= 127) {
            sbAlarmTriggerRssi.setProgress(progress);
            int value = progress - 127;
            tvAlarmTriggerRssiValue.setText(String.format("%dBm", value));
            tvAlarmTriggerRssiTips.setText(getString(R.string.alarm_trigger_rssi, value));
        }
    }

    public void setAlarmNotify(int alarmNotify) {
        if (alarmNotify <= 3)
            npvAlarmNotify.setValue(alarmNotify);
    }

    public void setVibrationDuration(int duration) {
        etVibrationDuration.setText(String.valueOf(duration));
    }

    public void setVibrationCycle(int cycle) {
        if (cycle >= 1 && cycle <= 600) {
            etVibrationCycle.setText(String.valueOf(cycle));
        }
    }
}
