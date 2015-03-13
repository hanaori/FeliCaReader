package suica.reader.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import suica.reaer.dto.SuicaDto;

public class SuicaReaderLibrary {
	/**
	 * FeliCaの履歴読み取りメソッド
	 * @param idm カード固有のID
	 * @param size 同時読み込み可能なブロック数
	 * @return FeliCaコマンド
	 * @throws IOException
	 */
	public byte[] readWithoutEncryption(byte[] idm, int size) throws IOException {
		ByteArrayOutputStream command = new ByteArrayOutputStream(100);
		command.write(0);           // データの長さ（ダミー）
		command.write(0x06);        // FeliCa通常コマンド: Read Without Encryption
		command.write(idm);         // カードID: 8byte
		command.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
		command.write(0x0f);        // 履歴のサービスコード下位バイト
		command.write(0x09);        // 履歴のサービスコード上位バイト
		command.write(size);        // ブロック数
		for (int i = 0; i < size; i++) {
			command.write(0x80);    // ブロックエレメント上位バイト 「FeliCaユーザマニュアル抜粋」の4.3項参照
			command.write(i);       // ブロック番号
		}

		byte[] req = command.toByteArray();
		req[0] = (byte) req.length; // 先頭１バイト=データの長さ
		return req;
	}

	/**
	 * 履歴Felica応答の解析
	 * @param res Felicaの応答
	 * @return 文字列表記
	 * @throws Exception
	 */
	public String parse(byte[] res) throws Exception {
		// res[0] = データ長
		// res[1] = 0x07
		// res[2〜9] = カードID
		// res[10,11] = エラーコード（0=正常）
		if (res[10] != 0x00) throw new Exception();
		// res[12] = 応答ブロック数
		// res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
		int size = res[12];
		String result = "******************************************************\n\n";
		for (int i = 0; i < size; i++) {
			// 個々の履歴の解析
			SuicaDto suicaDto = SuicaDto.parse(res, 13 + i * 16);
			result += suicaDto.toString() +"\n\n******************************************************\n\n";
		}
		return result;
	}
}
