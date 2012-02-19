package com.omdasoft.bookreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.omdasoft.bookreader.db.BookModel;

public class DownloadUtil {

	protected static final String LOGTAG = "DownloadUtil";

	private BookReaderActivity activity;

	private boolean flag = false;

	public static HashMap<Integer, Integer> pageMap = new HashMap<Integer, Integer>();

	public DownloadUtil(BookReaderActivity activity) {
		this.activity = activity;
	}

	public void runSaveImageByUrl(final String url, final Handler handler) {

		final String path = convertUrl2ImgFileName(url);

		File tmp = new File(path);
		if (tmp.exists()) {
			return;
		}

		Runnable saveUrl = new Runnable() {
			public void run() {

				HttpEntity resEntity = null;

				ApplicationEx app = (ApplicationEx) activity.getApplication();

				HttpClient httpclient = app.getHttpClient();

				HttpGet httpget;

				HttpResponse response;

				try {

					httpget = new HttpGet(url);

					response = httpclient.execute(httpget);

					int status = response.getStatusLine().getStatusCode();

					if (status == HttpStatus.SC_OK) {
						resEntity = response.getEntity();

						// save to sdcard
						save2card(resEntity, path);

						Message msg = handler.obtainMessage(0, path);
						handler.sendMessage(msg);
					}

				} catch (OutOfMemoryError e) {
					Log.e(LOGTAG, "Out of memory error :(");
				} catch (Exception e) {
					// remove this image from list and update
					// location of post

				} finally {

					if (resEntity != null) {
						resEntity = null;
					}
				}

			}
		};
		new Thread(saveUrl).start();

	}

	public String convertUrl2ImgFileName(String imgUrl) {
		final String dir = initBaseDir() + "/" + activity.getMode()
				+ "/coverimg/";
		initDir(dir);

		String path = dir + parseFileNameByUrl(imgUrl);
		return path;
	}

	public String convertUrl2TmpFileName(String imgUrl) {
		final String dir = initBaseDir() + "/" + activity.getMode() + "/temp/";
		initDir(dir);

		String path = dir + parseFileNameByUrl(imgUrl);
		return path;
	}

