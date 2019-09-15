package synapticloop.puzzlr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.exception.PuzzlrException;
import synapticloop.puzzlr.extractor.BaseExtractor;

public class Puzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Puzzle.class);

	protected boolean isCorrect = true;

	protected String name = null;
	protected String fileName = null;
	protected String formattedUrl = null;
	protected String extractor = null;
	protected String xsl = null;
	private String type = "date";
	private String translateDate = null;
	private String translateNumber = null;
	private Integer puzzleNumber = null;

	public Puzzle(String name, 
			String fileName, 
			String formattedUrl, 
			String extractor, 
			String xsl, 
			String type) {

		this.name = name;
		this.fileName = fileName;
		this.formattedUrl = formattedUrl;
		this.extractor = extractor;
		this.xsl = xsl;
		this.type = type;
	}
	
	public Puzzle(String name, 
			String fileName, 
			String formattedUrl, 
			String extractor, 
			String xsl, 
			String type, 
			String translateDate, 
			String translateNumber) {

		this(name, fileName, formattedUrl, extractor, xsl, type);
		this.translateDate = translateDate;
		this.translateNumber = translateNumber;
	}


	/**
	 * Get the data as a string from the url, after passing it through the
	 * registered extractor
	 * 
	 * @throws PuzzlrException if the extractor could not be instantiated, or
	 * the content could not be downloaded from the URL
	 */
	public String getData() throws PuzzlrException {
		// first get the extractor
		BaseExtractor extractorBase = null;
		try {
			extractorBase = (BaseExtractor)Class.forName(extractor).getDeclaredConstructor().newInstance();
		} catch (InstantiationException | 
				IllegalAccessException | 
				IllegalArgumentException | 
				InvocationTargetException | 
				NoSuchMethodException | 
				SecurityException | 
				ClassNotFoundException ex) {

			LOGGER.error("Could not instantiate extractor of '{}'", extractor, ex);
			throw new PuzzlrException("Could not instantiate extractor " + extractor, ex);
		}

		try {
			LOGGER.info("attempting to download from '{}'", formattedUrl);
			String extract = extractorBase.extract(IOUtils.toString(new URL(formattedUrl).openStream(), "UTF-8"));
			if(null == extract) {
				throw new PuzzlrException("Could not extract data");
			}
			return extract;
		} catch (IOException ex) {
			LOGGER.error("Could not download file from '{}'", formattedUrl, ex);
			throw new PuzzlrException("Could not download the file from url'" + formattedUrl + "'.", ex);
		}
	}

	public String getName() { return name; }

	public String getFileName() { return fileName; }

	public String getFormattedUrl() { return formattedUrl; }

	public String getExtractor() { return extractor; }

	public String getXsl() { return(xsl); }
	
	public Integer getPuzzleNumber() { return(puzzleNumber); }
	public void setPuzzleNumber(Integer puzzleNumber) { this.puzzleNumber = puzzleNumber; }

	public void setIsCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
	public boolean getIsCorrect() { return(this.isCorrect); }

	public String getType() { return(this.type); }

	/**
	 * 
	 * @return
	 */
	public Date getTranslateDate() { 
		try {
			return(new SimpleDateFormat("yyyyMMdd").parse(this.translateDate));
		} catch (ParseException e) {
			LOGGER.error("Could not parse date '{}' with format 'yyyyMMdd'", this.translateDate);
			return(null);
		} 
	}

	public Integer getTranslateNumber() { 
		if(null != this.translateNumber) {
			return(Integer.valueOf(this.translateNumber));
		}
		return(null);
	}

}
