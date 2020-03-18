package apple;

import apple.parsers.HtmlParser;

public class Main {
	static String baseURL = "https://";
	static String baseAbsPath = "/Users/toriumi/Desktop/apple_crowling/apple/apple_static/";
	static int maxHierarchy = 2;

	public static void main(String[] args) {
		HtmlParser htmlParser = HtmlParser.getInstance();
		htmlParser.parse("www.apple.com/jp/", 1);
	}

	public static String getBaseURL() {
		return baseURL;
	}

	public static String getBaseAbsPath() {
		return baseAbsPath;
	}

	public static int getMaxHierarchy() {
		return maxHierarchy;
	}
}
