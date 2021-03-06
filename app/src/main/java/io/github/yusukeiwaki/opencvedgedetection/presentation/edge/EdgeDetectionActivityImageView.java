package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * EdgeDetectionActivityViewModelをもとにsetImageBitmapするための
 * カスタムImageView
 * <p>
 * <io.github.yusukeiwaki.opencvedgedetection.presentation.edge.EdgeDetectionActivityImageView
 * layout_width="..."
 * layout_height="..."
 * app:viewModel="@{viewModel}"
 * <p>
 * のように使われる想定
 */
public class EdgeDetectionActivityImageView extends AppCompatImageView {

    public EdgeDetectionActivityImageView(Context context) {
        super(context);
    }

    public EdgeDetectionActivityImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EdgeDetectionActivityImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setViewModel(@Nullable EdgeDetectionActivityViewModel viewModel) {
        if (viewModel == null) {
            return;
        }

        boolean touched = viewModel.touched.get();
        Bitmap originalBitmap = viewModel.originalBitmap.get();
        Bitmap processedBitmap = viewModel.processedBitmap.get();

        if (processedBitmap == null) {
            if (originalBitmap != null) {
                setImageBitmap(originalBitmap);
            }
        } else {
            if (touched) {
                if (originalBitmap != null) {
                    setImageBitmap(originalBitmap);
                }
            } else {
                setImageBitmap(processedBitmap);
            }
        }
    }
}
