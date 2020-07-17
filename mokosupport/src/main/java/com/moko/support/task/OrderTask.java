package com.moko.support.task;

import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;

public abstract class OrderTask {
    public static final long DEFAULT_DELAY_TIME = 3000;
    public static final int RESPONSE_TYPE_READ = 0;
    public static final int RESPONSE_TYPE_WRITE = 1;
    public static final int RESPONSE_TYPE_NOTIFY = 2;
    public static final int RESPONSE_TYPE_DISABLE_NOTIFY = 4;
    public static final int RESPONSE_TYPE_WRITE_NO_RESPONSE = 3;
    public static final int ORDER_STATUS_SUCCESS = 1;
    public OrderType orderType;
    public MokoOrderTaskCallback callback;
    public OrderTaskResponse response;
    public long delayTime = DEFAULT_DELAY_TIME;
    public int orderStatus;

    public OrderTaskResponse getResponse() {
        return response;
    }

    public void setResponse(OrderTaskResponse response) {
        this.response = response;
    }

    public OrderTask(OrderType orderType, MokoOrderTaskCallback callback, int responseType) {
        response = new OrderTaskResponse();
        this.orderType = orderType;
        this.callback = callback;
        this.response.orderType = orderType;
        this.response.responseType = responseType;
    }

    public abstract byte[] assemble();

    public MokoOrderTaskCallback getCallback() {
        return callback;
    }

    public void setCallback(MokoOrderTaskCallback callback) {
        this.callback = callback;
    }

    public void parseValue(byte[] value) {
    }

    public Runnable timeoutRunner = new Runnable() {
        @Override
        public void run() {
            if (orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                if (timeoutPreTask()) {
                    MokoSupport.getInstance().pollTask();
                    callback.onOrderTimeout(response);
                    MokoSupport.getInstance().executeTask(callback);
                }
            }
        }
    };

    public boolean timeoutPreTask() {
        LogModule.i(orderType.getName() + "超时");
        return true;
    }
}
