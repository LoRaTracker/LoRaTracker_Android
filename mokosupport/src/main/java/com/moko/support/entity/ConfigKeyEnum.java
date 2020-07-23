package com.moko.support.entity;


import java.io.Serializable;

public enum ConfigKeyEnum implements Serializable {

    GET_IBEACON_UUID(0x01),
    GET_IBEACON_MAJOR(0x02),
    GET_IBEACON_MINOR(0x03),
    GET_MEASURE_POWER(0x04),
    GET_TRANSMISSION(0x05),
    GET_ADV_INTERVAL(0x06),
    GET_ADV_NAME(0x07),
    GET_BATTERY(0x09),
    GET_SCAN_INTERVAL(0x0A),
    GET_SCAN_WINDOW(0x0B),
    GET_CONNECTABLE(0x0C),
    GET_FILTER_MAC(0x0D),
    GET_FILTER_RSSI(0x0E),
    GET_FILTER_ADV_NAME(0x0F),
    GET_FILTER_MAJOR_RANGE(0x10),
    GET_FILTER_MINOR_RANGE(0x11),
    GET_ALARM_RSSI(0x12),
    GET_LORA_REPORT_INTERVAL(0x13),
    GET_ALARM_NOTIFY(0x14),
    GET_LORA_MODE(0x15),
    GET_LORA_DEV_EUI(0x16),
    GET_LORA_APP_EUI(0x17),
    GET_LORA_APP_KEY(0x18),
    GET_LORA_DEV_ADDR(0x19),
    GET_LORA_APP_SKEY(0x1A),
    GET_LORA_NWK_SKEY(0x1B),
    GET_LORA_REGION(0x1C),
    GET_LORA_MESSAGE_TYPE(0x1D),
    GET_LORA_CH(0x1E),
    GET_LORA_DR(0x1F),
    GET_LORA_ADR(0x20),
    GET_TIME(0x21),
    GET_LORA_CONNECTABLE(0x22),
    GET_DEVICE_MAC(0x23),
    GET_FILTER_ADV_RAW_DATA(0x25),

    SET_IBEACON_UUID(0x01),
    SET_IBEACON_MAJOR(0x02),
    SET_IBEACON_MINOR(0x03),
    SET_MEASURE_POWER(0x04),
    SET_TRANSMISSION(0x05),
    SET_ADV_INTERVAL(0x06),
    SET_ADV_NAME(0x07),
    SET_PASSWORD(0x08),
    SET_RESET(0x09),
    SET_SCAN_INTERVAL(0x0A),
    SET_SCAN_WINDOW(0x0B),
    SET_CONNECTABLE(0x0C),
    SET_FILTER_MAC(0x0D),
    SET_FILTER_RSSI(0x0E),
    SET_FILTER_ADV_NAME(0x0F),
    SET_FILTER_MAJOR_RANGE(0x10),
    SET_FILTER_MINOR_RANGE(0x11),
    SET_ALARM_RSSI(0x12),
    SET_LORA_REPORT_INTERVAL(0x13),
    SET_ALARM_NOTIFY(0x14),
    SET_LORA_MODE(0x15),
    SET_LORA_DEV_EUI(0x16),
    SET_LORA_APP_EUI(0x17),
    SET_LORA_APP_KEY(0x18),
    SET_LORA_DEV_ADDR(0x19),
    SET_LORA_APP_SKEY(0x1A),
    SET_LORA_NWK_SKEY(0x1B),
    SET_LORA_REGION(0x1C),
    SET_LORA_MESSAGE_TYPE(0x1D),
    SET_LORA_CH(0x1E),
    SET_LORA_DR(0x1F),
    SET_LORA_ADR(0x20),
    SET_TIME(0x21),
    SET_LORA_CHDR_RESET(0x22),
    SET_LORA_CONNECT(0x23),
    SET_CLOSE(0x24),
    SET_FILTER_ADV_RAW_DATA(0x25),
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
