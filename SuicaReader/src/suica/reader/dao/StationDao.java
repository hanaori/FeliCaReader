package suica.reader.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StationDao extends SQLiteOpenHelper {
	static final String DB_NAME_ASSET = "station_code.sqlite";
	static final String DB_NAME = "stationCode";
	private final Context mContext;  
	private final File mDatabasePath; 
	static final int DB_VERSION = 1;
	private SQLiteDatabase db;
	public StationDao(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;  
		mDatabasePath = mContext.getDatabasePath(DB_NAME); 
	}

	/** 
	 * assetに格納したデータベースをコピーするための空のデータベースを作成する 
	 */  
	public void createEmptyDataBase(Context context) throws IOException {  
		boolean dbExist = checkDataBaseExists();  
		if (dbExist) {  
			// すでにデータベースが作成されている  
		} else {  
			// このメソッドを呼ぶことで、空のデータベースがアプリのデフォルトシステムパスに作られる  
			getReadableDatabase();  
			try {  
				// asset に格納したデータベースをコピーする  
				copyDataBaseFromAsset(context);  
				String dbPath = mDatabasePath.getAbsolutePath();  
				SQLiteDatabase checkDb = null;  
				try {  
					checkDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);  
				} catch (SQLiteException e) {  
				}  
				if (checkDb != null) {  
					checkDb.setVersion(DB_VERSION);  
					checkDb.close();  
				}  
			} catch (IOException e) {  
				throw new Error("Error copying database");  
			}  
		}  
	}   


	/** 
	 * 再コピーを防止するために、すでにデータベースがあるかどうか判定する 
	 * 
	 * @return 存在している場合 {@code true} 
	 */  
	private boolean checkDataBaseExists() {  
		String dbPath = mDatabasePath.getAbsolutePath();  
		SQLiteDatabase checkDb = null;  
		try {  
			checkDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);  
		} catch (SQLiteException e) {  
			// データベースはまだ存在していない  
		}  
		if (checkDb == null) {  
			// データベースはまだ存在していない  
			return false;  
		}  
		int oldVersion = checkDb.getVersion();  
		int newVersion = DB_VERSION;  

		if (oldVersion == newVersion) {  
			// データベースは存在していて最新  
			checkDb.close();  
			return true;  
		}  
		// データベースが存在していて最新ではないので削除  
		File f = new File(dbPath);  
		f.delete();  
		return false;  
	}  

	/** 
	 * assetに格納したDBを、デフォルトのDBパスに作成した空のDBにコピーする 
	 */  
	private void copyDataBaseFromAsset(Context context) throws IOException{  
		// asset 内のデータベースファイルにアクセス  
		InputStream mInput = context.getAssets().open(DB_NAME_ASSET);  
		// デフォルトのデータベースパスに作成した空のDB  
		OutputStream mOutput = new FileOutputStream(mDatabasePath);  
		// コピー  
		byte[] buffer = new byte[1024];  
		int size;  
		while ((size = mInput.read(buffer)) > 0) {  
			mOutput.write(buffer, 0, size);  
		}  
		// Close the streams  
		mOutput.flush();  
		mOutput.close();  
		mInput.close();  
	}  
	public SQLiteDatabase openDataBase() throws SQLException {  
		return getReadableDatabase();  
	}  

	private static final String[] COLUMNS = {"RESION", "RAILROAD", "STATIONSEQ", "COMPANY", "RAILROADNAME", "STATIONNAME"};
	private static final String TABLE_NAME = "stationCode";
	public String[] findData(int regionCode, int railroadCode, int stationCode) {  		
		Cursor cursor;
		try{
			 int areaCode = regionCode & 0xff; 
            SQLiteDatabase db = this.openDataBase();
			cursor =  db.query(TABLE_NAME
					, COLUMNS
					, COLUMNS[0] + " = '" + areaCode + "' and "
					+ COLUMNS[1] + " = '" + (Integer.toHexString(railroadCode & 0xff)) + "' and "
					+ COLUMNS[2] + " = '" + (Integer.toHexString(stationCode & 0xff)) + "'"
					, null, null, null, null);
			return (cursor.moveToFirst()) 
					?  new String[]{ cursor.getString(3), cursor.getString(4), cursor.getString(5)}
			:  new String[]{"???", "???", "???"};
		} catch (Exception e) {
			e.printStackTrace();
			return new String[]{"error", "error", "error"};
		}
	}


		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}
