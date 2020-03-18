package apple.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import apple.downloaders.JsDownloader;
import apple.downloaders.MediaDownloader;

public class HtmlParser extends Parser {
	private static HtmlParser htmlParser = new HtmlParser();
	String htmlName = "index.html";

	private HtmlParser() {
	}

	public static HtmlParser getInstance() {
		return htmlParser;
	}

	@Override
	public void parse(String htmlPath, int hierarchy) {
		try {
			String hostName = this.extractHostName(htmlPath);
			System.out.println("host name is : " + hostName);
			Document doc = Jsoup.connect(baseURL + htmlPath).get();
			doc = this.parseLink(doc, hierarchy, hostName);
			doc = this.parseScript(doc, hostName);
			doc = this.parseNoscript(doc, hostName);
			doc = this.parseImg(doc, hostName);
			doc = this.parseVideo(doc, hostName);
			if(hierarchy < this.maxHierarchy){
				doc = this.parseA(doc, hierarchy, hostName);
			}
			String html = this.parseStyle(doc, hostName, htmlPath);
			this.createHtmlFile(htmlPath, html);
			System.out.println("finished..." + htmlPath + " hierarchy is " + hierarchy);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	Document parseLink(Document doc, int hierarchy, String hostName) {
		try {
			Elements linkElms = doc.select("link");
			for(Element linkElm : linkElms) {
				if(linkElm.hasAttr("rel") && linkElm.attr("rel").equals("stylesheet") && linkElm.hasAttr("href")) {
					String cssPath = this.convertPath(linkElm.attr("href"), hostName);
					CssParser cssParser = CssParser.getInstance();
					cssParser.parse(cssPath, hierarchy);

//					font
					if(!cssPath.endsWith(".css") && cssPath.startsWith("www.apple.com/wss/fonts")) cssPath = "www.apple.com/wss/font.css";

					linkElm.attr("href", this.createLocalAbs(cssPath));
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	Document parseScript(Document doc, String hostName) {
		try {
			Elements scriptElms = doc.select("script");
			for(Element scriptElm : scriptElms) {
				if(scriptElm.hasAttr("src")) {
					String jsPath = this.convertPath(scriptElm.attr("src"), hostName);
					JsDownloader jsDownloader = JsDownloader.getInstance();
					jsDownloader.download(jsPath);
					scriptElm.attr("src", this.createLocalAbs(jsPath));
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	Document parseNoscript(Document doc, String hostName) {
		try {
			Elements noscriptElms = doc.select("noscript");
			for(Element noscriptElm : noscriptElms) {
				noscriptElm.remove();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	Document parseImg(Document doc, String hostName) {
		try {
			Elements imgElms = doc.select("img");
			for(Element imgElm : imgElms) {
				if(imgElm.hasAttr("src")) {
					String imgPath = this.convertPath(imgElm.attr("src"), hostName);
					if (!imgPath.equals("")) {
						MediaDownloader mediaDownloader = MediaDownloader.getInstance();
						mediaDownloader.download(imgPath);
						Pattern pattern = Pattern.compile("(.+)\\?.+");
						Matcher matcher = pattern.matcher(imgPath);
						if(matcher.find()) {
							imgPath = matcher.replaceAll("$1");
						}
						imgElm.attr("src", this.createLocalAbs(imgPath));
					}
				}
				if (imgElm.hasAttr("data-viewport-src")) {
					String imgPath = this.convertPath(imgElm.attr("data-viewport-src"), hostName);
					if (!imgPath.equals("")) {
						MediaDownloader mediaDownloader = MediaDownloader.getInstance();
						mediaDownloader.download(imgPath);
						Pattern pattern = Pattern.compile("(.+)\\?.+");
						Matcher matcher = pattern.matcher(imgPath);
						if(matcher.find()) {
							imgPath = matcher.replaceAll("$1");
						}
						imgElm.attr("data-viewport-src", this.createLocalAbs(imgPath));
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	Document parseVideo(Document doc, String hostName) {
		try {
			Elements videoElms = doc.select("video");
			for(Element videoElm : videoElms) {
				String videoAttrs[] = {"data-src-large", "data-src-medium", "data-src-small"};
				for (String videoAttr : videoAttrs) {
					if(videoElm.hasAttr(videoAttr)) {
						String videoPath = this.convertPath(videoElm.attr(videoAttr), hostName);
						MediaDownloader mediaDownloader = MediaDownloader.getInstance();
						mediaDownloader.download(videoPath);
						videoElm.attr(videoAttr, this.createLocalAbs(videoPath));
					}
				}

				if(videoElm.hasAttr("data-video-source-basepath")) {
					String videoBasePath = this.convertPath(videoElm.attr("data-video-source-basepath"), hostName);
					MediaDownloader mediaDownloader = MediaDownloader.getInstance();
					mediaDownloader.download(videoBasePath + "small.mp4");
					videoElm.attr("data-video-source-basepath", this.createLocalAbs(videoBasePath));
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	Document parseA(Document doc, int hierarchy, String hostName) {
		try {
			Elements aElms = doc.select("a");
			for(Element aElm : aElms) {
				if(aElm.hasAttr("href") && !aElm.attr("href").startsWith("#")) {
					String htmlPath = this.convertPath(aElm.attr("href"), hostName);
					if (!htmlPath.endsWith("/")) htmlPath += "/";

					if (this.htmlExists(htmlPath) && hierarchy > 1) {
						System.out.println(htmlPath + " exists." + hierarchy);
						aElm.attr("href", this.createLocalAbs(htmlPath)+this.htmlName);
						continue;
					}

					HtmlParser htmlParser = HtmlParser.getInstance();
					htmlParser.parse(htmlPath, hierarchy+1);
					aElm.attr("href", this.createLocalAbs(htmlPath)+this.htmlName);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc;
	}

	String parseStyle(Document doc, String hostName, String htmlPath) {
		try {
			Elements styleElms = doc.select("style");
			if(styleElms.size() > 0) {
				String html = doc.html();
				Pattern pattern = Pattern.compile("(<style type=\"text/css\">)([\\s\\S]+)(</style>)");
				Matcher matcher = pattern.matcher(html);

				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					String styleText = CssParser.getInstance().parseUrl(matcher.group(2), htmlPath);
					matcher.appendReplacement(sb, "$1" + styleText + "</style >");
				}
				matcher.appendTail(sb);
				html = sb.toString();
				return html;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return doc.html();
	}

	void createHtmlFile(String htmlPath, String html) {
		try {
			File file = new File(this.baseAbsPath + htmlPath + this.htmlName);
			new File(file.getParent()).mkdirs();
			file.createNewFile();

			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(html);
			bw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	Boolean htmlExists(String htmlPath) {
		File html;
		Boolean isExist;
		try {
			String path = htmlPath + this.htmlName;
			html = new File(this.baseAbsPath + path);
			isExist = html.exists();
		} catch (Exception e) {
			return false;
		}
		return isExist;
	}
}
