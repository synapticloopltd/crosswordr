package synapticloop.crosswordr.extractor;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import synapticloop.crosswordr.extractor.data.Cell;

public class JSONDataToXMLExtractor extends ExtractorBase {
	private Map<String, String> keyValues = new HashMap<String, String>();

	private Cell[][] cells = new Cell[100][100];

	private int width = -1;
	private int height = -1;

	@Override
	public String extract(String data) {
		JSONObject jsonObject = new JSONObject(data);
		String urlData= jsonObject.getJSONArray("cells").getJSONObject(0).getJSONObject("meta").getString("data");

		String[] parameters = urlData.split("&");

		for (String parameter : parameters) {
			if(parameter.trim().length() == 0) {
				continue;
			}
			String[] keyValue = parameter.split("=");
			keyValues.put(keyValue[0], keyValue[1]);
		}
		// now that we have all of the values, time to generate the xml file
		height = Integer.valueOf(keyValues.get("num_rows"));
		width = Integer.valueOf(keyValues.get("num_columns"));
		return(generateDataAndXml());
	}

	private String generateDataAndXml() {
		int i = 0;
		while(true) {
			String value = keyValues.get("word" + i);

			if(value == null) {
				extractClues();
				return(generateXML());
			}

			String direction = keyValues.get("dir" + i);
			int x = Integer.parseInt(keyValues.get("start_k" + i));
			int y = Integer.parseInt(keyValues.get("start_j" + i));
			String clue = keyValues.get("clue" + i);

			if(direction.equals("a")) {
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
			} else if(direction.equals("d")) {
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
		stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				stringBuffer.append("<cell x=\"" + 
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

		stringBuffer.append("      </grid>\n"
				+ "      <clues ordering=\"normal\">\n" + 
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
					stringBuffer.append("<clue number=\"" + 
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

		stringBuffer.append("      </clues>\n      <clues ordering=\"normal\">\n" + 
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
					stringBuffer.append("<clue number=\"" + 
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

		stringBuffer.append("      </clues>\n" + 
				"    </crossword>\n" + 
				"  </rectangular-puzzle>\n" + 
				"</crossword-compiler>\n");
		return(stringBuffer.toString());
	}
}
