package synapticloop.crosswordr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
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

public class CrosswordrMain {
	private static final String CROSSWORD_TYPE_DATE = "date";

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

	public static void main(String[] args) throws IOException, FOPException, TransformerException {
		Date currentDate = Calendar.getInstance().getTime();

		String crosswordrJson = FileUtils.readFileToString(new File(CROSSWORDR_JSON), Charset.defaultCharset());
		JSONObject crosswordrJsonObject = new JSONObject(crosswordrJson);
		Iterator<Object> crosswordsArrayIterator = crosswordrJsonObject.getJSONArray(JSON_KEY_CROSSWORDS).iterator();
		while (crosswordsArrayIterator.hasNext()) {

			JSONObject crosswordObject = (JSONObject) crosswordsArrayIterator.next();

			String crosswordType = crosswordObject.optString(JSON_KEY_TYPE, CROSSWORD_TYPE_DATE);
			if(crosswordType.equalsIgnoreCase(CROSSWORD_TYPE_DATE)) {

				String urlFormat = crosswordObject.getString(JSON_KEY_URL_FORMAT);
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
			} else {
				// add a numeric format now
				LOGGER.info("Ignoring crossword...");
			}
		}

		for (Crossword crossword : crosswords) {
			if(crossword.getType().equalsIgnoreCase(CROSSWORD_TYPE_DATE)) {
				// do we have the file yet
				String xmlFileName = "./output/xml/" + crossword.getFileName()  + new SimpleDateFormat("yyyy-MM-dd").format(currentDate) + ".xml";
				File xmlFile = new File(xmlFileName);
				if(!xmlFile.exists()) {
					LOGGER.info("Downloading file '{}'", xmlFileName);
					FileUtils.writeStringToFile(xmlFile, crossword.getData(), Charset.defaultCharset());
				} else {
					LOGGER.info("File exists, not downloading file '{}'", xmlFileName);
				}
				convertToPDF(xmlFile, crossword, currentDate);
			}
		}
	}

	public static void convertToPDF(File xmlFile, Crossword crossword, Date date) {
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
			transformer.setParameter("crosswordIdentifier", new SimpleDateFormat("dd MMMM yyyy").format(date));

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
