package com.omdasoft.bookreader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.omdasoft.android.customview.ArrayWheelAdapter;
import com.omdasoft.android.customview.LIFOList;
import com.omdasoft.android.customview.MyWebView;
import com.omdasoft.android.customview.OnWheelChangedListener;
import com.omdasoft.android.customview.WheelView;
import com.omdasoft.android.customview.grid.DraggableGridView;
import com.omdasoft.android.customview.grid.OnRearrangeListener;
import com.omdasoft.bookreader.db.BookDAO;
import com.omdasoft.bookreader.db.BookDBOpenHelper;
import com.omdasoft.bookreader.db.BookModel;

import net.yihabits.bookreader.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery.LayoutParams;

public class BookReaderActivity extends Activity {
	private String uid = "";
	private String LOGTAG = "BookReaderActivity";

	private ViewFlipper flipper;
	private GridView bookGrid;
	private DraggableGridView bookOrderGrid;
	private WheelView sourceWheel;
	private WheelView bookCategoryWheel;
	private WheelView topCategoryWheel;
	private ListView topListView;
	private MyWebView webview;
	private EditText searchTxt;
	private LinearLayout searchBar;
	private LinearLayout fakeLL;

	private LIFOList lastScreen = new LIFOList();

	private ArrayList<BookModel> favoriteBookList;
	private ArrayList<BookModel> viewHistoryBookList;
	private ArrayList<BookModel> topBookList;

	private int actualAmountofFavorite;

	private Integer[] qiCategoryList;
	private Integer[] qiCategoryList2;
	private Integer[] qiTopTypeList;
	private Integer[] zhCategoryList;
	private Integer[] zhTopTypeList;

	private int mode = 1; // 1- qidian ; 2 - zongheng ; 3 - guoxue ; 4 - shuku ; 5 - local
	private int top = 1; // 1 -
	private int category = 1; // 1 - all
	private int screen = 1; // 1 - bookshelf ; 2 - find books ; 3 - view book
	private boolean isDragGrid = false;

	private int selected_item = -1; // the selected item in the list

	private int gridColumns = 3;

	private DownloadUtil util;
	private Bitmap default_image;
	private BookDAO dba;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// initialize DownloadUtil
		if (util == null) {
			util = new DownloadUtil(this);
			util.initBaseDir();
		}

		if (favoriteBookList == null) {
			favoriteBookList = new ArrayList<BookModel>();
		}

		if (viewHistoryBookList == null) {
			viewHistoryBookList = new ArrayList<BookModel>();
		}

		if (topBookList == null) {
			topBookList = new ArrayList<BookModel>();
		}

		qiCategoryList = new Integer[] { -1, 1, 2, 3, 4, 5, 6, 7 };
		qiCategoryList2 = new Integer[] { -1, 21, 1, 2, 22, 4, 5, 6, 7, 8, 9,
				10 };
		qiTopTypeList = new Integer[] { 3, 0, 1, 2, 4, 19, 22 };
		zhCategoryList = new Integer[] { -1, 1, 3, 6, 9, 21, 15, 18 };
		zhTopTypeList = new Integer[] { -1, 2, 3, 4 };

		default_image = BitmapFactory.decodeResource(getResources(),
				R.drawable.bookcover1);

		flipper = (ViewFlipper) findViewById(R.id.flipper);

		initBtn();

		dba = BookDAO.getInstance(this);

