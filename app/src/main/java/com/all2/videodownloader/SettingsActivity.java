package com.all2.videodownloader;

import android.os.Bundle;
//import android.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

//import android.preference.PreferenceActivity;

//import com.top1.videodownloader.R;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceFragmentCompat {
    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */

    @Override
    public  void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
//        setPreferencesFromResource(R.xml.pref_main,rootKey);

    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
//    private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            // Show the Up button in the action bar.
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    public boolean onIsMultiPane() {
//        return isXLargeTablet(this);
//    }
}
