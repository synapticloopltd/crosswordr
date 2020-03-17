package synapticloop.puzzlr.extractor.crossword;

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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.extractor.crossword.data.Cell;

public class JSONDataToXMLExtractor extends BaseCellExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(JSONDataToXMLExtractor.class);

	private static final String JSON_KEY_NUM_ROWS = "num_rows";
	private static final String JSON_KEY_NUM_COLUMNS = "num_columns";
	private static final String JSON_KEY_CELLS = "cells";
	private static final String JSON_KEY_META = "meta";
	private static final String JSON_KEY_DATA = "data";
	private static final String JSON_KEY_WORD = "word";
	private static final String JSON_KEY_DIR = "dir";
	private static final String JSON_KEY_START_K = "start_k";
	private static final String JSON_KEY_START_J = "start_j";
	private static final String JSON_KEY_CLUE = "clue";

	private static final String JSON_KEY_DIRECTION_A = "a";
	private static final String JSON_KEY_DIRECTION_D = "d";

	private Map<String, String> keyValues = new HashMap<String, String>();

	@Override
	public String extract(String data) {
		JSONObject jsonObject = new JSONObject(data);
		String urlData= jsonObject.getJSONArray(JSON_KEY_CELLS).getJSONObject(0).getJSONObject(JSON_KEY_META).getString(JSON_KEY_DATA);

		String[] parameters = urlData.split("&");

		for (String parameter : parameters) {
			if(parameter.trim().length() == 0) {
				continue;
			}
			String[] keyValue = parameter.split("=");
			keyValues.put(keyValue[0], keyValue[1]);
		}
		LOGGER.info("Got data: {}", urlData);
		// now that we have all of the values, time to generate the xml file
		try {
			height = Integer.valueOf(keyValues.get(JSON_KEY_NUM_ROWS));
			width = Integer.valueOf(keyValues.get(JSON_KEY_NUM_COLUMNS));
		} catch (NumberFormatException ex) {
			return(null);
		}
		return(generateDataAndXml());
	}

	private String generateDataAndXml() {
		int i = 0;
		while(true) {
			String value = keyValues.get(JSON_KEY_WORD + i);

			if(value == null) {
				extractClues();
				return(generateXML());
			}

			String direction = keyValues.get(JSON_KEY_DIR + i);
			int x = Integer.parseInt(keyValues.get(JSON_KEY_START_K + i));
			int y = Integer.parseInt(keyValues.get(JSON_KEY_START_J + i));
			String clue = keyValues.get(JSON_KEY_CLUE + i);

			if(direction.equals(JSON_KEY_DIRECTION_A)) {
				for (int j = 0; j < value.length(); j++) {
					Cell cell = cells[x + j][y];
					if(null == cell) {
						cell = new Cell();
					}
					if(j == 0) {
						cell.setAcrossClue(clue);
						cell.setCharacter(value.charAt(j));
						cell.setLength(value.length());
					} else {
						cell.setCharacter(value.charAt(j));
					}
					cells[x + j][y] = cell;
				}
			} else if(direction.equals(JSON_KEY_DIRECTION_D)) {
				for (int j = 0; j < value.length(); j++) {
					Cell cell = cells[x][y + j];
					if(null == cell) {
						cell = new Cell();
					}
					if(j == 0 ) {
						cell.setDownClue(clue);
						cell.setCharacter(value.charAt(j));
						cell.setLength(value.length());
					} else {
						cell.setCharacter(value.charAt(j));
					}
					cells[x][y + j] = cell;
				}
			}
			i++;
		}
	}

	private void extractClues() {
		int x = 0;
		int y = 0;

		int numClue = 1;
		boolean keepGoing = true;
		while(keepGoing) {
			Cell cell = cells[x][y];
			if(null != cell) {
				if(null != cell.getAcrossClue() || null != cell.getDownClue()) {
					cell.setNumber(numClue + "");
					numClue++;
				}
			}

			x++;

			if(x == width) {
				y++;
				x = 0;

				if(y == height) {
					keepGoing = false;
				}
			}
		}
	}
}
