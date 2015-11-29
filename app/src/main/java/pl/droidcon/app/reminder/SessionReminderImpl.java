package pl.droidcon.app.reminder;


import android.support.annotation.NonNull;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.model.api.Session;
import rx.Subscriber;
import timber.log.Timber;

public class SessionReminderImpl implements SessionReminder {

    @Inject
    ReminderPersistence persistence;

    @Inject
    Reminder reminder;

    public SessionReminderImpl() {
        DroidconInjector.get().inject(this);
    }

    @Override
    public boolean isReminding() {
        return persistence.isReminding();
    }

    @Override
    public void setReminding(boolean reminding) {
        persistence.setReminding(reminding);
        if (reminding) {
            setRemindForAllScheduledSessions();
        } else {
            cancelRemindForAllScheduledSessions();
        }
    }

    @Override
    public void addSessionToReminding(@NonNull Session session) {
        persistence.addSessionToReminding(session);
        if (isReminding()) {
            reminder.setRemind(session);
        }
    }

    @Override
    public void removeSessionFromReminding(@NonNull Session session) {
        persistence.removeSessionFromReminding(session);
        reminder.removeRemind(session);
    }

    @Override
    public void restoreReminders() {
        if (isReminding()) {
            setRemindForAllScheduledSessions();
        }
    }

    private void setRemindForAllScheduledSessions() {
        persistence.sessionsToRemind(new Subscriber<Session>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Session session) {
                reminder.setRemind(session);
            }
        });
    }

    private void cancelRemindForAllScheduledSessions() {
        persistence.sessionsToRemind(new Subscriber<Session>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Session session) {
                reminder.removeRemind(session);
            }
        });
    }
}
