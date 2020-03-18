package apple.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apple.Main;

abstract class Parser {
	String baseURL;
	String baseAbsPath;
	int maxHierarchy;

	public Parser() {
		this.baseURL = Main.getBaseURL();
		this.baseAbsPath = Main.getBaseAbsPath();
		this.maxHierarchy = Main.getMaxHierarchy();
	}

	abstract protected void parse(String path, int hierarchy);

	String createLocalAbs(String path){
		return this.baseAbsPath + path;
	}

	public String pwdParent(String staticPath) {
		Pattern pattern = Pattern.compile("(.+)(/.+/.*?$)");
		Matcher matcher = pattern.matcher(staticPath);
		String parentPath = matcher.replaceAll("$1");
		return parentPath;
	}

	public String convertPath(String text, String hostName) {
		if(text.startsWith("https://")) {
			Pattern pattern = Pattern.compile("https:\\/\\/(.+)");
			Matcher matcher = pattern.matcher(text);
			return matcher.replaceAll("$1");
		} else if (text.startsWith("//")) {
			Pattern pattern = Pattern.compile("\\/\\/(.+)");
			Matcher matcher = pattern.matcher(text);
			return matcher.replaceAll("$1");
		} else if (text.startsWith("/")) {
			return hostName + text;
		} else {
			Pattern pattern = Pattern.compile("(.+?)\\/.+");
			Matcher matcher = pattern.matcher(text);
			if (matcher.find() && matcher.group(1).contains(":")) {
				return "";
			} else {
				return hostName + "/" + text;
			}
		}
	}

	public String extractHostName(String path) {
		Pattern pattern = Pattern.compile("([^/]+)\\/.+\\/");
		Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			return matcher.group(1);
		}else {
			return "";
		}
	}
}
