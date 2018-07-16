package com.top1.videodownloader;


import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat {


    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
//        Preference rate = findPreference(getString(R.string.key_rating));
//        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                launchMarket();
//                return true;
//            }
//        });
//
//        Preference feedback = findPreference(getString(R.string.key_feedback));
//        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                launchFeedback();
//                return true;
//            }
//        });
//
        Preference download_folder = findPreference(getString(R.string.key_download_folder));
        download_folder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                return true;
            }
        });

        Preference share = findPreference(getString(R.string.key_share));
        share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //https://play.google.com/store/apps/details?id=com.freesocial.videodownloader
                launchShare();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    private  void launchShare()
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
//        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
        i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
        startActivity(Intent.createChooser(i, "Share URL"));
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "Unable to find market app", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.dev_email)});
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback " + getString(R.string.app_name));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(intent, "Send"));
    }


}
