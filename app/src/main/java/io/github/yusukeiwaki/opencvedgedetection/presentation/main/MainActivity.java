package io.github.yusukeiwaki.opencvedgedetection.presentation.main;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import io.github.yusukeiwaki.opencvedgedetection.R;
import io.github.yusukeiwaki.opencvedgedetection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private ActivityMainBinding binding;
    private CameraIntentManager cameraIntentManager;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        cameraIntentManager = new CameraIntentManager(this, savedInstanceState);
        cameraIntentManager.setCallback(new CameraIntentManager.Callback() {
            @Override
            public void onRenderBitmap(@NonNull Bitmap bitmap) {
                binding.imageCameraThumbnail.setImageBitmap(bitmap);
            }
        });

        // Example of a call to a native method
        binding.sampleText.setText(stringFromJNI());

        if (savedInstanceState == null) {
            cameraIntentManager.dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cameraIntentManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (cameraIntentManager != null && cameraIntentManager.onActivityResult(requestCode, resultCode, data)) {
            // Result is handled properly in cameraIntentManager.
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
