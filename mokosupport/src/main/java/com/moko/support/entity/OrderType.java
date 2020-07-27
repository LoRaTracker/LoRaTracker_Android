package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2020/4/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.entity.OrderType
 */
public enum OrderType implements Serializable {
    // 180a
    DEVICE_MODEL("DEVICE_MODEL", "00002a24-0000-1000-8000-00805f9b34fb"),
    FIRMWARE_VERSION("FIRMWARE_VERSION", "00002a26-0000-1000-8000-00805f9b34fb"),
    HARDWARE_VERSION("HARDWARE_VERSION", "00002a27-0000-1000-8000-00805f9b34fb"),
    SOFTWARE_VERSION("SOFTWARE_VERSION", "00002a28-0000-1000-8000-00805f9b34fb"),
    MANUFACTURER("MANUFACTURER", "00002a29-0000-1000-8000-00805f9b34fb"),
    // ff00
    PASSWORD("PASSWORD", "0000ff00-0000-1000-8000-00805f9b34fb"),
    DISCONNECTED_NOTIFY("DISCONNECTED_NOTIFY", "0000ff01-0000-1000-8000-00805f9b34fb"),
    WRITE_CONFIG("WRITE_CONFIG", "0000ff02-0000-1000-8000-00805f9b34fb"),
    ;


    private String uuid;
    private String name;

    OrderType(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
