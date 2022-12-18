package com.barco.pipeline.model.dto;

import com.barco.pipeline.model.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

/**
 * @author Nabeel Ahmed
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceJobQueueDto {

    private Long jobQueueId;
    private JobStatus jobStatus;
    private Long jobId;
    private String jobName;

    public SourceJobQueueDto() {}

    public Long getJobQueueId() {
        return jobQueueId;
    }

    public void setJobQueueId(Long jobQueueId) {
        this.jobQueueId = jobQueueId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
