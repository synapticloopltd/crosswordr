package synapticloop.puzzlr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.exception.PuzzlrException;
import synapticloop.puzzlr.extractor.ExtractorBase;

public abstract class Puzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Puzzle.class);

	protected boolean isCorrect = true;

	protected String name = null;
	protected String fileName = null;
	protected String formattedUrl = null;
	protected String extractor = null;
	protected String xsl = null;
	private Integer puzzleNumber = null;

	/**
	 * Get the data as a string from the url, after passing it through the
	 * registered extractor
	 * 
	 * @throws PuzzlrException if the extractor could not be instantiated, or
	 * the content could not be downloaded from the URL
	 */
	public String getData() throws PuzzlrException {
		// first get the extractor
		ExtractorBase extractorBase = null;
		try {
			extractorBase = (ExtractorBase)Class.forName(extractor).getDeclaredConstructor().newInstance();
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
			return(extractorBase.extract(IOUtils.toString(new URL(formattedUrl).openStream(), "UTF-8")));
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

	public void setIsCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
	public boolean getIsCorrect() { return(this.isCorrect); }

}
