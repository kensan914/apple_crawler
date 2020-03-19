package apple.downloaders;

import apple.parsers.CssParser;

public class CssDownloader extends Downloader{
//	private static CssDownloader cssDownloader = new CssDownloader();

	public CssDownloader(String cssPath) {
		this.path = cssPath;
	}

//	public static CssDownloader getInstance() {
//		return cssDownloader;
//	}

	@Override
	public void download(String cssPath) {
		if (this.fileExists(cssPath)) {
			return;
		}
		String cssText = this.downloadStatic(cssPath);
		CssParser cssParser = CssParser.getInstance();
		cssText = cssParser.parseUrl(cssText, cssPath);

		//font
		if(!cssPath.endsWith(".css") && cssPath.startsWith("www.apple.com/wss/fonts")) cssPath = "www.apple.com/wss/font.css";

		this.createStaticFile(cssText, cssPath);

		System.out.println("downloaded... " + this.baseURL + cssPath);
	}
}
