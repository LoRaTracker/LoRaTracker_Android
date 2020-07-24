package com.moko.support.task;

import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class SetFilterAdvRawData extends OrderTask {
    public byte[] data;
    private ArrayList<String> mFilterRawDatas;
    private int index;
    private int size;

    public SetFilterAdvRawData(ArrayList<String> filterRawDatas, MokoOrderTaskCallback callback) {
        super(OrderType.WRITE_CONFIG, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        mFilterRawDatas = filterRawDatas;
        if (filterRawDatas != null) {
            index = 0;
            size = filterRawDatas.size();
        }
    }

    @Override
    public byte[] assemble() {
        if (mFilterRawDatas == null) {
            data = new byte[5];
            data[0] = (byte) 0xEF;
            data[1] = (byte) 0x25;
            data[2] = (byte) 0x02;
            data[3] = (byte) 0x01;
            data[4] = (byte) 0x00;
            return data;
        }
        String rawData = mFilterRawDatas.get(index);
        byte[] rawDataBytes = MokoUtils.hex2bytes(rawData);
        int length = rawDataBytes.length;
        data = new byte[5 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) 0x25;
        data[2] = (byte) (length + 2);
        data[3] = (byte) (index > 0 ? 0x00 : 0x01);
        data[4] = (byte) (size - (index + 1));
        return data;
    }

    @Override
    public void parseValue(byte[] value) {
        int length = value.length;
        if (length != 4)
            return;
        if (0xEF != (value[0] & 0xFF))
            return;
        if (0x25 != (value[1] & 0xFF))
            return;
        if (0x01 != (value[3] & 0xFF))
            return;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        index++;
        if (index < size) {
            MokoSupport.getInstance().executeTask(callback);
        } else {
            response.responseValue = value;
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
        }
    }
}