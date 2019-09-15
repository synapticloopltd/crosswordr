package synapticloop.puzzlr.extractor.sudoku;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import synapticloop.puzzlr.extractor.ExtractorBase;

public class XMLPuzzleStringExtractor extends ExtractorBase {

	@Override
	public String extract(String data) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<sudoku>\n");

		Document document = Jsoup.parse(data);
		Elements elements = document.getElementsByTag("puzzleString");
		String puzzleString = elements.first().text();
		String[] split = puzzleString.split(";");
		for (String cell : split) {
			String[] cells = cell.split(",");

			String x = cells[1];
			String y = cells[0];
			if(y.equals("1")) {
				stringBuffer.append("<row>\n");
			}

			stringBuffer.append("  <cell");
			stringBuffer.append(" x=\"");
			stringBuffer.append(x);
			stringBuffer.append("\"");
			stringBuffer.append(" y=\"");
			stringBuffer.append(y);
			stringBuffer.append("\"");

			stringBuffer.append(" show=\"");
			stringBuffer.append(cells[4]);
			stringBuffer.append("\"");
			stringBuffer.append(" value=\"");
			stringBuffer.append(cells[5]);
			stringBuffer.append("\" />\n");
			if(y.equals("9")) {
				stringBuffer.append("</row>\n");
			}
		}
		stringBuffer.append("</sudoku>\n");
		System.out.println(stringBuffer.toString());
		return(stringBuffer.toString());
	}

}
