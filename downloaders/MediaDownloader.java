package apple.downloaders;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaDownloader extends Downloader {
//	private static MediaDownloader mediaDownloader = new MediaDownloader();

	public MediaDownloader(String mediaPath) {
		this.path = mediaPath;
	}

//	public static MediaDownloader getInstance() {
//		return mediaDownloader;
//	}

	@Override
	//download image, mp4, svg etc....
	public void download(String mediaPath) {
		//mediaPath is an absolute path of Apple. ex)www.apple.com/ac/flags/1/images/jp/16.png

		//test
		if(mediaPath.endsWith(".mp4")) {
			StringBuilder sb = new StringBuilder(mediaPath);
	        sb.setLength(sb.length()-4);
	        mediaPath = sb.toString() + "_2x.mp4";
		}

		if (this.fileExists(mediaPath)) {
			return;
		}

		try {
			URL url = new URL(this.baseURL + mediaPath);
			HttpURLConnection conn =(HttpURLConnection) url.openConnection();
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");

			conn.setRequestProperty("referer", "https://www.apple.com/jp/");

			conn.connect();
			int httpStatusCode = conn.getResponseCode();
			if(httpStatusCode != HttpURLConnection.HTTP_OK){
//				throw new Exception();
				System.out.println("media not found... " + mediaPath);
				return;
		    }

			//input stream
			DataInputStream dataInStream = new DataInputStream(conn.getInputStream());

			Pattern pattern = Pattern.compile("(.+)\\?.+");
			Matcher matcher = pattern.matcher(mediaPath);
			if(matcher.find()) {
				mediaPath = matcher.replaceAll("$1");
			}

			//create imagefile
			File imgFile = new File(this.baseAbsPath + mediaPath);
			new File(imgFile.getParent()).mkdirs();
			imgFile.createNewFile();

			//output stream
			DataOutputStream dataOutStream = null;
			if (imgFile.exists()){
				dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.baseAbsPath + mediaPath)));
			}else {
				throw new Exception();
			}

			byte[] b = new byte[4096];
			int readByte = 0;
			while(-1 != (readByte = dataInStream.read(b))){
				dataOutStream.write(b, 0, readByte);
			}
			dataInStream.close();
			dataOutStream.close();

			System.out.println("downloaded... " + this.baseURL + mediaPath);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
}
