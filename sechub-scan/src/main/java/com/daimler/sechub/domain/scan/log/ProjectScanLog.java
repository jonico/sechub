// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * Represents a mapping between a scan, job and and job configuration. So its an
 * INTERNAL information
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProjectScanLog.TABLE_NAME)
public class ProjectScanLog {

	/* +-----------------------------------------------------------------------+ */
	/* +............................ SQL ......................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String TABLE_NAME = "SCAN_PROJECT_LOG";

	public static final String COLUMN_UUID = "UUID";
	public static final String COLUMN_PROJECTID = "PROJECT_ID";
	public static final String COLUMN_EXECUTED_BY = "EXECUTED_BY";
	public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
	public static final String COLUMN_CONFIG = "CONFIG";
	public static final String COLUMN_STATUS = "STATUS";

	public static final String COLUMN_STARTED = "STARTED";
	public static final String COLUMN_ENDED = "ENDED";
	/* +-----------------------------------------------------------------------+ */
	/* +............................ JPQL .....................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String CLASS_NAME = ProjectScanLog.class.getSimpleName();

	public static final String PROPERTY_UUID = "uuid";
	public static final String PROPERTY_SECHUB_JOB_UUID = "sechubJobUUID";
	public static final String PROPERTY_EXECUTED_BY = "executedBy";
	public static final String PROPERTY_PROJECT_ID = "projectId";
	public static final String PROPERTY_STARTED = "started";
	public static final String PROPERTY_ENDED = "ended";
	public static final String PROPERTY_STATUS = "status";

	public static final String STATUS_STARTED = "STARTED";
	public static final String STATUS_OK = "OK";
	public static final String STATUS_FAILED = "FAILED";

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = COLUMN_UUID, updatable = false, nullable = false)
	UUID uUID;

	@Column(name = COLUMN_EXECUTED_BY)
	String executedBy;

	@Column(name = COLUMN_PROJECTID, nullable = false)
	String projectId;

	@Column(name = COLUMN_SECHUB_JOB_UUID, nullable = false)
	UUID sechubJobUUID;

	@Type(type = "text")
	@Column(name = COLUMN_CONFIG)
	String config;

	@Column(name = COLUMN_STATUS)
	String status;

	@Version
	@Column(name = "VERSION")
	Integer version;

	@Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime started;

	@Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime ended;

	ProjectScanLog() {
		// jpa only
	}

	public ProjectScanLog(String projectId, UUID sechubJobUUID, String executedBy, String config) {
		this.projectId = projectId;
		this.sechubJobUUID = sechubJobUUID;
		this.executedBy = executedBy;
		this.config = config;

		this.started = LocalDateTime.now();
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getConfig() {
		return config;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setEnded(LocalDateTime ended) {
		this.ended = ended;
	}

	public LocalDateTime getEnded() {
		return ended;
	}

	public UUID getUUID() {
		return uUID;
	}

	public UUID getSechubJobUUID() {
		return sechubJobUUID;
	}

	public String getExecutedBy() {
		return executedBy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(config, ended, executedBy, projectId, sechubJobUUID, started, uUID, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectScanLog other = (ProjectScanLog) obj;
		return Objects.equals(config, other.config) && Objects.equals(ended, other.ended) && Objects.equals(executedBy, other.executedBy)
				&& Objects.equals(projectId, other.projectId) && Objects.equals(sechubJobUUID, other.sechubJobUUID) && Objects.equals(started, other.started)
				&& Objects.equals(uUID, other.uUID) && Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		return "ProjectScanLog [\nuUID=" + uUID + ", \nexecutedBy=" + executedBy + ", \nprojectId=" + projectId + ", \nsechubJobUUID=" + sechubJobUUID
				+ ", \nstatus=" + status + ", \nstarted=" + started + ", \nended=" + ended + ", \nconfig=" + config + "\n]";
	}

}
