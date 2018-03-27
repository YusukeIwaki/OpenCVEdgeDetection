package io.github.yusukeiwaki.opencvedgedetection.util;

import android.arch.lifecycle.MutableLiveData;

public class IntLiveData extends MutableLiveData<Integer> {
    public IntLiveData(int defaultValue) {
        super();
        setValue(defaultValue);
    }
}
