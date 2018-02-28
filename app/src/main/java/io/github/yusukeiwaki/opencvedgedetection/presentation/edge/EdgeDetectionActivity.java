package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxSeekBar;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.github.yusukeiwaki.opencvedgedetection.R;
import io.github.yusukeiwaki.opencvedgedetection.databinding.ActivityEdgeDetectionBinding;
import io.github.yusukeiwaki.opencvedgedetection.presentation.base.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class EdgeDetectionActivity extends BaseActivity {
    public static Intent newIntent(Context context, @NonNull Uri imageUri) {
        Intent intent = new Intent(context, EdgeDetectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setData(imageUri);
        return intent;
    }

    private ActivityEdgeDetectionBinding binding;
    private Bitmap imageBitmap;
    private Bitmap imageBitmap2;
    private Disposable seelbarsSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edge_detection);
        Uri imageUri = parseImageUri();
        if (imageUri != null) {
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                Timber.e(e);
            }
            if (imageBitmap != null && imageBitmap.getWidth() > 0 && imageBitmap.getHeight() > 0) {
                binding.image.setImageBitmap(imageBitmap);
                this.imageBitmap = imageBitmap;
            }
        }

        Observable<Pair<Integer, Integer>> seekBarsAsObservable = Observable.combineLatest(
                RxSeekBar.userChanges(binding.seekbar1).doOnNext(integerInto(binding.textSeekbar1)),
                RxSeekBar.userChanges(binding.seekbar2).doOnNext(integerInto(binding.textSeekbar2)),
                new BiFunction<Integer, Integer, Pair<Integer, Integer>>() {
                    @Override
                    public Pair<Integer, Integer> apply(Integer integer, Integer integer2) throws Exception {
                        return new Pair<>(integer, integer2);
                    }
                });
        seelbarsSubscription = seekBarsAsObservable
                .debounce(300, TimeUnit.MILLISECONDS)
                .skip(1)
                .map(new Function<Pair<Integer,Integer>, Boolean>() {
                    @Override
                    public Boolean apply(Pair<Integer, Integer> integerIntegerPair) throws Exception {
                        canny(integerIntegerPair.first, integerIntegerPair.second);
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        binding.image.setImageBitmap(imageBitmap2);
                    }
                });

        binding.buttonReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.image.setImageBitmap(imageBitmap);
            }
        });
    }

    private Consumer<Integer> integerInto(final TextView textView) {
        return new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                textView.setText(Integer.toString(integer));
            }
        };
    }

    private @Nullable Uri parseImageUri() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        } else {
            return intent.getData();
        }
    }

    private void canny(double threshold1, double threshold2) {
        Mat src = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(imageBitmap, src);

        Mat cannyResult = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8U);
        Imgproc.Canny(src, cannyResult, threshold1, threshold2);
        src.release();

        Mat bitwiseResult = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8U);
        Core.bitwise_not(cannyResult, bitwiseResult);
        cannyResult.release();

        if (imageBitmap2 == null) {
            imageBitmap2 = imageBitmap.copy(imageBitmap.getConfig(), true);
        }
        Utils.matToBitmap(bitwiseResult, imageBitmap2);
        bitwiseResult.release();
    }

    @Override
    protected void onDestroy() {
        if (seelbarsSubscription != null) {
            seelbarsSubscription.dispose();
            seelbarsSubscription = null;
        }
        super.onDestroy();
    }
}
