package synapticloop.puzzlr.extractor.crossword.data;

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

public class Cell {
	String number = null;
	String acrossClue = null;
	String downClue = null;
	Character character = null;
	Integer length = null;

	public Cell() {
	}

	public String getNumber() { return number; }

	public void setNumber(String number) { this.number = number; }

	public Integer getLength() { return length; }

	public void setLength(Integer length) { this.length = length; }

	public String getAcrossClue() { return acrossClue; }

	public void setAcrossClue(String acrossClue) { this.acrossClue = acrossClue; }

	public String getDownClue() { return downClue; }

	public void setDownClue(String downClue) { this.downClue = downClue; }

	public Character getCharacter() { return character; }

	public void setCharacter(Character character) { this.character = character; }
}
