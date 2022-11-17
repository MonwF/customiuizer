package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class WebPage extends SubFragment {
	WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		supressMenu = true;
		super.onCreate(savedInstanceState);
		pageUrl = getArguments().getString("pageUrl");
		OnBackPressedCallback callback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
				}
				else {
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
		webSettings.setAllowFileAccess(false);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				WebPage.this.getActionBar().setTitle(view.getTitle());
				if (pageUrl.contains("tpsx.lanzou")) {
					mWebView.evaluateJavascript("document.getElementById('pwd').value = 'miui';window.file();", new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
						};
					});
				}
			}
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