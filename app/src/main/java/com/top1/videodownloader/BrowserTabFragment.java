package com.top1.videodownloader;


import android.*;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrowserTabFragment} factory method to
 * create an instance of this fragment.
 */
public class BrowserTabFragment extends Fragment {
    public WebView webView;
    private ProgressBar webProgress;
    private String urlDownloadOther;
    private LinearLayout infomationView;

    public boolean clearHistory = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browser_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        infomationView = (LinearLayout) view.findViewById(R.id.infomationView);
        webProgress = (ProgressBar) view.findViewById(R.id.webProgress);
        webProgress.setVisibility(ProgressBar.GONE);
        webView = (WebView) view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if( (url.contains("https://youtube.com") || url.contains("https://m.youtube.com")) && Main2Activity.jsonConfig.getIsAccept() == 0)
                {
                    getMainActivity().showNotSupportYoutube();
                    return true;
                }
                return  false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                urlDownloadOther = null;
                if (clearHistory)
                {
                    clearHistory = false;
                    webView.clearHistory();
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.contains(".mp4") || url.contains(".3gp")) {
                    if (!isValidUrl(url))
                        return;

                    urlDownloadOther = url;

                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(getMainActivity());
                    AlertDialog show = builder.setTitle(R.string.new_video_found)
                            .setMessage(urlDownloadOther)
                            .setPositiveButton(R.string.action_download, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();

                                    try
                                    {
                                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(urlDownloadOther));
                                        String fName = UUID.randomUUID().toString();
                                        if (urlDownloadOther.contains(".mp4")) {
                                            fName += ".mp4";

                                        } else if (urlDownloadOther.contains(".3gp")) {
                                            fName += ".3gp";
                                        }

                                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fName);
                                        r.allowScanningByMediaScanner();
                                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                        dm.enqueue(r);
                                        getMainActivity().logSiteDownloaded(webView.getUrl());
                                        getMainActivity().showFullAds();
                                    }
                                    catch (Exception e)
                                    {
                                        getMainActivity().showPlayThenDownloadError();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setCancelable(false)
                            .show();

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

        FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.btnDownload);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isStoragePermissionGranted()) {
                    return;
                }
                if (webView.getUrl() == null) {
                    getMainActivity().showErrorDownload();
                    return;
                }
                if (!getActivity().getPackageName().equals(Main2Activity.jsonConfig.getNewAppPackage())) {
                    getMainActivity().showPopupNewApp();
                    return;
                }

                if (webView.getUrl().contains("youtube.com")) {
                    getMainActivity().downloadYoutube(webView.getUrl());
                } else if (webView.getUrl().contains("vimeo.com")) {
                    getMainActivity().downloadVimeo(webView.getUrl());
                } else if (webView.getUrl().contains("twitter.com")) {
                    getMainActivity().downloadTwitter(webView.getUrl());
                } else {
                    if (urlDownloadOther == null) {
                        getMainActivity().showPlayThenDownloadError();
                    } else {
                        try
                        {
                            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(urlDownloadOther));
                            String fName = UUID.randomUUID().toString();
                            if (urlDownloadOther.contains(".mp4")) {
                                fName += ".mp4";

                            } else if (urlDownloadOther.contains(".3gp")) {
                                fName += ".3gp";
                            }

                            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fName);
                            r.allowScanningByMediaScanner();
                            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            DownloadManager dm = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            dm.enqueue(r);
                            getMainActivity().logSiteDownloaded(webView.getUrl());
                            getMainActivity().showFullAds();
                        }
                        catch (Exception e)
                        {
                            getMainActivity().showPlayThenDownloadError();
                        }
                    }

                }
            }
        });
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public  void showWebview(boolean isShowWebView)
    {
        if (isShowWebView)
        {
            webView.setVisibility(WebView.VISIBLE);
            infomationView.setVisibility(LinearLayout.GONE);
        }
        else
        {
            webView.setVisibility(WebView.GONE);
            infomationView.setVisibility(LinearLayout.VISIBLE);
        }
    }

    private Main2Activity getMainActivity()
    {
        return  (Main2Activity) getActivity();
    }

    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException e) {
        }
        return false;
    }


}
