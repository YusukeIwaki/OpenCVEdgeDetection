package io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.IOException;

import io.github.yusukeiwaki.opencvedgedetection.util.SimpleAsyncTask;
import timber.log.Timber;

class SaveImageTask extends SimpleAsyncTask<String> {
    private final ContentResolver contentResolver;
    private final Uri imageUri;

    public SaveImageTask(@NonNull Context context, @NonNull Uri imageUri) {
        this.contentResolver = context.getContentResolver();
        this.imageUri = imageUri;
    }

    @Override
    protected final String doInBackground() throws Exception {
        Bitmap imageBitmap = null;
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
        } catch (IOException e) {
            Timber.e(e);
        }

        String outUrl = MediaStore.Images.Media.insertImage(contentResolver, imageBitmap, "", "created by 指名手配カメラ");
        Timber.d("outUrl: %s", outUrl);
        if (outUrl == null) {
            throw new RuntimeException("保存に失敗しました");
        }
        return outUrl;
    }
}
