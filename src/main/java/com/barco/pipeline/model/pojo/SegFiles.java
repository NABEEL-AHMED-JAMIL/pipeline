package com.barco.pipeline.model.pojo;

import com.barco.pipeline.model.enums.FileStatus;
import com.barco.pipeline.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import javax.persistence.*;

/**
 * @author Nabeel Ahmed
 */
@Entity
@Table(name = "seg_files")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegFiles {

    @GenericGenerator(
        name = "segFilesSequenceGenerator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "seg_file_source_Seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @Id
    @Column(name = "file_id")
    @GeneratedValue(generator = "segFilesSequenceGenerator")
    private Long fileId;

    @Column(name = "folder_id", nullable = false)
    private Long folderId;

    @Column(name = "target_file_name")
    private String targetFileName;

    @Column(name = "target_file_size")
    private Long targetFileSize;

    @Column(name = "target_file_last_modified")
    private Long targetFileLastModified;

    @Column(name = "target_file_status")
    private FileStatus targetFileStatus;

    @Column(name = "ext_file_name")
    private String extFileName;

    @Column(name = "ext_file_size")
    private Long extFileSize;

    @Column(name = "ext_file_last_modified")
    private Long extFileLastModified;

    @Column(name = "status")
    private Status status;

    public SegFiles() {}

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public Long getTargetFileSize() {
        return targetFileSize;
    }

    public void setTargetFileSize(Long targetFileSize) {
        this.targetFileSize = targetFileSize;
    }

    public Long getTargetFileLastModified() {
        return targetFileLastModified;
    }

    public void setTargetFileLastModified(Long targetFileLastModified) {
        this.targetFileLastModified = targetFileLastModified;
    }

    public FileStatus getTargetFileStatus() {
        return targetFileStatus;
    }

    public void setTargetFileStatus(FileStatus targetFileStatus) {
        this.targetFileStatus = targetFileStatus;
    }

    public String getExtFileName() {
        return extFileName;
    }

    public void setExtFileName(String extFileName) {
        this.extFileName = extFileName;
    }

    public Long getExtFileSize() {
        return extFileSize;
    }

    public void setExtFileSize(Long extFileSize) {
        this.extFileSize = extFileSize;
    }

    public Long getExtFileLastModified() {
        return extFileLastModified;
    }

    public void setExtFileLastModified(Long extFileLastModified) {
        this.extFileLastModified = extFileLastModified;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
