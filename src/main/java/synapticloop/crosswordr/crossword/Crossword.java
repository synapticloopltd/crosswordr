package synapticloop.crosswordr.crossword;

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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.crosswordr.exception.CrosswordrException;
import synapticloop.crosswordr.extractor.ExtractorBase;

/**
 * 
 * @author synapticloop
 */
public class Crossword {
	private static final Logger LOGGER = LoggerFactory.getLogger(Crossword.class);

	private boolean isCorrect = true;

	private String name = null;
	private String fileName = null;
	private String formattedUrl = null;
	private String extractor = null;
	private String xsl = null;
	private String type = "date";
	private String translateDate = null;
	private String translateNumber = null;
	private Integer crosswordNumber = null;

	/**
	 * Instantiate a Crossword
	 * 
	 * @param name The name of the crossword which gets printed at the top
	 * @param fileName The output file name
	 * @param formattedUrl The date (or number) formatted url
	 * @param extractor The data extractor to be used 
	 * @param xsl The XSL transformation file to be used
	 * @param type The type - iether one of number or date
	 */
	public Crossword(String name, 
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

	public Crossword(String name, 
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
	 * @throws CrosswordrException if the extractor could not be instantiated, or
	 * the content could not be downloaded from the URL
	 */
	public String getData() throws CrosswordrException {
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
			throw new CrosswordrException("Could not instantiate extractor " + extractor, ex);
		}

		try {
			LOGGER.info("attempting to download from '{}'", formattedUrl);
			return(extractorBase.extract(IOUtils.toString(new URL(formattedUrl).openStream(), "UTF-8")));
		} catch (IOException ex) {
			LOGGER.error("Could not download file from '{}'", formattedUrl, ex);
			throw new CrosswordrException("Could not download the file from url'" + formattedUrl + "'.", ex);
		}
	}

	public String getName() { return name; }

	public String getFileName() { return fileName; }

	public String getFormattedUrl() { return formattedUrl; }

	public String getExtractor() { return extractor; }

	public String getXsl() { return(xsl); }

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

	public Integer getCrosswordNumber() { return crosswordNumber; }
	public void setCrosswordNumber(Integer crosswordNumber) { this.crosswordNumber = crosswordNumber; }

	public void setIsCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
	public boolean getIsCorrect() { return(this.isCorrect); }
}
