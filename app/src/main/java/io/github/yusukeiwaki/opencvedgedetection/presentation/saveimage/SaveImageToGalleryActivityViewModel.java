package io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SaveImageToGalleryActivityViewModel extends ViewModel {
    private final MutableLiveData<SavingState> savingState = new MutableLiveData<>();

    public final LiveData<SavingState> savingState() {
        // MutableLiveDataをimmutableにすることはできないので、
        // LiveData型として返すことでsetValue/postValueメソッドを隠す
        return savingState;
    }

    public final void resetSavingState() {
        savingState.postValue(null);
    }

    final static class SavingState {
        /**
         * 成功時に入れられる。
         */
        public final @Nullable String outUrl;

        /**
         * 失敗時に入れられる
         */
        public final @Nullable
        Exception error;

        public SavingState(@Nullable String outUrl, @Nullable Exception error) {
            this.outUrl = outUrl;
            this.error = error;
        }

        public static SavingState ofInProgress() {
            return new SavingState(null, null);
        }

        public static SavingState ofSuccess(@NonNull String outUrl) {
            return new SavingState(outUrl, null);
        }

        public static SavingState ofError(@NonNull Exception e) {
            return new SavingState(null, e);
        }

        public final boolean isInProgress() {
            return outUrl == null && error == null;
        }

        public final boolean isSuccess() {
            return outUrl != null && error == null;
        }

        public final boolean isError() {
            return outUrl == null && error != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SavingState that = (SavingState) o;

            if (outUrl != null ? !outUrl.equals(that.outUrl) : that.outUrl != null) return false;
            return error != null ? error.equals(that.error) : that.error == null;
        }

        @Override
        public int hashCode() {
            int result = outUrl != null ? outUrl.hashCode() : 0;
            result = 31 * result + (error != null ? error.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SavingState{" +
                    "outUrl='" + outUrl + '\'' +
                    ", error=" + error +
                    '}';
        }
    }

    public void saveImageToGallery(Context context, Uri imageUri) {
        SaveImageTask task = new SaveImageTask(context, imageUri);
        task.setCallback(new SaveImageTask.MainThreadCallback<String>() {
            @Override
            public void onPreExecute() {
                savingState.setValue(SavingState.ofInProgress());
            }

            @Override
            public void onSuccess(String outUrl) {
                savingState.setValue(SavingState.ofSuccess(outUrl));
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
}
