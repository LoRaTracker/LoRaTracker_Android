package com.moko.loratracker.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.GetDeviceModelTask;
import com.moko.support.task.GetFilterAdvRawData;
import com.moko.support.task.GetFirmwareVersionTask;
import com.moko.support.task.GetHardwareVersionTask;
import com.moko.support.task.GetManufacturerTask;
import com.moko.support.task.GetProductDateTask;
import com.moko.support.task.GetSoftwareVersionTask;
import com.moko.support.task.OpenNotifyTask;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.task.SetFilterAdvRawData;
import com.moko.support.task.SetPasswordTask;
import com.moko.support.task.WriteConfigTask;

import java.util.ArrayList;

/**
 * @Date 2020/4/21
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.loratracker.service.MokoService
 */
public class MokoService extends Service implements MokoOrderTaskCallback {

    @Override
    public void onOrderResult(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_RESULT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_TIMEOUT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        sendOrderedBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH), null);
    }

    @Override
    public void onCreate() {
        LogModule.v("MokoService...onCreate");
        mHandler = new ServiceHandler(this);
        super.onCreate();
    }

    public void connectBluetoothDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address);
    }

    public void disConnectBle() {
        MokoSupport.getInstance().disConnectBle();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.v("MokoService...onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogModule.v("MokoService...onBind");
        return mBinder;
    }

    @Override
    public void onLowMemory() {
        LogModule.v("MokoService...onLowMemory");
        disConnectBle();
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogModule.v("MokoService...onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogModule.v("MokoService...onDestroy");
        disConnectBle();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<MokoService> {

        public ServiceHandler(MokoService service) {
            super(service);
        }

        @Override
        protected void handleMessage(MokoService service, Message msg) {
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////////

    public OrderTask getManufacturer() {
        GetManufacturerTask getManufacturerTask = new GetManufacturerTask(this);
        return getManufacturerTask;
    }

    public OrderTask getDeviceModel() {
        GetDeviceModelTask getDeviceModelTask = new GetDeviceModelTask(this);
        return getDeviceModelTask;
    }

    public OrderTask getProductDate() {
        GetProductDateTask getProductDateTask = new GetProductDateTask(this);
        return getProductDateTask;
    }

    public OrderTask getHardwareVersion() {
        GetHardwareVersionTask getHardwareVersionTask = new GetHardwareVersionTask(this);
        return getHardwareVersionTask;
    }

    public OrderTask getFirmwareVersion() {
        GetFirmwareVersionTask getFirmwareVersionTask = new GetFirmwareVersionTask(this);
        return getFirmwareVersionTask;
    }

    public OrderTask getSoftwareVersion() {
        GetSoftwareVersionTask getSoftwareVersionTask = new GetSoftwareVersionTask(this);
        return getSoftwareVersionTask;
    }

    public OrderTask getBattery() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_BATTERY);
        return task;
    }

    public OrderTask getiBeaconUUID() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_IBEACON_UUID);
        return task;
    }

    public OrderTask getiBeaconMajor() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_IBEACON_MAJOR);
        return task;
    }

    public OrderTask getIBeaconMinor() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_IBEACON_MINOR);
        return task;
    }

    public OrderTask getMeasurePower() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_MEASURE_POWER);
        return task;
    }

    public OrderTask getTransmission() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_TRANSMISSION);
        return task;
    }

    public OrderTask getAdvInterval() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_ADV_INTERVAL);
        return task;
    }

    public OrderTask getAdvName() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_ADV_NAME);
        return task;
    }

    public OrderTask getScanInterval() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_SCAN_INTERVAL);
        return task;
    }

    public OrderTask getAlarmNotify() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_ALARM_NOTIFY);
        return task;
    }

    public OrderTask getAlarmRssi() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_ALARM_RSSI);
        return task;
    }


    public OrderTask getLoRaConnectable() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_CONNECTABLE);
        return task;
    }

    public OrderTask getScanWindow() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_SCAN_WINDOW);
        return task;
    }

    public OrderTask getConnectable() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_CONNECTABLE);
        return task;
    }

    public OrderTask getMacAddress() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_DEVICE_MAC);
        return task;
    }

    public OrderTask getFilterRssi() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_FILTER_RSSI);
        return task;
    }

    public OrderTask getFilterMac() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_FILTER_MAC);
        return task;
    }

    public OrderTask getFilterName() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_FILTER_ADV_NAME);
        return task;
    }

    public OrderTask getFilterAdvRawData() {
        GetFilterAdvRawData task = new GetFilterAdvRawData(this);
        return task;
    }

    public OrderTask getFilterMajorRange() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_FILTER_MAJOR_RANGE);
        return task;
    }

    public OrderTask getFilterMinorRange() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_FILTER_MINOR_RANGE);
        return task;
    }

    public OrderTask getLoraMode() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_MODE);
        return task;
    }

    public OrderTask getLoraDevEUI() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_DEV_EUI);
        return task;
    }

    public OrderTask getLoraAppEUI() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_APP_EUI);
        return task;
    }

    public OrderTask getLoraAppKey() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_APP_KEY);
        return task;
    }

    public OrderTask getLoraDevAddr() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_DEV_ADDR);
        return task;
    }

    public OrderTask getLoraAppSKey() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_APP_SKEY);
        return task;
    }

    public OrderTask getLoraNwkSKey() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_NWK_SKEY);
        return task;
    }

    public OrderTask getLoraRegion() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_REGION);
        return task;
    }

    public OrderTask getLoraReportInterval() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_REPORT_INTERVAL);
        return task;
    }

    public OrderTask getLoraMessageType() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_MESSAGE_TYPE);
        return task;
    }

    public OrderTask getLoraCH() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_CH);
        return task;
    }

    public OrderTask getLoraDR() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_DR);
        return task;
    }

    public OrderTask getLoraADR() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.GET_LORA_ADR);
        return task;
    }


    ///////////////////////////////////////////////////////////////////////////
    // WRITE
    ///////////////////////////////////////////////////////////////////////////

    public WriteConfigTask setWriteConfig(ConfigKeyEnum configKeyEnum) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(configKeyEnum);
        return writeConfigTask;
    }

    public OrderTask setPassword(String password) {
        SetPasswordTask setPasswordTask = new SetPasswordTask(this);
        setPasswordTask.setData(password);
        return setPasswordTask;
    }

    public WriteConfigTask setTime() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTime();
        return writeConfigTask;
    }

    public OrderTask setDeviceName(String deviceName) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setAdvName(deviceName);
        return writeConfigTask;
    }

    public OrderTask setUUID(String uuid) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setUUID(uuid);
        return writeConfigTask;
    }

    public OrderTask setMajor(int major) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setMajor(major);
        return writeConfigTask;
    }

    public OrderTask setMinor(int minor) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setMinor(minor);
        return writeConfigTask;
    }

    public OrderTask setAdvInterval(int advInterval) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setAdvInterval(advInterval);
        return writeConfigTask;
    }


    public OrderTask setTransmission(int transmission) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTransmission(transmission);
        return writeConfigTask;
    }

    public OrderTask setMeasurePower(int measurePower) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setMeasurePower(measurePower);
        return writeConfigTask;
    }

    public WriteConfigTask setScanInterval(int seconds) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setScanInterval(seconds);
        return writeConfigTask;
    }

    public WriteConfigTask setAlarmNotify(int notify) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setAlarmNotify(notify);
        return writeConfigTask;
    }

    public WriteConfigTask setAlarmTriggerRssi(int rssi) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setAlarmTirggerRssi(rssi);
        return writeConfigTask;
    }

    public OrderTask setConnectionMode(int connectionMode) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setConnectable(connectionMode);
        return writeConfigTask;
    }


    public OrderTask changePassword(String password) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.changePassword(password);
        return writeConfigTask;
    }

    public OrderTask setReset() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.reset();
        return writeConfigTask;
    }

    public OrderTask setScanWindow(int scannerState, int startTime) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setScanWinow(scannerState, startTime);
        return writeConfigTask;
    }

    public OrderTask closePower() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setData(ConfigKeyEnum.SET_CLOSE);
        return task;
    }

    public OrderTask setFilterRssi(int rssi) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setFilterRssi(rssi);
        return task;
    }

    public OrderTask setFilterMac(String mac) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setFilterMac(mac);
        return task;
    }

    public OrderTask setFilterName(String name) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setFilterName(name);
        return task;
    }

    public OrderTask setFilterAdvRawData(ArrayList<String> filterRawDatas) {
        SetFilterAdvRawData task = new SetFilterAdvRawData(filterRawDatas, this);
        return task;
    }

    public OrderTask setFilterMajorRange(int enable, int majorMin, int majorMax) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setFilterMajorRange(enable, majorMin, majorMax);
        return task;
    }

    public OrderTask setFilterMinorRange(int enable, int majorMin, int majorMax) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setFilterMinorRange(enable, majorMin, majorMax);
        return task;
    }


    public OrderTask setLoraDevAddr(String devAddr) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraDevAddr(devAddr);
        return task;
    }

    public OrderTask setLoraNwkSKey(String nwkSKey) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraNwkSKey(nwkSKey);
        return task;
    }

    public OrderTask setLoraAppSKey(String appSKey) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraAppSKey(appSKey);
        return task;
    }

    public OrderTask setLoraAppEui(String appEui) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraAppEui(appEui);
        return task;
    }

    public OrderTask setLoraDevEui(String devEui) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraDevEui(devEui);
        return task;
    }

    public OrderTask setLoraAppKey(String appKey) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraAppKey(appKey);
        return task;
    }

    public OrderTask setLoraUploadMode(int mode) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraUploadMode(mode);
        return task;
    }

    public OrderTask setLoraUploadInterval(int interval) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraUploadInterval(interval);
        return task;
    }

    public OrderTask setLoraMessageType(int type) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraMessageType(type);
        return task;
    }

    public OrderTask setLoraRegion(int region) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraRegion(region);
        return task;
    }

    public OrderTask setLoraCH(int ch1, int ch2) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraCH(ch1, ch2);
        return task;
    }

    public OrderTask setLoraDR(int dr1, int dr2) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraDR(dr1, dr2);
        return task;
    }

    public OrderTask setLoraADR(int adr) {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraADR(adr);
        return task;
    }

    public OrderTask setLoraConnect() {
        WriteConfigTask task = new WriteConfigTask(this);
        task.setLoraConnect();
        return task;
    }

    ///////////////////////////////////////////////////////////////////////////
    // NOTIFY
    ///////////////////////////////////////////////////////////////////////////
    public OrderTask openWriteConfigNotify() {
        OpenNotifyTask task = new OpenNotifyTask(OrderType.WRITE_CONFIG, this);
        return task;
    }

    public OrderTask openDisconnectedNotify() {
        OpenNotifyTask task = new OpenNotifyTask(OrderType.DISCONNECTED_NOTIFY, this);
        return task;
    }

    public OrderTask openPasswordNotify() {
        OpenNotifyTask task = new OpenNotifyTask(OrderType.PASSWORD, this);
        return task;
    }
}
