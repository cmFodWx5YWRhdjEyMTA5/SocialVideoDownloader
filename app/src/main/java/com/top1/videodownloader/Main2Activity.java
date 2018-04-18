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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.kobakei.ratethisapp.RateThisApp;
import com.top1.videodownloader.network.GetConfig;
import com.top1.videodownloader.network.JsonConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.breedrapps.vimeoextractor.OnVimeoExtractionListener;
import uk.breedrapps.vimeoextractor.VimeoExtractor;
import uk.breedrapps.vimeoextractor.VimeoVideo;

public class Main2Activity extends AppCompatActivity {
    public static JsonConfig jsonConfig;
    private ProgressDialog dialogLoading;

    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.AdView adViewFb;
    private com.facebook.ads.InterstitialAd interstitialAdFb;

    private SimpleCursorAdapter myAdapter;
    SearchView searchView = null;
    private String[] strArrData = {};
    private boolean isFirstAds = true;

    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        final String[] from = new String[]{"fishName"};
        final int[] to = new int[]{android.R.id.text1};
        myAdapter = new SimpleCursorAdapter(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        getConfigApp();
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
                Toast.makeText(Main2Activity.this, R.string.error_download_page, Toast.LENGTH_SHORT).show();
            }
        });

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
                        Uri uri = Uri.parse("market://details?id=" + jsonConfig.getNewAppPackage());
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
        RelativeLayout bannerView = (RelativeLayout) findViewById(R.id.adsBannerView);
        if (jsonConfig.getPriorityBanner().equals("facebook")) {

            adViewFb = new com.facebook.ads.AdView(this, jsonConfig.getIdBannerFacebook(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            Log.d("idbanner = ", jsonConfig.getIdBannerFacebook());
            bannerView.addView(adViewFb);
            // Request an ad
            adViewFb.loadAd();
        } else if (jsonConfig.getPriorityBanner().equals("admob")) {
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(jsonConfig.getIdBannerAdmob());
            bannerView.addView(adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void requestAds() {
        if (jsonConfig.getPriorityFull().equals("facebook")) {
            requestFBAds();
        } else if (jsonConfig.getPriorityFull().equals("admob")) {
            requestAdmob();
        }
    }

    private void requestAdmob() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(jsonConfig.getIdFullAdmob());


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                if (isFirstAds) {
                    isFirstAds = false;
                    mInterstitialAd.show();
                }
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
        interstitialAdFb = new com.facebook.ads.InterstitialAd(this, jsonConfig.getIdFullFacebook());
        Log.d("idbanner2 = ", jsonConfig.getIdFullFacebook());
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
//                Log.d("caomui1",adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Show the ad when it's done loading.
                if (isFirstAds) {
                    isFirstAds = false;
                    interstitialAdFb.show();
                }
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
        Random ran = new Random();
        if (ran.nextInt(100) < jsonConfig.getPercentAds()) {
            if (mInterstitialAd != null) {
                if (mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
                else
                    requestAdmob();

            } else if (interstitialAdFb != null) {
                if (interstitialAdFb.isAdLoaded())
                    interstitialAdFb.show();
                else
                    requestFBAds();
            }
        }
    }

    public void downloadYoutube(String url) {
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
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(r);
                        Toast.makeText(Main2Activity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
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
            searchView.setSuggestionsAdapter(myAdapter);
            // Getting selected (clicked) item suggestion
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionClick(int position) {
                    if (jsonConfig.getIsAccept() == 0 && strArrData[position].contains("youtube")) {
                        searchView.clearFocus();
                        showNotSupportYoutube();
                        return true;
                    } else {
                        String url = strArrData[position];
                        loadUrlWebview(url);
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
                    if (jsonConfig.getIsAccept() == 0) {
                        if (s.contains("youtube")) {
                            searchView.clearFocus();
                            showNotSupportYoutube();
                        } else {
                            if (isValid(s)) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://www.google.com.vn/search?q=" + Uri.encode(s + " -site:youtube.com") + "&tbm=vid";
                                loadUrlWebview(url);
                                searchView.clearFocus();
                            }
                        }
                        return true;
                    } else {
                        if (jsonConfig.getIsAccept() == 2) {
                            if (isValid(s)) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            } else {
                                String url = "https://www.google.com.vn/search?q=" + Uri.encode(s) + "&tbm=vid";
                                loadUrlWebview(url);
                                searchView.clearFocus();
                            }
                        } else {
                            if (s.equalsIgnoreCase("https://m.youtube.com")) {
                                loadUrlWebview(s);
                                searchView.clearFocus();
                            } else if (s.contains("youtube")) {
                                searchView.clearFocus();
                                showNotSupportYoutube();
                            } else {
                                if (isValid(s)) {
                                    loadUrlWebview(s);
                                    searchView.clearFocus();
                                } else {
                                    String url = "https://www.google.com.vn/search?q=" + Uri.encode(s + " -site:youtube.com") + "&tbm=vid";
                                    loadUrlWebview(url);
                                    searchView.clearFocus();
                                }
                            }
                        }
                        return true;
                    }
                }

                @Override
                public boolean onQueryTextChange(final String s) {
                    if (s.equals(""))
                        return  false;
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(AppConstants.BASE_URL_SEARCH)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    final GetConfig config = retrofit.create(GetConfig.class);

                    Call<String[]> call = config.getSuggestion(s);
                    call.enqueue(new Callback<String[]>() {
                        @Override
                        public void onResponse(Call<String[]> call, Response<String[]> response) {
                            Log.e("caomui",response.body().toString());

                            strArrData = response.body();
                            final MatrixCursor mc = new MatrixCursor(new String[]{BaseColumns._ID, "fishName"});
                            int count = strArrData.length;
                            if (count > 5 )
                                count = 5;
                            for (int i = 0; i < 5; i++) {
                                if (strArrData[i].toLowerCase().contains(s.toLowerCase()))
                                    mc.addRow(new Object[]{i, strArrData[i]});
                            }
                            myAdapter.changeCursor(mc);
                        }

                        @Override
                        public void onFailure(Call<String[]> call, Throwable t) {
                        }
                    });
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.URL_CONFIG)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GetConfig config = retrofit.create(GetConfig.class);

        Call<JsonConfig> call = config.getConfig();
        call.enqueue(new Callback<JsonConfig>() {
            @Override
            public void onResponse(Call<JsonConfig> call, Response<JsonConfig> response) {
                jsonConfig = response.body();
                strArrData = response.body().getUrlAccept().toArray(new String[0]);


                SharedPreferences mPrefs = getSharedPreferences("support_yt", 0);
                if (jsonConfig.getIsAccept() > 0) {
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putInt("accept", jsonConfig.getIsAccept()).commit();
                } else {
                    int support = mPrefs.getInt("accept", 0); //getString("tag", "default_value_if_variable_not_found");
                    if (support > 0) {
                        jsonConfig.setIsAccept(support);
                    }
                }

                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadTopSite();
                        dialogLoading.dismiss();
                        if (getPackageName().equals(jsonConfig.getNewAppPackage())) {
                            addBannerAds();
                            requestAds();

                            RateThisApp.Config config = new RateThisApp.Config(1, 3);
                            RateThisApp.init(config);
                            RateThisApp.showRateDialogIfNeeded(Main2Activity.this);
                        } else {
                            showPopupNewApp();
                        }
                    }
                });

            }

            @Override
            public void onFailure(Call<JsonConfig> call, Throwable t) {
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
        });
    }

    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() == 1) {
            BrowserTabFragment browserTab = (BrowserTabFragment) adapter.getItem(1);
            if (browserTab.webView != null && browserTab.webView.canGoBack())
                browserTab.webView.goBack();
            else {
                browserTab.webView.stopLoading();
                browserTab.clearHistory = true;
                browserTab.showWebview(false);//webView.loadUrl("about:blank");
            }
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

}
