package pl.droidcon.app.stetho;

import android.app.Activity;

import com.facebook.stetho.dumpapp.DumpException;
import com.facebook.stetho.dumpapp.DumperContext;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;

import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.lifecycle.ActivityProvider;
import pl.droidcon.app.model.api.Session;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.ui.activity.SessionActivity;
import rx.Subscriber;

public class AppDumperPlugin implements DumperPlugin {

    private final DatabaseManager dbManager;
    private final ActivityProvider activityProvider;

    @Inject
    public AppDumperPlugin(DatabaseManager dbManager, ActivityProvider activityProvider) {
        this.dbManager = dbManager;
        this.activityProvider = activityProvider;
    }

    @Override
    public String getName() {
        return "droidcon";
    }

    @Override
    public void dump(DumperContext dumpContext) throws DumpException {
        final PrintStream writer = dumpContext.getStdout();
        List<String> args = dumpContext.getArgsAsList();
        String commandName = (args.isEmpty()) ? "" : args.remove(0);

        switch (commandName) {
            case "day1":
                displaySessionsForDay(0, writer);
                break;
            case "day2":
                displaySessionsForDay(1, writer);
                break;
            case "currentSession":
                displayCurrentSessionData(writer);
                break;
            default:
                doUsage(writer);
                break;
        }
    }

    private void displaySessionsForDay(int day, final PrintStream writer) {
        dbManager.sessions(SessionDay.values()[day])
                .subscribe(new Subscriber<List<Session>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        writer.println("Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(List<Session> sessions) {
                        for (Session session : sessions) {
                            writer.println(session.title);
                        }
                    }
                });
    }

    private void displayCurrentSessionData(PrintStream writer) {
        Activity activity = activityProvider.getCurrentActivity();
        if (activity instanceof SessionActivity) {
            try {
                // Use reflection to access private "session" field
                Field field = SessionActivity.class.getDeclaredField("session");
                field.setAccessible(true);
                Session session = (Session) field.get(activity);

                DateTime date = session.date;
                session.date = null;
                writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(session));
                session.date = date;
            } catch (Exception e) {
                writer.println(e.getMessage());
            }
        } else {
            writer.println("SessionActivity not visible");
        }
    }

    private void doUsage(PrintStream writer) {
        writer.println("usage: dumpapp [arg]");
        writer.println();
        writer.println("arg:");
        writer.println("* day1: Display sessions for day 1");
        writer.println("* day2: Display sessions for day 2");
        writer.println("* currentSession: Display data about the current session visible on the screen (SessionActivity)");
    }
}
