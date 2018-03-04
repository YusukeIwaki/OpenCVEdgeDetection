package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;

public class EdgeDetectionActivityViewModel extends ViewModel {
    public final ObservableInt seekbarProgress1 = new ObservableInt(100);
    public final ObservableInt seekbarProgress2 = new ObservableInt(200);

    public final ObservableBoolean touched = new ObservableBoolean(false);
    public final ObservableField<Bitmap> originalBitmap = new ObservableField<>(null);
    public final ObservableField<Bitmap> processedBitmap = new ObservableField<>(null);
}
