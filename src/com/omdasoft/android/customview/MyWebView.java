package com.omdasoft.android.customview;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.omdasoft.bookreader.BookReaderActivity;
import com.omdasoft.bookreader.DownloadUtil;
import com.omdasoft.bookreader.db.BookModel;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class MyWebView extends WebView {

	private Context context;
	
	private DownloadUtil util;

	private BookModel bm;

	private int mode = 1; // 1 - book intro ; 2 - content ; 3 - full text ; 4 -
							// search ; 5 - free visit

	public MyWebView(Context context) {
		
		super(context);
		
		this.context = context;
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		 this.context = context;
	}

	@Override
	public void loadUrl(String url) {
		if(url == null){
			return;
		}
		if (url.startsWith("http://")) {
			String tmp = null;
			if (mode != 5 || ((BookReaderActivity)context).getMode() == 3) {

				tmp = util.runFetchNParseBookByUrl(bm, mode, url);
				if(((BookReaderActivity)context).getMode() == 4 && mode == 1 && tmp != null && tmp.startsWith("http://")){
					tmp = util.runFetchNParseBookByUrl(bm, mode, tmp);
					mode = 5;
				}
			}

			if (tmp != null) {
				url = tmp;
				if (mode == 2 || mode == 4) {
					mode = 5;
				}
			}
		}
		bm.setLastReadAt(new Date().getTime());
		bm.setLastReadUrl(url);
		((BookReaderActivity)getContext()).updateLastRead(bm);
		if (url.startsWith("http://")) {
			getSettings().setJavaScriptEnabled(false);
		}else{
			getSettings().setJavaScriptEnabled(true);
		}
		super.loadUrl(url);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public BookModel getBm() {
		return bm;
	}

	public void setBm(BookModel bm) {
		this.bm = bm;
	}

	public DownloadUtil getUtil() {
		return util;
	}

	public void setUtil(DownloadUtil util) {
		this.util = util;
	}

}
