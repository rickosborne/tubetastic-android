package org.rickosborne.tubetastic.android;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.about_version)).setText(versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            // don't care
        }
        catch (NullPointerException e) {
            // still don't care
        }
    }

}
