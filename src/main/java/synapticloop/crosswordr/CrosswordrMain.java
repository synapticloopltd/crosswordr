package synapticloop.crosswordr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import synapticloop.crosswordr.crossword.Crossword;
import synapticloop.crosswordr.exception.CrosswordrException;

public class CrosswordrMain {
	private static final String CROSSWORD_TYPE_DATE_DEFAULT = "date";
	private static final String CROSSWORD_TYPE_NUMBER = "number";

	private static final Logger LOGGER = LoggerFactory.getLogger(CrosswordrMain.class);

	private static List<Crossword> crosswords = new ArrayList<Crossword>();

	private static final String CROSSWORDR_JSON = "./crosswordr.json";

	private static final String JSON_KEY_CROSSWORDS = "crosswords";
	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_FILE_NAME = "file_name";
	private static final String JSON_KEY_URL_FORMAT = "url_format";
	private static final String JSON_KEY_EXTRACTOR = "extractor";
	private static final String JSON_KEY_XSL = "xsl";
	private static final String JSON_KEY_TYPE = "type";
	private static final String JSON_KEY_TRANSLATE_DATE = "translateDate";
	private static final String JSON_KEY_TRANSLATE_NUMBER = "translateNumber";

	public static void main(String[] args) throws IOException, FOPException, TransformerException, ParseException {
		Date currentDate = null;
		if(args.length != 0) {
			String argZero = args[0];
			LOGGER.info("Attempting to parse '{}' as a date.", argZero);
			try {
				currentDate = new SimpleDateFormat("yyyyMMdd").parse(argZero);
			} catch(ParseException ex) {
				LOGGER.error("Exception: {}. Exiting...", ex.getMessage());
				System.exit(-1);
			}

		} else {
			LOGGER.info("No date passed through the command line, using today's date.");
			currentDate = Calendar.getInstance().getTime();
		}


		String crosswordrJson = FileUtils.readFileToString(new File(CROSSWORDR_JSON), Charset.defaultCharset());
		JSONObject crosswordrJsonObject = new JSONObject(crosswordrJson);
		Iterator<Object> crosswordsArrayIterator = crosswordrJsonObject.getJSONArray(JSON_KEY_CROSSWORDS).iterator();
		while (crosswordsArrayIterator.hasNext()) {
			Integer crosswordNumber = null;
			JSONObject crosswordObject = (JSONObject) crosswordsArrayIterator.next();

			String crosswordType = crosswordObject.optString(JSON_KEY_TYPE, CROSSWORD_TYPE_DATE_DEFAULT);
			String urlFormat = crosswordObject.getString(JSON_KEY_URL_FORMAT);

			if(crosswordType.equalsIgnoreCase(CROSSWORD_TYPE_DATE_DEFAULT)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(urlFormat);
				String formattedUrl = simpleDateFormat.format(currentDate);

				crosswords.add(
						new Crossword(
								crosswordObject.getString(JSON_KEY_NAME), 
								crosswordObject.getString(JSON_KEY_FILE_NAME), 
								formattedUrl, 
								crosswordObject.getString(JSON_KEY_EXTRACTOR),
								crosswordObject.getString(JSON_KEY_XSL),
								crosswordType
								)
						);
			} else if(crosswordType.equalsIgnoreCase(CROSSWORD_TYPE_NUMBER)){
				// add a numeric format now
				String translateDate = crosswordObject.getString(JSON_KEY_TRANSLATE_DATE);
				String translateNumber = crosswordObject.getString(JSON_KEY_TRANSLATE_NUMBER);
				Date dateTranslate = new SimpleDateFormat("yyyyMMdd").parse(translateDate);

				int numDaysDifference = (int)((currentDate.getTime() - dateTranslate.getTime()) / (1000 * 60 * 60 * 24) );
				int parseInt = Integer.parseInt(translateNumber);

				crosswordNumber = parseInt + numDaysDifference;
				String formattedUrl = String.format(urlFormat, crosswordNumber);
				Crossword crossword = new Crossword(
						crosswordObject.getString(JSON_KEY_NAME), 
						crosswordObject.getString(JSON_KEY_FILE_NAME), 
						formattedUrl, 
						crosswordObject.getString(JSON_KEY_EXTRACTOR),
						crosswordObject.getString(JSON_KEY_XSL),
						crosswordType,
						translateDate,
						translateNumber
						);
				crossword.setCrosswordNumber(crosswordNumber);
				crosswords.add(
						crossword
						);
			} else {
				LOGGER.error("Unknown crossword type of '{}'", crosswordType);
			}
		}

		// check to see whether we have a duplicate URL
		boolean hasDuplicate = false;
		Set<String> urls = new HashSet<String>();
		for (Crossword crossword : crosswords) {
			String formattedUrl = crossword.getFormattedUrl();
			if(urls.contains(formattedUrl)) {
				LOGGER.error("Crossword already contains url '{}'", formattedUrl);
				hasDuplicate = true;
			}
			urls.add(formattedUrl);
		}

		if(hasDuplicate) {
			LOGGER.error("Found duplicate urls for crosswords, exiting...");
			System.exit(-1);
		}

		for (Crossword crossword : crosswords) {
			String xmlFileName = "./output/xml/" + crossword.getFileName()  + new SimpleDateFormat("yyyy-MM-dd").format(currentDate) + ".xml";
			File xmlFile = new File(xmlFileName);
			if(!xmlFile.exists()) {
				LOGGER.info("Downloading file '{}'", xmlFileName);
				try {
					FileUtils.writeStringToFile(xmlFile, crossword.getData(), Charset.defaultCharset());
				} catch (CrosswordrException ex) {
					LOGGER.error(ex.getMessage(), ex);
					continue;
				}
			} else {
				LOGGER.info("File exists, not downloading file '{}'", xmlFileName);
			}
			convertToPDF(xmlFile, crossword, currentDate, crossword.getCrosswordNumber());
		}
	}

	public static void convertToPDF(File xmlFile, Crossword crossword, Date date, Integer number) {
		String pdfFile = "./output/pdf/" + crossword.getFileName() + new SimpleDateFormat("yyyy-MM-dd").format(date) + ".pdf";

		LOGGER.info("Converting file '{}' to '{}', with xsl '{}'", xmlFile.getName(), pdfFile, crossword.getXsl());

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			InputStream resourceAsStream = CrosswordrMain.class.getResourceAsStream("/" + crossword.getXsl());

			Transformer transformer = factory.newTransformer(new StreamSource(resourceAsStream));
			transformer.setParameter("crosswordName", crossword.getName());
			//			if(crossword.getType().compareTo(CROSSWORD_TYPE_NUMBER)) {
			//				transformer.setParameter("crosswordIdentifier", "#" + crossword.getTranslateNumber());
			//			}
			transformer.setParameter("crosswordIdentifier", new SimpleDateFormat("dd MMMM yyyy").format(date));
			if(null != number) {
				transformer.setParameter("crosswordNumber", number);
			} else {
				transformer.setParameter("crosswordNumber", -1);
			}

			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			// That's where the XML is first transformed to XSL-FO and then 
			// PDF is created
			transformer.transform(xmlSource, res);
		} catch (FOPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
