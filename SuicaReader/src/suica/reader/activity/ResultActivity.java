package suica.reader.activity;

import jp.kronos.suicareader.R;
import suica.reader.lib.SuicaReaderLibrary;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class ResultActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suica_auth);
		// インテントの取得
		Intent intent = (Intent)(getIntent().getExtras().get("intent"));
		// ICカードの検出かチェック
		String action = intent.getAction();
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // IDｍ取得
        byte[] idm = new byte[]{0};
        if (tag != null) {
            idm = tag.getId();
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
        	// SuicaReaderLibraryのインスタンス取得
        	SuicaReaderLibrary reader = new SuicaReaderLibrary();
            TextView textView1 = (TextView) this.findViewById(R.id.textView1);
            try {
                // Suica読み取り
                byte[] request = reader.readWithoutEncryption(idm, 10);
                ResultActivity suicaAuth = new ResultActivity();
                // Suicaにリクエスト送信
                NfcF nfc = NfcF.get(tag);
                nfc.connect();
                byte[] responce = nfc.transceive(request);
                nfc.close();
                // 結果を文字列に変換して表示
                textView1.setText(reader.parse(responce));
            } catch (Exception e) {
            	e.printStackTrace();
            	textView1.setText("読み取れません。");
            }
        }
	}
}
