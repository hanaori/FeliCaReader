package suica.reader.activity;

import suica.reader.activity.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FirstActivity extends Activity {

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	TextView text;
	Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		Intent intent = new Intent(this, getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// PendingIntent を取得
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*"); // 本当は自分の使うもの一つに絞るべき
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		setContentView(R.layout.activity_main);
		text = (TextView) findViewById(R.id.text1);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter == null) {
			text.setText("この端末はNFCに対応していません。");
		} else {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
			button = (Button) findViewById(R.id.okButton);
			if (mAdapter.isEnabled()) {
				button.setVisibility(View.INVISIBLE);
				text.setText("Suicaをかざして下さい。");
			} else {
				text.setText("NFC機能をONにしてください。");
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						try{
							// NFC設定画面へ遷移
							startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
						}catch(ActivityNotFoundException e){
							// エアプレーンモード設定画面へ遷移
							startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
						}
					}
				});
			}
		}
	}
	// onNewIntentでForegroundDispatcherを受ける
	public void onNewIntent(Intent intent) {
		Intent newIntent = new Intent(this, ResultActivity.class);
		newIntent.putExtra("intent", intent);
		startActivity(newIntent);
	}
}
