package apple.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apple.downloaders.CssDownloader;
import apple.downloaders.MediaDownloader;

public class CssParser extends Parser {
	private static CssParser cssParser = new CssParser();

	private CssParser() {
	}

	public static CssParser getInstance() {
		return cssParser;
	}

	@Override
	public void parse(String cssPath, int hierarchy) {
		CssDownloader cssDownloader = CssDownloader.getInstance();
		cssDownloader.download(cssPath);
	}

	public String parseUrl(String cssText, String staticPath) {
		try {
			String hostName = this.extractHostName(staticPath);

			Pattern absPattern = Pattern.compile("(url\\(\\\")(/.+?)(\\\"\\))");
			Pattern relPattern = Pattern.compile("(url\\(\\\")\\.\\.(/.+?)(\\\"\\))");
			Pattern absPatternNonD = Pattern.compile("(url\\()(/.+?)(\\))");
			Pattern relPatternNonD = Pattern.compile("(url\\()\\.\\.(/.+?)(\\))");
			MediaDownloader mediaDownloader = MediaDownloader.getInstance();

			Matcher absMatcher = absPattern.matcher(cssText);
			Matcher absMatcherNonD = absPatternNonD.matcher(cssText);
			while (absMatcher.find()) {
				String imgPath = convertPath(absMatcher.group(2), hostName);
				mediaDownloader.download(imgPath);
			}
			while (absMatcherNonD.find()) {
				String imgPath = convertPath(absMatcherNonD.group(2), hostName);
				mediaDownloader.download(imgPath);
			}

			String parentPath = this.pwdParent(staticPath);
			Matcher relMatcher = relPattern.matcher(cssText);
			Matcher relMatcherNonD = relPatternNonD.matcher(cssText);

			while (relMatcher.find()) {
				mediaDownloader.download(parentPath + relMatcher.group(2));
			}
			while (relMatcherNonD.find()) {
				mediaDownloader.download(parentPath + relMatcherNonD.group(2));
			}

			absMatcher = absPattern.matcher(cssText);
			if (absMatcher.find())
				cssText = absMatcher.replaceAll("$1" + this.baseAbsPath + convertPath("$2", hostName) + "$3");
			absMatcherNonD = absPatternNonD.matcher(cssText);
			if (absMatcherNonD.find())
				cssText = absMatcherNonD.replaceAll("$1" + this.baseAbsPath + convertPath("$2", hostName) + "$3");
			relMatcher = relPattern.matcher(cssText);
			if (relMatcher.find())
				cssText = relMatcher.replaceAll("$1" + this.baseAbsPath + parentPath + "$2$3");
			relMatcherNonD = relPatternNonD.matcher(cssText);
			if (relMatcherNonD.find())
				cssText = relMatcherNonD.replaceAll("$1" + this.baseAbsPath + parentPath + "$2$3");

		} catch (Exception e) {
			System.out.println(e);
		}
		return cssText;
	}
}
