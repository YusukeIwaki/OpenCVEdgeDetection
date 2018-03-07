package io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * WRITE_EXTERNAL_STORAGE 権限をリクエストする
 */
public class RuntimePermissionHelper {
    private static final String PERMISSION = permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_CODE = 12;

    public interface Callback {
        /**
         * 承認済み、もしくは承認された
         */
        void onPermissionGranted();

        /**
         * 承認されなかった
         */
        void onPermissionDenied();
    }
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void requestPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            notifyPermissionGranted();
            return;
        }
        ActivityCompat.requestPermissions(activity, new String[] { PERMISSION }, REQUEST_CODE);
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            return handlePermissionResult(permissions, grantResults);
        }
        return false;
    }

    private boolean handlePermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        int i = 0;
        for (String permission : permissions) {
            if (PERMISSION.equals(permission)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    notifyPermissionGranted();
                } else {
                    notifyPermissionDenied();
                }
                return true;
            }
            i++;
        }
        return false;
    }

    private void notifyPermissionGranted() {
        if (callback != null) {
            callback.onPermissionGranted();
        }
    }

    private void notifyPermissionDenied() {
        if (callback != null) {
            callback.onPermissionDenied();
        }
    }
}
