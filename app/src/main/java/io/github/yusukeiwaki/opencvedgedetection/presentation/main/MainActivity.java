package io.github.yusukeiwaki.opencvedgedetection.presentation.main;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;

import io.github.yusukeiwaki.opencvedgedetection.R;
import io.github.yusukeiwaki.opencvedgedetection.databinding.ActivityMainBinding;
import io.github.yusukeiwaki.opencvedgedetection.presentation.base.BaseActivity;
import io.github.yusukeiwaki.opencvedgedetection.presentation.edge.EdgeDetectionActivity;

public class MainActivity extends BaseActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;
    private CameraIntentManager cameraIntentManager;
    private GalleryIntentManager galleryIntentManager;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        cameraIntentManager = new CameraIntentManager(this, savedInstanceState);
        cameraIntentManager.setCallback(new CameraIntentManager.Callback() {
            @Override
            public void onCaptureCamera(@NonNull Uri imageUri) {
                startActivity(EdgeDetectionActivity.newIntent(MainActivity.this, imageUri));
            }
        });
        galleryIntentManager = new GalleryIntentManager(this, savedInstanceState);
        galleryIntentManager.setCallback(new GalleryIntentManager.Callback() {
            @Override
            public void onPickImage(@NonNull Uri imageUri) {
                startActivity(EdgeDetectionActivity.newIntent(MainActivity.this, imageUri));
            }
        });

        // Example of a call to a native method
        //binding.buttonCapture.setText(stringFromJNI());

        binding.buttonCapture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraIntentManager.dispatchTakePictureIntent();
            }
        });
        binding.buttonPick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntentManager.dispatchPickImageIntent();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cameraIntentManager.onSaveInstanceState(outState);
        galleryIntentManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (cameraIntentManager != null && cameraIntentManager.onActivityResult(requestCode, resultCode, data)) {
            // Result is handled properly in cameraIntentManager.
            // Nothing to do here.
        } else if (galleryIntentManager != null && galleryIntentManager.onActivityResult(requestCode, resultCode, data)) {
            // Result is handled properly in galleryIntentManager.
            // Nothing to do here.
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
