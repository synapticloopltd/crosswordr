package synapticloop.puzzlr.sudoku.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import synapticloop.puzzlr.extractor.ExtractorBase;

public class XMLPuzzleStringExtractor extends ExtractorBase {

	@Override
	public String extract(String data) {
		Document document = Jsoup.parse(data);
		Elements elements = document.getElementsByTag("puzzleString");
		System.out.println(elements.first().text());
		return null;
	}

}
