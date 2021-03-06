package pl.droidcon.app.reminder;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import pl.droidcon.app.model.api.Session;
import timber.log.Timber;

public class ReminderImpl implements Reminder {

    private AlarmManager alarmManager;

    private Context context;

    public ReminderImpl(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void setRemind(@NonNull Session session) {
        PendingIntent pendingIntent = createIntentForRemind(session);
        DateTime now = DateTime.now();
        DateTime sessionDate = session.date.minusMinutes(1);//now.plusSeconds(10);//
        if (!sessionDate.isAfter(now)) {
            Timber.w("Not setting reminder for passed session");
            return;
        }
        Timber.d("Setting reminder on %s", sessionDate);
        alarmManager.set(AlarmManager.RTC_WAKEUP, sessionDate.getMillis(), pendingIntent);
    }

    @Override
    public void removeRemind(@NonNull Session session) {
        PendingIntent pendingIntent = createIntentForRemind(session);
        alarmManager.cancel(pendingIntent);
    }

    private PendingIntent createIntentForRemind(Session session) {
        Intent intent = ReminderReceiver.createReceiverIntent(context, session);
        return PendingIntent.getBroadcast(context, session.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
