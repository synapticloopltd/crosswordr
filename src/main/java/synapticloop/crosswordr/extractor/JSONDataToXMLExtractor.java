package synapticloop.crosswordr.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import synapticloop.crosswordr.extractor.data.Cell;

public class JSONDataToXMLExtractor extends ExtractorBase {
	private Map<String, String> keyValues = new HashMap<String, String>();

	private String[][] letters = new String[20][20];
	private Cell[][] cells = new Cell[20][20];

	private int width = -1;
	private int height = -1;
	private List<String> acrossClues = new ArrayList<String>();
	private List<String> downClues = new ArrayList<String>();

	@Override
	public String extract(String data) {
		JSONObject jsonObject = new JSONObject(data);
		String urlData= jsonObject.getJSONArray("cells").getJSONObject(0).getJSONObject("meta").getString("data");
		System.out.println(urlData);
		String[] parameters = urlData.split("&");

		for (String parameter : parameters) {
			if(parameter.trim().length() == 0) {
				continue;
			}
			System.out.println(parameter);
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

			System.out.println("word" + i + ": " + value);
			System.out.println("dir" + i + ": " + direction);
			System.out.println("startx" + i + ": " + x);
			System.out.println("starty" + i + ": " + y);
			System.out.println("clue" + i + ": " + clue);

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
						letters[x + j][y] = i + ":a:" + value.charAt(j) + "";
					} else {
						cell.setCharacter(value.charAt(j));
						letters[x + j][y] = value.charAt(j) + "";
					}
				}
				System.out.println();
			} else if(direction.equals("d")) {
				for (int j = 0; j < value.length(); j++) {
					Cell cell = cells[x + j][y];
					if(null == cell) {
						cell = new Cell();
					}
					if(j == 0 ) {
						cell.setDownClue(clue);
						cell.setCharacter(value.charAt(j));
						cell.setLength(value.length());
						letters[x][y + j] = i + ":d:" + value.charAt(j) + "";
					} else {
						if(letters[x][y + j] == null) {
							cell.setCharacter(value.charAt(j));
							letters[x][y + j] = value.charAt(j) + "";
						}
					}
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

		int clueNumber = 1;

		x = 0;
		y = 0;

		boolean keepPrinting = true;

		while(keepPrinting) {
			String letter = letters[x][y];
			if(null != letter) {
				if(letter.contains(":")) {
					if(letter.contains(":a:")) {
						acrossClues.add(
								clueNumber +
								":" +
								letter.substring(0, letter.indexOf(":") + 1) + 
								keyValues.get("clue" + letter.substring(0, letter.indexOf(":"))));
					} else {
						downClues.add(
								clueNumber + 
								":" + 
								letter.substring(0, letter.indexOf(":") + 1) + 
								keyValues.get("clue" + letter.substring(0, letter.indexOf(":"))));
					}
					System.out.print(String.format("%02d:", clueNumber) + letter.substring(letter.length() -1));
					letters[x][y] = String.format("%02d:", clueNumber) + letter.substring(letter.length() -1);
					clueNumber++;
				} else {
					System.out.print("   " + letter);
				}
			} else {
				System.out.print("    ");
			}
			x++;

			if(x == width) {
				y++;
				x = 0;
				System.out.println();
				if(y == height) {
					keepPrinting = false;
				}
			}
		}

		System.out.println("across");
		for (String string : acrossClues) {
			System.out.println(string);
		}

		System.out.println("down");
		for (String string : downClues) {
			System.out.println(string);
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
			String letter = letters[x][y];
			if(null != letter) {
				if(letter.contains(":")) {
					stringBuffer.append("<cell x=\"" + 
							(x + 1) + 
							"\" y=\"" + 
							(y + 1) + 
							"\" solution=\"" + 
							(letter.substring(letter.length() -1)) + 
							"\" number=\"" + 
							(Integer.valueOf(letter.substring(0, letter.indexOf(":"))) + "\" />\n"));
				} else {
					stringBuffer.append("<cell x=\"" + (x + 1) + "\" y=\"" + (y + 1) + "\" solution=\"" + letter + "\" />\n");
				}
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

		for (String clue : acrossClues) {
			stringBuffer.append("<clue number=\"" + 
					(clue.substring(0, clue.indexOf(":")))+ 
					"\" format=\"\">" + 
					clue.substring(clue.lastIndexOf(":") + 1) + 
					"</clue>\n");
		}
		stringBuffer.append("</clues>\n      <clues ordering=\"normal\">\n" + 
				"        <title>\n" + 
				"          <b>Down</b>\n" + 
				"        </title>\n");

		for (String clue : downClues) {
			stringBuffer.append("<clue number=\"" + 
					(clue.substring(0, clue.indexOf(":")))+ 
					"\" format=\"\">" + 
					clue.substring(clue.lastIndexOf(":") + 1) + 
					"</clue>\n");
		}

		stringBuffer.append("      </clues>\n" + 
				"    </crossword>\n" + 
				"  </rectangular-puzzle>\n" + 
				"</crossword-compiler>\n");
		System.out.println(stringBuffer.toString());
		return(stringBuffer.toString());
	}
}
