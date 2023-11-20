package synapticloop.puzzlr.util;

/*
 * Copyright (c) 2021 Synapticloop.
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

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.Constants;
import synapticloop.puzzlr.Puzzle;
import synapticloop.puzzlr.PuzzlrMain;

public class PDFHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFHelper.class);

	private static final String DOT_PDF = ".pdf";
	private static final String TO = "-to-";

	// all the XSLT variables that we are pumping into the template
	private static final String XSLT_VARIABLE_PUZZLE_NAME = "puzzleName";
	private static final String XSLT_VARIABLE_PUZZLE_SLUG = "puzzleSlug";
	private static final String XSLT_VARIABLE_PUZZLE_NUMBER = "puzzleNumber";
	private static final String XSLT_VARIABLE_PUZZLE_IDENTIFIER = "puzzleIdentifier";

	/**
	 * Convert the XML to a PDF
	 * 
	 * @param xmlFile The location of the XML file
	 * @param puzzle The puzzle data
	 * @param number The puzzle number (if not null)
	 * 
	 * @return The PDF file location that was generated (or null if no file was
	 * generated)
	 */
	public static String convertToPDF(File xmlFile, Puzzle puzzle, Integer number) {

		if(!puzzle.getIsCorrect()) {
			LOGGER.error("Puzzle '{}' for '{}' is marked as not correct, ignoring...", puzzle.getName(), puzzle.getFileName());
			return(null);
		}
		String pdfFile = "./output/pdf/" + puzzle.getFileName() + puzzle.getDate() + ".pdf";
		File testFile = new File(pdfFile);
		if(testFile.exists()) {
			LOGGER.info("File exists, not re-generating the pdf for '{}'.", pdfFile);
			return(pdfFile);
		}

		LOGGER.info("Converting file '{}' to '{}', with xsl '{}'", xmlFile.getName(), pdfFile, puzzle.getXsl());

		// the XML file which provides the input
		StreamSource xmlSource = new StreamSource(xmlFile);
		// create an instance of fop factory
		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
		// a user agent is needed for transformation
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		// Setup output
		OutputStream out = null;
		try {
			out = new java.io.FileOutputStream(pdfFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			InputStream resourceAsStream = PuzzlrMain.class.getResourceAsStream("/" + puzzle.getXsl());

			Transformer transformer = factory.newTransformer(new StreamSource(resourceAsStream));
			transformer.setParameter(XSLT_VARIABLE_PUZZLE_NAME, puzzle.getName());
			transformer.setParameter(XSLT_VARIABLE_PUZZLE_SLUG, puzzle.getSlug());
			transformer.setParameter(XSLT_VARIABLE_PUZZLE_IDENTIFIER, puzzle.getDate());
			if(null != number) {
				transformer.setParameter(XSLT_VARIABLE_PUZZLE_NUMBER, number);
			} else {
				transformer.setParameter(XSLT_VARIABLE_PUZZLE_NUMBER, -1);
			}

			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			// That's where the XML is first transformed to XSL-FO and then 
			// PDF is created
			transformer.transform(xmlSource, res);

			// if we get to this point - then the file has been added
			return(pdfFile);
		} catch (FOPException | TransformerException ex) {
			LOGGER.error("Exception caught, message was: {}", ex);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// do nothing
			}
		}
		return(null);
	}

	/**
	 * Merge all of the generated files into a single PDF file
	 * 
	 * @throws IOException
	 */
	public static void mergeFiles(List<String> generatedFiles, String optionRangeStart, String optionRangeEnd) throws IOException {
		// now merge the files
		if(generatedFiles.size() != 0) {
			PDFMergerUtility pdfMergerUtility= new PDFMergerUtility();

			StringBuffer destinationPdf = new StringBuffer(512);
			destinationPdf.append(Constants.DIR_OUTPUT_PDF);
			destinationPdf.append(optionRangeStart);

			if(!optionRangeEnd.equals(optionRangeStart)) {
				destinationPdf.append(TO);
				destinationPdf.append(optionRangeEnd);
			}
			destinationPdf.append(DOT_PDF);

			pdfMergerUtility.setDestinationFileName(destinationPdf.toString());
			for (String generatedFile : generatedFiles) {
				pdfMergerUtility.addSource(generatedFile);
				LOGGER.info("Merging file '{}' into '{}'", generatedFile, destinationPdf);
			}
			pdfMergerUtility.mergeDocuments(null);
			LOGGER.info("Merged {} file(s) to '{}'", generatedFiles.size(), destinationPdf);
		} else {
			LOGGER.error("No generated files to merge... skipping...");
		}
	}
}
