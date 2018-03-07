package io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.widget.Toast;

import io.github.yusukeiwaki.opencvedgedetection.presentation.base.BaseActivity;
import io.github.yusukeiwaki.opencvedgedetection.presentation.saveimage.SaveImageToGalleryActivityViewModel.SavingState;

public class SaveImageToGalleryActivity extends BaseActivity {

    private SaveImageToGalleryActivityViewModel viewModel;
    private ProgressDialog progressDialog;
    private RuntimePermissionHelper runtimePermissionHelper;

    public static Intent newIntent(Context context, Uri imageUri) {
        Intent intent = new Intent(context, SaveImageToGalleryActivity.class);
        intent.setData(imageUri);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SaveImageToGalleryActivityViewModel.class);
        viewModel.resetSavingState();
        viewModel.savingState().observe(this, new Observer<SavingState>() {
            @Override
            public void onChanged(@Nullable SavingState savingState) {
                if (savingState != null && savingState.isInProgress()) {
                    if (progressDialog == null) {
                        progressDialog = createProgressDialog();
                    }
                    progressDialog.show();
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }

                if (savingState != null && savingState.isSuccess()) {
                    onComplete();
                    viewModel.resetSavingState();
                }

                if (savingState != null && savingState.isError()) {
                    showError(savingState.error);
                    viewModel.resetSavingState();
                }
            }
        });
        runtimePermissionHelper = new RuntimePermissionHelper();
        runtimePermissionHelper.setCallback(new RuntimePermissionHelper.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPermissionGranted() {
                requestSave();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(SaveImageToGalleryActivity.this, "パーミッションの許可が必要です", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        runtimePermissionHelper.requestPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (runtimePermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            // handled. do nothing.
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
    private void requestSave() {
        Uri imageUri = parseImageUri();
        if (imageUri != null) {
            viewModel.saveImageToGallery(this, imageUri);
        }
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("ギャラリーに保存");
        progressDialog.setMessage("保存中...");
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                onUserCancel();
            }
        });
        return progressDialog;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private @Nullable
    Uri parseImageUri() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        } else {
            return intent.getData();
        }
    }

    private void onComplete() {
        Toast.makeText(this, "保存完了しました", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onUserCancel() {
        finish();
    }

    private void showError(Exception error) {
        Toast.makeText(this, "保存に失敗しました", Toast.LENGTH_SHORT).show();
        finish();
    }
}
