package synapticloop.puzzlr.extractor.sudoku;

/*
 * Copyright (c) 2019 Synapticloop.
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

import org.json.JSONObject;

import synapticloop.puzzlr.extractor.BaseExtractor;

public class JSONPuzzleStringExtractor extends BaseExtractor {

	@Override
	public String extract(String data) {
		JSONObject sudokuObject = new JSONObject(data);
		String sudokuData = sudokuObject.getJSONArray("cells").getJSONObject(0).getJSONObject("meta").getString("data");
		if(sudokuData.trim().length() == 0) {
			return(null);
		}
		String puzzle = sudokuData.substring(0, sudokuData.indexOf("$"));
		String solution = sudokuData.substring(sudokuData.lastIndexOf("$") + 1);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<sudoku>\n");
		int x = 1;
		int y = 1;
		for(int i=0; i < 81; i++) {
			if(y == 1) {
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
			if(puzzle.charAt(i) == '*') {
				stringBuffer.append("False");
			} else {
				stringBuffer.append("True");
			}
			stringBuffer.append("\"");
			stringBuffer.append(" value=\"");
			stringBuffer.append(solution.charAt(i));
			stringBuffer.append("\" />\n");
			if(y == 9) {
				stringBuffer.append("</row>\n");
			}
			y++;
			if(x > 9) {
				x = 1;
			}
			if(y > 9) {
				x++;
				y = 1;
			}
		}
		stringBuffer.append("</sudoku>\n");
		return(stringBuffer.toString());
	}

}
