package dreamfuture.iptv;

/**
 * Created by STAR-Z on 2017-09-16.
 */

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by JongDaesong on 2017.06.30.
 */

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
