package com.omdasoft.bookreader.db;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class BookDAO {

	private SQLiteDatabase db;
	private final Context context;

	private static BookDAO instance;
	private BookDBOpenHelper sdbHelper;
	
	private BookDAO(Context c) {
		this.context = c;
		this.sdbHelper = new BookDBOpenHelper(this.context);
	}

	public void close() {
		db.close();
	}

	public void open() throws SQLiteException {
		try {
			db = sdbHelper.getWritableDatabase();
			
			db.setLockingEnabled(true);
		} catch (SQLiteException ex) {
			Log.v("Open database exception caught", ex.getMessage());
			db = sdbHelper.getReadableDatabase();
		}
	}

	public static BookDAO getInstance(Context c) {
		if (instance == null) {
			instance = new BookDAO(c);
		}
		return instance;
	}

	public Cursor getFavoriteBook() {
		Cursor c = db.query(BookDBOpenHelper.BOOK_TABLE_NAME, null, BookDBOpenHelper.FAVORITE + " = 1", null,
				 null, null, BookDBOpenHelper.FAVORITE_ORDER + " ");
				
		return c;
	}
	
	public Cursor getBookListByCategory(int source, int category) {
		Cursor c = db.query(BookDBOpenHelper.BOOK_TABLE_NAME, null, BookDBOpenHelper.SOURCE + " = " + source + " and " + BookDBOpenHelper.CATEGORY + " = " + category, null,
				 null, null, BookDBOpenHelper.FAVORITE_ORDER + " ");
				
		return c;
	}
	
	public Cursor getAllFavoritePost(int mode, int page, int amount) {
		Cursor c = db.query(BookDBOpenHelper.BOOK_TABLE_NAME, null, BookDBOpenHelper.FAVORITE + " = 1", null,
				 null, null, BookDBOpenHelper.LATEST_CHAPTER + " desc",  ((page - 1)*amount) + ", " + amount);
				
		return c;
	}
	
	public Cursor getBookByUrl(String url) {
		Cursor c = db.query(BookDBOpenHelper.BOOK_TABLE_NAME, null, BookDBOpenHelper.URL + " = ?", new String[]{url},
				 null, null, null);
				
		return c;
	}

	public int getMaxId() {
		return 0;
	}

	public long insert(BookModel am) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.URL, am.getUrl());
			newRingtoneValue.put(BookDBOpenHelper.NAME, am.getName());
			newRingtoneValue.put(BookDBOpenHelper.INTRO, am.getIntro());
			newRingtoneValue.put(BookDBOpenHelper.BOOK_CODE, am.getBookCode());
			newRingtoneValue.put(BookDBOpenHelper.AUTHOR, am.getAuthor());
			newRingtoneValue.put(BookDBOpenHelper.URL, am.getUrl());
			newRingtoneValue.put(BookDBOpenHelper.CONTENT_URL, am.getContentUrl());
			newRingtoneValue.put(BookDBOpenHelper.COVER_IMG, am.getCoverImg());
			newRingtoneValue.put(BookDBOpenHelper.FULLTEXT, am.getFulltext());
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE, am.getIsFavorite());
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE_ORDER, am.getFavoriteOrder());
			newRingtoneValue.put(BookDBOpenHelper.SOURCE, am.getSource());
			newRingtoneValue.put(BookDBOpenHelper.CATEGORY, am.getCategory());
			newRingtoneValue.put(BookDBOpenHelper.LATEST_CHAPTER, am.getLatestChapter());
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_URL, am.getLastReadUrl());
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_AT, new Date().getTime());
			newRingtoneValue.put(BookDBOpenHelper.CREATED_AT, new Date().getTime());
			return db.insert(BookDBOpenHelper.BOOK_TABLE_NAME, null, newRingtoneValue);
			} catch(SQLiteException ex) {
				Log.v("Insert into database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long update(BookModel am) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.URL, am.getUrl());
			newRingtoneValue.put(BookDBOpenHelper.NAME, am.getName());
			newRingtoneValue.put(BookDBOpenHelper.INTRO, am.getIntro());
			newRingtoneValue.put(BookDBOpenHelper.BOOK_CODE, am.getBookCode());
			newRingtoneValue.put(BookDBOpenHelper.AUTHOR, am.getAuthor());
			newRingtoneValue.put(BookDBOpenHelper.URL, am.getUrl());
			newRingtoneValue.put(BookDBOpenHelper.CONTENT_URL, am.getContentUrl());
			newRingtoneValue.put(BookDBOpenHelper.COVER_IMG, am.getCoverImg());
			newRingtoneValue.put(BookDBOpenHelper.FULLTEXT, am.getFulltext());
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE, am.getIsFavorite());
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE_ORDER, am.getFavoriteOrder());
			newRingtoneValue.put(BookDBOpenHelper.SOURCE, am.getSource());
			newRingtoneValue.put(BookDBOpenHelper.CATEGORY, am.getCategory());
			newRingtoneValue.put(BookDBOpenHelper.LATEST_CHAPTER, am.getLatestChapter());
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_URL, am.getLastReadUrl());
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_AT, new Date().getTime());
//			newRingtoneValue.put(BookDBOpenHelper.CREATED_AT, new Date().getTime());
			int res = db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + am.getUrl() +"'", null);
			return res;
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateLastRead(BookModel am) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_URL, am.getLastReadUrl());
			newRingtoneValue.put(BookDBOpenHelper.LAST_READ_AT, new Date().getTime());
			int res = db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + am.getUrl() +"'", null);
			return res;
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public boolean updateOrInsert(BookModel rm) {
		long res = update(rm);
		if(res > 0){
			return true;
		}else{
			//insert
//			rm.setNameEn(translate(rm.getName()));
			insert(rm);
			return false;
		}
	}
	
	public long updateImgUrl(String url, String urls) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.COVER_IMG, urls);
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + url +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateFavoriet(BookModel bm, int isFavorite) {

		try{
			
			int order = -1;
			
			if(isFavorite == 1){
				Cursor c = db.rawQuery("select max(" + BookDBOpenHelper.FAVORITE_ORDER + ") from " + BookDBOpenHelper.BOOK_TABLE_NAME, null);
				
				if(c.moveToFirst()){
					order = c.getInt(0) + 1;
				}
				
				c.close();
			}
			
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE, isFavorite);
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE_ORDER, order);
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + bm.getUrl() +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateFavorietOrder(BookModel bm) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.FAVORITE_ORDER, bm.getFavoriteOrder());
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + bm.getUrl() +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateUrl(String url, String newUrl) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.URL, newUrl);
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + url +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateContentUrl(BookModel bm) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.CONTENT_URL, bm.getContentUrl());
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + bm.getUrl() +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateCoverImg(BookModel bm) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.COVER_IMG, bm.getCoverImg());
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + bm.getUrl() +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateIntro(BookModel bm) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(BookDBOpenHelper.INTRO, bm.getIntro());
			return db.update(BookDBOpenHelper.BOOK_TABLE_NAME, newRingtoneValue, BookDBOpenHelper.URL + "='" + bm.getUrl() +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long delete(long id) {
		try{
			return db.delete(BookDBOpenHelper.BOOK_TABLE_NAME, "_id=" + id, null);
			
			
			} catch(SQLiteException ex) {
				Log.v("delete database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
}
