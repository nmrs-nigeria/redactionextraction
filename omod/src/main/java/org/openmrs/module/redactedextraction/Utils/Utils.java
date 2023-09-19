package org.openmrs.module.redactedextraction.Utils;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.redactedextraction.model.DBConnection;
import org.openmrs.module.redactedextraction.model.Validator;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class Utils {
	
	DatatypeFactory datatypeFactory;
	
	private static final int DATE_ONLY_TEXT_LENGTH = 10;
	
	private static final int DATE_TIME_TEXT_LENGTH = 16;
	
	private static final int DATE_TIME_SECOND_TEXT_LENGTH = 19;
	
	public static final int PEPFAR_IDENTIFIER_INDEX = 4;
	
	public static final int HOSPITAL_IDENTIFIER_INDEX = 5;
	
	public Utils() {
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		}
		catch (Exception ignored) {}
	}
	
	public static DBConnection getNmrsConnectionDetails() {
		
		DBConnection result = new DBConnection();
		
		try {
			
			Properties props = new Properties();
			props = Context.getRuntimeProperties();
			
			result.setUsername(props.getProperty("connection.username"));
			result.setPassword(props.getProperty("connection.password"));
			result.setUrl(props.getProperty("connection.url"));
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return result;
		
	}
	
	public XMLGregorianCalendar getXmlDateTime(Date date) {
		XMLGregorianCalendar cal = null;
		if (date != null) {
			cal = datatypeFactory.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date));
		}
		return cal;
	}
	
	public XMLGregorianCalendar getXmlDateMessageHeader(Date date) {
		XMLGregorianCalendar cal = null;
		if (date != null) {
			cal = datatypeFactory.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd").format(date));
		}
		return cal;
	}
	
	public static String getFacilityName() {
		return Context.getAdministrationService().getGlobalProperty("Facility_Name");
	}
	
	public static String getFacilityType() {
		//use FAC for now
		return "FAC";
	}
	
	public static String getFacilityLocalId() {
		return Context.getAdministrationService().getGlobalProperty("facility_local_id");
	}
	
	public static String getIPShortName() {
		return Context.getAdministrationService().getGlobalProperty("partner_short_name");
	}
	
	public static String getFacilityDATIMId() {
		return Context.getAdministrationService().getGlobalProperty("facility_datim_code");
	}
	
	public static Marshaller createMarshaller(JAXBContext jaxbContext) throws JAXBException, SAXException {
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		java.net.URL xsdFilePath = Thread.currentThread().getContextClassLoader().getResource("NDR_RD_1.xsd");
		
		assert xsdFilePath != null;
		
		Schema schema = sf.newSchema(xsdFilePath);
		
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		
		jaxbMarshaller.setSchema(schema);
		
		//Call Validator class to perform the validation
		jaxbMarshaller.setEventHandler(new Validator());
		return jaxbMarshaller;
	}
	
	public static XMLGregorianCalendar getXmlDate(Date date) throws DatatypeConfigurationException {
		
		XMLGregorianCalendar cal = null;
		String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
		if (date != null) {
			System.out.println("about to create datatypefactory");
			DatatypeFactory df = DatatypeFactory.newInstance();
			System.out.println("finished creating datatypefactory");
			cal = df.newXMLGregorianCalendar(dateString);
		}
		return cal;
	}
	
	public static String zipFolder(HttpServletRequest request, String folderToZip, String zipFileName, String reportType) {
		
		File toZIP = new File(folderToZip);
		if (!toZIP.exists() || toZIP.listFiles() == null || Objects.requireNonNull(toZIP.listFiles()).length == 0) {
			return "no new commodity record found";
		}
		
		ZipUtil appZip = new ZipUtil(folderToZip);
		appZip.generateFileList(toZIP);
		appZip.zipIt(Paths.get(toZIP.getParent(), zipFileName).toString());
		
		//old implementation
		//  return request.getContextPath() + "/downloads/" + reportType + "/" + zipFileName;
		return Paths.get(request.getContextPath(), "Redacteddownloads", reportType, zipFileName).toString();
	}
	
	public static Date parseCustomOpenhmisDateString(String dateText) {
		if (StringUtils.isEmpty(dateText)) {
			return null;
		}
		
		SimpleDateFormat dateFormat = null;
		if (dateText.length() == DATE_ONLY_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} else if (dateText.length() == DATE_TIME_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		} else if (dateText.length() == DATE_TIME_SECOND_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		
		Date result = null;
		if (dateFormat == null) {
			//		LOG.warn("Could not parse the date string '" + dateText + "'.");
		} else {
			try {
				result = dateFormat.parse(dateText);
			}
			catch (ParseException pex) {
				//		LOG.warn("Could not parse the date string '" + dateText + "'.", pex);
			}
		}
		
		return result;
	}
	
	public static String ensureReportDownloadFolderExist(HttpServletRequest request) {
		String folder = Paths.get(
		    new File(request.getSession().getServletContext().getRealPath(request.getContextPath())).getParentFile()
		            .toString(), "CMReports").toString();
		
		File dir = new File(folder);
		Boolean b = dir.mkdir();
		//System.out.println("Creating download folder : " + folder + "was successful : " + b);
		return folder;
	}
	
	public static String ensureDownloadFolderExist(HttpServletRequest request) {
		String folder = Paths.get(
		    new File(request.getSession().getServletContext().getRealPath(request.getContextPath())).getParentFile()
		            .toString(), "Redacteddownloads").toString();
		
		File dir = new File(folder);
		Boolean b = dir.mkdir();
		System.out.println("Creating download folder : " + folder + "was successful : " + b);
		return folder;
	}
	
	public static String ensureReportFolderExist(final HttpServletRequest request, final String reportType) {
		final String downloadFolder = ensureDownloadFolderExist(request);
		final String reportFolder = Paths.get(downloadFolder, reportType).toString();
		File dir = new File(reportFolder);
		dir.mkdir();
		System.out.println(reportType + " folder exist ? : " + dir.exists());
		final boolean b = false;
		final String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		final String todayFolders = Paths.get(reportFolder, dateString).toString();
		dir = new File(todayFolders);
		if (dir.exists()) {
			final File[] previousFiles = dir.listFiles();
			assert previousFiles != null;
			for (final File f : previousFiles) {
				System.out.println("deleted previous xml successfully ? " + b);
			}
			System.out.println("deleted previous folder successfully ? " + b);
		}
		dir.mkdir();
		System.out.println(todayFolders + " folder exist ? " + dir.exists());
		return todayFolders;
	}
	
	public static String ensureReportFolderExistDelete(final HttpServletRequest request, final String reportType) {
		final String downloadFolder = ensureDownloadFolderExist(request);
		final String reportFolder = Paths.get(downloadFolder, reportType).toString();
		File dir = new File(reportFolder);
		dir.mkdir();
		System.out.println(reportType + " folder exist ? : " + dir.exists());
		final String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		final String todayFolders = Paths.get(reportFolder, dateString).toString();
		dir = new File(todayFolders);
		if (dir.exists()) {
			final File[] previousFiles = dir.listFiles();
			assert previousFiles != null;
			for (final File f : previousFiles) {
				final boolean b = f.delete();
				System.out.println("deleted previous xml successfully ? " + b);
			}
			final boolean b = dir.delete();
			System.out.println("deleted previous folder successfully ? " + b);
		}
		dir.mkdir();
		System.out.println(todayFolders + " folder exist ? " + dir.exists());
		return todayFolders;
	}
	
}
