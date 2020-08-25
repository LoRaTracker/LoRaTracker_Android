package com.moko.support.task;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;

public class SetFilterAdvRawData extends OrderTask {
    public byte[] data;
    private ArrayList<byte[]> mSplitRawDatas;
    private int index;
    private int size;

    public SetFilterAdvRawData(ArrayList<String> filterRawDatas) {
        super(OrderType.WRITE_CONFIG, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        if (filterRawDatas != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String rawData : filterRawDatas) {
                stringBuffer.append(rawData);
            }
            byte[] mRawDatas = MokoUtils.hex2bytes(stringBuffer.toString());
            final int length = mRawDatas.length;
            size = length / 15 + 1;
            mSplitRawDatas = new ArrayList<>();
            if (size == 1) {
                mSplitRawDatas.add(mRawDatas);
            } else {
                for (int i = 0; i < size; i++) {
                    byte[] rawData;
                    if ((i + 1) * 15 < length) {
                        rawData = Arrays.copyOfRange(mRawDatas, i * 15, (i + 1) * 15);
                    } else {
                        rawData = Arrays.copyOfRange(mRawDatas, i * 15, length);
                    }
                    mSplitRawDatas.add(rawData);
                }
            }
        }
    }

    @Override
    public byte[] assemble() {
        if (mSplitRawDatas == null || mSplitRawDatas.size() == 0) {
            data = new byte[5];
            data[0] = (byte) 0xEF;
            data[1] = (byte) 0x25;
            data[2] = (byte) 0x02;
            data[3] = (byte) 0x01;
            data[4] = (byte) 0x00;
            return data;
        }
        byte[] rawDataBytes = mSplitRawDatas.get(index);
        int length = rawDataBytes.length;
        data = new byte[5 + length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) 0x25;
        data[2] = (byte) (length + 2);
        data[3] = (byte) (index > 0 ? 0x00 : 0x01);
        data[4] = (byte) (size - (index + 1));
        for (int i = 0; i < length; i++) {
            data[5 + i] = rawDataBytes[i];
        }
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
            MokoSupport.getInstance().executeTask();
        } else {
            response.responseValue = value;
            MokoSupport.getInstance().pollTask();
            OrderTaskResponseEvent event = new OrderTaskResponseEvent();
            event.setAction(MokoConstants.ACTION_ORDER_RESULT);
            event.setResponse(response);
            EventBus.getDefault().post(event);
            MokoSupport.getInstance().executeTask();
        }
    }
}
