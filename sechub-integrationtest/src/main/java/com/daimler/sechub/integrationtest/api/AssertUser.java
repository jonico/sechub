// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class AssertUser extends AbstractAssert {

	private TestUser user;

	AssertUser(TestUser user) {
		this.user = user;
	}

	public AssertUser doesNotExist() {
		expectHttpClientError(HttpStatus.NOT_FOUND, () -> fetchUserDetails(), user.getUserId() + " found!");
		return this;
	}

	/**
	 * Asserts user does exist
	 * @return
	 */
	public AssertUser doesExist() {
		fetchUserDetails();// will fail with http error when not available
		return this;

	}

	public AssertUser isNotAssignedToProject(TestProject project) {
		if (internalIsAssignedToProject(project)) {
			fail("User " + user.getUserId() + " is assigned to project " + project.getProjectId());
		}
		return this;
	}

	public AssertUser isAssignedToProject(TestProject project) {
		if (!internalIsAssignedToProject(project)) {
			fail("User " + user.getUserId() + " is NOT assigned to project " + project.getProjectId());
		}
		return this;
	}

	boolean internalIsAssignedToProject(TestProject project) {
		String fetchUserDetails = fetchUserDetails();
		return isAssignedToProject(project, fetchUserDetails);
	}

	static boolean isAssignedToProject(TestProject project, String fetchedUserDetails) {
		return isInList(project, fetchedUserDetails,"projects");
	}

	public AssertUser isSuperAdmin() {
		assertTrue("Is not a super admin!", internalIsSuperAdmin());
		return this;
	}

	public AssertUser isInSuperAdminList() {
		assertTrue("Is not in super admin list!", internalIsInSuperAdminList());
		return this;
	}

	public AssertUser isNotInSuperAdminList() {
		assertFalse("Is in super admin list, but shouldn't!", internalIsInSuperAdminList());
		return this;
	}

	private boolean internalIsInSuperAdminList() {
		String adminList = fetchSuperAdminList();
		return adminList.contains("\""+user.getUserId()+"\"");
	}

	boolean internalIsSuperAdmin() {
		String fetchUserDetails = fetchUserDetails();
		return isSuperAdmin(fetchUserDetails);
	}


	public AssertUser isNotSuperAdmin() {
		assertFalse("Is a super admin!",internalIsSuperAdmin());
		return this;
	}

	static boolean isSuperAdmin(String fetchedUserDetails) {
		try {
			JsonNode json = JSONTestSupport.DEFAULT.fromJson(fetchedUserDetails);
			JsonNode superAdmin = json.get("superAdmin");
			return  superAdmin.asBoolean();
		} catch (IOException e) {
			throw new AssertionError("Was not able to parse json:"+fetchedUserDetails,e);
		}
		}

	static boolean isOwnerOfProject(TestProject project, String fetchedUserDetails) {
		return isInList(project, fetchedUserDetails,"ownedProjects");
	}
	static boolean isInList(TestProject project, String fetchedUserDetails, String listName) {
		try {
			JsonNode json = JSONTestSupport.DEFAULT.fromJson(fetchedUserDetails);
			JsonNode projects = json.get(listName);
			if (! projects.isArray()) {
				fail("not a array found!");
			}
			ArrayNode array = (ArrayNode) projects;
			Iterator<JsonNode> elements = array.elements();
			while (elements.hasNext()) {
				JsonNode element = elements.next();
				String text = element.asText();
				if (text.contentEquals(project.getProjectId())) {
					return true;
				}
			}
			return false;
		} catch (IOException e) {
			throw new AssertionError("Was not able to parse json:"+fetchedUserDetails,e);
		}
	}

	private String fetchUserDetails() {
		return getRestHelper().getJSon(getUrlBuilder().buildGetUserDetailsUrl(user.getUserId()));
	}

	private String fetchSuperAdminList() {
		return getRestHelper().getJSon(getUrlBuilder().buildAdminListsAdminsUrl());
	}

	/**
	 * Assert user cannot create project - but will also fail if the project exists
	 * before
	 *
	 * @param project
	 * @return this
	 */
	public AssertUser cannotCreateProject(TestProject project, String owner, HttpStatus errorStatus) {
		if (errorStatus == null) {
			errorStatus = HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesNotExist();

		expectHttpFailure(() -> as(user).createProject(project,owner),errorStatus);

		assertProject(project).doesNotExist();
		return this;
	}

	/**
	 * Asserts that the user can create given project. Will fail if the project does
	 * exist before, create project not possible or the project does not exist after
	 * call. After this is executed the project exists
	 *
	 * @param project
	 * @return this
	 */
	public AssertUser canCreateProject(TestProject project, String owner) {
		assertProject(project).doesNotExist();
		as(user).createProject(project,owner);
		assertProject(project).doesExist();

		return this;
	}

	/**
	 * Asserts that the user can assign targetUser to given project. Will fail if
	 * the project or target user does not exist before, or assignment is not
	 * possible.<br>
	 * <br>
	 * After this is executed the user is assigned to project or test fails
	 *
	 * @param targetUser
	 * @param project
	 * @return
	 */
	public AssertUser canAssignUserToProject(TestUser targetUser, TestProject project) {
		/* @formatter:off */
		assertProject(project).
			doesExist();
		assertUser(targetUser).
			doesExist().
			isNotAssignedToProject(project);

		as(this.user).
			assignUserToProject(targetUser, project);

		assertUser(targetUser).
			isAssignedToProject(project);
		/* @formatter:on */
		return this;
	}

	/**
	 * Asserts that the user can NOT assign targetUser to given project. Will fail if
	 * the project or target user does not exist before, or assignment is was
	 * possible.<br>
	 * <br>
	 * After this is executed the user is NOT assigned to project or test fails
	 *
	 * @param targetUser
	 * @param project
	 * @return
	 */
	public AssertUser canNotAssignUserToProject(TestUser targetUser, TestProject project, HttpStatus expectedError) {
		if (expectedError==null) {
			expectedError=HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesExist();
		assertUser(targetUser).doesExist().isNotAssignedToProject(project);

		expectHttpFailure(() -> as(user).assignUserToProject(targetUser, project), expectedError);

		assertUser(targetUser).isNotAssignedToProject(project);
		return this;
	}

	public AssertUser canAccessProjectInfo(TestProject project) {
		assertProject(project).doesExist();
		accessProjectInfo(project);
		return this;
	}

	private void accessProjectInfo(TestProject project) {
		as(user).getStringFromURL(getUrlBuilder().buildAdminFetchProjectInfoUrl(project.getProjectId()));
	}

	public AssertUser canNotListProject(TestProject project, HttpStatus expectedError) {
		if (expectedError==null) {
			expectedError=HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesExist();

		expectHttpFailure(() -> accessProjectInfo(project), expectedError);

		return this;

	}

	/**
	 * Creates a webscan job for project (but job is not started)
	 * @param project
	 * @return uuid for created job
	 */
	public UUID canCreateWebScan(TestProject project) {
		return canCreateWebScan(project,null);
	}

	/**
	 * Creates a webscan job for project (but job is not started)
	 * @param project
	 * @return uuid for created job
	 */
	public UUID canCreateAndApproveWebScan(TestProject project) {
		UUID jobUUID = canCreateWebScan(project,null);
		assertNotNull(jobUUID);
		canApproveJob(project, jobUUID);
		return jobUUID;
	}


	/**
	 * Creates a webscan job for project (but job is not started)
	 * @param project
	 * @param runModem mode to use
	 * @return uuid for created job
	 */
	public UUID canCreateWebScan(TestProject project, IntegrationTestMockMode runMode) {
		return TestAPI.as(user).createWebScan(project,runMode);
	}

	public AssertUser canNotCreateWebScan(TestProject project, HttpStatus expectedError) {
		if (expectedError==null) {
			expectedError=HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesExist();

		expectHttpFailure(() -> canCreateWebScan(project), expectedError);
		return this;
	}

	public AssertUser canGetStatusForJob(TestProject project, UUID jobUUID) {
		as(user).getJobStatus(project.getProjectId(), jobUUID);
		return this;

	}

	public AssertUser canApproveJob(TestProject project, UUID jobUUID) {
		as(user).approveJob(project, jobUUID);
		return this;
	}

	public AssertUser canNotApproveJob(TestProject project, UUID jobUUID) {
		expectHttpFailure(() -> canApproveJob(project,jobUUID), HttpStatus.NOT_FOUND);
		return this;
	}

	public AssertUser canNotGetStatusForJob(TestProject project, UUID jobUUID, HttpStatus expectedError) {
		if (expectedError==null) {
			expectedError=HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesExist();

		expectHttpFailure(() -> canGetStatusForJob(project,jobUUID), expectedError);
		return this;
	}

	public AssertUser canGetReportForJob(TestProject project, UUID jobUUID) {
		as(user).getJobReport(project.getProjectId(), jobUUID);
		return this;

	}

	public AssertUser canLogin() {
		as(user).getServerURL();
		return this;
	}


	public AssertUser canNotGetReportForJob(TestProject project, UUID jobUUID, HttpStatus expectedError) {
		if (expectedError==null) {
			expectedError=HttpStatus.FORBIDDEN;
		}
		assertProject(project).doesExist();

		expectHttpFailure(() -> canGetReportForJob(project,jobUUID), expectedError);
		return this;
	}

	public AssertUser canUploadSourceZipFile(TestProject project, UUID jobUUID, String pathInsideResources) {
		File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
		String checkSum = TestAPI.createSHA256Of(uploadFile);
		as(user).upload(project, jobUUID, uploadFile,checkSum);
		/* check if file is uploaded on server location */
		File downloadedFile = TestAPI.getFileUploaded(project,jobUUID,"sourcecode.zip");
		assertNotNull(downloadedFile);
		return this;
	}

	public AssertJobInformationAdministration onJobAdministration() {
		return new AssertJobInformationAdministration(user);
	}

	public AssertUser hasUserRole() {
		try {
			as(user).getStringFromURL(getUrlBuilder().buildCheckRoleUser());
		}catch(HttpClientErrorException e) {
			if (e.getRawStatusCode()==403) {
				fail("User has not user role or access layer has an error");
			}
			throw e;
		}
		return this;
	}
	public AssertUser hasOwnerRole() {
		try {
			as(user).getStringFromURL(getUrlBuilder().buildCheckRoleOwner());
		}catch(HttpClientErrorException e) {
			if (e.getRawStatusCode()==403) {
				fail("User has not owner role or access layer has an error");
			}
			throw e;
		}
		return this;
	}

	public AssertUser isOwnerOf(TestProject project) {
		assertProject(project).hasOwner(user);
		return this;
	}

	public AssertUser hasNotUserRole() {
		expectHttpFailure(() -> as(user).getStringFromURL(getUrlBuilder().buildCheckRoleUser()), HttpStatus.FORBIDDEN);
		return this;
	}
	public AssertUser hasNotOwnerRole() {
		expectHttpFailure(() -> as(user).getStringFromURL(getUrlBuilder().buildCheckRoleOwner()), HttpStatus.FORBIDDEN);
		return this;
	}
	public AssertUser isNotOwnerOf(TestProject project) {
		assertProject(project).hasNotOwner(user);
		return this;
	}

	public AssertUser canDownloadReportForJob(TestProject project, UUID jobUUID) {
		as(user).getStringFromURL(getUrlBuilder().buildGetJobReportUrl(project.getProjectId(), jobUUID));;
		return this;
	}

	public AssertUser canNotDownloadReportForJob(TestProject project, UUID jobUUID) {
		expectHttpFailure(() -> as(user).getStringFromURL(getUrlBuilder().buildGetJobReportUrl(project.getProjectId(), jobUUID)), HttpStatus.NOT_FOUND);
		return this;
	}

	public AssertUser canGrantSuperAdminRightsTo(TestUser targetUser) {
		as(user).grantSuperAdminRightsTo(targetUser);
		return this;
	}

	public AssertUser canNotGrantSuperAdminRightsTo(TestUser targetUser, HttpStatus expected) {
		expectHttpFailure(()-> as(user).grantSuperAdminRightsTo(targetUser), expected);
		return this;
	}

	public AssertUser canRevokeSuperAdminRightsTo(TestUser targetUser) {
		as(user).revokeSuperAdminRightsFrom(targetUser);
		return this;
	}

	public AssertUser canNotRevokeSuperAdminRightsFrom(TestUser targetUser, HttpStatus expected) {
		expectHttpFailure(()-> as(user).revokeSuperAdminRightsFrom(targetUser), expected);
		return this;
	}

	public AssertUser hasReceivedEmail(String subject) {
		AssertMail.assertMailExists(user.getEmail(),subject);
		return this;
	}





}
