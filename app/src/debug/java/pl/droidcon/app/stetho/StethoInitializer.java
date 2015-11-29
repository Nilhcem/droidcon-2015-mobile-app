package pl.droidcon.app.stetho;

import android.content.Context;

import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.facebook.stetho.timber.StethoTree;
import com.squareup.okhttp.OkHttpClient;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class StethoInitializer implements DumperPluginsProvider {

    private final Context context;
    private final OkHttpClient httpClient;
    private final AppDumperPlugin appDumper;

    @Inject
    public StethoInitializer(Context context, OkHttpClient httpClient, AppDumperPlugin appDumper) {
        this.context = context;
        this.httpClient = httpClient;
        this.appDumper = appDumper;
    }

    public void init() {
        Timber.plant(new StethoTree());
        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableDumpapp(this)
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                        .build());
        httpClient.networkInterceptors().add(new StethoInterceptor());
    }

    @Override
    public Iterable<DumperPlugin> get() {
        List<DumperPlugin> plugins = new ArrayList<>();
        for (DumperPlugin plugin : Stetho.defaultDumperPluginsProvider(context).get()) {
            plugins.add(plugin);
        }
        plugins.add(appDumper);
        return plugins;
    }
}
