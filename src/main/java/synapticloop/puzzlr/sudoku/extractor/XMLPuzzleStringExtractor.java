package synapticloop.puzzlr.sudoku.extractor;

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
				stringBuffer.append("<cell");
				stringBuffer.append(" x=\"");
				stringBuffer.append(cells[1]);
				stringBuffer.append("\"");
				stringBuffer.append(" y=\"");
				stringBuffer.append(cells[0]);
				stringBuffer.append("\"");

				stringBuffer.append(" show=\"");
				stringBuffer.append(cells[4]);
				stringBuffer.append("\"");
				stringBuffer.append(" value=\"");
				stringBuffer.append(cells[5]);
				stringBuffer.append("\" />\n");
		}
		stringBuffer.append("</sudoku>\n");
		System.out.println(stringBuffer.toString());
		System.exit(-1);
		return(stringBuffer.toString());
	}

}
