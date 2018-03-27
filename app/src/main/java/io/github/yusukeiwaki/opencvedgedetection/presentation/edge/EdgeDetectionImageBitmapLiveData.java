package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import io.github.yusukeiwaki.opencvedgedetection.util.BoolLiveData;

public class EdgeDetectionImageBitmapLiveData extends MediatorLiveData<Bitmap> {
    private final BoolLiveData touched;
    private final MutableLiveData<Bitmap> originalBitmap;
    private final MutableLiveData<Bitmap> processedBitmap;

    public EdgeDetectionImageBitmapLiveData(BoolLiveData touched, MutableLiveData<Bitmap> originalBitmap, MutableLiveData<Bitmap> processedBitmap) {
        this.touched = touched;
        this.originalBitmap = originalBitmap;
        this.processedBitmap = processedBitmap;

        addSource(touched, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                updateValue();
            }
        });
        addSource(originalBitmap, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                updateValue();
            }
        });
        addSource(processedBitmap, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                updateValue();
            }
        });
    }

    private void updateValue() {
        if (processedBitmap.getValue() == null) {
            if (getValue() != originalBitmap.getValue()) {
                setValue(originalBitmap.getValue());
            }
        } else {
            if (touched.getValue()) {
                if (getValue() != originalBitmap.getValue()) {
                    setValue(originalBitmap.getValue());
                }
            } else {
                if (getValue() != processedBitmap.getValue()) {
                    setValue(processedBitmap.getValue());
                }
            }
        }
    }
}
