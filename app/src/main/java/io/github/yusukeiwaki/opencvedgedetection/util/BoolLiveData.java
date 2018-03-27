package io.github.yusukeiwaki.opencvedgedetection.util;

import android.arch.lifecycle.MutableLiveData;

public class BoolLiveData extends MutableLiveData<Boolean> {
    public BoolLiveData(boolean defaultValue) {
        super();
        setValue(defaultValue);
    }
}
