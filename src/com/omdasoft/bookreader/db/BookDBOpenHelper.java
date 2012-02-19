package com.omdasoft.bookreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDBOpenHelper extends SQLiteOpenHelper {
	
	public  static final int DATABASE_VERSION = 3;
    public  static final String BOOK_TABLE_NAME = "book";
	public  static final String URL = "URL";
	public  static final String CONTENT_URL = "CONTENT_URL";
	public  static final String NAME = "NAME";
	public  static final String INTRO = "INTRO";
	public  static final String BOOK_CODE = "BOOK_CODE";
	public  static final String AUTHOR = "AUTHOR";
	public  static final String COVER_IMG = "COVER_IMG";
	public  static final String FULLTEXT = "FULLTEXT";
	public  static final String FAVORITE = "FAVORITE";
	public  static final String FAVORITE_ORDER = "FAVORITE_ORDER";
	public  static final String SOURCE = "SOURCE";
	public  static final String CATEGORY = "CATEGORY";
	public  static final String LATEST_CHAPTER = "LATEST_CHAPTER";
	public  static final String LAST_READ_URL = "LAST_READ_URL";
	public  static final String LAST_READ_AT = "LAST_READ_AT";
	public  static final String CREATED_AT = "CREATED_AT";
	
    public  static final String POST_TABLE_CREATE =
                "CREATE TABLE " + BOOK_TABLE_NAME + " (" +
                "_id integer primary key autoincrement," +
                URL + " TEXT, " +
                CONTENT_URL + " TEXT, " +
                COVER_IMG + " TEXT, " +
                FULLTEXT + " TEXT, " +
                FAVORITE + " INTEGER, " +
                FAVORITE_ORDER + " INTEGER, " +
                SOURCE + " INTEGER, " +
                CATEGORY + " INTEGER, " +
                LAST_READ_AT + " INTEGER, " +
                CREATED_AT + " INTEGER, " +
                LATEST_CHAPTER + " TEXT, " +
                LAST_READ_URL + " TEXT, " +
                AUTHOR + " TEXT, " +
                BOOK_CODE + " TEXT, " +
                INTRO + " TEXT, " +
    NAME + " TEXT);";
	public static final String DATABASE_NAME = "post";

    BookDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(POST_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants", "Upgrading database, which will destroy all old	data");
				db.execSQL("DROP TABLE IF EXISTS " + BOOK_TABLE_NAME);
				onCreate(db);
		
	}

}
