package com.moko.support.entity;


import java.io.Serializable;

public enum ConfigKeyEnum implements Serializable {

    KEY_IBEACON_UUID(0x01),
    KEY_IBEACON_MAJOR(0x02),
    KEY_IBEACON_MINOR(0x03),
    KEY_MEASURE_POWER(0x04),
    KEY_TRANSMISSION(0x05),
    KEY_ADV_INTERVAL(0x06),
    KEY_ADV_NAME(0x07),
    KEY_PASSWORD(0x08),
    KEY_BATTERY(0x09),
    KEY_RESET(0x09),
    KEY_SCAN_INTERVAL(0x0A),
    KEY_SCAN_WINDOW(0x0B),
    KEY_CONNECTABLE(0x0C),
    KEY_FILTER_MAC(0x0D),
    KEY_FILTER_RSSI(0x0E),
    KEY_FILTER_ADV_NAME(0x0F),
    KEY_FILTER_MAJOR_RANGE(0x10),
    KEY_FILTER_MINOR_RANGE(0x11),
    KEY_ALARM_RSSI(0x12),
    KEY_LORA_REPORT_INTERVAL(0x13),
    KEY_ALARM_NOTIFY(0x14),
    KEY_LORA_MODE(0x15),
    KEY_LORA_DEV_EUI(0x16),
    KEY_LORA_APP_EUI(0x17),
    KEY_LORA_APP_KEY(0x18),
    KEY_LORA_DEV_ADDR(0x19),
    KEY_LORA_APP_SKEY(0x1A),
    KEY_LORA_NWK_SKEY(0x1B),
    KEY_LORA_REGION(0x1C),
    KEY_LORA_MESSAGE_TYPE(0x1D),
    KEY_LORA_CH(0x1E),
    KEY_LORA_DR(0x1F),
    KEY_LORA_ADR(0x20),
    KEY_TIME(0x21),
    KEY_LORA_CONNECTABLE(0x22),
    KEY_DEVICE_MAC(0x23),
    KEY_FILTER_ADV_RAW_DATA(0x25),
    KEY_LORA_CONNECT(0x23),
    KEY_CLOSE(0x24),
    KEY_VIBRATION_INTENSITY(0x27),
    KEY_VIBRATION_DURATION(0x28),
    KEY_VIBRATION_CYCLE(0x29),
    ;

    private int configKey;

    ConfigKeyEnum(int configKey) {
        this.configKey = configKey;
    }


    public int getConfigKey() {
        return configKey;
    }

    public static ConfigKeyEnum fromConfigKey(int configKey) {
        for (ConfigKeyEnum configKeyEnum : ConfigKeyEnum.values()) {
            if (configKeyEnum.getConfigKey() == configKey) {
                return configKeyEnum;
            }
        }
        return null;
    }
}
