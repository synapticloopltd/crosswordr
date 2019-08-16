package synapticloop.crosswordr.crossword;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.crosswordr.extractor.ExtractorBase;

public class Crossword {
	private static final Logger LOGGER = LoggerFactory.getLogger(Crossword.class);

	private String name = null;
	private String fileName = null;
	private String formattedUrl = null;
	private String extractor = null;
	private String xsl = null;
	private String type = "date";
	private String translateDate = null;
	private String translateNumber = null;

	public Crossword(String name, String fileName, String formattedUrl, String extractor, String xsl, String type) {
		this.name = name;
		this.fileName = fileName;
		this.formattedUrl = formattedUrl;
		this.extractor = extractor;
		this.xsl = xsl;
		this.type = type;
	}

	public Crossword(String name, String fileName, String formattedUrl, String extractor, String xsl, String type, String translateDate, String translateNumber) {
		this(name, fileName, formattedUrl, extractor, xsl, type);
		this.translateDate = translateDate;
		this.translateNumber = translateNumber;
	}

	public String getData() {
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
		}

		try {
			LOGGER.info("attempting to download from '{}'", formattedUrl);
			return(extractorBase.extract(IOUtils.toString(new URL(formattedUrl).openStream(), "UTF-8")));
		} catch (IOException ex) {
			LOGGER.error("Could not find extractor for '{}'", extractor, ex);
		}
		return(null);
	}

	public String getName() { return name; }

	public String getFileName() { return fileName; }

	public String getFormattedUrl() { return formattedUrl; }

	public String getExtractor() { return extractor; }
	
	public String getXsl() { return(xsl); }
	
	public String getType() { return(this.type); }
}
