
package com.androidesk.camera.network.source;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.content.UriMatcher;
import android.net.Uri;

import com.androidesk.camera.network.ImageDownloader;

public class SourceManager {
	private static SourceManager instance = null;

	public ImageDownloader mDownloader;

	private static final String URL_CATALOG_LIST = "http://service.androidesk.com/cate/list";
	private static final String URL_CATALOG = "http://service.androidesk.com/img/listcate?cid=%s&skip=%s&limit=%s&rank=date";
	private static final String URL_WALLPAPER = "http://static.androidesk.com/wallpaper?imgid=%s&reso=%sx%s";

	private static final int MATCH_ANDROIDESK = 0;
	private static final int MATCH_ANDROIDESK_STATIC = 1;
	private static final String AUTHRITY_ANDROIDESK = "service.androidesk.com";// androidesk服务列表
	private static final String AUTHRITY_ANDROIDESK_STATIC = "static.androidesk.com";// androidesk获取图片域名

	UriMatcher matcher;

	private SourceManager() {
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHRITY_ANDROIDESK, "*/*", MATCH_ANDROIDESK);
		matcher.addURI(AUTHRITY_ANDROIDESK_STATIC, "*", MATCH_ANDROIDESK_STATIC);
	}

	public static SourceManager instance() {
		if (instance == null) instance = new SourceManager();
		return instance;
	}

	public SourceManager initialize(ImageDownloader downloader) {
		mDownloader = downloader;
		return this;
	}

	public URI getDefaultCatalogListURI() {
		return URI.create(URL_CATALOG_LIST);
	}

	public BaseCatalogList getDefaultCatalogList() throws IOException {
		return getCatalogList(getDefaultCatalogListURI());
	}

	private BaseCatalogList getCatalogList(URI uri) throws IOException {
		BaseCatalogList cl = null;

		String response = mDownloader.getString(uri);
		int match = matcher.match(Uri.parse(uri.toString()));
		switch (match) {
			case MATCH_ANDROIDESK:
				try {
					cl = new AndroideskCatalogList(response, Uri.parse(uri.toString()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}

		return cl;
	}

	public BaseCatalog getDefaultCatalog(String id, int skip, int size) throws IOException {
		String url = getDefaultCatalogListURI().toString();
		return getCatalog(url, id, skip, size);
	}

	private BaseCatalog getCatalog(String url, String id, int skip, int size) throws IOException {
		BaseCatalog catalog = null;

		int match = matcher.match(Uri.parse(url));
		switch (match) {
			case MATCH_ANDROIDESK:
				try {
					String getCatalogUrl = String.format(URL_CATALOG, id, skip, size);
					URI uri = URI.create(getCatalogUrl);
					String response = mDownloader.getString(uri);
					catalog = new AndroideskCatalog(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}

		return catalog;
	}

	public String genarateGetCatalogUrl(String id, int skip, int size) {
		String url = getDefaultCatalogListURI().toString();
		return genarateGetCatalogUrl(url, id, skip, size);
	}

	private String genarateGetCatalogUrl(String url, String id, int skip, int size) {
		String getCatalogUrl = null;

		int match = matcher.match(Uri.parse(url));
		switch (match) {
			case MATCH_ANDROIDESK:
				getCatalogUrl = String.format(URL_CATALOG, id, skip, size);
				break;
			default:
				break;
		}

		return getCatalogUrl;
	}

	public String getPictureId(String url) {
		String value = "";

		value = getValue(url, "imgid");
		int match = matcher.match(Uri.parse(url));
		switch (match) {
			case MATCH_ANDROIDESK_STATIC:
				//value = getValue(url, "imgid");
				break;
			default:
				break;
		}

		return value;
	}

	public String generateDefaultGetPictureUrl(String id, int width, int height) {
		String url = getDefaultCatalogListURI().toString();
		return generateGetPictureUrl(url, id, width, height);
	}

	private String generateGetPictureUrl(String url, String id, int width, int height) {
		String value = "";

		int match = matcher.match(Uri.parse(url));
		switch (match) {
			case MATCH_ANDROIDESK:
				value = String.format(URL_WALLPAPER, id, width, height);
				break;
			default:
				break;
		}
		return value;
	}

	// 在url中获取名称为name的参数值
	private static String getValue(String url, String name) {
		Pattern p = Pattern.compile("(\\?|&)" + name + "=([^&?]*)");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return m.group(2);
		}
		return null;
	}

}
