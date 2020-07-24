package com.moko.support.task;

import com.moko.support.entity.OrderType;

public class SetPasswordTask extends OrderTask {
    public byte[] data;

    public SetPasswordTask() {
        super(OrderType.PASSWORD, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(String password) {
        this.data = new byte[9];
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data[0] = (byte) 0xED;
        for (int i = 0; i < length; i++) {
            data[i + 1] = passwordBytes[i];
        }
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
