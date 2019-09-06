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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import synapticloop.puzzlr.crossword.extractor.data.Cell;
import synapticloop.puzzlr.extractor.ExtractorBase;

public class JSONDataToXMLExtractor extends ExtractorBase {
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

	private Cell[][] cells = new Cell[100][100];

	private int width = -1;
	private int height = -1;

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
		// now that we have all of the values, time to generate the xml file
		height = Integer.valueOf(keyValues.get(JSON_KEY_NUM_ROWS));
		width = Integer.valueOf(keyValues.get(JSON_KEY_NUM_COLUMNS));
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
					cell.setNumber(numClue);
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

	private String generateXML() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
						"<crossword-compiler xmlns=\"http://crossword.info/xml/crossword-compiler\">\n" + 
						"  <rectangular-puzzle xmlns=\"http://crossword.info/xml/rectangular-puzzle\" alphabet=\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\">\n" + 
						"    <metadata>\n" + 
						"      <title />\n" + 
						"      <creator />\n" + 
						"      <copyright />\n" + 
						"      <description />\n" + 
						"    </metadata>\n" + 
						"    <crossword>\n" + 
						"      <grid width=\"" + width + "\" height=\"" + height + "\">\n");

		// now for the cells
		int x = 0;
		int y = 0;

		boolean keepPrinting = true;

		while(keepPrinting) {
			Cell cell = cells[x][y];
			if(null != cell) {
				stringBuffer.append(
						"<cell x=\"" + 
								(x + 1) + 
								"\" y=\"" + 
								(y + 1) + 
								"\" solution=\"" + 
								cell.getCharacter() +
						"\"");
				if(null != cell.getNumber()) {
					stringBuffer.append(" number=\"");
					stringBuffer.append(cell.getNumber());
					stringBuffer.append("\"");
				}
				stringBuffer.append("/>\n");
			} else {
				stringBuffer.append("<cell x=\"" + (x + 1) + "\" y=\"" + (y + 1) + "\" type=\"block\" />\n");
			}
			y++;

			if(y == width) {
				x++;
				y = 0;
				if(x == height) {
					keepPrinting = false;
				}
			}
		}

		stringBuffer.append(
				"      </grid>\n" +
						"      <clues ordering=\"normal\">\n" + 
						"        <title>\n" + 
						"          <b>Across</b>\n" + 
				"        </title>\n");

		x = 0;
		y = 0;

		boolean keepGoing = true;
		while(keepGoing) {
			Cell cell = cells[x][y];
			if(null != cell) {
				if(null != cell.getAcrossClue()) {
					stringBuffer.append(
							"<clue number=\"" + 
									cell.getNumber()+ 
									"\" format=\"\">" + 
									cell.getAcrossClue() + 
							"</clue>\n");
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

		stringBuffer.append(
				"      </clues>\n" + 
						"      <clues ordering=\"normal\">\n" + 
						"        <title>\n" + 
						"          <b>Down</b>\n" + 
				"        </title>\n");

		x = 0;
		y = 0;

		keepGoing = true;
		while(keepGoing) {
			Cell cell = cells[x][y];
			if(null != cell) {
				if(null != cell.getDownClue()) {
					stringBuffer.append(
							"<clue number=\"" + 
									cell.getNumber()+ 
									"\" format=\"\">" + 
									cell.getDownClue() + 
							"</clue>\n");
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

		stringBuffer.append(
				"      </clues>\n" + 
						"    </crossword>\n" + 
						"  </rectangular-puzzle>\n" + 
				"</crossword-compiler>\n");
		return(stringBuffer.toString());
	}
}
