package apple.downloaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import apple.Main;

abstract class Downloader {
	String baseURL;
	String baseAbsPath;

	public Downloader() {
		this.baseURL = Main.getBaseURL();
		this.baseAbsPath = Main.getBaseAbsPath();
	}

	abstract protected void download(String path);

	String downloadStatic(String path) {
		try {
			URL url = new URL(this.baseURL + path);
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");

			//font
			http.setRequestProperty("referer", "https://www.apple.com/jp/");

			http.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String staticText = "", line = "";
			while((line = reader.readLine()) != null)
				staticText += line;

			reader.close();
			return staticText;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	void createStaticFile(String staticText, String path) {
		try {
			File cssFile = new File(this.baseAbsPath + path);
			new File(cssFile.getParent()).mkdirs();
			cssFile.createNewFile();

			BufferedWriter bw;
			if (cssFile.exists()){
				bw = new BufferedWriter(new FileWriter(this.baseAbsPath + path));
				bw.write(staticText);
			}else {
				throw new Exception();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Boolean fileExists(String path) {
		File file = new File(this.baseAbsPath + path);
		return file.exists();
	}
}
