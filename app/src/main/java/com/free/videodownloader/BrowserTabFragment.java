package com.free.videodownloader;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return  true;
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
                if (url.contains(".mp4")) {
                    urlDownloadOther = url;
                    Toast.makeText( getActivity(), R.string.detect_download_link, Toast.LENGTH_SHORT).show();
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
                    if (Main2Activity.jsonConfig.getIsAccept() == 0)
                    {
                        getMainActivity().showNotSupportYoutube();
                    }
                    else
                    {
                        getMainActivity().showFullAds();
                        getMainActivity().downloadYoutube(webView.getUrl());
                    }
                } else if (webView.getUrl().contains("vimeo.com")) {
                    getMainActivity().showFullAds();
                    getMainActivity().downloadVimeo(webView.getUrl());
                } else {
                    if (urlDownloadOther == null) {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(getMainActivity(), android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(getMainActivity());
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
                    } else {
                        getMainActivity().showFullAds();
                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(urlDownloadOther));
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, UUID.randomUUID().toString());
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        dm.enqueue(r);

                        Toast.makeText(getMainActivity(), R.string.downloading, Toast.LENGTH_SHORT).show();
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


}
