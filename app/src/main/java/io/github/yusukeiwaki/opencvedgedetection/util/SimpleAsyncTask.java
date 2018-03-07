package io.github.yusukeiwaki.opencvedgedetection.util;

import android.os.AsyncTask;

/**
 * ほぼAsyncTaskだけど
 * <p>
 * * executeに引数指定はできない
 * * コールバッククラスを分離
 * <p>
 * というカスタマイズをしたAsyncTask
 */
public abstract class SimpleAsyncTask<Result> extends AsyncTask<Void, Void, Result> {

    private MainThreadCallback<Result> callback;
    private Exception error;

    protected abstract Result doInBackground() throws Exception;

    public void setCallback(MainThreadCallback<Result> callback) {
        this.callback = callback;
    }

    @Override
    protected final void onPreExecute() {
        if (callback != null) {
            callback.onPreExecute();
        }
    }

    @Override
    protected final Result doInBackground(Void... voids) {
        try {
            return doInBackground();
        } catch (Exception e) {
            error = e;
            cancel(true);
            return null;
        }
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
    protected final void onCancelled(Result result) {
        onCancelled();
    }

    @Override
    protected final void onPostExecute(Result result) {
        onSuccess(result);
    }

    private void onSuccess(Result result) {
        if (callback != null) {
            callback.onSuccess(result);
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

    public static class MainThreadCallback<Result> {
        public void onPreExecute() {
        }

        public void onSuccess(Result result) {
        }

        public void onError(Exception e) {
        }

        public void onCancel() {
        }
    }
}
