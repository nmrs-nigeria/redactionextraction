package org.openmrs.module.redactedextraction.fragment.controller;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.redactedextraction.Utils.Utils;
import org.openmrs.module.redactedextraction.db.NdrDBManager;
import org.openmrs.module.redactedextraction.model.ndr.*;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RedactedextractionHomeFragmentController {
	
	private static final Log LOG = LogFactory.getLog(RedactedextractionHomeFragmentController.class);
	
	Gson gson = new Gson();
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	SimpleDateFormat visitID = new SimpleDateFormat("yyyyMMdd");
	
	Container containerTemplate = null;
	
	NdrDBManager nd = new NdrDBManager();
	
	private String reportFolder;
	
	private List<Integer> list;
	
	String reportType = "RedactedExtraction";
	
	private String formattedDate;
	
	String dateFormat2;
	
	private ResultSet result;
	
	List<String> filesListInDir = new ArrayList<String>();
	
	public RedactedextractionHomeFragmentController() {
		this.dateFormat2 = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	public String extractRedacted(@RequestParam(value = "startdate", required = true) String startdate,
								  @RequestParam(value = "enddate", required = true) String enddate,
								  @RequestParam(value = "patientidentifiers", required = false) String patientidentifiers,
								  HttpServletRequest request) {
		List<String> outputList = new ArrayList<>();
		try {

			list = new ArrayList<>();
			Utils.ensureReportFolderExistDelete(request, reportType);
			nd.openConnection();
			list = nd.getPatientsWithVoidedVisitsList(startdate, enddate,patientidentifiers);
			if (this.list.isEmpty()) {
				this.nd.closeConnection();
				return this.gson.toJson("No record found");
			}
			getPatientRedactedContainer(startdate, enddate, request);


			String facilityName = Utils.getFacilityName();
			String IPShortName = Utils.getIPShortName();
			String datimCode = Utils.getFacilityDATIMId();
			String zipFileName = IPShortName + "_" + "Redactedextraction" + "_" + datimCode + "_" + formattedDate + ".zip";
			String filepath = Utils.zipFolder(request, reportFolder, zipFileName, reportType);

			outputList.add(zipFileName);
			outputList.add(dateFormat2);
			outputList.add(String.valueOf(list.size()));
			outputList.add(filepath);

		}catch (Exception e) {

			Logger.getLogger(RedactedextractionHomeFragmentController.class.getName()).log(Level.SEVERE, null, e);

		}finally {
			try{
				nd.closeConnection();
			}catch (Exception e){

			}
		}
		return gson.toJson(outputList);
	}
	
	public void getPatientRedactedContainer(String startdate, String enddate, HttpServletRequest request)
			throws Exception {
		String patientIdentifier = "";
		for (int i = 0; i < list.size(); i++) {
			System.out.println("getPatientRedactedContainer " + String.valueOf(list.get(i)));
			result = nd.getPatientVoidedVisits(startdate, enddate, Integer.parseInt(String.valueOf(list.get(i))));
			containerTemplate = new Container();

			XMLGregorianCalendar dataCaptured = null;
			Calendar cal = Calendar.getInstance();
			//NdrDBManager nd = new NdrDBManager();
			Utils u = new Utils();

			Date date = new Date();
			MessageHeaderType messageHeaderType = new MessageHeaderType();
			messageHeaderType.setMessageCreationDateTime(u.getXmlDateMessageHeader(cal.getTime()));
			messageHeaderType.setMessageUniqueID(UUID.randomUUID().toString());
			messageHeaderType.setMessageVersion(1.0f);
			messageHeaderType.setXmlType("REDACTED");

			MessageSendingOrganisationType messageSendingOrganisationType = new MessageSendingOrganisationType();
			messageSendingOrganisationType.setFacilityID(u.getFacilityDATIMId());
			messageSendingOrganisationType.setFacilityName(u.getFacilityName());
			messageSendingOrganisationType.setFacilityTypeCode(u.getFacilityType());

			messageHeaderType.setMessageSendingOrganisation(messageSendingOrganisationType);

			containerTemplate.setMessageHeader(messageHeaderType);
			patientIdentifier = nd.getPatientIdentifier(String.valueOf(list.get(i)));
			PatientDemographicsType patientDemographicsType = new PatientDemographicsType();
			patientDemographicsType.setPatientIdentifier(patientIdentifier);
			List <String> voidList = nd.checkIfPatientIsVoided(String.valueOf(list.get(i)));
			patientDemographicsType.setRedactedPatient((Integer.parseInt(voidList.get(0)) == 1) ? "YES" : "NO");
			patientDemographicsType.setRedactedPatientReason(voidList.get(1));


			containerTemplate.setEmrType("NMRS");
			List<RedactedVisitType> redactedVisitList = new ArrayList<>();
			while (result.next()) {
				System.out.println("Visit Id " + visitID.format(result.getDate("encounter_datetime")));

				RedactedVisitType redactedVisitType = new RedactedVisitType();
				redactedVisitType.setRedactedVisitReason(result.getString("void_reason"));
				redactedVisitType.setVisitID(visitID.format(result.getDate("encounter_datetime")));

				redactedVisitList.add(redactedVisitType);


							}

			patientDemographicsType.setRedactedVisit(redactedVisitList);
			containerTemplate.setPatientDemographics(patientDemographicsType);

			exportXML(patientIdentifier, request);

		}

	}
	
	private void writeFile(Container ndrReportTemplate, File file, Marshaller jaxbMarshaller) {
		
		try {
			//	javax.xml.validation.Validator validator = jaxbMarshaller.getSchema().newValidator();
			jaxbMarshaller.marshal(ndrReportTemplate, file);
			
		}
		catch (Exception ex) {
			System.out.println("File " + file.getName() + " throw an exception \n" + ex.getMessage());
			//	throw ex;
		}
		
	}
	
	public void exportXML(String PatientIdentifier, HttpServletRequest request) throws Exception {
		
		JAXBContext jaxbContext;
		String datimCode = Utils.getFacilityDATIMId();
		String IPShortName = Utils.getIPShortName();
		System.out.println("about to create jaxb context");
		// jaxbContext = JAXBContext.newInstance("org.openmrs.module.openhmis.ndrmodel");
		jaxbContext = JAXBContext.newInstance(Container.class);
		System.out.println("done creating jaxb context");
		System.out.println("about to create marshaller");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		System.out.println("done creating marshaller");
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		formattedDate = new SimpleDateFormat("ddMMyy").format(new Date());
		
		if (containerTemplate != null) {
			
			System.out.println("starting xml creating process");
			LOG.info("Testing log4j");
			reportFolder = Utils.ensureReportFolderExist(request, reportType);
			datimCode = datimCode.replace("/", "_");
			PatientIdentifier = PatientIdentifier.replace("/", "_").replace(".", "_");
			
			String fileName = IPShortName + "_" + "Redactedextraction" + "_" + datimCode + "_" + PatientIdentifier + "_"
			        + formattedDate;
			System.out.println("File name is " + fileName);
			
			String xmlFile = Paths.get(reportFolder, fileName + ".xml").toString();
			
			File aXMLFile = new File(xmlFile);
			
			Boolean b;
			
			b = aXMLFile.createNewFile();
			//System.out.println("creating xml file : " + xmlFile + "was successful : " + b);
			writeFile(containerTemplate, aXMLFile, jaxbMarshaller);
			
		}
	}
	
	private void zipDirectory(File dir, String zipDirName) {
		try {
			populateFilesList(dir);
			//now zip files one by one
			//create ZipOutputStream to write to the zip file
			FileOutputStream fos = new FileOutputStream(zipDirName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (String filePath : filesListInDir) {
				System.out.println("Zipping " + filePath);
				//for ZipEntry we need to keep only relative file path, so we used substring on absolute path
				ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
				zos.putNextEntry(ze);
				//read the file and write to ZipOutputStream
				FileInputStream fis = new FileInputStream(filePath);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
			fos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method populates all the files in a directory to a List
	 * 
	 * @param dir
	 * @throws IOException
	 */
	private void populateFilesList(File dir) throws IOException {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile())
				filesListInDir.add(file.getAbsolutePath());
			else
				populateFilesList(file);
		}
	}
	
	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
	
}
