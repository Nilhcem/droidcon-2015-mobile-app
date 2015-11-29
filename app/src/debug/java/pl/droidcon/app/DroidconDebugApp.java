package pl.droidcon.app;

import android.os.Build;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;

import jp.wasabeef.takt.Takt;
import pl.droidcon.app.dagger.DroidconInjector;

public class DroidconDebugApp extends DroidconApp {

    /**
     * Change it manually when you want to display the FPS.
     * Useful to test the frame rate.
     */
    private static final boolean DISPLAY_FPS = false;

    @Inject OkHttpClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();
        displayFps(true);
        DroidconInjector.get().inject(this);
    }

    @Override
    public void onTerminate() {
        displayFps(false);
        super.onTerminate();
    }

    private void displayFps(boolean enable) {
        if (DISPLAY_FPS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (enable) {
                Takt.stock(this).play();
            } else {
                Takt.finish();
            }
        }
    }
}
