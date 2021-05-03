package synapticloop.puzzlr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.puzzlr.PuzzlrMain;

public class CommandParserHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandParserHelper.class);

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

	private static final String PARSED_AND_CALCULATED_PARAMETER = "Parsed and calculated parameter '{}' with value '{}'";
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public CommandParserHelper(String[] args) throws org.apache.commons.cli.ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("puzzlr", options);

		// now extract the commands
		parseAndValidateCommandLineArguments(cmd);
	}

	private void parseAndValidateCommandLineArguments(CommandLine cmd) {
		PuzzlrMain.optionDate = cmd.getOptionValue(CMD_DATE, SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis())));
		PuzzlrMain.optionRangeStart = cmd.getOptionValue(CMD_RANGE_START, null);
		PuzzlrMain.optionRangeEnd = cmd.getOptionValue(CMD_RANGE_END, null);
		PuzzlrMain.optionSlugs = cmd.getOptionValue(CMD_SLUGS, null);

		if(null != PuzzlrMain.optionSlugs) {
			String[] splits = PuzzlrMain.optionSlugs.split(",");
			for (String string : splits) {
				PuzzlrMain.WANTED_SLUGS.add(string);
			}
		}

		// now check for the range start and end
		if(null == PuzzlrMain.optionRangeStart) {
			PuzzlrMain.optionRangeStart = PuzzlrMain.optionDate;
		}

		if(null == PuzzlrMain.optionRangeEnd) {
			PuzzlrMain.optionRangeEnd = PuzzlrMain.optionDate;
		}

		logCommandLine(CMD_DATE, PuzzlrMain.optionDate);
		logCommandLine(CMD_RANGE_START, PuzzlrMain.optionRangeEnd);
		logCommandLine(CMD_RANGE_END, PuzzlrMain.optionRangeStart);
		logCommandLine(CMD_SLUGS, PuzzlrMain.optionSlugs);

		// finally - if optionRangeEnd is less than optionRangeStart, then just exit
		try {
			Date end = SIMPLE_DATE_FORMAT.parse(PuzzlrMain.optionRangeEnd);
			Date start = SIMPLE_DATE_FORMAT.parse(PuzzlrMain.optionRangeStart);
			if(end.before(start)) {
				LOGGER.error("End date is before start date");
				System.exit(-1);
			}
		} catch (ParseException e) {
			LOGGER.error("Could not parse the options");
			System.exit(-1);
		}
	}
	
	private void logCommandLine(String param, String value) {
		LOGGER.info(PARSED_AND_CALCULATED_PARAMETER, param, value);
	}
}
