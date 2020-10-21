package com.moko.support.task;

import android.text.TextUtils;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

public class GetFilterAdvRawData extends OrderTask {
    public byte[] data;
    private StringBuffer stringBuffer = new StringBuffer("");

    public GetFilterAdvRawData() {
        super(OrderType.WRITE_CONFIG, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        data = new byte[]{(byte) 0xED, (byte) 0x25, (byte) 0xED};
        return data;
    }

    @Override
    public void parseValue(byte[] value) {
        int length = value.length;
        if (length < 5)
            return;
        if (0xED != (value[0] & 0xFF))
            return;
        if (0x25 != (value[1] & 0xFF))
            return;
        int dataLength = (value[2] & 0xFF) - 2;
        int isStart = value[3] & 0xFF;
        int isEnd = value[4] & 0xFF;
        if (dataLength > 0) {
            String data = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 5, 5 + dataLength));
            stringBuffer.append(data);
        }
        if (isEnd == 0) {
            String rawData = stringBuffer.toString();
            if (!TextUtils.isEmpty(rawData)) {
                byte[] rawDataBytes = MokoUtils.hex2bytes(stringBuffer.toString());
                int rawDataLength = rawDataBytes.length;
                byte[] responseValue = new byte[rawDataLength + 3];
                responseValue[0] = (byte) 0xED;
                responseValue[1] = (byte) 0x25;
                responseValue[2] = (byte) rawDataLength;
                for (int i = 0; i < rawDataLength; i++) {
                    responseValue[i + 3] = rawDataBytes[i];
                }
                response.responseValue = responseValue;
            } else {
                response.responseValue = value;
            }
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            OrderTaskResponseEvent event = new OrderTaskResponseEvent();
            event.setAction(MokoConstants.ACTION_ORDER_RESULT);
            event.setResponse(response);
            EventBus.getDefault().post(event);
            MokoSupport.getInstance().executeTask();
        }
    }
}
