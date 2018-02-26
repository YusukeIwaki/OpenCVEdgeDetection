package io.github.yusukeiwaki.opencvedgedetection.presentation.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * NoDisplay指定するActivityのベースクラス
 */
public abstract class BaseNoDisplayActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doAction();
        finish();
    }

    /**
     * NoDisplayのアクティビティでやることを定義する
     */
    protected abstract void doAction();
}
