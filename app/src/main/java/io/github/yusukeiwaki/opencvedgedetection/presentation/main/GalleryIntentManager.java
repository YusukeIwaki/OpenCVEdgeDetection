package io.github.yusukeiwaki.opencvedgedetection.presentation.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class GalleryIntentManager {
    private static final int REQUEST_PICK_IMAGE = 2;
    private final Activity activity;
    private Callback callback;
    private Uri imageUri;

    public GalleryIntentManager(Activity activity, @Nullable Bundle savedInstanceState) {
        this.activity = activity;
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("GalleryIntentManager_imageUri");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("GalleryIntentManager_imageUri", imageUri);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void dispatchPickImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    imageUri = data.getData();
                }
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
            callback.onPickImage(imageUri);
        }
    }

    public interface Callback {
        void onPickImage(@NonNull Uri imageUri);
    }
}
