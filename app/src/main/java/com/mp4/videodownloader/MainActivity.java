package com.mp4.videodownloader;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ConfigurationCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.mp4.videodownloader.network.JsonConfig;
import com.mp4.videodownloader.network.Site;
import com.mp4.videodownloader.services.MyService;
import com.mp4.videodownloader.utils.AppConstants;
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

public class MainActivity extends AppCompatActivity {

    private JsonConfig jsonConfig;
    private WebView webView;
    private ProgressBar webProgress;
    //    private boolean isClearHistory = false;
    private ProgressDialog dialogLoading;
//    private boolean isDownloadFacebook = false;
//    private String urlDownloadFB;

    private SimpleCursorAdapter myAdapter;

    SearchView searchView = null;
    private String[] strArrData = {};
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.AdView adViewFb;
    private com.facebook.ads.InterstitialAd interstitialAdFb;

    private boolean isFirstAds = true;
    private String urlDownloadOther;
    private boolean isClearHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .twitterAuthConfig(new TwitterAuthConfig(AppConstants.TWITTER_KEY, AppConstants.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        webProgress = (ProgressBar) findViewById(R.id.webProgress);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Returning 'false' unconditionally is fine.

                if ((url.contains("https://youtube.com") || url.contains("https://m.youtube.com")) && jsonConfig.isAccept == 0) {
                    showNotSupportYoutube();
                    return true;
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isClearHistory) {
                    isClearHistory = false;
                    webView.clearHistory();
                }

                urlDownloadOther = null;
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.contains(".mp4") || url.contains(".3gp")) {
                    urlDownloadOther = url;
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && webProgress.getVisibility() == ProgressBar.GONE) {
                    webProgress.setVisibility(ProgressBar.VISIBLE);
                }

                webProgress.setProgress(progress);
                if (progress == 100) {
                    webProgress.setProgress(0);
                    webProgress.setVisibility(ProgressBar.GONE);
                }
            }
        });

        dialogLoading = new ProgressDialog(this); // this = YourActivity
        dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialogLoading.setIndeterminate(true);
        dialogLoading.setCanceledOnTouchOutside(false);
        dialogLoading.dismiss();

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.btnDownload);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isStoragePermissionGranted()) {
                    return;
                }
                if (webView.getUrl() == null) {
                    showErrorDownload();
                    return;
                }
                if (!getPackageName().equals(jsonConfig.newAppPackage)) {
                    showPopupNewApp();
                    return;
                }

                if (webView.getUrl().contains("youtube.com")) {
                    downloadYoutube(webView.getUrl());
                } else if (webView.getUrl().contains("vimeo.com")) {
                    downloadVimeo(webView.getUrl());
                } else if (webView.getUrl().contains("twitter.com")) {
                    downloadTwitter(webView.getUrl());
                } else {
                    if (urlDownloadOther == null) {
                        showPlayThenDownloadError();
                    } else {
                        try {
                            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(urlDownloadOther));
                            String fName = UUID.randomUUID().toString();
                            if (urlDownloadOther.endsWith(".mp4")) {
                                fName += ".mp4";
                            } else if (urlDownloadOther.endsWith(".3gp")) {
                                fName += ".3gp";
                            }

                            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fName);
                            r.allowScanningByMediaScanner();
                            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            dm.enqueue(r);
                            logSiteDownloaded();
                            Toast.makeText(MainActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
                            showFullAds();
                        } catch (Exception e) {
                            showPlayThenDownloadError();
                        }

                    }

                }
            }
        });

        final String[] from = new String[]{"fishName"};
        final int[] to = new int[]{android.R.id.text1};

        // setup SimpleCursorAdapter
        myAdapter = new SimpleCursorAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        RateThisApp.onCreate(this);
        RateThisApp.Config config1 = new RateThisApp.Config(0, 0);
        RateThisApp.init(config1);

        getConfigApp();
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
                    MainActivity.this.runOnUiThread(new Runnable() {
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showFullAds();
                        showListViewDownload(listTitle, listUrl, filename + ".mp4");
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        logEventFb("FAIL_TWITTER");
                        showErrorDownload();
                    }
                });
            }
        });
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

    private void addBannerAds() {
        if (jsonConfig.percentAds == 0)
            return;

        RelativeLayout bannerView = (RelativeLayout) findViewById(R.id.adsBannerView);
        if (jsonConfig.priorityBanner.equals("facebook")) {

            adViewFb = new com.facebook.ads.AdView(this, jsonConfig.idBannerFacebook, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            Log.d("idbanner = ", jsonConfig.idBannerFacebook);
            bannerView.addView(adViewFb);
            // Request an ad
            adViewFb.setAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    if (adError.getErrorCode() != AdError.NETWORK_ERROR_CODE) {
                        jsonConfig.priorityBanner = "admob";
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
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(jsonConfig.idFullAdmob);


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
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
//                Log.d("caomui1",adError.getErrorMessage());
                if (adError.getErrorCode() != AdError.NETWORK_ERROR_CODE) {
                    jsonConfig.priorityFull = "admob";
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

    private void showFullAds() {
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

    private void showErrorDownload() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(MainActivity.this);
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

    private void downloadYoutube(String url) {
        dialogLoading.show();

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

                if (ytFiles != null && ytFiles.size() > 0) {
                    List<String> listTitle = new ArrayList<String>();
                    List<String> listUrl = new ArrayList<String>();
                    List<String> listExt = new ArrayList<String>();

                    for (int i = 0; i < ytFiles.size(); i++) {
                        YtFile file = ytFiles.valueAt(i);
                        if (file.getFormat().getHeight() < 0)
                            listTitle.add(file.getFormat().getExt() + " - Audio only");
                        else {
                            String title = file.getFormat().getExt() + " - " + file.getFormat().getHeight() + "p - ";
                            if (file.getFormat().getItag() == 17 || file.getFormat().getItag() == 36 || file.getFormat().getItag() == 5 || file.getFormat().getItag() == 43
                                    || file.getFormat().getItag() == 18 || file.getFormat().getItag() == 22) {
                                title += "With Audio";
                            } else
                                title += "No audio";
                            listTitle.add(title);

                        }
                        listExt.add(file.getFormat().getExt());
                        listUrl.add(file.getUrl());
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialogLoading.dismiss();
                        }
                    });

                    showListViewDownloadYoutube(listTitle, listUrl, listExt, vMeta.getTitle() + "");
                } else {
                    showErrorDownload();
                }


            }
        }.extract(urlExtra, false, false);
    }

    private void showListViewDownloadYoutube(List<String> listTitle, List<String> listUrl, List<String> listExt, String fileName) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.popup_download);
                ListView myQualities = (ListView) dialog.findViewById(R.id.listViewDownload);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, listTitle);
                myQualities.setAdapter(adapter);
                myQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(listUrl.get(position)));
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + listExt.get(position));
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(r);
                        Toast.makeText(MainActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();

                        boolean isShow = RateThisApp.showRateDialogIfNeeded(MainActivity.this);
                        if (!isShow)
                            showFullAds();
                    }
                });

                dialog.setCancelable(true);
                dialog.show();
            }
        });

    }

    private void downloadVimeo(String url) {
        dialogLoading.show();
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

                Log.d("111111", video.getStreams().size() + "");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showFullAds();
                        showListViewDownload(listTitle, listUrl, video.getTitle() + ".mp4");
                    }
                });

            }

            @Override
            public void onFailure(Throwable throwable) {
                //Error handling here
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        showErrorDownload();
                    }
                });
            }
        });
    }

    private void downloadOtherSite(String url) {
        Toast.makeText(this, R.string.error_download_other_site, Toast.LENGTH_SHORT).show();
    }

    private void showListViewDownload(final List<String> listTitle, final List<String> listUrl, final String fileName) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.popup_download);
                ListView myQualities = (ListView) dialog.findViewById(R.id.listViewDownload);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, listTitle);
                myQualities.setAdapter(adapter);
                myQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(listUrl.get(position)));
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(r);
                        Toast.makeText(MainActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();

                        RateThisApp.showRateDialogIfNeeded(MainActivity.this);
                    }
                });

                dialog.setCancelable(true);
                dialog.show();
            }
        });

    }

