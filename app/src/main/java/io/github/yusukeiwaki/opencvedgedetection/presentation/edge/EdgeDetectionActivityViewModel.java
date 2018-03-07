package io.github.yusukeiwaki.opencvedgedetection.presentation.edge;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public class EdgeDetectionActivityViewModel extends ViewModel {
    public final ObservableInt seekbarProgress1 = new ObservableInt(100);
    public final ObservableInt seekbarProgress2 = new ObservableInt(200);

    public final ObservableBoolean touched = new ObservableBoolean(false);
    public final ObservableField<Bitmap> originalBitmap = new ObservableField<>(null);
    public final ObservableField<Bitmap> processedBitmap = new ObservableField<>(null);
    private final MutableLiveData<SavingState> savingState = new MutableLiveData<>();

    public final LiveData<SavingState> savingState() {
        // MutableLiveDataをimmutableにすることはできないので、
        // LiveData型として返すことでsetValue/postValueメソッドを隠す
        return savingState;
    }

    public final void resetSavingState() {
        savingState.postValue(null);
    }

    public void saveImage(Bitmap bitmap, File outFile, final Uri outUri) {
        SaveImageTask task = new SaveImageTask(bitmap, outFile);
        task.setCallback(new SaveImageTask.MainThreadCallback<Void>() {
            @Override
            public void onPreExecute() {
                savingState.setValue(SavingState.ofInProgress());
            }

            @Override
            public void onSuccess(Void x) {
                savingState.setValue(SavingState.ofSuccess(outUri));
            }

            @Override
            public void onError(Exception e) {
                savingState.setValue(SavingState.ofError(e));
            }

            @Override
            public void onCancel() {
                resetSavingState();
            }
        });
        task.execute();
    }

    final static class SavingState {
        /**
         * 成功時に入れられる。
         */
        public final @Nullable
        Uri uri;

        /**
         * 失敗時に入れられる
         */
        public final @Nullable
        Exception error;

        public SavingState(@Nullable Uri uri, @Nullable Exception error) {
            this.uri = uri;
            this.error = error;
        }

        public static SavingState ofInProgress() {
            return new SavingState(null, null);
        }

        public static SavingState ofSuccess(@NonNull Uri uri) {
            return new SavingState(uri, null);
        }

        public static SavingState ofError(@NonNull Exception e) {
            return new SavingState(null, e);
        }

        public final boolean isInProgress() {
            return uri == null && error == null;
        }

        public final boolean isSuccess() {
            return uri != null && error == null;
        }

        public final boolean isError() {
            return uri == null && error != null;
        }

        @Override
        public String toString() {
            return "SavingState{" +
                    "uri=" + uri +
                    ", error=" + error +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SavingState that = (SavingState) o;

            if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
            return error != null ? error.equals(that.error) : that.error == null;
        }

        @Override
        public int hashCode() {
            int result = uri != null ? uri.hashCode() : 0;
            result = 31 * result + (error != null ? error.hashCode() : 0);
            return result;
        }
    }
}
