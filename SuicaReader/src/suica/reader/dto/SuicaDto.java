package suica.reader.dto;

import java.io.IOException;

import suica.reader.dao.StationDao;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

public class SuicaDto {
	public int termId;
	public int processId;
	public int year;
	public int month;
	public int day;
	public String kind;
	public String outkind;
	public int remain;
	public int seqNo;
	public int region;
	public int inLineCode;
	public int outLineCode;
	public int inStationCode;
	public int outStationCode;
	private StationDao stationDao; 
	String inStationName;
	String outStationName;
	private SQLiteDatabase db;
	

	public SuicaDto parse(byte[] res, int order, Context context) {
		SuicaDto dto = new SuicaDto();
		try {
			dto.init(res, order, context);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dto;
	}

	private void init(byte[] res, int order, Context context) throws IOException {
		this.termId = res[order+0]; // 0: 端末種
		this.processId = res[order+1]; // 1: 処理
		// 2-3: ??
		// 4-5: 日付 (先頭から7ビットが年、４ビットが月、残り５ビットが日) 全16ビット
		int mixDate = combineIndex(res, order, 4,5);
		this.year  = (mixDate >> 9) & 0x7f; // 0x7f=01111111
		this.month = (mixDate >> 5) & 0x0f; // 0x0f=00001111
		this.day   = mixDate & 0x1f; // 0x1f=00011111
		if (isBuppan(this.processId)) {
			this.kind = "物販";
		} else if (isBus(this.processId)) {
			this.kind = "バス";
		} else {
			// 線区が 0x7f 以下のとき: 0 (JR線)
			// 線区が 0x80 以上のとき:(公営・私鉄) 
			this.inLineCode = res[order+6];
			this.kind = res[order+6] < 0x80 ? "JR" : "公営/私鉄";
		}
		this.inStationCode = res[order+7];
		this.outLineCode = res[order+8];
		this.outkind = res[order+8] < 0x80 ? "JR" : "公営/私鉄";
		this.outStationCode = res[order+9];
		this.remain  = combineIndex(res, order, 11,10); // 10-11: 残高 (little endian)
		this.seqNo   = combineIndex(res, order, 12,13,14); // 12-14: 連番
		this.region = res[order+15]; // 15: リージョン	

		StationDao dao = new StationDao(context);
		dao.createEmptyDataBase(context);
		db = dao.openDataBase();
		this.inStationName = dao.findData(0, inLineCode, inStationCode)[2];
		this.outStationName = dao.findData(0, outLineCode, outStationCode)[2];
		db.close();
	}

	private int combineIndex(byte[] res, int order, int... index) {
		int num = 0;
		for (int i = 0; i < index.length; i++) {
			num = num << 8;
			num += ((int)res[order+index[i]]) & 0xff; // 0xff=11111111
		}
		return num;
	}
	private boolean isBuppan(int processId) {
		return processId == 70 || processId == 73 || processId == 74
				|| processId == 75 || processId == 198 || processId == 203;
	}
	private boolean isBus(int processId) {
		return processId == 13|| processId == 15|| processId ==  31|| processId == 35;
	} 

		public String toString() {
			String str = seqNo
					+", " + TERM_MAP.get(termId)
					+", " + PROCESS_MAP.get(processId)
					+"\n" + year + "/" + month + "/" + day
					+"\n" + "(" + kind + ")" + inStationName.toString() + "-" + "(" + outkind + ")" + outStationName.toString()
					+", 残："+remain+"円";
			return str;
		}

		public static final SparseArray<String> TERM_MAP = new SparseArray<String>();
		public static final SparseArray<String> PROCESS_MAP = new SparseArray<String>();
		static {
			TERM_MAP.put(3, "精算機");
			TERM_MAP.put(4 , "携帯型端末");
			TERM_MAP.put(5 , "車載端末");
			TERM_MAP.put(7 , "券売機");
			TERM_MAP.put(8 , "券売機");
			TERM_MAP.put(9 , "入金機");
			TERM_MAP.put(18 , "券売機");
			TERM_MAP.put(20 , "券売機等");
			TERM_MAP.put(21 , "券売機等");
			TERM_MAP.put(22 , "改札機");
			TERM_MAP.put(23 , "簡易改札機");
			TERM_MAP.put(24 , "窓口端末");
			TERM_MAP.put(25 , "窓口端末");
			TERM_MAP.put(26 , "改札端末");
			TERM_MAP.put(27 , "携帯電話");
			TERM_MAP.put(28 , "乗継精算機");
			TERM_MAP.put(29 , "連絡改札機");
			TERM_MAP.put(31 , "簡易入金機");
			TERM_MAP.put(70 , "VIEW ALTTE");
			TERM_MAP.put(72 , "VIEW ALTTE");
			TERM_MAP.put(199 , "物販端末");
			TERM_MAP.put(200 , "自販機");

			PROCESS_MAP.put(1 , "運賃支払(改札出場)");
			PROCESS_MAP.put(2 , "チャージ");
			PROCESS_MAP.put(3 , "券購(磁気券購入)");
			PROCESS_MAP.put(4 , "精算");
			PROCESS_MAP.put(5 , "精算 (入場精算)");
			PROCESS_MAP.put(6 , "窓出 (改札窓口処理)");
			PROCESS_MAP.put(7 , "新規 (新規発行)");
			PROCESS_MAP.put(8 , "控除 (窓口控除)");
			PROCESS_MAP.put(13 , "バス (PiTaPa系)");
			PROCESS_MAP.put(15 , "バス (IruCa系)");
			PROCESS_MAP.put(17 , "再発 (再発行処理)");
			PROCESS_MAP.put(19 , "支払 (新幹線利用)");
			PROCESS_MAP.put(20 , "入A (入場時オートチャージ)");
			PROCESS_MAP.put(21 , "出A (出場時オートチャージ)");
			PROCESS_MAP.put(31 , "入金 (バスチャージ)");
			PROCESS_MAP.put(35 , "券購 (バス路面電車企画券購入)");
			PROCESS_MAP.put(70 , "物販");
			PROCESS_MAP.put(72 , "特典 (特典チャージ)");
			PROCESS_MAP.put(73 , "入金 (レジ入金)");
			PROCESS_MAP.put(74 , "物販取消");
			PROCESS_MAP.put(75 , "入物 (入場物販)");
			PROCESS_MAP.put(198 , "物現 (現金併用物販)");
			PROCESS_MAP.put(203 , "入物 (入場現金併用物販)");
			PROCESS_MAP.put(132 , "精算 (他社精算)");
			PROCESS_MAP.put(133 , "精算 (他社入場精算)");
		}
	}
