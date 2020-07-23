package com.moko.support.task;

import android.support.annotation.IntRange;
import android.text.TextUtils;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

import java.util.Calendar;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.WriteConfigTask
 */
public class WriteConfigTask extends OrderTask {
    public byte[] data;

    public WriteConfigTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE_CONFIG, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ConfigKeyEnum key) {
        switch (key) {
            case GET_ADV_NAME:
            case GET_IBEACON_UUID:
            case GET_IBEACON_MAJOR:
            case GET_IBEACON_MINOR:
            case GET_ADV_INTERVAL:
            case GET_MEASURE_POWER:
            case GET_TRANSMISSION:
            case GET_SCAN_INTERVAL:
            case GET_ALARM_NOTIFY:
            case GET_ALARM_RSSI:
            case GET_SCAN_WINDOW:
            case GET_CONNECTABLE:
            case GET_BATTERY:

            case GET_DEVICE_MAC:
            case GET_FILTER_RSSI:
            case GET_FILTER_MAC:
            case GET_FILTER_ADV_NAME:
            case GET_FILTER_MAJOR_RANGE:
            case GET_FILTER_MINOR_RANGE:
                createGetConfigData(key.getConfigKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) 0xED, (byte) configKey, (byte) 0xED};
    }

    public void setFilterRssi(@IntRange(from = -127, to = 0) int rssi) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_FILTER_RSSI.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) rssi;
    }

    public void setScanInterval(@IntRange(from = 1, to = 600) int seconds) {
        byte[] intervalBytes = MokoUtils.toByteArray(seconds, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_SCAN_INTERVAL.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = intervalBytes[0];
        data[4] = intervalBytes[1];
    }

    public void setAlarmNotify(@IntRange(from = 0, to = 3) int notify) {
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_ALARM_NOTIFY.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) notify;
    }

    public void setAlarmTirggerRssi(@IntRange(from = -127, to = 0) int rssi) {
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_ALARM_RSSI.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) rssi;
    }

    public void setTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] yearBytes = MokoUtils.toByteArray(year, 2);
        data = new byte[10];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_TIME.getConfigKey();
        data[2] = (byte) 0x07;
        data[3] = yearBytes[0];
        data[4] = yearBytes[1];
        data[5] = (byte) month;
        data[6] = (byte) date;
        data[7] = (byte) hour;
        data[8] = (byte) minute;
        data[9] = (byte) second;
    }

    public void setFilterMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            data = new byte[3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MAC.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] macBytes = MokoUtils.hex2bytes(mac);
            int length = macBytes.length;
            data = new byte[3 + length];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MAC.getConfigKey();
            data[2] = (byte) length;
            for (int i = 0; i < macBytes.length; i++) {
                data[3 + i] = macBytes[i];
            }
        }
    }

    public void setFilterName(String name) {
        if (TextUtils.isEmpty(name)) {
            data = new byte[3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_ADV_NAME.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] nameBytes = name.getBytes();
            int length = nameBytes.length;
            data = new byte[3 + length];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_ADV_NAME.getConfigKey();
            data[2] = (byte) length;
            for (int i = 0; i < nameBytes.length; i++) {
                data[3 + i] = nameBytes[i];
            }
        }
    }

    public void setFilterMajorRange(@IntRange(from = 0, to = 1) int enable,
                                    @IntRange(from = 0, to = 65535) int majorMin,
                                    @IntRange(from = 0, to = 65535) int majorMax) {
        if (enable == 0) {
            data = new byte[3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MAJOR_RANGE.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] majorMinBytes = MokoUtils.toByteArray(majorMin, 2);
            byte[] majorMaxBytes = MokoUtils.toByteArray(majorMax, 2);
            data = new byte[7];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MAJOR_RANGE.getConfigKey();
            data[2] = (byte) 0x04;
            data[3] = majorMinBytes[0];
            data[4] = majorMinBytes[1];
            data[5] = majorMaxBytes[0];
            data[6] = majorMaxBytes[1];
        }
    }

    public void setFilterMinorRange(@IntRange(from = 0, to = 1) int enable,
                                    @IntRange(from = 0, to = 65535) int minorMin,
                                    @IntRange(from = 0, to = 65535) int minorMax) {
        if (enable == 0) {
            data = new byte[3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MINOR_RANGE.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] minorMinBytes = MokoUtils.toByteArray(minorMin, 2);
            byte[] minorMaxBytes = MokoUtils.toByteArray(minorMax, 2);
            data = new byte[8];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.SET_FILTER_MINOR_RANGE.getConfigKey();
            data[2] = (byte) 0x04;
            data[3] = minorMinBytes[0];
            data[4] = minorMinBytes[1];
            data[5] = minorMaxBytes[0];
            data[6] = minorMaxBytes[1];
        }
    }

    public void setAdvName(String advName) {
        byte[] advNameBytes = advName.getBytes();
        int length = advNameBytes.length;
        data = new byte[length + 3];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_ADV_NAME.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < advNameBytes.length; i++) {
            data[i + 3] = advNameBytes[i];
        }
    }

    public void setUUID(String uuid) {
        byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
        data = new byte[19];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_IBEACON_UUID.getConfigKey();
        data[2] = (byte) 0x10;
        for (int i = 0; i < uuidBytes.length; i++) {
            data[i + 3] = uuidBytes[i];
        }
    }

    public void setMajor(int major) {
        byte[] majorBytes = MokoUtils.toByteArray(major, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_IBEACON_MAJOR.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = majorBytes[0];
        data[4] = majorBytes[1];
    }

    public void setMinor(int minor) {
        byte[] minorBytes = MokoUtils.toByteArray(minor, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_IBEACON_MINOR.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = minorBytes[0];
        data[4] = minorBytes[1];
    }

    public void setAdvInterval(int advInterval) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_ADV_INTERVAL.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) advInterval;
    }

    public void setTransmission(int transmission) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_TRANSMISSION.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) transmission;
    }

    public void setMeasurePower(int measurePower) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_MEASURE_POWER.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) measurePower;
    }

    public void setConnectable(int connectionMode) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_CONNECTABLE.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) connectionMode;
    }

    public void changePassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[length + 3];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_PASSWORD.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < passwordBytes.length; i++) {
            data[i + 3] = passwordBytes[i];
        }
    }

    public void reset() {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_RESET.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) 0x01;
    }


    public void setScanWinow(int scannerState, int startTime) {
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.SET_SCAN_WINDOW.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = (byte) scannerState;
        data[4] = (byte) startTime;
    }
}
