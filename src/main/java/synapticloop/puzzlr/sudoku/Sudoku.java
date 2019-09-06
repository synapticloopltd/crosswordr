package synapticloop.puzzlr.sudoku;

import synapticloop.puzzlr.Puzzle;

public class Sudoku extends Puzzle {

	public Sudoku(String name, 
			String fileName, 
			String formattedUrl, 
			String extractor, 
			String xsl) {

		this.name = name;
		this.fileName = fileName;
		this.formattedUrl = formattedUrl;
		this.extractor = extractor;
		this.xsl = xsl;
		
	}
}
