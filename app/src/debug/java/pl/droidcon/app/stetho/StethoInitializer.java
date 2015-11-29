package pl.droidcon.app.stetho;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.facebook.stetho.timber.StethoTree;
import com.squareup.okhttp.OkHttpClient;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import javax.inject.Inject;

import timber.log.Timber;

public class StethoInitializer {

    private final Context context;
    private final OkHttpClient httpClient;

    @Inject
    public StethoInitializer(Context context, OkHttpClient httpClient) {
        this.context = context;
        this.httpClient = httpClient;
    }

    public void init() {
        Timber.plant(new StethoTree());
        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                        .build());
        httpClient.networkInterceptors().add(new StethoInterceptor());
    }
}
