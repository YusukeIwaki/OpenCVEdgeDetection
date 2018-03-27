package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.app.ProgressDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;

import com.jakewharton.rxbinding2.widget.RxSeekBar;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.github.yusukeiwaki.opencvedgedetection.BuildConfig;
import io.github.yusukeiwaki.opencvedgedetection.R;
import io.github.yusukeiwaki.opencvedgedetection.databinding.ActivityEdgeDetectionBinding;
import io.github.yusukeiwaki.opencvedgedetection.presentation.base.BaseActivity;
import io.github.yusukeiwaki.opencvedgedetection.presentation.edge.EdgeDetectionActivityViewModel.SavingState;
import io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage.SaveImageToGalleryActivity;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class EdgeDetectionActivity extends BaseActivity {
    private EdgeDetectionActivityViewModel viewModel;
    private ActivityEdgeDetectionBinding binding;
    private Disposable seekbarsSubscription;
    private ProgressDialog progressDialog;

    public static Intent newIntent(Context context, @NonNull Uri imageUri) {
        Intent intent = new Intent(context, EdgeDetectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setData(imageUri);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(EdgeDetectionActivityViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edge_detection);
        // setViewModelだけではなぜかプログレスがセットされないので↓
        if (viewModel.seekbarProgress1.getValue() == null) {
            viewModel.seekbarProgress1.setValue(100);
        }
        binding.seekbar1.setProgress(viewModel.seekbarProgress1.getValue());
        if (viewModel.seekbarProgress2.getValue() == null) {
            viewModel.seekbarProgress2.setValue(200);
        }
        binding.seekbar2.setProgress(viewModel.seekbarProgress2.getValue());
        // setViewModelだけではなぜかプログレスがセットされないので↑
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        Uri imageUri = parseImageUri();
        if (imageUri != null) {
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                Timber.e(e);
            }
            if (imageBitmap != null && imageBitmap.getWidth() > 0 && imageBitmap.getHeight() > 0) {
                int rotation = getRotationOf(imageUri);
                if (rotation == 0) {
                    viewModel.originalBitmap.setValue(imageBitmap);
                } else {
                    Bitmap rotatedBitmap = createRotatedBitmap(imageBitmap, rotation);
                    imageBitmap.recycle();
                    viewModel.originalBitmap.setValue(rotatedBitmap);

                }
            }
        }

        Observable<Pair<Integer, Integer>> seekBarsAsObservable = Observable.combineLatest(
                seekbarObservable(binding.seekbar1, viewModel.seekbarProgress1),
                seekbarObservable(binding.seekbar2, viewModel.seekbarProgress2),
                new BiFunction<Integer, Integer, Pair<Integer, Integer>>() {
                    @Override
                    public Pair<Integer, Integer> apply(Integer integer, Integer integer2) throws Exception {
                        return new Pair<>(integer, integer2);
                    }
                });
        seekbarsSubscription = seekBarsAsObservable
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Pair<Integer, Integer>>() {
                    @Override
                    public void accept(Pair<Integer, Integer> integerIntegerPair) throws Exception {
                        canny(integerIntegerPair.first, integerIntegerPair.second);
                    }
                });

        binding.image.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    viewModel.touched.setValue(true);
                    binding.invalidateAll();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    // do nothing.
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    viewModel.touched.setValue(false);
                    binding.invalidateAll();
                }
                return true;
            }
        });

        binding.buttonShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap processedBitmap = viewModel.processedBitmap.getValue();
                if (processedBitmap != null) {
                    saveAndShare(processedBitmap);
                }
            }
        });

        viewModel.savingState().observe(this, new Observer<SavingState>() {
            @Override
            public void onChanged(@Nullable SavingState savingState) {
                if (savingState != null && savingState.isInProgress()) {
                    if (progressDialog == null) {
                        progressDialog = createProgressDialog();
                    }
                    progressDialog.show();
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }

                if (savingState != null && savingState.isSuccess()) {
                    showShareDialog(savingState.uri);
                    viewModel.resetSavingState();
                }

                if (savingState != null && savingState.isError()) {
                    showError(savingState.error);
                    viewModel.resetSavingState();
                }
            }
        });
    }

    private int getRotationOf(Uri imageUri) {
        ExifInterface exifInterface = null;
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            if (inputStream != null) {
                exifInterface = new ExifInterface(inputStream);
            }
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }

        int rotation = 0;
        if (exifInterface != null) {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;

                //TODO: FLIP_VERTICAL, FLIP_HORIZONTALの考慮もする必要があるかも
            }
        }
        return rotation;
    }

    private Bitmap createRotatedBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("保存中...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    /**
     * RxSeekbar.userChangesは初期値が通知されないため、初期値を手動でconcatしたObservable.
     */
    private Observable<Integer> seekbarObservable(final SeekBar seekbar, MutableLiveData<Integer> field) {
        return Observable.just(seekbar.getProgress())
                .concatWith(RxSeekBar.userChanges(seekbar))
                .doOnNext(updateSeekbarProgressInto(field));
    }

    private Consumer<Integer> updateSeekbarProgressInto(final MutableLiveData<Integer> field) {
        return new Consumer<Integer>() {
            @Override
            public void accept(Integer progress) throws Exception {
                field.setValue(progress);
            }
        };
    }

    private @Nullable
    Uri parseImageUri() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        } else {
            return intent.getData();
        }
    }

    private void canny(double threshold1, double threshold2) {
        Bitmap originalBitmap = viewModel.originalBitmap.getValue();
        Bitmap processedBitmap = viewModel.processedBitmap.getValue();

        Mat src = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(originalBitmap, src);

        Mat cannyResult = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8U);
        Imgproc.Canny(src, cannyResult, threshold1, threshold2);
        src.release();

        Mat bitwiseResult = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8U);
        Core.bitwise_not(cannyResult, bitwiseResult);
        cannyResult.release();

        if (processedBitmap == null) {
            processedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        }
        Utils.matToBitmap(bitwiseResult, processedBitmap);
        bitwiseResult.release();

        viewModel.processedBitmap.postValue(processedBitmap);
        binding.invalidateAll();
    }

    private @Nullable
    File createFileForOutput() {
        File outDir = new File(getCacheDir(), "images");
        if (outDir.exists() || outDir.mkdir()) {
            String fileName = UUID.randomUUID().toString();
            return new File(outDir + "/" + fileName + ".png");
        } else {
            Timber.e("failed to create image/edge.png in cache dir.");
            return null;
        }
    }

    private void saveAndShare(@NonNull Bitmap bitmap) {
        File outFile = createFileForOutput();
        if (outFile != null) {
            final Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, outFile);
            viewModel.saveImage(bitmap, outFile, uri);
        }
    }

    private void showShareDialog(Uri uri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");

        Intent saveImageToGalleryIntent = SaveImageToGalleryActivity.newIntent(this, uri);

        Intent chooserIntent = Intent.createChooser(shareIntent, "共有");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { saveImageToGalleryIntent });
        startActivity(chooserIntent);
    }

    private void showError(Exception e) {
        Timber.e(e);
    }

    @Override
    protected void onDestroy() {
        if (seekbarsSubscription != null) {
            seekbarsSubscription.dispose();
            seekbarsSubscription = null;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onDestroy();
    }
}
