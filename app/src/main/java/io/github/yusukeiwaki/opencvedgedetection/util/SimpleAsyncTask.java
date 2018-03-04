package io.github.yusukeiwaki.opencvedgedetection.util;

import android.os.AsyncTask;

/**
 * ほぼAsyncTaskだけど
 *
 * * executeに引数指定はできない
 * * コールバッククラスを分離
 *
 * というカスタマイズをしたAsyncTask
 */
public abstract class SimpleAsyncTask extends AsyncTask<Void, Void, Void> {

    public static class MainThreadCallback {
        public void onPreExecute() {}

        public void onSuccess() {}

        public void onError(Exception e) {}

        public void onCancel() {}
    }

    private MainThreadCallback callback;

    private Exception error;

    protected abstract void doInBackground() throws Exception;

    public void setCallback(MainThreadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected final void onPreExecute() {
        if (callback != null) {
            callback.onPreExecute();
        }
    }

    @Override
    protected final Void doInBackground(Void... voids) {
        try {
            doInBackground();
        } catch (Exception e) {
            error = e;
            cancel(true);
        }
        return null;
    }

    @Override
    protected final void onCancelled() {
        if (error != null) {
            onError(error);
            error = null;
        } else {
            onCancel();
        }
    }

    @Override
    protected final void onCancelled(Void result) {
        onCancelled();
    }

    @Override
    protected final void onPostExecute(Void result) {
        onSuccess();
    }

    private void onSuccess() {
        if (callback != null) {
            callback.onSuccess();
        }
    }

    private void onError(Exception e) {
        if (callback != null) {
            callback.onError(e);
        }
    }

    private void onCancel() {
        if (callback != null) {
            callback.onCancel();
        }
    }
}
