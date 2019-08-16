package synapticloop.crosswordr.extractor;

public class JavascriptExtractor extends ExtractorBase {

	@Override
	public String extract(String data) {
		int startQuote = data.indexOf("\"");
		int endQuote = data.lastIndexOf("\"");
		return(data.substring(startQuote + 1, endQuote).replaceAll("\\\\", ""));
	}

}
