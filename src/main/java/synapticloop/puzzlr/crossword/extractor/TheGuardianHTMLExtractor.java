package synapticloop.puzzlr.crossword.extractor;

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

import org.json.JSONArray;


import org.json.JSONObject;

import synapticloop.puzzlr.crossword.extractor.data.Cell;


public class TheGuardianHTMLExtractor extends CellBasedExtractor {
	private static final String START_DATA_CROSSWORD_DATA = "data-crossword-data=\"";

	@Override
	public String extract(String data) {
		int startQuote = data.indexOf(START_DATA_CROSSWORD_DATA) + START_DATA_CROSSWORD_DATA.length();
		int endQuote = data.indexOf("\">", startQuote);
		String source = data.substring(startQuote, endQuote).replaceAll("&quot;", "\"");
		return(parseJSONToXML(new JSONObject(source)));
	}

	private String parseJSONToXML(JSONObject crosswordObject) {
		// time to generate the cells
		width = crosswordObject.getJSONObject("dimensions").getInt("rows");
		height = crosswordObject.getJSONObject("dimensions").getInt("cols");

		JSONArray entriesArray = crosswordObject.getJSONArray("entries");
		for (Object object : entriesArray) {
			JSONObject entryObject = (JSONObject)object;

			int x = entryObject.getJSONObject("position").getInt("x");
			int y = entryObject.getJSONObject("position").getInt("y");
			String solution = entryObject.getString("solution");
			String clue = entryObject.getString("clue");

			String direction = entryObject.getString("direction");
			if(direction.equals("across")) {
				for (int j = 0; j < solution.length(); j++) {
					Cell cell = cells[x + j][y];
					if(null == cell) {
						cell = new Cell();
					}
					if(j == 0) {
						cell.setAcrossClue(clue);
						cell.setCharacter(solution.charAt(j));
						cell.setLength(solution.length());
						cell.setNumber(Integer.parseInt(entryObject.getString("humanNumber")));
					} else {
						cell.setCharacter(solution.charAt(j));
					}
					cells[x + j][y] = cell;
				}
			} else {
				// we are going down
				for (int j = 0; j < solution.length(); j++) {
					Cell cell = cells[x][y + j];
					if(null == cell) {
						cell = new Cell();
					}
					if(j == 0 ) {
						cell.setDownClue(clue);
						cell.setCharacter(solution.charAt(j));
						cell.setLength(solution.length());
						cell.setNumber(Integer.parseInt(entryObject.getString("humanNumber")));
					} else {
						cell.setCharacter(solution.charAt(j));
					}
					cells[x][y + j] = cell;
				}
			}
		}
		return(generateXML());
	}

}
