package andreyv.beonline.gcm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class PushActivity extends TrackedActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push);

        Intent intent = getIntent();
        String message = intent.getStringExtra(Constants.KEY_MESSAGE_BODY);

        WebView messageView = (WebView) findViewById(R.id.push_message);
        messageView.loadData(message, "text/html", "UTF8");
    }

    @SuppressWarnings("UnusedParameters")
    public void goBack(View view) {
        Intent intent = new Intent(PushActivity.this, HelloActivity.class);
        startActivity(intent);
    }
}