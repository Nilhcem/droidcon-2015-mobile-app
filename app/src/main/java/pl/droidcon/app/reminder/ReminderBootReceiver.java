package pl.droidcon.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pl.droidcon.app.dagger.DroidconInjector;
import timber.log.Timber;


public class ReminderBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received boot");
        DroidconInjector.get().sessionReminder().restoreReminders();
    }
}
