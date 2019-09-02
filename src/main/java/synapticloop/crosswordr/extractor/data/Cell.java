package synapticloop.crosswordr.extractor.data;

public class Cell {
	Integer number = null;
	String acrossClue = null;
	String downClue = null;
	Character character = null;
	Integer length = null;

	public Cell() {
	}

	public Integer getNumber() { return number; }

	public void setNumber(Integer number) { this.number = number; }

	public Integer getLength() { return length; }

	public void setLength(Integer length) { this.length = length; }

	public String getAcrossClue() { return acrossClue; }

	public void setAcrossClue(String acrossClue) { this.acrossClue = acrossClue; }

	public String getDownClue() { return downClue; }

	public void setDownClue(String downClue) { this.downClue = downClue; }

	public Character getCharacter() { return character; }

	public void setCharacter(Character character) { this.character = character; }
}
