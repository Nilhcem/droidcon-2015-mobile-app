package pl.droidcon.app.reminder;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.model.api.Session;
import pl.droidcon.app.model.common.SessionNotification;
import pl.droidcon.app.model.db.RealmSessionNotification;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ReminderPersistenceImpl implements ReminderPersistence {

    private static final String SHARED_PREFERENCES_NAME = "reminder";
    private static final String REMINDING_KEY = "reminding";

    private SharedPreferences sharedPreferences;

    @Inject
    DatabaseManager databaseManager;

    public ReminderPersistenceImpl(Context context) {
        DroidconInjector.get().inject(this);
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isReminding() {
        return sharedPreferences.getBoolean(REMINDING_KEY, true);
    }

    @Override
    public void setReminding(boolean reminding) {
        sharedPreferences.edit()
                .putBoolean(REMINDING_KEY, reminding)
                .apply();
    }

    @Override
    public void addSessionToReminding(@NonNull final Session session) {
        databaseManager.addToNotification(SessionNotification.of(session))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RealmSessionNotification>() {
                    @Override
                    public void call(RealmSessionNotification sessionNotification) {
                        Timber.d("Added session %s to notifications", session.title);
                    }
                });
    }

    @Override
    public void removeSessionFromReminding(@NonNull final Session session) {
        databaseManager.removeFromNotification(SessionNotification.of(session))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        Timber.d("Removed notification for session %s", session.title);
                    }
                });
    }

    @Override
    public void sessionsToRemind(final Subscriber<? super Session> topSubscriber) {
        databaseManager.notifications()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<List<SessionNotification>>() {
                    @Override
                    public void call(List<SessionNotification> sessionNotifications) {
                        Timber.d("sessions notification size=%d", sessionNotifications.size());
                        for (SessionNotification sessionNotification : sessionNotifications) {
                            databaseManager.session(sessionNotification.getSessionId())
                                    .subscribe(new Action1<Session>() {
                                        @Override
                                        public void call(Session session) {
                                            topSubscriber.onNext(session);
                                        }
                                    });
                        }
                        topSubscriber.onCompleted();
                    }
                });
    }
}