	public ArrayList<BookModel> runFetchBookListByKeyword(final String keyword) {

		HttpEntity resEntity = null;

		String encode = "gb2312";
		if (activity.getMode() == 3) {
			encode = "GBK";
		}

		ApplicationEx app = (ApplicationEx) activity.getApplication();

		HttpClient httpclient = app.getHttpClient();

		HttpGet httpget;

		HttpResponse response;

		try {

			httpget = new HttpGet(getSearchUrl(keyword));

			response = httpclient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				resEntity = response.getEntity();

				return parseSearchBookList(EntityUtils.toString(resEntity, encode),
						encode);
			}

		} catch (OutOfMemoryError e) {
			Log.e(LOGTAG, "Out of memory error :(");
		} catch (Exception e) {
			// remove this image from list and update
			// location of post
			Log.e(LOGTAG, "-----------" + e.getMessage());
		} finally {

			if (resEntity != null) {
				resEntity = null;
			}
		}
		return new ArrayList<BookModel>();
	}

	private String getSearchUrl(String keyword) {
		String res = "";
		switch (activity.getMode()) {
		case 1: {
			try {
				res = "http://sosu.qidian.com/searchresult.aspx?searchkey=" + URLEncoder.encode(keyword, "gb2312") + "&searchtype=%E7%BB%BC%E5%90%88";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			break;
		case 2: {
				res = "http://wap.zongheng.com/search?keywords=" + keyword + "&field=bookName";
		}
			break;
		case 3: {

		}
			break;
		case 4: {
			try {
				res = "http://www.google.com.hk/#hl=zh-CN&site=dangdang.com&q=" + URLEncoder.encode(keyword, "gb2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
			break;
		}
		return res;
	}

	public ArrayList<BookModel> runFetchBookListByUrl(final String url) {

		// while source is shuku, and already fetch booklist ever
		if (activity.getMode() == 4) {
			ArrayList<BookModel> list = activity.getBookListByCategory(activity
					.getTop());
			if (list != null && list.size() > 0) {
				return list;
			}
		} else if (activity.getMode() == 3) {
			// while source is guoxue, and already fetch booklist ever
			int cat = activity.getTop();
			if (cat == 3) {
				cat = activity.getTop() * 10 + activity.getCategory();
			}
			ArrayList<BookModel> list = activity.getBookListByCategory(cat);
			if (list != null && list.size() > 0) {
				return list;
			}
		}

		HttpEntity resEntity = null;

		String encode = "gb2312";
		if (activity.getMode() == 3) {
			encode = "GBK";
		}

		ApplicationEx app = (ApplicationEx) activity.getApplication();

		HttpClient httpclient = app.getHttpClient();

		HttpGet httpget;

		HttpResponse response;

		try {

			httpget = new HttpGet(url);

			response = httpclient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				resEntity = response.getEntity();

				return parseBookList(EntityUtils.toString(resEntity, encode),
						encode);
			}

		} catch (OutOfMemoryError e) {
			Log.e(LOGTAG, "Out of memory error :(");
		} catch (Exception e) {
			// remove this image from list and update
			// location of post
			Log.e(LOGTAG, "runFetchBookListByUrl-----------" + e.getMessage());
			e.printStackTrace();
		} finally {

			if (resEntity != null) {
				resEntity = null;
			}
		}
		return new ArrayList<BookModel>();
	}

	private ArrayList<BookModel> parseBookList(String content, String encode) {
		ArrayList<BookModel> reslist = new ArrayList<BookModel>();
		Source source = new Source(content);
		switch (activity.getMode()) {
		case 1: {
			if (activity.getTop() != 2) {
				// top parse
				// deal with navigation bar
				Element ele = source.getElementById("imglist");
				if (ele == null)
					break;
				List<Element> trList = ele.getAllElements(HTMLElementName.TR);

				for (Element ul : trList) {
					List<Element> aList = ul.getAllElements(HTMLElementName.A);
					BookModel bm = new BookModel();
					bm.setName(aList.get(1).getTextExtractor().toString());
					bm.setAuthor(aList.get(3).getTextExtractor().toString());
					String href = aList.get(0).getAttributeValue("href");
					if (href.startsWith("/")) {
						href = "http://www.qidian.com" + href;
					}
					bm.setUrl(href);
					bm.setBookCode(href.substring(href.lastIndexOf("/") + 1,
							href.length() - 5));
					bm.setCoverImg(ul.getFirstElement(HTMLElementName.IMG)
							.getAttributeValue("src"));
					bm.setCaretedAt(new Date().getTime());
					bm.setIsFavorite(0);
					bm.setSource(activity.getMode());
					bm.setCategory(activity.getCategory());

					reslist.add(bm);
				}
			} else {
				// commend parse
				// List<Element> list = source.getAllElementsByClass("f1");
				List<Element> list2 = source.getAllElementsByClass("b2");
				content = "";
				for (int i = 0; i < list2.size(); i++) {
					// content += ele.toString();
					List<Element> aList = list2.get(i).getAllElements(
							HTMLElementName.A);
					for (int j = 0; j < aList.size(); j++) {
						BookModel bm = new BookModel();
						bm.setName(aList.get(j).getTextExtractor().toString());
						String href = aList.get(j).getAttributeValue("href");
						if (href.startsWith("/")) {
							href = "http://www.qidian.com" + href;
						}
						bm.setUrl(href);
						bm.setAuthor(aList.get(j++).getTextExtractor()
								.toString());
						bm.setBookCode(href.substring(
								href.lastIndexOf("/") + 1, href.length() - 5));
						bm.setCoverImg("http://image.cmfu.com/books/"
								+ bm.getBookCode() + "/" + bm.getBookCode()
								+ ".jpg");
						bm.setCaretedAt(new Date().getTime());
						bm.setIsFavorite(0);
						bm.setSource(activity.getMode());
						bm.setCategory(activity.getCategory());

						reslist.add(bm);
					}
				}

			}
		}
			break;
		case 2: {

			// top parse
			// deal with navigation bar
			Element ele = source.getFirstElementByClass("list");
			if (ele == null)
				break;
			List<Element> aList = ele.getAllElements(HTMLElementName.A);

			for (Element ul : aList) {
				BookModel bm = new BookModel();
				bm.setName(ul.getTextExtractor().toString());
				if (bm.getName().equals("<<")) {
					break;
				}
				// bm.setAuthor(aList.get(3).getTextExtractor().toString());
				String href = ul.getAttributeValue("href");
				if (href.startsWith("/")) {
					href = "http://wap.zongheng.com" + href;
				}
				bm.setUrl(href + "&more=1");
				bm.setBookCode(href.substring(href.lastIndexOf("=") + 1,
						href.length()));
				// bm.setCoverImg(ul.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
				bm.setCaretedAt(new Date().getTime());
				bm.setIsFavorite(0);
				bm.setSource(activity.getMode());
				bm.setCategory(activity.getCategory());

				reslist.add(bm);
			}
		}
			break;
		case 3: {
			// get book list
			try {
				List<Element> tdlist = source
						.getAllElements(HTMLElementName.TD);
				for (Element ele : tdlist) {
					String width = ele.getAttributeValue("width");
					if (width != null && Integer.valueOf(width) == 604) {
						Element table = ele.getAllElements(
								HTMLElementName.TABLE).get(2);

						List<Element> trList = table
								.getAllElements(HTMLElementName.TR);

						int i = 0;
						for (Element trEle : trList) {
							BookModel bm = new BookModel();
							List<Element> tdList = trEle
									.getAllElementsByClass("t3");
							List<Element> tdList2 = trEle
									.getAllElementsByClass("t2");
							if (tdList != null && tdList.size() > 0) {
								Element tdEle = tdList.get(0);
								// set title
								bm.setName(tdEle.getTextExtractor().toString());
								// set author
								List<Element> tdList3 = trEle
										.getAllElementsByClass("t2");
								if (tdList3.size() == 3) {
									bm.setUrl(tdList3.get(2)
											.getAllElements(HTMLElementName.A)
											.get(0).getAttributeValue("href"));
									bm.setAuthor(tdList3.get(0)
											.getTextExtractor().toString());
								} else if (tdList3.size() == 2) {
									bm.setUrl(tdList3.get(1)
											.getAllElements(HTMLElementName.A)
											.get(0).getAttributeValue("href"));
									bm.setAuthor("NA");
								} else {
									continue;
								}
								// set created at time
								bm.setCaretedAt(new Date().getTime());
								bm.setSource(activity.getMode());
								if (activity.getTop() != 3) {
									bm.setCategory(activity.getTop());
								} else {
									bm.setCategory(activity.getTop() * 10
											+ activity.getCategory());

								}
								
								activity.insertNewBook(bm);

								reslist.add(bm);
							} else if (tdList2.size() == 1) {
								i++;
								if (i == 1) {
									continue;
								}
								// set intro
									int j = 1;
									while(reslist.size() >= j && reslist.get(reslist.size() - j).getIntro() == null){
								reslist.get(reslist.size() - j).setIntro(
										tdList2.get(0).getTextExtractor()
												.toString());
								activity.updateIntro(reslist.get(reslist
										.size() - j));
									}

								
							}
						}

						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			break;
		case 4: {
			String topString = activity.getTopString() + "</font>";
			int start = content.indexOf(topString);
			content = content.substring(start);
			topString = activity.getNextTopString();
			int end = content.indexOf(topString);
			content = content.substring(7, end);

			source = new Source(content);
			List<Element> alist = source.getAllElements(HTMLElementName.A);
			for (Element ele : alist) {
				BookModel bm = new BookModel();
				bm.setName(ele.getTextExtractor().toString());
				bm.setAuthor("NA");
				String href = ele.getAttributeValue("href");
				bm.setUrl(href);
				bm.setCaretedAt(new Date().getTime());
				bm.setIsFavorite(0);
				bm.setSource(activity.getMode());
				// set category
				bm.setCategory(activity.getTop());

				activity.insertNewBook(bm);

				reslist.add(bm);
			}
		}
			break;
		}
		return reslist;
	}
	
	private ArrayList<BookModel> parseSearchBookList(String content, String encode) {
		ArrayList<BookModel> reslist = new ArrayList<BookModel>();
		Source source = new Source(content);
		switch (activity.getMode()) {
		case 1: {
				// top parse
				Element ele = source.getElementById("showdiv");
				if (ele == null)
					break;
				List<Element> liList = ele.getAllElements(HTMLElementName.LI);

				for (Element ul : liList) {
					List<Element> aList = ul.getAllElements(HTMLElementName.A);
					BookModel bm = new BookModel();
					bm.setName(aList.get(0).getTextExtractor().toString());
					bm.setAuthor(aList.get(1).getTextExtractor().toString());
					String href = aList.get(0).getAttributeValue("href");
					if (href.startsWith("/")) {
						href = "http://www.qidian.com" + href;
					}
					bm.setUrl(href);
					bm.setBookCode(href.substring(href.lastIndexOf("/") + 1,
							href.length() - 5));
//					bm.setCoverImg(ul.getFirstElement(HTMLElementName.IMG)
//							.getAttributeValue("src"));
					bm.setCaretedAt(new Date().getTime());
					bm.setIsFavorite(0);
					bm.setSource(activity.getMode());
//					bm.setCategory(activity.getCategory());

					reslist.add(bm);

			}
		}
			break;
		case 2: {

			// top parse
			// deal with navigation bar
			Element ele = source.getFirstElementByClass("list dashline");
			if (ele == null)
				break;
			List<Element> aList = ele.getAllElements(HTMLElementName.A);

			for (Element ul : aList) {
				BookModel bm = new BookModel();
				bm.setName(ul.getTextExtractor().toString());
				if (bm.getName().equals("<<")) {
					break;
				}
				// bm.setAuthor(aList.get(3).getTextExtractor().toString());
				String href = ul.getAttributeValue("href");
				if (href.startsWith("/")) {
					href = "http://wap.zongheng.com" + href;
				}
				bm.setUrl(href + "&more=1");
				bm.setBookCode(href.substring(href.lastIndexOf("=") + 1,
						href.length()));
				// bm.setCoverImg(ul.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
				bm.setCaretedAt(new Date().getTime());
				bm.setIsFavorite(0);
				bm.setSource(activity.getMode());
//				bm.setCategory(activity.getCategory());

				reslist.add(bm);
			}
		}
			break;
		case 3: {
		}
			break;
		case 4: {
			String topString = activity.getTopString() + "</font>";
			int start = content.indexOf(topString);
			content = content.substring(start);
			topString = activity.getNextTopString();
			int end = content.indexOf(topString);
			content = content.substring(7, end);

			source = new Source(content);
			List<Element> alist = source.getAllElements(HTMLElementName.A);
			for (Element ele : alist) {
				BookModel bm = new BookModel();
				bm.setName(ele.getTextExtractor().toString());
				bm.setAuthor("NA");
				String href = ele.getAttributeValue("href");
				bm.setUrl(href);
				bm.setCaretedAt(new Date().getTime());
				bm.setIsFavorite(0);
				bm.setSource(activity.getMode());
				// set category
				bm.setCategory(activity.getTop());

				activity.insertNewBook(bm);

				reslist.add(bm);
			}
		}
			break;
		}
		return reslist;
	}

	public String runFetchNParseBookByUrl(BookModel bm, int mode, String orgurl) {

		// 1.remove ? & / from url and get the path
		String url = "";
		switch (mode) {
		case 1:
			url = bm.getUrl();
			break;
		case 2:
			url = bm.getContentUrl();
			break;
		case 3:
			url = bm.getFulltextUrl();
			break;
		case 4:
			url = bm.getSearchUrl();
			break;
		case 5:
			if (activity.getMode() == 3) {
				url = orgurl;
				mode = 2;
			}
			break;

		}
		String path = convertUrl2TmpFileName(url);
		if (path == null) {
			return url;
		}
		File tmp = new File(path);
		if (tmp.exists()
				&& (new Date().getTime() - tmp.lastModified() < 3600000)) {
			return "file://" + path;
		} else if (tmp.exists()) {
			tmp.delete();
		}

		HttpEntity resEntity = null;

		String encode = "gb2312";

		ApplicationEx app = (ApplicationEx) activity.getApplication();

		HttpClient httpclient = app.getHttpClient();

		HttpGet httpget = null;

		HttpResponse response;

		try {

			httpget = new HttpGet(url);
			response = httpclient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				resEntity = response.getEntity();

				switch (mode) {
				case 1:
					return parseBookIntro(
							EntityUtils.toString(resEntity, encode), path, url);
				case 2:
					return parseBookContent(
							EntityUtils.toString(resEntity, encode), path, url);
				case 3:
					return parseBookFullText(
							EntityUtils.toString(resEntity, encode), path);
				case 4:
					return parseBookSearch(
							EntityUtils.toString(resEntity, encode), path);
				}

			}

		} catch (OutOfMemoryError e) {
			Log.e(LOGTAG, "Out of memory error :(");
		} catch (Exception e) {
			e.printStackTrace();
			// return runFetchNParseBookByUrl(bm, mode);
		} finally {

			if (resEntity != null) {
				resEntity = null;
			}
		}
		return null;
	}

	private String parseBookSearch(String content, String path) {
		String res = null;
		Source source = new Source(content);
		switch (activity.getMode()) {
		case 1:
			// get table
			source = new Source(content);
			Element ele = source.getElementById("sopage");
			content = ele.toString();
			int end = content.indexOf("<div id=\"link");
			if (end > 0) {
				content = content.substring(0, end);
			}

			// replace class="gre" by class="nave"
			// content = content.replace("class=\"gre\"", "class=\"nave\"");

			// remove the first column - 1 li every four lies
			source = new Source(content);
			List<Element> list = source.getAllElements(HTMLElementName.UL);
			int i = 0;
			for (Element liele : list) {
				String tmp = liele.toString();
				i++;

				if (i % 3 == 1) {
					if (i == 1) {
						content = content.replace(tmp, "<tr>");
					} else {
						content = content.replace(tmp, "");
					}
				} else if (i % 3 == 0) {
					content = content.replace(tmp, tmp + "</tr><tr>");
				}
			}

			content = content.replace("<li>", "<td nowrap='nowrap'><li>");
			content = content.replace("</li>", "</li></td nowrap='nowrap'>");
			content = content.replace("<a href='",
					"<a href='http://www.fv0.net");

			// no result tips
			if (i == 4) {
				content += "No search result...";
			}

			content = addHeadEndNoBook(content, "gb2312");

			// 3.save html page
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		}
		return res;
	}

	private String parseBookFullText(String content, String path) {
		String res = null;
		switch (activity.getMode()) {
		case 1: {
			// get center
			Source source = new Source(content);
			content = source.getFirstElement(HTMLElementName.CENTER).toString();

			// remove div:nav
			Element ele = source.getFirstElementByClass("nav");
			if (ele != null) {
				content = content.replace(ele.toString(), "");
			}

			// remove all ad link
			content = removeAd(content);

			content = addHeadEnd(content, "gb2312");

			// 3.save html page
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
		}
			break;
		case 2: {
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
		}
			break;
		case 3:
			break;
		case 4:
			break;
		}
		return res;
	}

	private String parseBookContent(String content, String path, String url) {
		String res = null;
		Source source = new Source(content);
		switch (activity.getMode()) {
		case 1: {
			Element ele = source.getElementById("content");
			content = ele.toString();

			content = replaceHref4Qidian(content, url);

			content = addHeadEndNoBook(content, "gb2312");

			// 3.save html page
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
		}
			break;
		case 2:
			break;
		case 3: {
			// while reading tag <a> more than one
			content = replaceHref4Guoxue(content, url);
			source = new Source(content);

			Element body = getBody(source);
			Element jing = get13jingHref(body);
			if (jing != null) {
				String tmp = content.replace(jing.toString(), "");
				source = new Source(tmp);
			}

			List<Element> aele = source.getAllElements(HTMLElementName.A);
			if (url.contains("/gxzi/")) {
				List<Element> tdlist = source
						.getAllElements(HTMLElementName.TD);
				for (Element iele : tdlist) {
					String tmp = iele.getTextExtractor().toString();
					if (tmp.length() > 200) {
						content = iele.toString();
					}
				}
			} else if (aele != null && aele.size() > 3
					&& !url.contains("/qts_")) {
				content = "";
				String tmp = "";
				for (Element ele : aele) {
					String href = ele.getAttributeValue("href");
					if (href != null && tmp.contains(href + ",")
							&& ele.getTextExtractor().toString().length() < 2) {
						continue;
					} else {
						tmp += href + ",";
					}
					content += addTrTd(removeSpanFont(ele));
				}
			} else {
				Element table = findCorrectTable(body);
				if (table != null) {
					content = table.toString();
				}
			}

			content = addHeadEndNoBook(content, "GBK");

			// 3.save html page
			try {
				save2card(content.getBytes("GBK"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
		}
			break;
		case 4:
			break;
		}
		return res;
	}

	private String parseBookIntro(String content, String path, String url) {
		String res = null;
		Source source = new Source(content);
		Element ele;
		switch (activity.getMode()) {
		case 1:
			// deal with picture
			ele = source.getFirstElementByClass("pic_box");
			content = ele.toString();

			// get book title
			ele = source.getFirstElementByClass("title");
			content += ele.toString();

			// remove ad
			ele = source.getElementById("adandroid");
			content = content.replace(ele.toString(), "");

			// get book introduction
			ele = source.getFirstElementByClass("intro");
			content += ele.toString();

			// get latest chapter
			ele = source.getAllElementsByClass("box_cont").get(2);
			content += parseRemoveOptDiv(ele.toString());

			// replace href
			content = content.replace("href=\"",
					"href=\"http://wap.zongheng.com");

			content = addHeadEnd(content, "gb2312");

			// 3.save html page
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
			break;
		case 2:
			// get book title
			ele = source.getFirstElementByClass("tj");
			content = ele.toString();

			List<Element> plList = source.getAllElementsByClass("pl3");

			// get book introduction
			content += plList.get(0).toString();

			// get latest chapter
			content += plList.get(1).toString();

			content = addHeadEnd(content, "gb2312");

			// 3.save html page
			try {
				save2card(content.getBytes("gb2312"), path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get content
			res = "file://" + path;
			break;
		case 3: {

		}
			break;
		case 4: {
			// if content contains "在线阅读二"
			if (content.contains("在线阅读二")) {
				ele = source.getAllElements(HTMLElementName.A).get(1);
				// change url from old to new
				BookModel bm = activity.getWebview().getBm();

				activity.updateUrl(bm.getUrl(), ele.getAttributeValue("href"));

				bm.setUrl(ele.getAttributeValue("href"));

				res = bm.getUrl();
			} else if (!content.contains("下载地址")) {
				// deal with the cover page of the book
				String currentPath = "/";
				if (url.indexOf("/") >= 0) {
					currentPath = url.substring(0, url.lastIndexOf("/") + 1);
				}

				ele = source.getAllElements(HTMLElementName.DIV).get(2);
				Element aEle = ele.getFirstElement(HTMLElementName.A);
				String href = aEle.getAttributeValue("href");
				
				BookModel bm = activity.getWebview().getBm();
				
				if (!href.startsWith("http://")) {
					content = content.replace(href, currentPath + href);
					
					bm.setContentUrl(currentPath + href);
				}else{
					bm.setContentUrl(href);
				}
				// change url from old to new
				activity.updateContentUrl(bm);
				
				Element iEle = aEle.getFirstElement(HTMLElementName.IMG);
				if (iEle != null) {
					String src = iEle.getAttributeValue("src");
					content = content.replace(src, currentPath + src);
					
					//update cover image
					bm.setCoverImg(currentPath + src);
					
					activity.updateCoverImg(bm);
				}
				
				//remove the footer
				int end = content.indexOf("本书由");
				if(end > 0){
				content = content.substring(0, end-20);
				}

				// 3.save html page
				try {
					save2card(content.getBytes("gb2312"), path);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// get content
				res = "file://" + path;
			}
		}

			break;
		}
		return res;
	}

	public String convertUrl2Path(String url, String imgUrl) {
		final String dir = initBaseDir() + "/" + activity.getMode() + "/"
				+ convertUrl2Path(url) + "/";
		initDir(dir);

		String path = dir + parseFileNameByUrl(imgUrl);
		return path;
	}

	private String parseFileNameByUrl(String url) {
		int start = url.lastIndexOf("//");
		if (start < 0) {
			return "";
		} else {
			url = url.substring(start + 2);
		}
		start = url.indexOf("/");
		String fileName = url.substring(start + 1);
		start = fileName.lastIndexOf("?");
		fileName = fileName.substring(start + 1);
		fileName = fileName.replace("&", "_");
		fileName = fileName.replace("=", "_");
		fileName = fileName.replace("%", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(" ", "_");
		return fileName;

	}

	public String convertUrl2Path(String url) {
		int start = url.lastIndexOf("/");
		if (start > 0) {
			String tmp = url.substring(start + 1);
			tmp = tmp.replace("?", "_");
			tmp = tmp.replace("/", "_");
			tmp = tmp.replace("&", "_");
			tmp = tmp.replace("=", "_");
			tmp = tmp.replace("%", "_");
			tmp = tmp.replace(",", "_");
			return tmp;
		} else {
			return null;
		}
	}

	public String convertUrl2Location(String url, String imageUrls) {
		String dir = initBaseDir() + "/" + activity.getMode() + "/"
				+ convertUrl2Path(url) + "/";
		if (imageUrls.contains(",")) {
			String res = "";
			// split array
			String[] urls = imageUrls.split(",");
			for (String u : urls) {
				res += dir + parseFileNameByUrl(u) + ",";
			}
			if (res.endsWith(",")) {
				res = res.substring(0, res.length() - 1);
			}
			return res;
		} else {
			// directly convert
			return dir + parseFileNameByUrl(imageUrls);
		}
	}

	private void save2card(HttpEntity resEntity, String path) {
		try {
			// save to sdcard
			FileOutputStream fos = new FileOutputStream(new File(path));
			resEntity.writeTo(fos);

			// release all instances
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private void save2card(byte[] bytes, String path) {
		try {
			// save to sdcard
			FileOutputStream fos = new FileOutputStream(new File(path));
			IOUtils.write(bytes, fos);

			// release all instances
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void save2card(String content, String path, String encode) {
		try {
			// save to sdcard
			FileOutputStream fos = new FileOutputStream(new File(path));
			IOUtils.write(content, fos, encode);

			// release all instances
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String initBaseDir() {
		File sdDir = Environment.getExternalStorageDirectory();
		File uadDir = null;
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir = Environment.getDataDirectory();

		}
		uadDir = new File(sdDir.getAbsolutePath() + "/novel/");
		if (!uadDir.exists()) {
			uadDir.mkdirs();
		}
		sdDir = new File(uadDir.getAbsolutePath() + "/scripts/");
		if (!sdDir.exists()) {
			String outPath = uadDir.getAbsolutePath();
			try {
				IOUtils.copy(activity.getAssets().open("scripts.zip"), new FileOutputStream(outPath+ "/scripts.zip"));
				IOUtils.copy(activity.getAssets().open("styles.zip"), new FileOutputStream(outPath+ "/styles.zip"));
				UnZipper unzipper = new UnZipper("scripts", outPath, outPath);
			    unzipper.unzip();
			    unzipper = new UnZipper("styles", outPath, outPath);
			    unzipper.unzip();
			} catch (IOException e) {
				Log.e(LOGTAG, e.getMessage());
			}
		}
		return uadDir.getAbsolutePath();
	}

	private void initDir(String dir) {
		File sdDir = new File(dir);
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir.mkdirs();

		}
	}

	public static String getIndexPath() {
		File sdDir = Environment.getExternalStorageDirectory();
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir = Environment.getDataDirectory();

		}
		File index = new File(sdDir.getAbsolutePath() + "/download/index.html");
		return index.getAbsolutePath();
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	private String addHeadEnd(String content, String encode) {
		content = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=" + encode + "'><!-- MONOCLE CORE -->    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/monocle.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/compat/env.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/compat/css.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/compat/stubs.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/compat/browser.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/events.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/factory.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/styles.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/reader.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/book.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/component.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/core/place.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/controls/panel.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/panels/twoPane.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/panels/marginal.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/panels/eink.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/dimensions/columns.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/flippers/slider.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/flippers/instant.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/dimensions/vert.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/flippers/legacy.js\"></script>    " +
						"<script type=\"text/javascript\" src=\"../../scripts/controls/scrubber.js\"></script>" +
						"<script type=\"text/javascript\" src=\"../../scripts/controls/placesaver.js\"></script>" +
						"<link rel=\"stylesheet\" type=\"text/css\" href=\"../../styles/monocore.css\" />    " +
						"<link rel=\"stylesheet\" type=\"text/css\" href=\"../../styles/monoctrl.css\" />    " +
						"<style type=\"text/css\">      #reader {   position: absolute;     top: 0;        left: 0;        right: 0;        bottom: 0;      }    </style>   " +
						"<script type=\"text/javascript\"> var placeSaver = new Monocle.Controls.PlaceSaver('placesaver');" +
						" function configureReader(reader) {  " +
						"var control = new Monocle.Controls.Scrubber(reader);  reader.addControl(control, 'standard'); " +
						"reader.addControl(placeSaver, 'invisible');} </script>" +
						"<script type=\"text/javascript\">  Monocle.DEBUG = false;         Monocle.Events.listen(        window,        'load',        " +
						"function () { window.reader = Monocle.Reader('reader', null, {place: placeSaver.savedPlace() }, configureReader); " +
						"Monocle.Events.listen(            window,            'resize',            function () { window.reader.resized() }          );}      );    </script>"
				+ getCss() + "</head><body>" + "<div id=\"reader\">" + content
				+ "</div> </body></html>";
		return content;
	}
	
	private String addHeadEndNoBook(String content, String encode) {
		content = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=" + encode + "'>"
				+ getCss() + "</head><body>"
				+ "<table border='0'>" + content
				+ "</table>" + "</body></html>";
		return content;
	}

	private String getCss() {
		return "<style>ul.nave {float:none; width:100%; margin:5 auto} ul.nave li { display:inline;} "
				+ "ul.nave li {        display:block;        float:left;        padding:0 10px;        border-left:1px solid #4040FF;       }</style>";
	}

	private String parseRemoveOptDiv(String content) {
		Source source = new Source(content);
		String opt = source.getFirstElementByClass("opt").toString();
		return content.replace(opt, "");
	}

	private String parseName(String content) {
		Source source = new Source(content);
		Element ele = source.getFirstElement(HTMLElementName.H1);
		String name = ele.getTextExtractor().toString();
		return name;
	}

	private String parseLi(String content) {
		Source source = new Source(content);
		Element ele = source.getFirstElement(HTMLElementName.LI);
		return ele.toString();
	}

	private String removeAd(String content) {
		Source source = new Source(content);
		// remove all ad link
		List<Element> list = source.getAllElements(HTMLElementName.A);
		for (Element aele : list) {
			String src = aele.getAttributeValue("href");
			if (src != null && src.equals("http://www.qidian.com")) {
				content = content.replace(aele.toString(), "");
				break;
			}
		}
		list = source.getAllElements(HTMLElementName.SCRIPT);
		for (Element aele : list) {
				content = content.replace(aele.toString(), "");
		}
		return content;
	}

	private Element getBody(Source source) {
		Element body = source.getFirstElement(HTMLElementName.BODY);
		if (body == null) {
			body = source.getFirstElement(HTMLElementName.TABLE);
		}
		return body;
	}

	private Element get13jingHref(Element ele) {
		Element res = null;
		if (ele == null)
			return null;
		List<Element> tlist = ele.getAllElements(HTMLElementName.TABLE);
		int i = 0;
		for (Element iele : tlist) {

			if (i != 0 && containsTable(iele)) {
				Element tmp = get13jingHref(iele);
				if (tmp != null) {
					return tmp;
				}
			} else {
				List<Element> alist = iele.getAllElements(HTMLElementName.A);
				for (Element aele : alist) {
					String href = aele.toString();
					if (href.contains("zouyi.htm")) {
						return iele;

					}
				}
			}
			i++;
		}
		return res;
	}

	private boolean containsTable(Element ele) {
		boolean flag = false;
		List<Element> tlist = ele.getAllElements(HTMLElementName.TABLE);
		if (tlist != null && tlist.size() > 1) {
			flag = true;
		}
		return flag;
	}

	private Element findCorrectTable(Element ele) {
		if (containsTable(ele)) {
			List<Element> tlist = ele.getAllElements(HTMLElementName.TABLE);
			int i = 0;
			for (Element iele : tlist) {
				if (i++ == 0)
					continue;
				Element tmp = findCorrectTable(iele);
				if (tmp != null) {
					return tmp;
				}
			}
		} else {
			String tmp = ele.getTextExtractor().toString();
			if (tmp.length() > 100) {
				return ele;
			}
		}
		return null;
	}

	private String removeSpanFont(Element ele) {
		String res = ele.toString();
		List<Element> slist = ele.getAllElements();
		if (slist != null) {
			int i = 0;
			for (Element e : slist) {
				if (i++ == 0)
					continue;
				String tmp = e.getTextExtractor().toString();
				if (tmp != null && tmp.length() > 1) {
					res = res.replace(e.toString(), tmp);
				} else {
					res = res.replace(e.toString(), "");
				}
			}
		}
		return res;
	}

	private String addTrTd(String content) {
		return "<TR><TD>" + content + "</TD></TR>";
	}

	private String replaceHref4Guoxue(String content, String path) {
		String currentPath = "";
		if (path.indexOf("/") >= 0) {
			currentPath = path.substring(0, path.lastIndexOf("/") + 1);
		} else {
			currentPath = "/";
		}

		Source source = new Source(content);

		List<Element> list = source.getAllElements(HTMLElementName.A);
		int i = 0;
		for (Element ele : list) {
			String href = ele.getAttributeValue("href");
			String newHref = "";
			if (href == null)
				continue;
			if (href.contains("webmaster")) {
				String tmp = ele.toString();
				content = content.replace(tmp, "");
				continue;
			}
			if (href.startsWith("http")) {
				// book list
				continue;
			} else if (href.startsWith("javascript")
					|| href.startsWith("mailto")) {
				// remove tag <a>
				String tmp = ele.toString();
				content = content.replace(tmp, "");
				continue;
			} else if (href.startsWith(".")) {
				if (href.startsWith("..")) {
					// remove tag <a>
					String tmp = ele.toString();
					content = content.replace(tmp, "");
				} else {
					// same path prefix with ".", need to remove "."
					newHref = currentPath + href.substring(2);
				}
			} else if (href.startsWith("/")) {
				// href value is directly used as path
				newHref = "http://www.guoxue.com" + href;
			} else {
				// same path prefix without "."
				newHref = currentPath + href;
			}
			String tmp = ele.toString();
			String tmp2 = tmp.replace(href, newHref);
			content = content.replace(tmp, tmp2);

		}

		// deal with imgaes
		List<Element> imglist = source.getAllElements(HTMLElementName.IMG);
		if (imglist != null && imglist.size() > 0) {
			if (path.indexOf("/") >= 0) {
				currentPath = path.substring(0, path.lastIndexOf("/") + 1);
			} else {
				currentPath = "/";
			}
			for (Element img : imglist) {
				String src = img.getAttributeValue("src");
				if (src != null && !src.startsWith("http")
						&& !src.endsWith(".gif")) {
					String tmp = "http://www.guoxue.com" + currentPath + src;
					content = content.replace(src, tmp);
				} else {
					String tmp = img.toString();
					content = content.replace(tmp, "");
				}

			}
		}

		return content;
	}

	private String replaceHref4Qidian(String content, String path) {
		String currentPath = "/";
		if (path.indexOf("/") >= 0) {
			currentPath = path.substring(0, path.lastIndexOf("/") + 1);
		}

		Source source = new Source(content);

		List<Element> list = source.getAllElements(HTMLElementName.A);
		int i = 0;
		for (Element ele : list) {
			String href = ele.getAttributeValue("href");
			String newHref = "";
			if (href == null)
				continue;
			if (href.startsWith("http")) {
				// domain = www.qidian
				continue;
			} else if (href.startsWith(".")) {
				// same path prefix with ".", need to remove "."
				newHref = currentPath + href.substring(2);
			} else if (href.startsWith("/")) {
				// href value is directly used as path
				newHref = "http://www.qidian.com" + href;
			} else if (href.startsWith("#")) {
				// href value is directly used as path
				newHref = path + href;
			} else {
				// same path prefix without "."
				newHref = currentPath + href;
			}
			String tmp = ele.toString();
			String tmp2 = tmp.replace(href, newHref);
			content = content.replace(tmp, tmp2);
		}
		return content;
	}

}
