package com.moko.support.task;

import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

public class GetFilterAdvRawData extends OrderTask {
    public byte[] data;
    private StringBuffer stringBuffer = new StringBuffer("");

    public GetFilterAdvRawData(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE_CONFIG, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        int dataLength = value[2] & 0xFF - 2;
        int isStart = value[3] & 0xFF;
        int isEnd = value[4] & 0xFF;
        if (dataLength > 0) {
            String data = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 5, 5 + dataLength));
            stringBuffer.append(data);
        }
        if (isEnd == 0) {
            MokoSupport.getInstance().filterRawData = stringBuffer.toString();
            response.responseValue = value;
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
        }
    }
}
