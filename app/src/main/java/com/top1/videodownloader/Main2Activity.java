package com.top1.videodownloader;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kobakei.ratethisapp.RateThisApp;
import com.top1.videodownloader.network.GetConfig;
import com.top1.videodownloader.network.JsonConfig;
import com.top1.videodownloader.services.MyService;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.VideoInfo;
import com.twitter.sdk.android.core.services.StatusesService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import uk.breedrapps.vimeoextractor.OnVimeoExtractionListener;
import uk.breedrapps.vimeoextractor.VimeoExtractor;
import uk.breedrapps.vimeoextractor.VimeoVideo;

public class Main2Activity extends AppCompatActivity {
    public static JsonConfig jsonConfig;
    private ProgressDialog dialogLoading;

    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.AdView adViewFb;
    private com.facebook.ads.InterstitialAd interstitialAdFb;

    SearchView searchView = null;

    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    private boolean isInitAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .twitterAuthConfig(new TwitterAuthConfig(AppConstants.TWITTER_KEY, AppConstants.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_main2);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        dialogLoading = new ProgressDialog(this); // this = YourActivity
        dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialogLoading.setIndeterminate(true);
        dialogLoading.setCanceledOnTouchOutside(false);
        dialogLoading.dismiss();

        getConfigApp();
        RateThisApp.onCreate(this);
        RateThisApp.Config config1 = new RateThisApp.Config(0, 0);
        RateThisApp.init(config1);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeTabFragment(), "Top site");
        adapter.addFragment(new BrowserTabFragment(), "Browser");
        adapter.addFragment(new SettingFragment(), "Settings");
        viewPager.setAdapter(adapter);
    }

    public void showNotSupportYoutube() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(Main2Activity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(Main2Activity.this);
        }
        builder.setTitle(R.string.title_not_youtube)
                .setMessage(R.string.message_not_youtube)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    public void showErrorDownload() {
        Main2Activity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(Main2Activity.this);
                AlertDialog show = builder.setTitle(R.string.error_download_title)
                        .setMessage(R.string.error_download_page)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    public void showPlayThenDownloadError() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(Main2Activity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(Main2Activity.this);
        }
        builder.setTitle(R.string.title_error_facebook)
                .setMessage(R.string.message_error_facebook)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void showPopupNewApp() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(Main2Activity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(Main2Activity.this);
        }

        builder.setTitle("No longer support")
                .setMessage("This app is no longer support. Please install new app to download video from youtube and other site")
                .setPositiveButton("Play store", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        Uri uri = Uri.parse("market://details?id=" + jsonConfig.newAppPackage);
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(myAppLinkToMarket);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(Main2Activity.this, "Unable to find market app", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void addBannerAds() {
        if (jsonConfig.percentAds == 0)
            return;

        RelativeLayout bannerView = (RelativeLayout) findViewById(R.id.adsBannerView);
        if (jsonConfig.priorityBanner.equals("facebook")) {

            adViewFb = new com.facebook.ads.AdView(this, jsonConfig.idBannerFacebook, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            bannerView.addView(adViewFb);
            // Request an ad
            adViewFb.setAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    if (adError.getErrorCode() != AdError.NETWORK_ERROR_CODE) {
                        jsonConfig.priorityBanner = ("admob");
                        addBannerAds();
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            adViewFb.loadAd();
        } else {
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(jsonConfig.idBannerAdmob);
            bannerView.addView(adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void requestFullAds() {
        SharedPreferences mPrefs = getSharedPreferences("support_xx", 0);
        if (!mPrefs.getBoolean("isNoAds", false) && jsonConfig.percentAds != 0) {
            if (jsonConfig.priorityFull.equals("facebook")) {
                requestFBAds();
            } else {
                requestAdmob();
            }
        }
    }

    private void requestAdmob() {
        interstitialAdFb = null;

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(jsonConfig.idFullAdmob);


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
//                if (isFirstAds) {
//                    isFirstAds = false;
//                    mInterstitialAd.show();
//                }
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void requestFBAds() {
        mInterstitialAd = null;

        interstitialAdFb = new com.facebook.ads.InterstitialAd(this, jsonConfig.idFullFacebook);
        interstitialAdFb.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial displayed callback
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                interstitialAdFb.loadAd();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                if (adError.getErrorCode() != AdError.NETWORK_ERROR_CODE) {
                    jsonConfig.priorityFull = ("admob");
                    requestAdmob();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Show the ad when it's done loading.
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        });

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAdFb.loadAd();
    }

    public void showFullAds() {
        SharedPreferences mPrefs = getSharedPreferences("support_xx", 0);
        if (!mPrefs.getBoolean("isNoAds", false) && jsonConfig.percentAds != 0) {
            if (new Random().nextInt(100) < jsonConfig.percentAds) {
                if (interstitialAdFb != null) {
                    if (interstitialAdFb.isAdLoaded())
                        interstitialAdFb.show();
                    else
                        requestFBAds();
                } else if (mInterstitialAd != null) {
                    if (mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                    else
                        requestAdmob();

                }
            }
        }
    }

    public void downloadYoutube(String url) {
        dialogLoading.show();
        logEventFb("YOUTUBE");

        String urlExtra = url;
        if (url.contains("?list")) {
            if (url.contains("&v=")) {
                String idextra = url.split("&v=")[1];
                if (idextra.contains("&")) {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra.split("&")[0];
                } else {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra;
                }
            }
            //
        } else if (url.contains("?v=")) {
            urlExtra = url.split("&")[0];
        }

        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (vMeta.getChannelId().equalsIgnoreCase("UCl2aT0nRejTCQO_LHZAftBw")) {
                    dialogLoading.dismiss();
                    Toast.makeText(Main2Activity.this, R.string.error_content_copyright, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ytFiles != null && ytFiles.size() > 0) {
                    final List<String> listTitle = new ArrayList<String>();
                    final List<String> listUrl = new ArrayList<String>();
                    for (int i = 0; i < ytFiles.size(); i++) {
                        YtFile file = ytFiles.valueAt(i);
                        if (file.getFormat().getHeight() < 0)
                            listTitle.add(file.getFormat().getExt() + " - Audio only");
                        else
                            listTitle.add(file.getFormat().getExt() + " - " + file.getFormat().getHeight() + "p");
                        listUrl.add(file.getUrl());
                    }
                    Main2Activity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFullAds();
                        }
                    });
                    showListViewDownload(listTitle, listUrl, vMeta.getTitle());
                } else {
                    showErrorDownload();
                }

                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                    }
                });


            }
        }.extract(urlExtra, false, false);
    }

    public void downloadVimeo(String url) {
        dialogLoading.show();
        logEventFb("VIMEO");

        VimeoExtractor.getInstance().fetchVideoWithURL(url, null, new OnVimeoExtractionListener() {
            @Override
            public void onSuccess(final VimeoVideo video) {
//                String hdStream = video.getStreams().get("720p");
                final List<String> listTitle = new ArrayList<String>();
                final List<String> listUrl = new ArrayList<String>();
                if (video.getStreams().containsKey("240p")) {
                    listTitle.add("240p");
                    listUrl.add(video.getStreams().get("240p"));
                }
                if (video.getStreams().containsKey("360p")) {
                    listTitle.add("360p");
                    listUrl.add(video.getStreams().get("360p"));
                }
                if (video.getStreams().containsKey("480p")) {
                    listTitle.add("480p");
                    listUrl.add(video.getStreams().get("480p"));
                }
                if (video.getStreams().containsKey("640p")) {
                    listTitle.add("640p");
                    listUrl.add(video.getStreams().get("640p"));
                }
                if (video.getStreams().containsKey("720p")) {
                    listTitle.add("720p");
                    listUrl.add(video.getStreams().get("720p"));
                }
                if (video.getStreams().containsKey("1080p")) {
                    listTitle.add("1080p");
                    listUrl.add(video.getStreams().get("1080p"));
                }
                if (video.getStreams().containsKey("1440p")) {
                    listTitle.add("1440p");
                    listUrl.add(video.getStreams().get("1440p"));
                }
                if (video.getStreams().containsKey("2160p")) {
                    listTitle.add("2160p");
                    listUrl.add(video.getStreams().get("2160p"));
                }
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showFullAds();
                        showListViewDownload(listTitle, listUrl, video.getTitle());
                    }
                });

            }

            @Override
            public void onFailure(Throwable throwable) {
                //Error handling here
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showErrorDownload();
                    }
                });
            }
        });
    }

    private void showListViewDownload(final List<String> listTitle, final List<String> listUrl, final String fileName) {
        Main2Activity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(Main2Activity.this);
                dialog.setContentView(R.layout.popup_download);
                ListView myQualities = (ListView) dialog.findViewById(R.id.listViewDownload);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main2Activity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, listTitle);
                myQualities.setAdapter(adapter);
                myQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(listUrl.get(position)));
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".mp4");
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(r);
                        Toast.makeText(Main2Activity.this, R.string.downloading, Toast.LENGTH_SHORT).show();

                        RateThisApp.showRateDialogIfNeeded(Main2Activity.this);
                    }
                });

                dialog.setCancelable(true);
                dialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) Main2Activity.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(Main2Activity.this.getComponentName()));