//    private  void initToolBar()
//    {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        Log.d("hhhhh","new itent");
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            if (searchView != null) {
//                searchView.clearFocus();
//            }
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
//            searchView.setIconified(false);
            AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchAutoCompleteTextView.setThreshold(0);
            searchView.setSuggestionsAdapter(myAdapter);
            // Getting selected (clicked) item suggestion
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionClick(int position) {
                    if (jsonConfig.isAccept == 0 && strArrData[position].contains("youtube")) {
                        searchView.clearFocus();
                        showNotSupportYoutube();
                        return true;
                    } else {
                        String url = strArrData[position];
                        webProgress.setVisibility(ProgressBar.VISIBLE);
                        webView.loadUrl(url);
                        searchView.clearFocus();
                        searchItem.collapseActionView();
                        return true;
                    }

                }

                @Override
                public boolean onSuggestionSelect(int position) {

                    return true;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    if (jsonConfig.isAccept == 0) {
                        if (s.contains("youtube")) {
                            searchView.clearFocus();
                            showNotSupportYoutube();
                        } else {
                            if (isValid(s)) {
                                webProgress.setVisibility(ProgressBar.VISIBLE);
                                webView.loadUrl(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://www.google.com/search?q=" + Uri.encode(s);
                                webProgress.setVisibility(ProgressBar.VISIBLE);
                                webView.loadUrl(url);
                                searchView.clearFocus();
                            }
                        }
                        return true;
                    } else {
                        if (jsonConfig.isAccept == 2) {
                            if (isValid(s)) {
                                webProgress.setVisibility(ProgressBar.VISIBLE);
                                webView.loadUrl(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://www.youtube.com/results?search_query=" + Uri.encode(s);
                                webProgress.setVisibility(ProgressBar.VISIBLE);
                                webView.loadUrl(url);
                                searchView.clearFocus();
                            }
                        } else {
                            if (s.equalsIgnoreCase("https://m.youtube.com")) {
                                webProgress.setVisibility(ProgressBar.VISIBLE);
                                webView.loadUrl(s);
                                searchView.clearFocus();
                            } else if (s.contains("youtube")) {
                                searchView.clearFocus();
                                showNotSupportYoutube();
                            } else {
                                if (isValid(s)) {
                                    webProgress.setVisibility(ProgressBar.VISIBLE);
                                    webView.loadUrl(s);
                                    searchView.clearFocus();
                                } else {
                                    String url = "https://vimeo.com/search?q=" + Uri.encode(s);
                                    webProgress.setVisibility(ProgressBar.VISIBLE);
                                    webView.loadUrl(url);
                                    searchView.clearFocus();
                                }
                            }
                        }
                        return true;
                    }
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // Filter data
                    MatrixCursor mc = new MatrixCursor(new String[]{BaseColumns._ID, "fishName"});
                    for (int i = 0; i < strArrData.length; i++) {
                        if (strArrData[i].toLowerCase().contains(s.toLowerCase())) {
                            mc.addRow(new Object[]{i, strArrData[i]});
                        }
                    }
                    myAdapter.changeCursor(mc);


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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        searchView.clearFocus();
        switch (item.getItemId()) {
            case R.id.action_home:
                webView.loadUrl("about:blank");
                searchView.clearFocus();
                isClearHistory = true;
                return true;
            case R.id.action_reload:
                if (webView.getVisibility() == View.VISIBLE)
                    webView.reload();
                searchView.clearFocus();
                return true;
            case R.id.action_folder:
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                return true;
//            case R.id.action_setting:
//                startActivity(new Intent(this, SettingsActivity.class));
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
//        if (webView != null && webView.canGoBack())
//            webView.goBack();
//        else {
//            webView.loadUrl("about:blank");
////            isClearHistory = true;
////            webView.setVisibility(View.GONE);
//        }

        if (webView != null && webView.canGoBack())
            webView.goBack();
        else {
            webView.loadUrl("about:blank");
            super.onBackPressed();
        }
    }

    private void getConfigApp() {
        dialogLoading.show();
        SharedPreferences mPrefs = getSharedPreferences("adsserver", 0);

        String urlRequest = AppConstants.URL_CLIENT_CONFIG + "?id_game=" + getPackageName();
        if (!mPrefs.contains("uuid")) {
            String uuid = UUID.randomUUID().toString();
            mPrefs.edit().putString("uuid", "mp4" + uuid).commit();
        }
        Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        urlRequest += "&lg=" + locale.getLanguage().toLowerCase() + "&lc=" + locale.getCountry().toLowerCase();


        OkHttpClient client = new OkHttpClient();
        Request okRequest = new Request.Builder()
                .url(urlRequest)
                .build();

        client.newCall(okRequest).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(MainActivity.this);
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

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt("intervalService", jsonConfig.intervalService);
//                editor.putString("idFullService", jsonConfig.idFullService);
                editor.putInt("delayService", jsonConfig.delayService);
                editor.putInt("delay_report", jsonConfig.delay_report);
//                editor.putString("idFullFbService", jsonConfig.idFullFbService);
                editor.putInt("delay_retention", jsonConfig.delay_retention).commit();

//                if (!mPrefs.contains("delay_retention")) {
//                    if (new Random().nextInt(100) < jsonConfig.retention) {
//
//                    } else {
//                        editor.putInt("delay_retention", -1).commit();
//                    }
//                }
                editor.commit();

                SharedPreferences mPrefs2 = getSharedPreferences("support_xx", 0);
                if (mPrefs2.getBoolean("isNoAds", false) && mPrefs2.getInt("accept", 0) == 2) {
                    jsonConfig.isAccept = 2;
                } else {
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.dismiss();

                        if (jsonConfig.isAccept == 0) {
                            strArrData = new String[jsonConfig.urlAccept.size() - 1];
                            int count = 0;
                            for (Site site : jsonConfig.urlAccept) {
                                if (!site.getUrl().contains("vimeo"))
                                    strArrData[count++] = site.getUrl();
                            }
                        }
                        else {
                            strArrData = new String[jsonConfig.urlAccept.size() + 1];
                            strArrData[0] = "https://m.youtube.com";
                            int count = 1;
                            for (Site site : jsonConfig.urlAccept) {
                                strArrData[count++] = site.getUrl();
                            }

                        }


                        Intent myIntent = new Intent(MainActivity.this, MyService.class);
                        startService(myIntent);

                        if (getPackageName().equals(jsonConfig.newAppPackage)) {
                            addBannerAds();
                            requestFullAds();
//                            if (jsonConfig.isAccept == 2 && RateThisApp.getLaunchCount(MainActivity.this) >= 2)
//                                RateThisApp.showRateDialogIfNeeded(MainActivity.this);
                        } else {
                            showPopupNewApp();
                        }
                    }
                });

            }
        });

    }

    private void showPopupNewApp() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }

        builder.setTitle("No longer support!")
                .setMessage("This app is no longer support. Please install new app to download video from youtube and other site")
                .setPositiveButton("Play store", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        Uri uri = Uri.parse("market://details?id=" + jsonConfig.newAppPackage);
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(myAppLinkToMarket);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "Unable to find market app", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showNotSupportYoutube() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
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

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to find market app", Toast.LENGTH_SHORT).show();
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

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
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

    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException e) {
        }
        return false;
    }

    private void logSiteDownloaded() {
        if (webView == null || webView.getUrl() == null)
            return;
        if (webView.getUrl().contains("facebook")) {
            logEventFb("FACEBOOK");
        } else if (webView.getUrl().contains("instagram")) {
            logEventFb("INSTAGRAM");
        } else {
            logEventFb("OTHER_WEB");
        }
    }

    private void logEventFb(String event) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent(event);
    }

    private void showPlayThenDownloadError() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
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
}

