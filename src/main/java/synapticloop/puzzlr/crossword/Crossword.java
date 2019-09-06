package synapticloop.puzzlr.crossword;

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

import synapticloop.puzzlr.Puzzle;
import synapticloop.puzzlr.exception.PuzzlrException;
import synapticloop.puzzlr.extractor.ExtractorBase;

/**
 * 
 * @author synapticloop
 */
public class Crossword extends Puzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Crossword.class);

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

}
