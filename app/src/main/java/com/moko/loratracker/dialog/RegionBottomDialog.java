package com.moko.loratracker.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.loratracker.R;
import com.moko.loratracker.entity.Region;
import com.moko.loratracker.view.WheelView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegionBottomDialog extends MokoBaseDialog {


    @Bind(R.id.wv_bottom)
    WheelView wvBottom;
    private ArrayList<Region> mDatas;
    private HashMap<Integer, Region> regionHashMap;
    private ArrayList<String> mNames;
    private int mIndex;


    @Override
    public int getLayoutRes() {
        return R.layout.dialog_bottom;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        regionHashMap = new HashMap<>();
        mNames = new ArrayList<>();
        for (int i = 0, size = mDatas.size(); i < size; i++) {
            Region region = mDatas.get(i);
            regionHashMap.put(i, region);
            if (mIndex == region.value) {
                mIndex = i;
            }
            mNames.add(region.name);
        }
        wvBottom.setData(mNames);
        wvBottom.setDefault(mIndex);
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                if (TextUtils.isEmpty(wvBottom.getSelectedText())) {
                    return;
                }
                dismiss();
                final int selected = wvBottom.getSelected();
                if (listener != null) {
                    Region region = regionHashMap.get(selected);
                    listener.onValueSelected(region.value);
                }
                break;
        }
    }

    public void setDatas(ArrayList<Region> datas, int index) {
        this.mDatas = datas;
        this.mIndex = index;
    }

    private OnBottomListener listener;

    public void setListener(OnBottomListener listener) {
        this.listener = listener;
    }

    public interface OnBottomListener {
        void onValueSelected(int value);
    }
}
