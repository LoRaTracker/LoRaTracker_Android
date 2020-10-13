package com.moko.support.task;

import android.support.annotation.IntRange;
import android.text.TextUtils;

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

    public WriteConfigTask() {
        super(OrderType.WRITE_CONFIG, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ConfigKeyEnum key) {
        switch (key) {
            case KEY_ADV_NAME:
            case KEY_IBEACON_UUID:
            case KEY_IBEACON_MAJOR:
            case KEY_IBEACON_MINOR:
            case KEY_ADV_INTERVAL:
            case KEY_MEASURE_POWER:
            case KEY_TRANSMISSION:
            case KEY_SCAN_INTERVAL:
            case KEY_ALARM_NOTIFY:
            case KEY_ALARM_RSSI:
            case KEY_LORA_CONNECTABLE:
            case KEY_SCAN_WINDOW:
            case KEY_CONNECTABLE:
            case KEY_BATTERY:

            case KEY_DEVICE_MAC:
            case KEY_FILTER_RSSI:
            case KEY_FILTER_MAC:
            case KEY_FILTER_ADV_NAME:
            case KEY_FILTER_MAJOR_RANGE:
            case KEY_FILTER_MINOR_RANGE:

            case KEY_LORA_MODE:
            case KEY_LORA_DEV_EUI:
            case KEY_LORA_APP_EUI:
            case KEY_LORA_APP_KEY:
            case KEY_LORA_DEV_ADDR:
            case KEY_LORA_APP_SKEY:
            case KEY_LORA_NWK_SKEY:
            case KEY_LORA_REGION:
            case KEY_LORA_REPORT_INTERVAL:
            case KEY_LORA_MESSAGE_TYPE:
            case KEY_LORA_CH:
            case KEY_LORA_DR:
            case KEY_LORA_ADR:
            case KEY_VIBRATION_INTENSITY:
            case KEY_VIBRATION_CYCLE:
            case KEY_VIBRATION_DURATION:
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
        data[1] = (byte) ConfigKeyEnum.KEY_FILTER_RSSI.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) rssi;
    }

    public void setScanInterval(@IntRange(from = 1, to = 600) int seconds) {
        byte[] intervalBytes = MokoUtils.toByteArray(seconds, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_SCAN_INTERVAL.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = intervalBytes[0];
        data[4] = intervalBytes[1];
    }

    public void setAlarmNotify(@IntRange(from = 0, to = 3) int notify) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_ALARM_NOTIFY.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) notify;
    }

    public void setAlarmTirggerRssi(@IntRange(from = -127, to = 0) int rssi) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_ALARM_RSSI.getConfigKey();
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
        data[1] = (byte) ConfigKeyEnum.KEY_TIME.getConfigKey();
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
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MAC.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] macBytes = MokoUtils.hex2bytes(mac);
            int length = macBytes.length;
            data = new byte[3 + length];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MAC.getConfigKey();
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
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_ADV_NAME.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] nameBytes = name.getBytes();
            int length = nameBytes.length;
            data = new byte[3 + length];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_ADV_NAME.getConfigKey();
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
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MAJOR_RANGE.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] majorMinBytes = MokoUtils.toByteArray(majorMin, 2);
            byte[] majorMaxBytes = MokoUtils.toByteArray(majorMax, 2);
            data = new byte[7];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MAJOR_RANGE.getConfigKey();
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
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MINOR_RANGE.getConfigKey();
            data[2] = (byte) 0x00;
        } else {
            byte[] minorMinBytes = MokoUtils.toByteArray(minorMin, 2);
            byte[] minorMaxBytes = MokoUtils.toByteArray(minorMax, 2);
            data = new byte[7];
            data[0] = (byte) 0xEF;
            data[1] = (byte) ConfigKeyEnum.KEY_FILTER_MINOR_RANGE.getConfigKey();
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
        data[1] = (byte) ConfigKeyEnum.KEY_ADV_NAME.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < advNameBytes.length; i++) {
            data[i + 3] = advNameBytes[i];
        }
    }

    public void setUUID(String uuid) {
        byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
        data = new byte[19];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_IBEACON_UUID.getConfigKey();
        data[2] = (byte) 0x10;
        for (int i = 0; i < uuidBytes.length; i++) {
            data[i + 3] = uuidBytes[i];
        }
    }

    public void setMajor(int major) {
        byte[] majorBytes = MokoUtils.toByteArray(major, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_IBEACON_MAJOR.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = majorBytes[0];
        data[4] = majorBytes[1];
    }

    public void setMinor(int minor) {
        byte[] minorBytes = MokoUtils.toByteArray(minor, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_IBEACON_MINOR.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = minorBytes[0];
        data[4] = minorBytes[1];
    }

    public void setAdvInterval(int advInterval) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_ADV_INTERVAL.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) advInterval;
    }

    public void setTransmission(int transmission) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_TRANSMISSION.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) transmission;
    }

    public void setMeasurePower(int measurePower) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_MEASURE_POWER.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) measurePower;
    }

    public void setConnectable(int connectionMode) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_CONNECTABLE.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) connectionMode;
    }

    public void setClosePower() {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_CLOSE.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) 0x01;
    }

    public void changePassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[length + 3];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_PASSWORD.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < passwordBytes.length; i++) {
            data[i + 3] = passwordBytes[i];
        }
    }

    public void reset() {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_RESET.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) 0x01;
    }


    public void setScanWinow(int scannerState, int startTime) {
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_SCAN_WINDOW.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = (byte) scannerState;
        data[4] = (byte) startTime;
    }

    public void setLoraDevAddr(String devAddr) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(devAddr);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_DEV_ADDR.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraAppSKey(String appSkey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appSkey);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_APP_SKEY.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < rawDataBytes.length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraNwkSKey(String nwkSkey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(nwkSkey);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_NWK_SKEY.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < rawDataBytes.length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraDevEui(String devEui) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(devEui);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_DEV_EUI.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < rawDataBytes.length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraAppEui(String appEui) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appEui);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_APP_EUI.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < rawDataBytes.length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraAppKey(String appKey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appKey);
        int length = rawDataBytes.length;
        data = new byte[3 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_APP_KEY.getConfigKey();
        data[2] = (byte) length;
        for (int i = 0; i < rawDataBytes.length; i++) {
            data[i + 3] = rawDataBytes[i];
        }
    }

    public void setLoraUploadMode(int mode) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_MODE.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) mode;
    }

    public void setLoraUploadInterval(int interval) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_REPORT_INTERVAL.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) interval;
    }

    public void setLoraMessageType(int type) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_MESSAGE_TYPE.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) type;
    }

    public void setLoraRegion(int region) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_REGION.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) region;
    }

    public void setLoraCH(int ch1, int ch2) {
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_CH.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = (byte) ch1;
        data[4] = (byte) ch2;
    }

    public void setLoraDR(int dr1) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_DR.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) dr1;
    }

    public void setLoraADR(int adr) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_ADR.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) adr;
    }

    public void setLoraConnect() {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_LORA_CONNECT.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) 0x01;
    }

    public void setVibrationIntensity(@IntRange(from = 0, to = 100) int intensity) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_VIBRATION_INTENSITY.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) intensity;
    }

    public void setVibrationDuration(@IntRange(from = 0, to = 255) int duration) {
        data = new byte[4];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_VIBRATION_DURATION.getConfigKey();
        data[2] = (byte) 0x01;
        data[3] = (byte) duration;
    }

    public void setVibrationCycle(@IntRange(from = 1, to = 600) int cycle) {
        byte[] cycleBytes = MokoUtils.toByteArray(cycle, 2);
        data = new byte[5];
        data[0] = (byte) 0xEF;
        data[1] = (byte) ConfigKeyEnum.KEY_VIBRATION_CYCLE.getConfigKey();
        data[2] = (byte) 0x02;
        data[3] = cycleBytes[0];
        data[4] = cycleBytes[1];
    }
}
