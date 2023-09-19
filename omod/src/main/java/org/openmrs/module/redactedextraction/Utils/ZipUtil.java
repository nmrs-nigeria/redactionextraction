/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.redactedextraction.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Tobechi
 */
public class ZipUtil {
	
	private List<String> fileList;
	
	private static String SOURCE_FOLDER;
	
	public ZipUtil(String sourceFolder) {
		fileList = new ArrayList<String>();
		SOURCE_FOLDER = sourceFolder;
	}
	
	/*public static void main(String[] args) {
		ZipUtil appZip = new ZipUtil();
		appZip.generateFileList(new File(SOURCE_FOLDER));
		appZip.zipIt(OUTPUT_ZIP_FILE);
	}*/
	
	public void zipIt(String outputZipFile) {
		final int size = 1024;
		byte[] buffer = new byte[size];
		String source = new File(SOURCE_FOLDER).getName();
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			fos = new FileOutputStream(outputZipFile);
			zos = new ZipOutputStream(fos);
			
			System.out.println("Output to Zip : " + outputZipFile);
			FileInputStream in = null;
			
			for (String file : this.fileList) {
				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				}
				finally {
					in.close();
				}
			}
			
			zos.closeEntry();
			System.out.println("Folder successfully compressed");
			
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		finally {
			try {
				zos.close();
			}
			catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public void generateFileList(File node) {
		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));
		}
		
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}
	
	private String generateZipEntry(String file) {
		return file.substring(SOURCE_FOLDER.length() + 1, file.length());
	}
}
