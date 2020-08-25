package com.moko.loratracker.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.SparseArray;

import com.moko.loratracker.entity.BeaconInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2020/5/1
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.loratracker.utils.BeaconInfoParseableImpl
 */
public class BeaconInfoParseableImpl implements DeviceInfoParseable<BeaconInfo> {
    private HashMap<String, BeaconInfo> beaconInfoHashMap;

    public BeaconInfoParseableImpl() {
        this.beaconInfoHashMap = new HashMap<>();
    }

    @Override
    public BeaconInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        if (map == null || map.isEmpty())
            return null;
        SparseArray<byte[]> manufacturer = result.getScanRecord().getManufacturerSpecificData();
        if (manufacturer == null || manufacturer.size() == 0)
            return null;
        byte[] manufacturerSpecificDataByte = record.getManufacturerSpecificData(manufacturer.keyAt(0));
        if (manufacturerSpecificDataByte.length == 0 || (manufacturerSpecificDataByte[0] != 0x02 || manufacturerSpecificDataByte[1] != 0x15))
            return null;
        String major = null;
        String minor = null;
        int rssi_1m = 0;
        int txPower = 0;
        int connectable = 0;
        int track = 0;
        int battery = 0;
        int deviceType = 0;
        Iterator iterator = map.keySet().iterator();
        if (iterator.hasNext()) {
            ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
            if (parcelUuid.toString().startsWith("0000ff03")) {
                byte[] bytes = map.get(parcelUuid);
                if (bytes != null) {
                    deviceType = bytes[0] & 0xFF;
                    major = String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(bytes, 1, 3)));
                    minor = String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5)));
                    rssi_1m = bytes[5];
                    txPower = bytes[6];
                    String binary = MokoUtils.hexString2binaryString(MokoUtils.byte2HexString(bytes[7]));
                    connectable = Integer.parseInt(binary.substring(4, 5));
                    track = Integer.parseInt(binary.substring(5, 6));
                    battery = bytes[8] & 0xFF;
                }
            } else {
                return null;
            }
        }

        String uuid = String.format("%s-%s-%s-%s-%s"
                , MokoUtils.bytesToHexString(Arrays.copyOfRange(manufacturerSpecificDataByte, 2, 6))
                , MokoUtils.bytesToHexString(Arrays.copyOfRange(manufacturerSpecificDataByte, 6, 8))
                , MokoUtils.bytesToHexString(Arrays.copyOfRange(manufacturerSpecificDataByte, 8, 10))
                , MokoUtils.bytesToHexString(Arrays.copyOfRange(manufacturerSpecificDataByte, 10, 12))
                , MokoUtils.bytesToHexString(Arrays.copyOfRange(manufacturerSpecificDataByte, 12, 18))
        );
        BeaconInfo beaconInfo;
        if (beaconInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconInfo = beaconInfoHashMap.get(deviceInfo.mac);
            beaconInfo.name = deviceInfo.name;
            beaconInfo.rssi = deviceInfo.rssi;
            beaconInfo.battery = battery;
            beaconInfo.deviceType = deviceType;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - beaconInfo.scanTime;
            beaconInfo.intervalTime = intervalTime;
            beaconInfo.scanTime = currentTime;
        } else {
            beaconInfo = new BeaconInfo();
            beaconInfo.name = deviceInfo.name;
            beaconInfo.mac = deviceInfo.mac;
            beaconInfo.rssi = deviceInfo.rssi;
            beaconInfo.battery = battery;
            beaconInfo.deviceType = deviceType;
            beaconInfo.connectable = connectable;
            beaconInfo.track = track;
            beaconInfo.major = major;
            beaconInfo.minor = minor;
            beaconInfo.uuid = uuid;
            beaconInfo.rssi_1m = rssi_1m;
            beaconInfo.txPower = txPower;
            double distance = MokoUtils.getDistance(deviceInfo.rssi, rssi_1m);
            String distanceDesc = "Unknown";
            if (distance <= 0.1) {
                distanceDesc = "Immediate";
            } else if (distance > 0.1 && distance <= 1.0) {
                distanceDesc = "Near";
            } else if (distance > 1.0) {
                distanceDesc = "Far";
            }
            beaconInfo.proximity = distanceDesc;
            beaconInfo.scanTime = SystemClock.elapsedRealtime();
            beaconInfoHashMap.put(deviceInfo.mac, beaconInfo);
        }

        return beaconInfo;
    }
}
