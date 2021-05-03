package synapticloop.puzzlr;

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.FOPException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.exception.PuzzlrException;
import synapticloop.puzzlr.util.PDFHelper;

public class PuzzlrMain {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final Logger LOGGER = LoggerFactory.getLogger(PuzzlrMain.class);

	private static final String CMD_DATE = "date";
	private static final String CMD_RANGE_END = "end";
	private static final String CMD_RANGE_START = "start";
	private static final String CMD_SLUGS = "slugs";

	// command line options
	private static Options options = new Options();
	static {
		options.addOption(CMD_DATE, true, "The single date to search for");
		options.addOption(CMD_RANGE_START, true, "The range of date to start (inclusive).");
		options.addOption(CMD_RANGE_END, true, "The range of date to end (inclusive)");
		options.addOption(CMD_SLUGS, true, "The comma separated list of slugs to download");
	}

	// slug to title hashmap
	private static final Map<String, String> SLUG_MAP = new LinkedHashMap<>();

	// command line argument
	private static final String CMD_DATE_FORMAT = "yyyyMMdd";

	private static final String PUZZLE_TYPE_DATE_DEFAULT = CMD_DATE;
	private static final String PUZZLE_TYPE_NUMBER = "number";

	private static final String PUZZLR_JSON = "./puzzlr.json";

	// all the keys for the JSON parser
	private static final String JSON_KEY_PUZZLES = "puzzles";


	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_SLUG = "slug";
	private static final String JSON_KEY_URL_FORMAT = "url_format";
	private static final String JSON_KEY_EXTRACTOR = "extractor";
	private static final String JSON_KEY_XSL = "xsl";
	private static final String JSON_KEY_TYPE = "type";
	private static final String JSON_KEY_TRANSLATE_DATE = "translateDate";
	private static final String JSON_KEY_TRANSLATE_NUMBER = "translateNumber";

	private static List<Puzzle> puzzles = new ArrayList<Puzzle>();

	private static List<String> GENERATED_FILES = new ArrayList<String>();

	private static final Set<String> WANTED_SLUGS = new HashSet<>();

	// the default command line arguments
	private static String optionDate = null;
	private static String optionRangeStart = null;
	private static String optionRangeEnd = null;
	private static String optionSlugs = null;

	public static void main(String[] args) throws IOException, FOPException, TransformerException, ParseException, org.apache.commons.cli.ParseException {
		FileUtils.forceMkdir(new File(Constants.DIR_OUTPUT_XML));
		FileUtils.forceMkdir(new File(Constants.DIR_OUTPUT_PDF));

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("puzzlr", options);

		// now extract the commands
		parseAndValidateCommandLineArguments(cmd);


		String puzzlrJson = FileUtils.readFileToString(new File(PUZZLR_JSON), Charset.defaultCharset());
		JSONObject puzzlrJsonObject = new JSONObject(puzzlrJson);
		// now let us print out the slugs
		Iterator<Object> puzzlesArrayIterator = puzzlrJsonObject.getJSONArray(JSON_KEY_PUZZLES).iterator();
		while (puzzlesArrayIterator.hasNext()) {
			JSONObject puzzleObject = (JSONObject) puzzlesArrayIterator.next();
			String slug = puzzleObject.optString(JSON_KEY_SLUG, null);
			String puzzleName = puzzleObject.getString(JSON_KEY_NAME);
			if(null == slug) {
				LOGGER.warn("No slug found for puzzle named '{}', you WILL NOT be able to reference this puzzle", puzzleName);
			} else {
				LOGGER.info("Found slug '{}' for puzzle named '{}'", slug, puzzleName);
				if(SLUG_MAP.containsKey(slug)) {
					LOGGER.error("Slug '{}' already exists for puzzle named '{}', so the puzzle named '{}' will be ignored.", slug, SLUG_MAP.get(slug), puzzleName);
				} else {
					SLUG_MAP.put(slug, puzzleName);
				}
			}
		}


		// at this point in time we are going to go through the range start and end
		boolean shouldStop = false;

		optionDate = optionRangeStart;
		while(!shouldStop) {
			generatePuzzles(puzzlrJsonObject);
			if(optionRangeEnd.equals(optionDate)) {
				shouldStop = true;
			} else {
				// increment defaultCommandDate
				Date downloadDate = SIMPLE_DATE_FORMAT.parse(optionDate);
				Calendar instance = Calendar.getInstance();
				instance.setTime(downloadDate);
				instance.add(Calendar.DAY_OF_MONTH, 1);
				optionDate = SIMPLE_DATE_FORMAT.format(instance.getTime());
			}
		}

		writeXmlFilesAndMerge();

		PDFHelper.mergeFiles(GENERATED_FILES, optionRangeStart, optionRangeEnd);
	}

