package com.barco.pipeline.model.dto;

import com.barco.pipeline.model.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import java.time.LocalDateTime;

/**
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueMessageStatusDto {

    public final static String AUDIT_LOG = "AUDIT_LOG";
    public final static String QUEUE_DETAIL = "QUEUE_DETAIL";

    private Long jobId;
    private JobStatus jobStatus;
    private Long jobQueueId;
    private String logsDetail;
    private LocalDateTime endTime;
    private String messageType;

    public QueueMessageStatusDto() {}

    public QueueMessageStatusDto(Long jobQueueId, String logsDetail, String messageType) {
        this.jobQueueId = jobQueueId;
        this.logsDetail = logsDetail;
        this.messageType = messageType;
    }

    public QueueMessageStatusDto(Long jobId, JobStatus jobStatus,
                                 Long jobQueueId, String logsDetail, String messageType) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.jobQueueId = jobQueueId;
        this.logsDetail = logsDetail;
        this.messageType = messageType;
    }

    public QueueMessageStatusDto(Long jobId, JobStatus jobStatus, Long jobQueueId,
        String logsDetail, LocalDateTime endTime, String messageType) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.jobQueueId = jobQueueId;
        this.logsDetail = logsDetail;
        this.endTime = endTime;
        this.messageType = messageType;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Long getJobQueueId() {
        return jobQueueId;
    }

    public void setJobQueueId(Long jobQueueId) {
        this.jobQueueId = jobQueueId;
    }

    public String getLogsDetail() {
        return logsDetail;
    }

    public void setLogsDetail(String logsDetail) {
        this.logsDetail = logsDetail;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
