package synapticloop.puzzlr.extractor.crossword;

import synapticloop.puzzlr.extractor.ExtractorBase;
import synapticloop.puzzlr.extractor.crossword.data.Cell;

public abstract class CellBasedExtractor extends ExtractorBase {
	protected Cell[][] cells = new Cell[100][100];

	protected int width = -1;
	protected int height = -1;

	protected String generateXML() {
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