	private static void generatePuzzles(JSONObject puzzlrJsonObject) throws ParseException {
		Iterator<Object> puzzlesArrayIterator;
		puzzlesArrayIterator = puzzlrJsonObject.getJSONArray(JSON_KEY_PUZZLES).iterator();
		while (puzzlesArrayIterator.hasNext()) {
			Integer puzzleNumber = null;
			JSONObject puzzleObject = (JSONObject) puzzlesArrayIterator.next();

			String puzzleType = puzzleObject.optString(JSON_KEY_TYPE, PUZZLE_TYPE_DATE_DEFAULT);
			String urlFormat = puzzleObject.getString(JSON_KEY_URL_FORMAT);

			if(puzzleType.equalsIgnoreCase(PUZZLE_TYPE_DATE_DEFAULT)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(urlFormat);
				String formattedUrl = simpleDateFormat.format(SIMPLE_DATE_FORMAT.parse(optionDate));

				// if the wanted slugs set is empty - we want all, if not empty, only those slugs

				Puzzle puzzle = new Puzzle(
						puzzleObject.getString(JSON_KEY_NAME), 
						puzzleObject.optString(JSON_KEY_SLUG, null),
						optionDate,
						formattedUrl, 
						puzzleObject.getString(JSON_KEY_EXTRACTOR),
						puzzleObject.getString(JSON_KEY_XSL),
						puzzleType,
						optionDate,
						"-1"
						);
				if(WANTED_SLUGS.isEmpty() || WANTED_SLUGS.contains(puzzle.getSlug())) {
					puzzles.add(puzzle);
				}

			} else if(puzzleType.equalsIgnoreCase(PUZZLE_TYPE_NUMBER)){
				// add a numeric format now
				String translateDate = puzzleObject.getString(JSON_KEY_TRANSLATE_DATE);
				String translateNumber = puzzleObject.getString(JSON_KEY_TRANSLATE_NUMBER);
				Date dateTranslate = new SimpleDateFormat(CMD_DATE_FORMAT).parse(translateDate);

				int numDaysDifference = (int)((SIMPLE_DATE_FORMAT.parse(optionDate).getTime() - dateTranslate.getTime()) / (1000 * 60 * 60 * 24) );
				int parseInt = Integer.parseInt(translateNumber);

				puzzleNumber = parseInt + numDaysDifference;
				String formattedUrl = String.format(urlFormat, puzzleNumber);
				Puzzle puzzle = new Puzzle(
						puzzleObject.getString(JSON_KEY_NAME), 
						puzzleObject.getString(JSON_KEY_SLUG),
						optionDate,
						formattedUrl, 
						puzzleObject.getString(JSON_KEY_EXTRACTOR),
						puzzleObject.getString(JSON_KEY_XSL),
						puzzleType,
						translateDate,
						translateNumber
						);
				puzzle.setPuzzleNumber(puzzleNumber);

				// if the wanted slugs set is empty - we want all, if not empty, only those slugs
				if(WANTED_SLUGS.isEmpty() || WANTED_SLUGS.contains(puzzle.getSlug())) {
					puzzles.add(puzzle);
				}
			} else {
				LOGGER.error("Unknown puzzle type of '{}'", puzzleType);
			}
		}

		// check to see whether we have a duplicate URL
		boolean hasDuplicate = false;
		Set<String> urls = new HashSet<String>();
		for (Puzzle puzzle : puzzles) {
			String formattedUrl = puzzle.getFormattedUrl();
			if(urls.contains(formattedUrl)) {
				LOGGER.error("Puzzle already contains url '{}'", formattedUrl);
				hasDuplicate = true;
			}
			urls.add(formattedUrl);
		}

		if(hasDuplicate) {
			LOGGER.error("Found duplicate urls for puzzles, exiting...");
			System.exit(-1);
		}
	}

	private static void parseAndValidateCommandLineArguments(CommandLine cmd) {
		optionDate = cmd.getOptionValue(CMD_DATE, SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis())));
		optionRangeStart = cmd.getOptionValue(CMD_RANGE_START, null);
		optionRangeEnd = cmd.getOptionValue(CMD_RANGE_END, null);
		optionSlugs = cmd.getOptionValue(CMD_SLUGS, null);

		if(null != optionSlugs) {
			String[] splits = optionSlugs.split(",");
			for (String string : splits) {
				WANTED_SLUGS.add(string);
			}
		}

		// now check for the range start and end
		if(null == optionRangeStart) {
			optionRangeStart = optionDate;
		}

		if(null == optionRangeEnd) {
			optionRangeEnd = optionDate;
		}

		System.out.println("Parsed and calculated parameter '" + CMD_DATE + "' of '" + optionDate + "'.");
		System.out.println("Parsed and calculated parameter '" + CMD_RANGE_START + "' of '" + optionRangeEnd + "'.");
		System.out.println("Parsed and calculated parameter '" + CMD_RANGE_END + "' of '" + optionRangeStart + "'.");
		System.out.println("Parsed and calculated parameter '" + CMD_SLUGS + "' of '" + optionSlugs + "'.");

		// finally - if optionRangeEnd is less than optionRangeStart, then just exit
		try {
			Date end = SIMPLE_DATE_FORMAT.parse(optionRangeEnd);
			Date start = SIMPLE_DATE_FORMAT.parse(optionRangeStart);
			if(end.before(start)) {
				LOGGER.error("End date is before start date");
				System.exit(-1);
			}
		} catch (ParseException e) {
			LOGGER.error("Could not parse the options");
			System.exit(-1);
		}
	}

	/**
	 * Write out the XML file into the location
	 * 
	 * @throws IOException if there was an error writing the file
	 */
	private static void writeXmlFilesAndMerge() throws IOException {
		for (Puzzle puzzle : puzzles) {
			String xmlFileName = Constants.DIR_OUTPUT_XML + puzzle.getFileName()  + puzzle.getDate() + ".xml";
			File xmlFile = new File(xmlFileName);
			if(!xmlFile.exists()) {
				LOGGER.info("Downloading file '{}'", xmlFileName);
				try {
					FileUtils.writeStringToFile(xmlFile, puzzle.getData(), Charset.defaultCharset());
				} catch (PuzzlrException ex) {
					puzzle.setIsCorrect(false);
					LOGGER.error(ex.getMessage());
					continue;
				}
			} else {
				LOGGER.info("File exists, not downloading file '{}'", xmlFileName);
			}
			String pdfFile = PDFHelper.convertToPDF(xmlFile, puzzle , puzzle.getPuzzleNumber());
			if(null != pdfFile) {
				GENERATED_FILES.add(pdfFile);
			}
		}
	}
}
