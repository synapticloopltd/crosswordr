package synapticloop.puzzlr.extractor.sudoku;

/*
 * Copyright (c) 2019 - 2021 Synapticloop.
 * 
 * All rights reserved.
 * 
 * This code may contain contributions from other parties which, where 
 * applicable, will be listed in the default build file for the project 
 * ~and/or~ in a file named CONTRIBUTORS.txt in the root of the project.
 * 
 * This source code and any derived binaries are covered by the terms and 
 * conditions of the Licence agreement ("the Licence").  You may not use this 
 * source code or any derived binaries except in compliance with the Licence.  
 * A copy of the Licence is available in the file named LICENSE.txt shipped with 
 * this source code or binaries.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import synapticloop.puzzlr.extractor.BaseExtractor;

public class XMLPuzzleStringExtractor extends BaseExtractor {

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
		return(stringBuffer.toString());
	}

}
