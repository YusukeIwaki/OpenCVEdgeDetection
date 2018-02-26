package io.github.yusukeiwaki.opencvedgedetection.presentation.entrypoint;

import io.github.yusukeiwaki.opencvedgedetection.presentation.base.BaseNoDisplayActivity;
import io.github.yusukeiwaki.opencvedgedetection.presentation.main.MainActivity;

/**
 * MainActivityに遷移する
 */
public class EntryPointActivity extends BaseNoDisplayActivity {
    @Override
    protected void doAction() {
        startActivity(MainActivity.newIntent(this));
    }
}
