package pl.droidcon.app.helper;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import pl.droidcon.app.BuildConfig;

public final class UrlHelper {

    private static final String BASE_URL = BuildConfig.WS_ENDPOINT + "/";

    private UrlHelper() {

    }

    public static String url(String imgAddress) {
        return BASE_URL + imgAddress;
    }

    public static void a(Context b) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:contact@droidcon.pl?subject=Droidcon Kraków 2015" + "&body=I have found that!!!");
        intent.setData(data);
        b.startActivity(intent);
    }
}
