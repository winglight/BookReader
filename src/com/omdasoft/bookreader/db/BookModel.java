package com.omdasoft.bookreader.db;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BookModel {

	private long id = -1;
	private String name;
	private String intro;
	private String bookCode;
	private String author;
	private String url;
	private String contentUrl;
	private String coverImg; // facto, it stores a urls for the cover image
	private String fulltext; //facto, it stores a location for the txt file
	private int isFavorite = 0; // 0 - false ; 1 - true
	private int favoriteOrder = -1; // -1 - not ordered 
	private int source = 0; // 1 - qiandian ; 2 - zongheng ; 3 - guoxue ; 4 - shuku
	private int category = 0; // depends on source
	private String latestChapter; // update info
	private String lastReadUrl; //the url of the book ends with #lastposition that should be inserted into the book file while scrolling
	private long lastReadAt; 
	private long caretedAt; 
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCoverImg() {
		return coverImg;
	}
	public void setCoverImg(String coverImg) {
		this.coverImg = coverImg;
	}
	public String getFulltext() {
		return fulltext;
	}
	public void setFulltext(String fulltext) {
		this.fulltext = fulltext;
	}
	public int getIsFavorite() {
		return isFavorite;
	}
	public void setIsFavorite(int isFavorite) {
		this.isFavorite = isFavorite;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public String getLatestChapter() {
		return latestChapter;
	}
	public void setLatestChapter(String latestChapter) {
		this.latestChapter = latestChapter;
	}
	public String getLastReadUrl() {
		return lastReadUrl;
	}
	public void setLastReadUrl(String lastReadUrl) {
		this.lastReadUrl = lastReadUrl;
	}
	public long getLastReadAt() {
		return lastReadAt;
	}
	public void setLastReadAt(long lastReadAt) {
		this.lastReadAt = lastReadAt;
	}
	public long getCaretedAt() {
		return caretedAt;
	}
	public void setCaretedAt(long caretedAt) {
		this.caretedAt = caretedAt;
	}
	public String getBookCode() {
		return bookCode;
	}
	public void setBookCode(String bookCode) {
		this.bookCode = bookCode;
	}
	
	public int getFavoriteOrder() {
		return favoriteOrder;
	}
	public void setFavoriteOrder(int favoriteOrder) {
		this.favoriteOrder = favoriteOrder;
	}
	
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}
	public String getContentUrl(){
		String url = null;
		switch(source){
		case 1:
			url = "http://www.qidian.com/BookReader/" + bookCode + ".aspx";
			break;
		case 2:
			url = "http://wap.zongheng.com/chapter/list?bookid=" + bookCode;
			break;
		case 3:
			url = this.url;
			break;
		case 4:
			if(this.contentUrl != null){
			url = this.contentUrl;
			}else{
				url = this.url;
			}
			break;
		}
		return url;
	}
	
	public String getFulltextUrl(){
		String url = null;
		switch(source){
		case 1:
			url = "http://down1.qidian.com/bookall/" + bookCode + ".htm";
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		}
		return url;
	}
	
	public String getSearchUrl(){
		String url = null;
		switch(source){
		case 1:
			try {
				url = "http://www.fv0.net/s.jsp?i=b&p=1&k=" + URLEncoder.encode(name, "gb2312") ;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			try {
				url = "http://www.fv0.net/s.jsp?i=b&p=1&k=" + URLEncoder.encode(name, "gb2312") ;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		case 3:
			try {
				url = "http://www.google.com.hk/#hl=zh-CN&site=dangdang.com&q=" + URLEncoder.encode(name, "GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		case 4:
			try {
				url = "http://www.google.com.hk/#hl=zh-CN&site=dangdang.com&q=" + URLEncoder.encode(name, "gb2312");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		}
		return url;
	}
	
}
