/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.redactedextraction.db;

import org.openmrs.module.redactedextraction.Utils.Utils;
import org.openmrs.module.redactedextraction.model.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NdrDBManager {
	
	private Statement presparent = null;
	
	public Connection conn = null;
	
	PreparedStatement pStatement1 = null;
	
	Statement pStatement = null;
	
	ResultSet rs = null;
	
	CallableStatement stmt = null;
	
	private ResultSet resultSet = null;
	
	private String sql;
	
	private ResultSet rs2 = null;
	
	public String parent = "";
	
	private Statement preschild = null;
	
	private Statement preschild2 = null;
	
	private Statement preschildparent = null;
	
	private Statement presnegativechild = null;
	
	public String jsonformat = "";
	
	public int gencount = 0;
	
	public NdrDBManager() {
		
	}
	
	public void openConnection() throws SQLException {
		DBConnection openmrsConn = Utils.getNmrsConnectionDetails();
		
		conn = DriverManager.getConnection(openmrsConn.getUrl(), openmrsConn.getUsername(), openmrsConn.getPassword());
	}
	
	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
			if (pStatement != null) {
				pStatement.close();
			}
			if (pStatement1 != null) {
				pStatement1.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (rs2 != null) {
				rs2.close();
			}
		}
		catch (Exception ex) {
			
		}
	}
	
	public List<Integer> getPatientsWithVoidedVisitsList(String startdate, String enddate, String patientartno) throws Exception {
		List<Integer> list = new ArrayList<>();


		try {

			if(!patientartno.equalsIgnoreCase("") || patientartno !=""){
				String[] patientartnoArray = patientartno.split(",");
				List <String> patientartnoArrayList = Arrays.asList(patientartnoArray);
				for(String patientidentifier: patientartnoArrayList){

					String sql2 = "SELECT distinct v.patient_id FROM encounter v join patient_identifier p on p.patient_id = v.patient_id where p.identifier_type =4 and v.voided = 1 AND DATE(v.encounter_datetime) between ? AND ?  AND p.identifier = ?";

					pStatement1 = conn.prepareStatement(sql2);
					pStatement1.setString(1, startdate);
					pStatement1.setString(2, enddate);
					pStatement1.setString(3, patientidentifier);

					rs2 = pStatement1.executeQuery();
					while (rs2.next()) {
						list.add(rs2.getInt("patient_id"));
					}
				}


			}else{
				String sql2 = "SELECT distinct patient_id FROM encounter where voided = 1 AND DATE(encounter_datetime) between ? and ? ";
				pStatement1 = conn.prepareStatement(sql2);
				pStatement1.setString(1, startdate);
				pStatement1.setString(2, enddate);

				rs2 = pStatement1.executeQuery();
				while (rs2.next()) {
					list.add(rs2.getInt("patient_id"));
				}
			}

//            String sql3 = "SELECT person_id FROM person where voided = 1";
//            pStatement1 = conn.prepareStatement(sql3);
//            rs2 = pStatement1.executeQuery();
//            while (rs2.next()) {
//                list.add(rs2.getInt("person_id"));
//            }



		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL Error" + e);
		}
		finally {
		}

		return list;
	}
	
	public ResultSet getPatientVoidedVisits(String startdate, String enddate, Integer patientid) throws Exception {
		
		try {
			String sql = "select * from `encounter` where voided = 1 AND patient_id = ? and DATE(encounter_datetime) between ? and ? group by date(encounter_datetime)";
			pStatement1 = conn.prepareStatement(sql);
			pStatement1.setInt(1, patientid);
			pStatement1.setString(2, startdate);
			pStatement1.setString(3, enddate);
			rs = pStatement1.executeQuery();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL Error" + e);
		}
		finally {}
		return rs;
	}
	
	public List<String> checkIfPatientIsVoided(String patientartno) throws Exception {
		List <String> voidlist = new ArrayList<>();
		try {
			String sql2 = "SELECT voided, void_reason FROM person where person_id = ?";
			pStatement1 = conn.prepareStatement(sql2);
			pStatement1.setInt(1, Integer.parseInt(patientartno));

			rs2 = pStatement1.executeQuery();
			while (rs2.next()) {

				voidlist.add(rs2.getString("voided"));
				voidlist.add(rs2.getString("void_reason"));
			}

		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL Error" + e);
		}
		finally {
		}

		return voidlist;
	}
	
	public String getPatientIdentifier(String patientartno) throws Exception {
		String identifier = "";
		try {
			String sql2 = "SELECT `identifier` FROM patient_identifier where identifier_type =4 AND patient_id = ?";
			pStatement1 = conn.prepareStatement(sql2);
			pStatement1.setInt(1, Integer.parseInt(patientartno));
			
			rs2 = pStatement1.executeQuery();
			while (rs2.next()) {
				identifier = rs2.getString("identifier");
			}
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQL Error" + e);
		}
		finally {}
		
		return identifier;
	}
	
}