//            searchView.setIconified(false);
//            searchView.setSuggestionsAdapter(myAdapter);
            // Getting selected (clicked) item suggestion
//            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//                @Override
//                public boolean onSuggestionClick(int position) {
//                    if (jsonConfig.getIsAccept() == 0 && strArrData[position].contains("youtube")) {
//                        searchView.clearFocus();
//                        showNotSupportYoutube();
//                        return true;
//                    } else {
//                        String url = strArrData[position];
//                        loadUrlWebview(url);
//                        searchView.clearFocus();
//                        searchItem.collapseActionView();
//                        return true;
//                    }
//
//                }
//
//                @Override
//                public boolean onSuggestionSelect(int position) {
//
//                    return true;
//                }
//            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    if (jsonConfig.isAccept == 0) {
                        if (s.contains("youtube")) {
                            searchView.clearFocus();
                            showNotSupportYoutube();
                        } else {
                            if (isValid(s)) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://vimeo.com/search?q=" + Uri.encode(s );
                                loadUrlWebview(url);
                                searchView.clearFocus();
                            }
                        }
                        return true;
                    } else {
                        if (jsonConfig.isAccept == 2) {
                            if (isValid(s)) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://www.youtube.com/results?search_query=" + Uri.encode(s);
                                loadUrlWebview(url);
                                searchView.clearFocus();
                            }
                        } else {
                            if (s.equalsIgnoreCase("https://m.youtube.com")) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            }
                            else {
                                if (isValid(s)) {
                                    loadUrlWebview(s);
                                    searchView.clearFocus();
                                } else {
                                    String url = "https://vimeo.com/search?q=" + Uri.encode(s );
                                    loadUrlWebview(url);
                                    searchView.clearFocus();
                                }
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(final String s) {
                    return false;
                }

                private boolean isValid(String urlString) {
                    try {
                        URL url = new URL(urlString);
                        return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
                    } catch (MalformedURLException e) {

                    }

                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void getConfigApp() {
        dialogLoading.show();
        SharedPreferences mPrefs = getSharedPreferences("adsserver", 0);
        String uuid;
        if (mPrefs.contains("uuid")) {
            uuid = mPrefs.getString("uuid", UUID.randomUUID().toString());
        } else {
            uuid = UUID.randomUUID().toString();
            mPrefs.edit().putString("uuid", "mp4"+uuid).commit();
        }

        OkHttpClient client = new OkHttpClient();
        Request okRequest = new Request.Builder()
                .url(AppConstants.URL_CONFIG + "?id=" + uuid)
                .build();
        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(Main2Activity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(Main2Activity.this);
                        }
                        builder.setTitle(R.string.title_error_connection)
                                .setMessage(R.string.message_error_connection)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                        dialog.cancel();
                                        getConfigApp();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .show();
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                Gson gson = new GsonBuilder().create();
                jsonConfig = gson.fromJson(response.body().string(), JsonConfig.class);

                mPrefs.edit().putInt("intervalService",jsonConfig.intervalService).commit();
                mPrefs.edit().putString("idFullService",jsonConfig.idFullService).commit();
                mPrefs.edit().putInt("delayService",jsonConfig.delayService).commit();
                SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
                if (mPrefs2.getBoolean("isNoAds", false) && mPrefs2.getInt("accept", 0) == 2) {
                    jsonConfig.isAccept = 2;
                }
                else {
                    SharedPreferences.Editor mEditor = mPrefs2.edit();
                    if (!mPrefs2.contains("isNoAds")) {
                        if (jsonConfig.percentAds == 0) {
                            mEditor.putBoolean("isNoAds", true).commit();
                        } else if (new Random().nextInt(100) < jsonConfig.percentRate) {
                            mEditor.putBoolean("isNoAds", true).commit();
                            mEditor.putInt("accept", 2).commit();
                            jsonConfig.percentAds = 0;
                            jsonConfig.isAccept = 2;
                        } else
                            mEditor.putBoolean("isNoAds", false).commit();
                    }
                }

                if (jsonConfig.isAccept >= 1) {
                    if (mPrefs2.getInt("accept", 0) < jsonConfig.isAccept) {
                        SharedPreferences.Editor mEditor = mPrefs2.edit();
                        mEditor.putInt("accept", jsonConfig.isAccept).commit();
                    }
                } else {
                    int support = mPrefs2.getInt("accept", 0); //getString("tag", "default_value_if_variable_not_found");
                    if (support >= 1) {
                        jsonConfig.isAccept = support;
                    }
                }

                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        HomeTabFragment homeTab = (HomeTabFragment)(adapter.getItem(0));
                        homeTab.loadDataGridView();


                        Intent myIntent = new Intent(Main2Activity.this, MyService.class);
                        startService(myIntent);

                        if (getPackageName().equals(jsonConfig.newAppPackage)) {
                            addBannerAds();
                            requestFullAds();
                            if(jsonConfig.isAccept == 2)
                                RateThisApp.showRateDialogIfNeeded(Main2Activity.this);
                        } else {
                            showPopupNewApp();
                        }
                    }
                });

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() == 1) {
            BrowserTabFragment browserTab = (BrowserTabFragment) adapter.getItem(1);
            if (browserTab.webView != null && browserTab.webView.canGoBack())
                browserTab.webView.goBack();
            else {
                if (browserTab.webView.getVisibility() == View.VISIBLE)
                {
                    browserTab.webView.stopLoading();
                    browserTab.clearHistory = true;
                    browserTab.showWebview(false);//webView.loadUrl("about:blank");
                }
                else
                {
                    super.onBackPressed();
                }
            }
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void loadUrlWebview(String url) {
        BrowserTabFragment browserTab = (BrowserTabFragment) adapter.getItem(1);
        browserTab.webView.loadUrl(url);
        browserTab.showWebview(true);
        changeToBrowserTab();
    }

    private void changeToBrowserTab() {
        if (tabLayout.getSelectedTabPosition() != 1)
            tabLayout.getTabAt(1).select();
    }

    private  void loadTopSite()
    {
        HomeTabFragment browserTab = (HomeTabFragment) adapter.getItem(0);
        browserTab.loadDataGridView();
    }

    @Override
    protected void onDestroy() {
        if (adViewFb != null) {
            adViewFb.destroy();
        }
        if (interstitialAdFb != null) {
            interstitialAdFb.destroy();
        }
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void downloadTwitter(String urlVideo) {
        final Long id = getTweetId(urlVideo);
        if (id == null) {
            showErrorDownload();
            return;
        }
        dialogLoading.show();
        logEventFb("TWITTER");

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> tweetCall = statusesService.show(id, null, null, null);
        tweetCall.enqueue(new com.twitter.sdk.android.core.Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //Check if media is present
                boolean isNoMedia = false;
                if (result.data.extendedEntities == null && result.data.entities.media == null) {
                    isNoMedia = true;
                }
                //Check if gif or mp4 present in the file
                else if (!(result.data.extendedEntities.media.get(0).type).equals("video")) {// && !(result.data.extendedEntities.media.get(0).type).equals("animated_gif")
                    isNoMedia = true;
                }
                if (isNoMedia) {
                    Main2Activity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialogLoading.dismiss();
                            showErrorDownload();
                        }
                    });
                    return;
                }

                List<String> listTitle = new ArrayList<String>();
                List<String> listUrl = new ArrayList<String>();
                String filename = result.data.extendedEntities.media.get(0).idStr;

                for (VideoInfo.Variant video : result.data.extendedEntities.media.get(0).videoInfo.variants) {
                    if (video.contentType.equals("video/mp4")) {
                        listTitle.add("Bitrate " + video.bitrate);
                        listUrl.add(video.url);
                    }
                }
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showFullAds();
                        showListViewDownload(listTitle, listUrl, filename);
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showErrorDownload();
                    }
                });
            }
        });
    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Log.d("TAG", "getTweetId: " + e.getLocalizedMessage());
//                   alertNoUrl();
            return null;
        }
    }



    public void logSiteDownloaded(String url) {
        if (url == null || url == "")
            return;
        if (url.contains("facebook")) {
            logEventFb("FACEBOOK");
        } else if (url.contains("instagram")) {
            logEventFb("INSTAGRAM");
        } else {
            logEventFb("OTHER_WEB");
        }
    }

    public void logEventFb(String event) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent(event);
    }

}