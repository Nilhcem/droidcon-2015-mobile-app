package pl.droidcon.app;

import android.os.Build;

import javax.inject.Inject;

import jp.wasabeef.takt.Takt;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.lifecycle.ActivityProvider;
import pl.droidcon.app.stetho.StethoInitializer;

public class DroidconDebugApp extends DroidconApp {

    /**
     * Change it manually when you want to display the FPS.
     * Useful to test the frame rate.
     */
    private static final boolean DISPLAY_FPS = false;

    @Inject
    StethoInitializer stetho;

    @Inject
    ActivityProvider activityProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        DroidconInjector.get().inject(this);
        displayFps(true);
        stetho.init();
        activityProvider.init(this);
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
