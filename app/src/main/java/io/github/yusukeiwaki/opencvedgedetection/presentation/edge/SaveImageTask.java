package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

import io.github.yusukeiwaki.opencvedgedetection.util.SimpleAsyncTask;

class SaveImageTask extends SimpleAsyncTask<Void> {
    private final Bitmap bitmap;
    private final File outFile;

    /**
     * @param bitmap  保存したいBitmap。
     * @param outFile 保存先。FileProviderで公開されているパスを指定する必要がある。
     */
    public SaveImageTask(Bitmap bitmap, File outFile) {
        this.bitmap = bitmap;
        this.outFile = outFile;
    }

    @Override
    protected final Void doInBackground() throws Exception {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(outFile));
        return null;
    }
}
