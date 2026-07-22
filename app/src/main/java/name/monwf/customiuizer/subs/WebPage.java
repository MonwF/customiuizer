package name.monwf.customiuizer.subs;

import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.Helpers;

public class WebPage extends SubFragment {
    WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.padded = false;
        toolbarMenu = true;
        activeMenus = "openinweb";
        super.onCreate(savedInstanceState);
        pageUrl = getArguments().getString("pageUrl");
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    mWebView.destroy();
                    this.remove();
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWebView = getView().findViewById(R.id.mainWeb);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                ActionBar ab = WebPage.this.getActionBar();
                if (ab != null) {
                    ab.setTitle(view.getTitle());
                }
            }
//			@Override
//			public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
//				String resUrl = webResourceRequest.getUrl().toString();
//				if (resUrl.contains("https://statics.woozooo.com/img/bd.js")) {
//					String customJs = "let pwdElem = document.getElementById('pwd');if (pwdElem) {pwdElem.value = 'miui';window.file();}";
//					return new WebResourceResponse("text/javascript", "utf-8", new ByteArrayInputStream(customJs.getBytes(StandardCharsets.UTF_8)));
//				}
//				return super.shouldInterceptRequest(webView, webResourceRequest);
//			}
        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Helpers.openURL(getValidContext(), url);
            }
        });
        mWebView.loadUrl(pageUrl);
    }
}