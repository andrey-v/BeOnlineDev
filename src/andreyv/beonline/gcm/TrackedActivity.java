package andreyv.beonline.gcm;

import android.app.Activity;
import android.os.Bundle;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackedActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState, String tag) {
        super.onCreate(savedInstanceState);
        EasyTracker.getInstance().setContext(this);
    }

    @Override
    protected void onStart() {
        if(getResources().getBoolean(R.bool.analytics_enabled)) {
            EasyTracker.getInstance().activityStart(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
