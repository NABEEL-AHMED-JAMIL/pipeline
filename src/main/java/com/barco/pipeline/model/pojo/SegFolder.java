package com.barco.pipeline.model.pojo;

import com.barco.pipeline.model.enums.FileStatus;
import com.barco.pipeline.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author Nabeel Ahmed
 */
@Entity
@Table(name = "seg_folder")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegFolder {

    @GenericGenerator(
        name = "segFolderSequenceGenerator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "seg_folder_source_Seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @Id
    @Column(name = "folder_id")
    @GeneratedValue(generator = "segFolderSequenceGenerator")
    private Long folderId;

    @Column(name = "source_job_id")
    private Long sourceJobId;

    @Column(name = "pipeline_id")
    private Long pipelineId;

    @Column(name = "ext_folder_location", nullable = false)
    private String extFolderLocation;

    @Column(name = "target_folder_name", nullable = false)
    private String targetFolderName;

    @Column(name = "target_last_modified",nullable = false)
    private Long targetLastModified;

    @Column(name = "target_folder_total_files")
    private Long targetFolderTotalFiles;

    @Column(name = "targetFolder_valid_files")
    private Long targetFolderValidFiles;

    @Column(name = "target_folder_status")
    private FileStatus targetFolderStatus;

    @Column(name = "status")
    private Status status;

    @Column(name = "created_date")
    private Timestamp createdDate;

    public SegFolder() {}

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getSourceJobId() {
        return sourceJobId;
    }

    public void setSourceJobId(Long sourceJobId) {
        this.sourceJobId = sourceJobId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getExtFolderLocation() {
        return extFolderLocation;
    }

    public void setExtFolderLocation(String extFolderLocation) {
        this.extFolderLocation = extFolderLocation;
    }

    public String getTargetFolderName() {
        return targetFolderName;
    }

    public void setTargetFolderName(String targetFolderName) {
        this.targetFolderName = targetFolderName;
    }

    public Long getTargetLastModified() {
        return targetLastModified;
    }

    public void setTargetLastModified(Long targetLastModified) {
        this.targetLastModified = targetLastModified;
    }

    public Long getTargetFolderTotalFiles() {
        return targetFolderTotalFiles;
    }

    public void setTargetFolderTotalFiles(Long targetFolderTotalFiles) {
        this.targetFolderTotalFiles = targetFolderTotalFiles;
    }

    public Long getTargetFolderValidFiles() {
        return targetFolderValidFiles;
    }

    public void setTargetFolderValidFiles(Long targetFolderValidFiles) {
        this.targetFolderValidFiles = targetFolderValidFiles;
    }

    public FileStatus getTargetFolderStatus() {
        return targetFolderStatus;
    }

    public void setTargetFolderStatus(FileStatus targetFolderStatus) {
        this.targetFolderStatus = targetFolderStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
