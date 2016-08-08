package com.campaign.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class Campaign {
	@POST
	@Path("/ad")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response campaignPost(CampaingPojo cmpData) {

		PreparedStatement createPreparedStatement = null;
		PreparedStatement insertPreparedStatement = null;
		PreparedStatement updatePreparedStatement = null;
		Connection connection = null;
		CampaingPojo returnData = new CampaingPojo();

		String createCapaign = "CREATE TABLE CAMPAIGN(CAMPAIGN_ID VARCHAR(255)  NOT NULL, AD_CONTENT varchar(255), DURATION INT, ACTIVE INT, PARTNER_ID VARCHAR(255),CRT_TIME TIMESTAMP,  FOREIGN KEY(PARTNER_ID) REFERENCES PARTNER(PARTNER_ID))";
		String createPartner = "CREATE TABLE PARTNER (PARTNER_ID VARCHAR(255) PRIMARY KEY, PARTNER_NAME VARCHAR(255), CRT_TIME TIMESTAMP)";
		String insertPartner = "INSERT INTO PARTNER"
				+ "(PARTNER_ID, PARTNER_NAME, CRT_TIME) values"
				+ "(?,?,CURRENT_TIMESTAMP)";
		String insertCampaign = "INSERT INTO CAMPAIGN"
				+ "(CAMPAIGN_ID,  AD_CONTENT,DURATION, ACTIVE, PARTNER_ID, CRT_TIME) values"
				+ "(SELECT IFNULL(MAX(CAMPAIGN_ID),0)FROM CAMPAIGN +1,?,?,1,?,CURRENT_TIMESTAMP)";

		String updateCampaign = "UPDATE  CAMPAIGN SET ACTIVE=0 WHERE UPPER(PARTNER_ID) = UPPER(?) ";
		String selectPartner = "SELECT PARTNER_ID FROM PARTNER WHERE UPPER(PARTNER_ID) = UPPER(?) ";

		try {

			connection = DBUtil.getDBConnection();

			DatabaseMetaData dm = connection.getMetaData();

			String tbName = null;
			ResultSet res = dm.getTables(null, null, "PARTNER",
					new String[] { "TABLE" });

			while (res.next()) {
				tbName = res.getString("TABLE_NAME");
			}

			if (tbName == null || !tbName.equalsIgnoreCase("PARTNER")) {
				connection.setAutoCommit(false);
				createPreparedStatement = connection
						.prepareStatement(createPartner);
				createPreparedStatement.executeUpdate();
				createPreparedStatement.close();
				connection.commit();
				createPreparedStatement = connection
						.prepareStatement(createCapaign);
				createPreparedStatement.executeUpdate();
				createPreparedStatement.close();
				connection.commit();
			}

			connection.setAutoCommit(false);

			insertPreparedStatement = connection
					.prepareStatement(selectPartner);

			insertPreparedStatement.setString(1, cmpData.getPartner_id());

			ResultSet rs1 = insertPreparedStatement.executeQuery();
			String count = null;
			while (rs1.next()) {
				count = rs1.getString("PARTNER_ID");
			}
			insertPreparedStatement.close();

			if (count != null
					&& count.equalsIgnoreCase(cmpData.getPartner_id())) {

				updatePreparedStatement = connection
						.prepareStatement(updateCampaign);
				updatePreparedStatement.setString(1, cmpData.getPartner_id());
				updatePreparedStatement.executeUpdate();
				updatePreparedStatement.close();

				insertPreparedStatement = connection
						.prepareStatement(insertCampaign);
				insertPreparedStatement.setString(1, cmpData.getAd_content());
				insertPreparedStatement.setInt(2, cmpData.getDuration()
						.intValue());
				insertPreparedStatement.setString(3, cmpData.getPartner_id());
				insertPreparedStatement.executeUpdate();
				insertPreparedStatement.close();

				connection.commit();

			} else {

				insertPreparedStatement = connection
						.prepareStatement(insertPartner);
				insertPreparedStatement.setString(1, cmpData.getPartner_id());
				insertPreparedStatement.setString(2, cmpData.getPartner_id());
				insertPreparedStatement.executeUpdate();
				insertPreparedStatement.close();

				updatePreparedStatement = connection
						.prepareStatement(updateCampaign);
				updatePreparedStatement.setString(1, cmpData.getPartner_id());
				updatePreparedStatement.executeUpdate();
				updatePreparedStatement.close();

				insertPreparedStatement = connection
						.prepareStatement(insertCampaign);
				insertPreparedStatement.setString(1, cmpData.getAd_content());
				insertPreparedStatement.setInt(2, cmpData.getDuration()
						.intValue());
				insertPreparedStatement.setString(3, cmpData.getPartner_id());
				insertPreparedStatement.executeUpdate();
				insertPreparedStatement.close();

				connection.commit();

			}

		} catch (Exception e) {

			System.out.println("SQL Error " + e.getMessage());

			returnData.setError("Interna Error occurred please try again");

			return Response.status(501).entity(returnData).build();
		} finally {
			try {
				connection.rollback();
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//
		returnData
				.setSuccess("Successfully created Campaign for give partner ID");
		return Response.status(200).entity(returnData).build();
	}

	@Path("/ad/{partnerId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response campaignGET(@PathParam("partnerId") String partnerId) {
		String selectCampaign = "SELECT CRT_TIME, AD_CONTENT, DURATION FROM CAMPAIGN WHERE UPPER(PARTNER_ID) = UPPER(?) AND ACTIVE=1 ";

		PreparedStatement selectPreparedStatement = null;
		Connection connection = null;
		CampaingPojo camp = new CampaingPojo();
		String adContent = null;
		int duration = 0;
		java.sql.Timestamp timeStamp = null;

		try {

			connection = DBUtil.getDBConnection();
			selectPreparedStatement = connection
					.prepareStatement(selectCampaign);
			selectPreparedStatement.setString(1, partnerId);

			ResultSet rs = selectPreparedStatement.executeQuery();

			if (rs != null) {
				while (rs.next()) {

					timeStamp = rs.getTimestamp("CRT_TIME");
					adContent = rs.getString("AD_CONTENT");
					duration = rs.getInt("DURATION");
				}

				if (timeStamp != null) {

					DateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss:m");

					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, -duration);

					Date dt = dateFormat
							.parse(dateFormat.format(cal.getTime()));

					java.sql.Timestamp dt1 = new Timestamp(dt.getTime());

					if (dt1.before(timeStamp)) {

						camp.setPartner_id(partnerId);
						camp.setDuration(duration);
						camp.setAd_content(adContent);
						camp.setCreated(timeStamp);
						camp.setSuccess("SUCCESS");

					} else {

						camp.setPartner_id(partnerId);
						camp.setError("NO active ad campaign exists for this partner Id");

					}

				}

			} else {

				return Response.status(204)
						.entity("No data found based on this Parent ID")
						.build();
			}

			selectPreparedStatement.close();

		} catch (Exception e) {

			System.out.println("SQL Error ");
		} finally {

			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// return HTTP response 200 in case of success
		return Response.status(200).entity(camp).build();

	}

	@Path("/ad/all/{partnerId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<CampaingPojo> campaignGETAll(
			@PathParam("partnerId") String partnerId) {
		String selectCampaign = "SELECT CRT_TIME, AD_CONTENT, DURATION FROM CAMPAIGN WHERE UPPER(PARTNER_ID) = UPPER(?) ";

		PreparedStatement selectPreparedStatement = null;
		Connection connection = null;
		ArrayList<CampaingPojo> campaingList = new ArrayList<CampaingPojo>();

		try {

			connection = DBUtil.getDBConnection();
			selectPreparedStatement = connection
					.prepareStatement(selectCampaign);
			selectPreparedStatement.setString(1, partnerId);

			ResultSet rs = selectPreparedStatement.executeQuery();

			if (rs != null) {
				while (rs.next()) {
					CampaingPojo camp = new CampaingPojo();
					camp.setCreated(rs.getTimestamp("CRT_TIME"));
					camp.setAd_content(rs.getString("AD_CONTENT"));
					camp.setDuration(rs.getInt("DURATION"));
					campaingList.add(camp);

				}

			} else {

				CampaingPojo camp = new CampaingPojo();
				camp.setPartner_id(partnerId);
				camp.setError("No data found based on this Parent ID");
				campaingList.add(camp);

				return campaingList;
			}

			selectPreparedStatement.close();

		} catch (Exception e) {

			System.out.println("SQL Error ");
		} finally {

			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return campaingList;

		// return Response.status(200).entity(campaingList).build();

	}

}
