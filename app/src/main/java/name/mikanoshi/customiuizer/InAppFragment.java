package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class InAppFragment extends SubFragment implements PurchasesUpdatedListener {

	private int donated = 0;
	private boolean hasNetwork = false;
	private List<String> skuList = new ArrayList<>();
	private SparseArray<SkuDetails> donations = new SparseArray<SkuDetails>();
	private BillingClient billingClient;
	boolean mIsServiceConnected = false;
	BillingClientStateListener billingClientState = new BillingClientStateListener() {
		@Override
		public void onBillingSetupFinished(BillingResult billingResult) {
			if (checkBillingError(billingResult)) return;
			mIsServiceConnected = true;
			updatePurchases();
			updateDetails();
		}

		@Override
		public void onBillingServiceDisconnected() {
			mIsServiceConnected = false;
		}
	};

	private int skuToInt(String sku) {
		return Integer.parseInt(sku.replace("donate", ""));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();

		skuList.add("donate1");
		skuList.add("donate2");
		skuList.add("donate3");
		skuList.add("donate5");
		skuList.add("donate10");
		skuList.add("donate15");
		skuList.add("donate50");
		skuList.add("donate100");

		for (String sku: skuList) {
			Preference pref = findPreference(sku);
			pref.setTitle(getResources().getString(R.string.support_donate, skuToInt(sku)));
			pref.setSummary("...");
			pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					SkuDetails details = donations.get(skuToInt(preference.getKey()));
					if (details == null) return false;
					BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(details).build();
					BillingResult result = billingClient.launchBillingFlow(act, flowParams);
					checkBillingError(result);
					return true;
				}
			});
		}

		Preference consume = findPreference("consume");
		consume.setTitle("Consume all");
		consume.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Purchase.PurchasesResult result = billingClient.queryPurchases(SkuType.INAPP);
				List<Purchase> purchases = result.getPurchasesList();
				if (purchases == null || purchases.size() == 0) return false;
				for (Purchase purchase: purchases) {
					if (purchase.getPurchaseState() != PurchaseState.PURCHASED) continue;
					ConsumeParams params = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).setDeveloperPayload(purchase.getDeveloperPayload()).build();
					billingClient.consumeAsync(params, new ConsumeResponseListener() {
						@Override
						public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
							checkBillingError(billingResult);
						}
					});
				}
				Toast.makeText(getActivity(), "All donations consumed!", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		if (!BuildConfig.DEBUG) getPreferenceScreen().removePreference(consume);

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(350);
					Activity act = getActivity();
					if (act != null && !act.isFinishing())
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							billingClient = BillingClient.newBuilder(act).enablePendingPurchases().setListener(InAppFragment.this).build();
							billingClient.startConnection(billingClientState);
						}
					});
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		if (billingClient != null) billingClient.endConnection();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		unregisterNetReceiver();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mIsServiceConnected) {
			updatePurchases();
			updateDetails();
		}
		registerNetReceiver();
	}

	boolean checkBillingError(BillingResult billingResult) {
		Activity act = getActivity();
		if (act == null || act.isFinishing()) return false;
		int resp = billingResult.getResponseCode();
		if (resp == BillingResponseCode.OK) return false;
		if (resp == BillingResponseCode.SERVICE_UNAVAILABLE) return true;
		if (resp == BillingResponseCode.USER_CANCELED)
			Toast.makeText(getActivity(), R.string.billing_canceled, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), R.string.billing_error, Toast.LENGTH_LONG).show();
		Log.e("miuizer", "[Billing error " + billingResult.getResponseCode() + "] " + billingResult.getDebugMessage());
		return true;
	}

	void processPurchases(BillingResult billingResult, List<Purchase> purchases) {
		if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
			for (String sku: skuList) findPreference(sku).setEnabled(true);
			for (Purchase purchase: purchases) {
				int state = purchase.getPurchaseState();
				if (state == PurchaseState.PURCHASED)
				donated = Math.max(donated, skuToInt(purchase.getSku()));
				findPreference(purchase.getSku()).setEnabled(state != PurchaseState.PURCHASED);

				if (state == PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
					AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).setDeveloperPayload(purchase.getDeveloperPayload()).build();
					billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
						@Override
						public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
							Log.e("miuizer", billingResult.getResponseCode() == BillingResponseCode.OK ? "Donation acknowledged!" : "[Ack error " + billingResult.getResponseCode() + "] " + billingResult.getDebugMessage());
						}
					});
				}

				if (getView() != null) {
					TextView donateMsg = getView().findViewById(R.id.donate_msg);
					if (donated == 0)
						donateMsg.setText(R.string.support_donate_summ);
					else if (donated < 5)
						donateMsg.setText(R.string.donate_level1);
					else if (donated <= 15)
						donateMsg.setText(R.string.donate_level2);
					else if (donated <= 100)
						donateMsg.setText(R.string.donate_level3);
				}
			}
		} else if (billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED) {
			if (getActivity() != null) Toast.makeText(getActivity(), R.string.billing_canceled, Toast.LENGTH_SHORT).show();
		} else {
			checkBillingError(billingResult);
		}
	}

	@Override
	public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
		processPurchases(billingResult, purchases);
	}

	void updatePurchases() {
		Purchase.PurchasesResult result = billingClient.queryPurchases(SkuType.INAPP);
		processPurchases(result.getBillingResult(), result.getPurchasesList());
	}

	void updateDetails() {
		SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
		params.setSkusList(skuList).setType(SkuType.INAPP);
		billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
			@Override
			public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
				if (checkBillingError(billingResult)) {
					for (String sku: skuList) findPreference(sku).setEnabled(false);
					return;
				}
				donations.clear();
				for (SkuDetails skuDetail: skuDetailsList) try {
					int sku = skuToInt(skuDetail.getSku());
					donations.append(sku, skuDetail);
					Preference pref = findPreference(skuDetail.getSku());
					if (!"USD".equals(skuDetail.getPriceCurrencyCode()))
					pref.setTitle(getResources().getString(R.string.support_donate, sku) + " (" + skuDetail.getPrice() + ")");
					pref.setSummary(skuDetail.getDescription());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
		@Override
		public void onAvailable(Network network) {
			if (hasNetwork) return;
			hasNetwork = true;
			if (mIsServiceConnected) {
				Activity act = getActivity();
				if (act != null && !act.isFinishing())
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updatePurchases();
						updateDetails();
					}
				});
			}
		}

		@Override
		public void onLost(Network network) {
			if (!hasNetwork) return;
			hasNetwork = false;
		}
	};

	void registerNetReceiver() {
		ConnectivityManager connectMgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectMgr.getActiveNetworkInfo();
		hasNetwork = activeNetwork != null && activeNetwork.isConnected();
		connectMgr.registerDefaultNetworkCallback(networkCallback);
	}

	void unregisterNetReceiver() {
		ConnectivityManager connectMgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		connectMgr.unregisterNetworkCallback(networkCallback);
	}

}
