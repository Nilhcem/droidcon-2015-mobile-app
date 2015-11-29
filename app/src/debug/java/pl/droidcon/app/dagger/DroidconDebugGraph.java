package pl.droidcon.app.dagger;

import pl.droidcon.app.DroidconDebugApp;

public interface DroidconDebugGraph extends DroidconGraph {

    void inject(DroidconDebugApp app);
}
