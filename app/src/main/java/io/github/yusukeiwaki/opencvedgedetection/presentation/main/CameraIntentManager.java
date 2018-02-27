package io.github.yusukeiwaki.opencvedgedetection.presentation.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;

import io.github.yusukeiwaki.opencvedgedetection.BuildConfig;
import timber.log.Timber;

class CameraIntentManager {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private final Activity activity;
    private Callback callback;
    private Uri imageUri;

    public CameraIntentManager(Activity activity, @Nullable Bundle savedInstanceState) {
        this.activity = activity;
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("imageUri");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("imageUri", imageUri);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private File createTempFile() throws IOException {
        File tmpDir = new File(activity.getFilesDir(), "tmp");
        if (tmpDir.exists() || tmpDir.mkdir()) {
            return File.createTempFile("IMG_", ".jpg", tmpDir);
        }

        throw new IOException("failed to create tmp/");
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createTempFile();
            } catch (IOException e) {
                Timber.e(e);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (imageUri != null) {
                    doCallback(imageUri);
                    return true;
                }
            }
        }
        return false;
    }

    private void doCallback(@NonNull Uri imageUri) {
        if (callback != null) {
            callback.onCaptureCamera(imageUri);
        }
    }

    public interface Callback {
        void onCaptureCamera(@NonNull Uri imageUri);
    }
}
