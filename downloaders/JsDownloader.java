package apple.downloaders;

public class JsDownloader extends Downloader {
	private static JsDownloader jsDownloader = new JsDownloader();

	private JsDownloader() {
	}

	public static JsDownloader getInstance() {
		return jsDownloader;
	}

	@Override
	public void download(String jsPath) {
		if (this.fileExists(jsPath)) {
			return;
		}

		String jsText = this.downloadStatic(jsPath);

		this.createStaticFile(jsText, jsPath);

		System.out.println("downloaded... " + this.baseURL + jsPath);
	}
}