		// ad initialization
		// Create the adView
		AdView adView = new AdView(this, AdSize.BANNER, "a14dd47bd0a3dd4");
		// Lookup your LinearLayout assuming it��s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_layout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		 adView.loadAd(new AdRequest());

	}

	private void initBtn() {
		final ToggleButton arrangeBtn = (ToggleButton) findViewById(R.id.arrangeBtn);
		arrangeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					resetGridDragMenu(arrangeBtn.isChecked());
				refreshGrid();
			}
		});
	}

	private void initWebviewBtn() {
		Button bookintroBtn = (Button) findViewById(R.id.bookIntroBtn);
		bookintroBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				webview.setMode(1);
				loadWeviewbUrl(webview.getBm().getUrl(), webview.getBm().getIntro(), false);
			}
		});

		Button contentBtn = (Button) findViewById(R.id.contentBtn);
		contentBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				webview.setMode(2);
				loadWeviewbUrl(webview.getBm().getContentUrl(), null, false);
			}
		});
		Button fulltextBtn = (Button) findViewById(R.id.fulltextBtn);
		fulltextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(webview.getBm().getFulltextUrl() == null){
					toastMsg(R.string.noFulltext);
				}else{
				webview.setMode(3);
				loadWeviewbUrl(webview.getBm().getFulltextUrl(), null, false);
				}
			}
		});
		Button searchBtn = (Button) findViewById(R.id.searchBtn);
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				webview.setMode(4);
				loadWeviewbUrl(webview.getBm().getSearchUrl(), null, false);
			}
		});
		Button addFavoriteBtn = (Button) findViewById(R.id.addFavoriteBtn);
		addFavoriteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addFavorite(webview.getBm());
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// We do nothing here. We're only handling this to keep orientation
		// or keyboard hiding from causing the WebView activity to restart.
	}

	@Override
	protected void onStart() {
		super.onStart();

		initPanel();

	}

	private void initPanel() {
		
		fakeLL = (LinearLayout) findViewById(R.id.fakeLL);
		
		switch (screen) {
		case 1: {
			if (bookGrid == null) {
				bookGrid = (GridView) findViewById(R.id.bookGrid);

				bookGrid.setAdapter(new BookGridAdapter(this));

				bookGrid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> adapter, View view,
							int position, long arg3) {
						if (position < actualAmountofFavorite) {
						gotoScreen(3);
						webview.setMode(1);
						webview.setBm(favoriteBookList.get(position));
						loadWeviewbUrl(webview.getBm().getUrl(), webview.getBm().getIntro(), true);
						}
					}
				});
				
				bookOrderGrid = (DraggableGridView) findViewById(R.id.bookOrderGrid);
				
				bookOrderGrid.setOnRearrangeListener(new OnRearrangeListener() {
					public void onRearrange(int oldIndex, int newIndex) {
						BookModel bm = favoriteBookList.remove(oldIndex);
						favoriteBookList.add(newIndex, bm);
						for(int i = 0; i < favoriteBookList.size() ; i++){
							bm = favoriteBookList.get(i);
							bm.setFavoriteOrder(i);
							updateFavoriteOrder(bm);
						}
					}
				});

				resetGridDragMenu(isDragGrid);
			}
			
			
			
			refreshGrid();

			break;
		}
		case 2: {
			if (topListView == null) {
				Button getTopListBtn = (Button) findViewById(R.id.getTopListBtn);
				getTopListBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						addMoreBooksToList();

					}
				});
				
				searchBar = (LinearLayout) findViewById(R.id.searchBar);
				
				ImageButton searchBtn = (ImageButton) findViewById(R.id.searchImgBtn);
				searchBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						searchMoreBooksToList();
						searchTxt.clearFocus();
						fakeLL.requestFocus();
						InputMethodManager imm = (InputMethodManager) getSystemService(
							    INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					}
				});
				
				searchTxt = (EditText) findViewById(R.id.searchTxt);

				topListView = (ListView) findViewById(R.id.topListView);
				topListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
				topListView.setAdapter(new ListAdapter(this));
				topListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> adapter, View view,
							int position, long arg3) {
						if (position == topBookList.size()) {
							addMoreBooksToList();
						} else {

							selected_item = position;

							gotoScreen(3);

							BookModel bookModel = topBookList
									.get(selected_item);
							if(mode == 1 || mode == 2){
							insertNewBook(bookModel);
							}

							webview.setBm(bookModel);

							webview.setMode(1);

							loadWeviewbUrl(webview.getBm().getUrl(), webview.getBm().getIntro(), true);
						}
					}
				});

				sourceWheel = (WheelView) findViewById(R.id.sourceWheelView);
				sourceWheel.setVisibleItems(5);
				sourceWheel.setAdapter(new ArrayWheelAdapter<String>(
						getResources().getStringArray(R.array.source)));
				sourceWheel.invalidate();

				bookCategoryWheel = (WheelView) findViewById(R.id.categoryWheelView);
				bookCategoryWheel.setVisibleItems(5);
				topCategoryWheel = (WheelView) findViewById(R.id.topListWheelView);
				topCategoryWheel.setVisibleItems(5);

				resetWheel(0, 0, 0);

				sourceWheel.addChangingListener(new OnWheelChangedListener() {

					@Override
					public void onChanged(WheelView wheel, int oldValue,
							int newValue) {
						resetWheel(newValue, 0, 0);

					}
				});

				bookCategoryWheel
						.addChangingListener(new OnWheelChangedListener() {

							@Override
							public void onChanged(WheelView wheel,
									int oldValue, int newValue) {
								resetWheel(mode - 1, top - 1, newValue);

							}
						});

				topCategoryWheel
						.addChangingListener(new OnWheelChangedListener() {

							@Override
							public void onChanged(WheelView wheel,
									int oldValue, int newValue) {
								resetWheel(mode - 1, newValue, category - 1);

							}
						});

			}
			break;
		}
		case 3: {

			if (webview == null) {
				final ProgressBar loadPB = (ProgressBar) findViewById(R.id.loadProgressBar);
				loadPB.setVisibility(View.GONE);
				
				webview = (MyWebView) findViewById(R.id.webView);

				webview.setWebChromeClient(new WebChromeClient() {
		            public void onProgressChanged(WebView view, int progress)
		            {
		            	if(loadPB.getVisibility() == View.GONE){
		            		loadPB.setVisibility(View.VISIBLE);
		            	}
		            	
		                loadPB.setProgress(progress * 100);
		 
		                if(progress == 100){
		                    loadPB.setVisibility(View.GONE);
		                }
		            }
		        });
				webview.setWebViewClient(new WebViewClient() {

					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						if (url.startsWith("http://")) {
							view.loadUrl(url);
							return true;
						} else {
							return false;
						}
					}

					@Override
					public void onReceivedError(WebView view, int errorCode,
							String description, String failingUrl) {
						// TODO Auto-generated method stub
						super.onReceivedError(view, errorCode, description, "");
					}
				});

				webview.setUtil(util);

				initWebviewBtn();
				
			}
			
			break;
		}

		default: {

		}
		}

	}

	private void loadWeviewbUrl(final String url, final String intro, final boolean isClear) {
		final ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.waitTitle), getString(R.string.waitContent),
				true,true);

		Runnable saveUrl = new Runnable() {

			public void run() {
				if((mode == 3 || webview.getBm().getSource() == 3) && webview.getMode() == 1){
						webview.loadData(intro, "text/html", "GBK");
				}else{
				webview.loadUrl(url);
				}
				dialog.dismiss();
				if(isClear){
					//clear history
					webview.clearHistory();
				}
			}
		};
		new Thread(saveUrl).start();
	}

	private void resetWheel(int source, int top, int category) {
		
		this.top = top + 1;
		this.category = category + 1;
		showSearchBar(false);
		searchTxt.setHint(R.string.searchHint);
		
		switch (source) {
		case 0:{
			mode = 1;
			
			topCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.qitopcategory)));
			 

			if (top == 0) {
				bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
						getResources().getStringArray(R.array.qibookcategory)));
			} else {
				bookCategoryWheel
						.setAdapter(new ArrayWheelAdapter<String>(
								getResources().getStringArray(
										R.array.qibookcategory2)));
			}
			
			searchTxt.setHint(R.string.searchHint2);
		}
			break;
		case 1:{
			mode = 2;

			topCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.zhtopcategory)));

			if (top == 0) {
				bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
						getResources().getStringArray(R.array.zhbookcategory)));
			} else if (this.top == 5 || this.top == 6) {
				bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
						new String[] {}));
			} else {
				bookCategoryWheel
						.setAdapter(new ArrayWheelAdapter<String>(
								getResources().getStringArray(
										R.array.zhbookcategory2)));
			}
			
			showSearchBar(true);
		}
			break;
		case 2:{
			mode = 3;
			
			topCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.guotopcategory)));
			if (top == 2) {
				bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
						getResources().getStringArray(R.array.guozicategory)));
			} else {
				bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
						new String[] {}));
			}
			
		}
			break;
		case 3:{
			mode = 4;
			topCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.shukutopcategory)));
			bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					new String[] {}));
			
		}
			break;
		case 4:{
			mode = 5;
			bookCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.localbookcategory)));
			topCategoryWheel.setAdapter(new ArrayWheelAdapter<String>(
					getResources().getStringArray(R.array.localtopcategory)));
		}
			break;
		}
		
		topCategoryWheel.setCurrentItem(top);
		bookCategoryWheel.setCurrentItem(category);
		
	}
	
	private void showSearchBar(final boolean flag){
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if(flag){
					searchBar.setVisibility(View.VISIBLE);
				}else{
					//hide search bar
					searchBar.setVisibility(View.GONE);
				}
			}
		});
	}

	public String getUrlByWheel() {
		String res = null;
		switch (mode) {
		case 1:{
			res = "http://www.qidian.com/";

			// get the beginning url by top category
			if (this.top == 2) {
				// push top
				if (this.category == 1) {
					res += "Book/CommendListNew.aspx?GroupId=3";
					return res;
				} else {
					res += "Book/CommendListNew.aspx?GroupId=129&categoryId=";
				}

			} else {
				// other tops
				res += "Book/TopDetail.aspx?TopType="
						+ qiTopTypeList[this.top - 1]
						+ "&DisplayMethod=1&Category=";

			}

			if (this.top == 1) {
				res += qiCategoryList[this.category - 1];
			} else {
				res += qiCategoryList2[this.category - 1];
			}
		}
			break;
		case 2:{
			res = "http://wap.zongheng.com/";

			// get the beginning url by top category
			if (this.top == 1) {
				// category hit top
				if (this.category == 1) {
					res += "rank?rankType=1&timeType=2";
				} else {
					res += "rank?rankType=1&timeType=2&cid="
							+ zhCategoryList[this.category - 1];
				}

			} else if (this.top == 5) {
				res += "rank/new";
			} else if (this.top == 6) {
				res += "rank/end";
			} else {
				// other tops
				res += "rank?rankType=2" + zhTopTypeList[this.top - 1]
						+ "&&timeType=" + (this.category - 1);

			}
		}
			break;
		case 3:{
			res = "http://www.guoxue.com/";

			// get the beginning url by top category
			switch(top){
			case 1:{
				res += "wenxian/wxjing/jingbu.htm";
				break;
			}
			case 2:{
				res += "wenxian/wxshi/shibu.htm";
				break;
			}
			case 3:{
				switch(category){
				case 1:{
					res += "wenxian/wxzhi/zibu-bing.htm";
					break;
				}
				case 2:{
					res += "wenxian/wxzhi/zibu-zhuzi.htm";
					break;
				}
				case 3:{
					res += "wenxian/wxzhi/zibu-zajia.htm";
					break;
				}
				case 4:{
					res += "wenxian/wxzhi/zibu-leishu.htm";
					break;
				}
				case 5:{
					res += "wenxian/wxzhi/zibu-xiaoshuo.htm";
					break;
				}
				case 6:{
					res += "wenxian/wxzhi/zibu-biji.htm";
					break;
				}
				}
				break;
			}
			case 4:{
				res += "wenxian/wxji/jibu.htm";
				break;
			}
				
			}
		}
			break;
		case 4:{
			res = "http://www.bookcool.com/";

		}
			break;
		}

		return res;
	}

	private void addMoreBooksToList() {
		// show waiting dialog
		final ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.waitTitle), getString(R.string.waitContent),
				true,true);

		Runnable saveUrl = new Runnable() {

			public void run() {
				selected_item = -1;
				topBookList.clear();
				topBookList.addAll(util.runFetchBookListByUrl(getUrlByWheel()));
				refreshList();
				dialog.dismiss();
			}
		};
		new Thread(saveUrl).start();

	}
	
	private void searchMoreBooksToList() {
		final String key = searchTxt.getText().toString();
		
		// show waiting dialog
		final ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.waitTitle), getString(R.string.waitContent),
				true,true);

		Runnable saveUrl = new Runnable() {

			public void run() {
				selected_item = -1;
				topBookList.clear();
				topBookList.addAll(util.runFetchBookListByKeyword(key));
				refreshList();
				dialog.dismiss();
			}
		};
		new Thread(saveUrl).start();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);

		if (((AdapterContextMenuInfo) menuInfo).position < actualAmountofFavorite) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		BookModel bm = favoriteBookList.get(info.position);

		switch (item.getItemId()) {
		case R.id.menu_view_this_book:
			gotoScreen(3);
			webview.setMode(1);
			webview.setBm(bm);
			loadWeviewbUrl(bm.getUrl(), webview.getBm().getIntro(), true);
			break;
		case R.id.menu_view_content:
			gotoScreen(3);
			webview.setMode(2);
			webview.setBm(bm);
			loadWeviewbUrl(bm.getContentUrl(), null, true);
			break;
		case R.id.menu_view_last_read:
			String url = bm.getLastReadUrl();
			if (url != null && url.startsWith("http://")) {
				gotoScreen(3);
				webview.setMode(5);
				webview.setBm(bm);
				loadWeviewbUrl(url, null, true);
			} else {
				toastMsg(R.string.noLastReadRecord);
			}
			break;
		case R.id.menu_search_online:
			gotoScreen(3);
			webview.setMode(4);
			webview.setBm(bm);
			loadWeviewbUrl(bm.getSearchUrl(), null, true);
			break;
		case R.id.menu_delete_this_book:
			removeFavorite(bm);
			refreshGrid();
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_bookshelf:
			gotoScreen(1);
			return true;
		case R.id.menu_toplist:
			gotoScreen(2);
			return true;
		case R.id.menu_help:
			// popup the about window
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(BookReaderActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void toastMsg(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public String getUid() {
		return Settings.Secure.getString(getContentResolver(),
				Settings.Secure.ANDROID_ID);
	}

	private void fillBooklist() {
		while (favoriteBookList.size() < 12) {
			favoriteBookList.add(new BookModel());
		}
		if (favoriteBookList.size() > 12) {
			while (favoriteBookList.size() % gridColumns != 0) {
				favoriteBookList.add(new BookModel());
			}
		}
	}

	public void refreshGrid() {
		favoriteBookList.clear();
		favoriteBookList.addAll(getFavoriteBooks());
		actualAmountofFavorite = favoriteBookList.size();
		
		bookOrderGrid.removeAllViews();
		for(BookModel bm : favoriteBookList){
			bookOrderGrid.addView(getThumb(bm));
		}
		
		fillBooklist();
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				fillBooklist();
				((BaseAdapter) bookGrid.getAdapter()).notifyDataSetChanged();
//				bookGrid.invalidateViews();
			}
		});
	}
	
	private ImageView getThumb(BookModel bm)
	{
		ImageView iv = new ImageView(this);
		iv.setLayoutParams(new ViewGroup.LayoutParams(101, 121));
		
		Bitmap bmp = Bitmap.createBitmap(101, 121, Bitmap.Config.RGB_565);
	    if (bm.getName() != null) {
			Canvas canvas = new Canvas(bmp);
		    Paint paint = new Paint();
	    	paint.setColor(Color.BLACK);
		    paint.setTextSize(20);
		    paint.setTextAlign(Align.CENTER);
		    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		    canvas.drawColor(Color.WHITE);
		    canvas.drawText(bm.getName(), 50, 60, paint);
		}
		if (bm.getCoverImg() != null) {
			iv.setImageBitmap(decodeFile(bm.getCoverImg(),new ImageCallback(iv)));
		}else{
			iv.setImageBitmap(bmp);
		}
	    
		return iv;
	}
	
	private void resetGridDragMenu(boolean flag){
		isDragGrid = flag;
		
		if(isDragGrid){
			unregisterForContextMenu(bookGrid);
			bookGrid.setVisibility(View.GONE);
			bookOrderGrid.setVisibility(View.VISIBLE);
//			bookGrid.setDropListener(mDropListener);
//			bookGrid.setRemoveListener(mRemoveListener);
//			bookGrid.setDragListener(mDragListener);
		}else{
			registerForContextMenu(bookGrid);
			bookGrid.setVisibility(View.VISIBLE);
			bookOrderGrid.setVisibility(View.GONE);
//			bookGrid.setOnTouchListener(null);
//			bookGrid.setDropListener(null);
//			bookGrid.setRemoveListener(null);
//			bookGrid.setDragListener(null);
		}
	}

	public void refreshList() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((BaseAdapter) topListView.getAdapter()).notifyDataSetChanged();
				topListView.setSelection(0);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
            if (screen == 3 && action == KeyEvent.ACTION_DOWN) {
            	webview.pageUp(false);
               return true;
            }
            break;
            
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (screen == 3 && action == KeyEvent.ACTION_DOWN) {
            	webview.pageDown(false);
            	return true;
            }
            break;
		case KeyEvent.KEYCODE_BACK:
			if(screen == 3 && webview.canGoBack()){
				webview.goBack();
			}else{
			goPrevScreen();
			}
			return true;
		default:
			return false;
		}
		return false;
	}

	public void goPrevScreen() {
		Integer code = (Integer) lastScreen.pop();
		if (code != null) {
			// 1.change the current screen code
			screen = code;

			// 2.jump to previous screen by code
			flipper.setInAnimation(inFromLeftAnimation());
			flipper.setOutAnimation(outToRightAnimation());
			flipper.setDisplayedChild(code - 1);

			// initiate panel
			initPanel();

		} else {
			// quit app
			this.finish();
		}
	}

	private class BookGridAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public BookGridAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return favoriteBookList.size();
		}

		public BookModel getItem(int i) {
			return favoriteBookList.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(final int position, View convertView, ViewGroup vg) {
			if (favoriteBookList == null || position < 0
					|| position >= favoriteBookList.size())
				return null;

			final View row = mInflater.inflate(R.layout.grid_item, null);

				GridViewHolder holder = (GridViewHolder) row.getTag();
				if (holder == null) {
					holder = new GridViewHolder(row);
					row.setTag(holder);
				}

				// other normal row
				final BookModel bm = favoriteBookList.get(position);
				if (bm.getName() != null) {
					holder.title.setText(bm.getName());
				}
				if (bm.getCoverImg() != null) {
					holder.icon.setImageBitmap(decodeFile(bm.getCoverImg(),
							new ImageCallback(holder.icon)));
					if(bm.getSource() == 3 || bm.getSource() == 4){
					holder.icon.setLayoutParams(new LinearLayout.LayoutParams(101, 121));
					}
				}

			if ((position + 1) % gridColumns == 1) {
				row.setBackgroundResource(R.drawable.grid_level_left);
			} else if ((position + 1) % gridColumns == 0) {
				row.setBackgroundResource(R.drawable.grid_level_right);
			} else {
				row.setBackgroundResource(R.drawable.grid_level_mid);
			}

			return (row);
		}
		
	}

	class GridViewHolder {
		TextView title = null;
		ImageView icon = null;

		GridViewHolder(View base) {
			this.title = (TextView) base.findViewById(R.id.titleTxt);
			this.icon = (ImageView) base.findViewById(R.id.coverImg);
		}
	}
	

	private class ListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return topBookList.size();
		}

		public BookModel getItem(int i) {
			return topBookList.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(final int position, View convertView, ViewGroup vg) {
			if (topBookList == null || position < 0
					|| position > topBookList.size())
				return null;

			final View row = mInflater.inflate(R.layout.list_item, null);

			// the last row is the more... row
			if (position == topBookList.size()) {

				ViewHolder holder = (ViewHolder) row.getTag();
				if (holder == null) {
					holder = new ViewHolder(row);
					row.setTag(holder);
				}

				// set the nameTxt
				holder.nameTxt.setText(R.string.addMore);
				holder.icon.setVisibility(View.GONE);
				holder.favorite.setVisibility(View.GONE);
				holder.coverImg.setVisibility(View.GONE);
			} else {

				ViewHolder holder = (ViewHolder) row.getTag();
				if (holder == null) {
					holder = new ViewHolder(row);
					row.setTag(holder);
				}

				// other normal row
				final BookModel rm = topBookList.get(position);

				// set name to label
				holder.nameTxt.setText(position + 1 + "." + rm.getName());

				if (rm.getCoverImg() != null) {
					holder.coverImg.setImageBitmap(decodeFile(rm.getCoverImg(),
							new ImageCallback(holder.coverImg)));
				}

				if (position == selected_item) {
					holder.icon.setImageResource(R.drawable.right_selected_24);
				} else {
					holder.icon.setImageResource(R.drawable.right_24);
				}

				holder.favorite.setVisibility(View.VISIBLE);
				if (rm.getIsFavorite() == 1) {
					holder.favorite.setText(R.string.isFavorite);
				}

			}

			return (row);
		}

	}

	class ViewHolder {
		TextView nameTxt = null;
		TextView favorite = null;
		ImageView icon = null;
		ImageView coverImg = null;

		ViewHolder(View base) {
			this.nameTxt = (TextView) base.findViewById(R.id.nameTxt);
			this.favorite = (TextView) base.findViewById(R.id.favoriteTxt);
			this.icon = (ImageView) base.findViewById(R.id.icon);
			this.coverImg = (ImageView) base.findViewById(R.id.coverImg);
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(String url, final ImageCallback callback) {
		String path = util.convertUrl2ImgFileName(url);

		File f = new File(path);

		// 1. check f is exist
		if (f.exists()) {

			return dealImg(path);
		} else {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String tmp = (String) msg.obj;
					callback.setDrawable(dealImg(tmp));
				}
			};
			util.runSaveImageByUrl(url, handler);
			return default_image;
		}
	}

	public Bitmap dealImg(String path) {
		try {
			File f = new File(path);
			// Decode image size
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
					null, null);

			return bitmap;
		} catch (OutOfMemoryError e) {
			Log.e(LOGTAG, "Out of memory error :(");
		} catch (Exception e) {

		}
		return default_image;
	}

	class ImageCallback {
		ImageView iv;
		int position;

		ImageCallback(ImageView iv) {
			this.iv = iv;
			this.position = -1;
		}

		ImageCallback(int position) {
			this.iv = null;
			this.position = position;
		}

		public void setDrawable(final Drawable drawable) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (iv != null) {
						iv.setImageDrawable(drawable);
					}
				}
			});
		}

		public void setDrawable(final Bitmap bitmap) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (iv != null) {
						iv.setImageBitmap(bitmap);
					}
				}
			});
		}
	}

	public int getMode() {
		return mode;
	}

	public int getTop() {
		return top;
	}
	
	public String getTopString() {
		return topCategoryWheel.getAdapter().getItem(top-1);
	}
	
	public String getNextTopString() {
		if(top-1 < topCategoryWheel.getAdapter().getItemsCount()){
		return topCategoryWheel.getAdapter().getItem(top);
		}else{
			return "</tbody>";
		}
	}

	public int getCategory() {
		return category;
	}

	public MyWebView getWebview() {
		return webview;
	}

	public ArrayList<BookModel> getFavoriteBooks() {
		ArrayList<BookModel> list;
		dba.open();

		Cursor c = dba.getFavoriteBook();

		list = convertCusor2Model(c);

		c.close();

		dba.close();

		return list;
	}
	
	public ArrayList<BookModel> getBookListByCategory(int cat) {
		ArrayList<BookModel> list;
		dba.open();

		Cursor c = dba.getBookListByCategory(mode, cat);

		list = convertCusor2Model(c);

		c.close();

		dba.close();

		return list;
	}

	private ArrayList<BookModel> convertCusor2Model(Cursor c) {
		ArrayList<BookModel> list = new ArrayList<BookModel>();
		startManagingCursor(c);
		if (c.moveToFirst()) {
			do {
				long id = c.getLong(0);
				String name = c.getString(c
						.getColumnIndex(BookDBOpenHelper.NAME));
				String intro = c.getString(c
						.getColumnIndex(BookDBOpenHelper.INTRO));
				String author = c.getString(c
						.getColumnIndex(BookDBOpenHelper.AUTHOR));
				String url = c
						.getString(c.getColumnIndex(BookDBOpenHelper.URL));
				String contentUrl = c
						.getString(c.getColumnIndex(BookDBOpenHelper.CONTENT_URL));
				String imgUrl = c.getString(c
						.getColumnIndex(BookDBOpenHelper.COVER_IMG));
				String fulltext = c.getString(c
						.getColumnIndex(BookDBOpenHelper.FULLTEXT));
				String bookcode = c.getString(c
						.getColumnIndex(BookDBOpenHelper.BOOK_CODE));
				String latestChapter = c.getString(c
						.getColumnIndex(BookDBOpenHelper.LATEST_CHAPTER));
				String lastReadUrl = c.getString(c
						.getColumnIndex(BookDBOpenHelper.LAST_READ_URL));
				int isFavorite = c.getInt(c
						.getColumnIndex(BookDBOpenHelper.FAVORITE));
				int source = c
						.getInt(c.getColumnIndex(BookDBOpenHelper.SOURCE));
				int category = c
						.getInt(c.getColumnIndex(BookDBOpenHelper.CATEGORY));
				long createdAt = c.getInt(c
						.getColumnIndex(BookDBOpenHelper.CREATED_AT));
				long lastReadAt = c.getInt(c
						.getColumnIndex(BookDBOpenHelper.LAST_READ_AT));

				BookModel temp = new BookModel();
				temp.setId(id);
				temp.setName(name);
				temp.setIntro(intro);
				temp.setBookCode(bookcode);
				temp.setAuthor(author);
				temp.setUrl(url);
				temp.setContentUrl(contentUrl);
				temp.setCoverImg(imgUrl);
				temp.setFulltext(fulltext);
				temp.setIsFavorite(isFavorite);
				temp.setSource(source);
				temp.setCategory(category);
				temp.setCaretedAt(createdAt);
				temp.setLatestChapter(latestChapter);
				temp.setLastReadUrl(lastReadUrl);
				temp.setLastReadAt(lastReadAt);

				list.add(temp);
			} while (c.moveToNext());
		}
		return list;
	}

	public void addFavorite(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateFavoriet(bm, 1);

		dba.close();

		toastMsg(R.string.addFavoriteBookSuccess);
	}
	
	public void removeFavorite(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateFavoriet(bm, 0);

		dba.close();

		toastMsg(R.string.removeFavoriteBookSuccess);
	}
	
	public void updateLastRead(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateLastRead(bm);

		dba.close();

	}
	
	public void updateFavoriteOrder(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateFavorietOrder(bm);

		dba.close();

	}
	
	public void updateUrl(String url, String newUrl) {
		// update or insert db
		dba.open();
		dba.updateUrl(url, newUrl);

		dba.close();

	}
	
	public void updateContentUrl(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateContentUrl(bm);

		dba.close();

	}
	
	public void updateCoverImg(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateCoverImg(bm);

		dba.close();

	}
	
	public void updateIntro(BookModel bm) {
		// update or insert db
		dba.open();
		dba.updateIntro(bm);

		dba.close();

	}

	public boolean insertNewBook(final BookModel rm) {
		boolean flag = false;
		// update or insert db
		dba.open();
		
		Cursor c = dba.getBookByUrl(rm.getUrl());
		if(!c.moveToFirst()){
		
			flag = dba.insert(rm) != -1;
		}
		c.close();
		dba.close();

		return flag;

	}

	public void toastMsg(int resId, String... args) {
		final String msg = this.getString(resId, args);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private void gotoScreen(int code) {
		// 1.exit while no changes
		if (code == screen)
			return;

		lastScreen.push(screen);

		flipper.setInAnimation(inFromRightAnimation());
		flipper.setOutAnimation(outToLeftAnimation());
		flipper.setDisplayedChild(code - 1);

		screen = code;

		initPanel();
	}

	private Animation inFromRightAnimation() {

		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	private Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
}