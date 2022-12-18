package com.barco.pipeline.model.parser;

import com.barco.pipeline.util.PipelineUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Nabeel.amd
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtractionXmlParser {

    private String jobStatusUrl;
    private String extractionLinePattern;
    private String extractionFolder;
    private String extractionFileType;
    private String targetFolder;
    private String targetFileType;
    private String extractionFolderPattern;
    private String extractionFilePattern;
    private String targetFolderPattern;
    private String targetFilePattern;
    private String targetTaskFolderPattern;
    private List<String> csvFiled;

    public ExtractionXmlParser() {}

    @XmlElement
    public String getJobStatusUrl() {
        return jobStatusUrl;
    }

    public void setJobStatusUrl(String jobStatusUrl) {
        this.jobStatusUrl = jobStatusUrl;
    }

    @XmlElement
    public String getExtractionLinePattern() {
        return extractionLinePattern;
    }

    public void setExtractionLinePattern(String extractionLinePattern) {
        this.extractionLinePattern = extractionLinePattern;
    }

    @XmlElement
    public String getExtractionFolder() {
        return extractionFolder;
    }

    public void setExtractionFolder(String extractionFolder) {
        this.extractionFolder = extractionFolder;
    }

    @XmlElement
    public String getExtractionFileType() {
        return extractionFileType;
    }

    public void setExtractionFileType(String extractionFileType) {
        this.extractionFileType = extractionFileType;
    }

    @XmlElement
    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    @XmlElement
    public String getTargetFileType() {
        return targetFileType;
    }

    public void setTargetFileType(String targetFileType) {
        this.targetFileType = targetFileType;
    }

    public String getExtractionFolderPattern() {
        return extractionFolderPattern;
    }

    public void setExtractionFolderPattern(String extractionFolderPattern) {
        this.extractionFolderPattern = extractionFolderPattern;
    }

    public String getExtractionFilePattern() {
        return extractionFilePattern;
    }

    public void setExtractionFilePattern(String extractionFilePattern) {
        this.extractionFilePattern = extractionFilePattern;
    }

    public String getTargetFolderPattern() {
        return targetFolderPattern;
    }

    public void setTargetFolderPattern(String targetFolderPattern) {
        this.targetFolderPattern = targetFolderPattern;
    }

    public String getTargetFilePattern() {
        return targetFilePattern;
    }

    public void setTargetFilePattern(String targetFilePattern) {
        this.targetFilePattern = targetFilePattern;
    }

    public String getTargetTaskFolderPattern() {
        return targetTaskFolderPattern;
    }

    public void setTargetTaskFolderPattern(String targetTaskFolderPattern) {
        this.targetTaskFolderPattern = targetTaskFolderPattern;
    }

    public List<String> getCsvFiled() {
        return csvFiled;
    }

    public void setCsvFiled(List<String> csvFiled) {
        this.csvFiled = csvFiled;
    }

    public String isValidParserObject() {
        StringBuilder stringBuilder = new StringBuilder();
        if (PipelineUtil.isNull(jobStatusUrl)) {
            stringBuilder.append("Job Status Url Empty\n");
        }
        if (PipelineUtil.isNull(extractionFolder)) {
            stringBuilder.append("Extraction Folder Empty\n");
        }
        if (PipelineUtil.isNull(extractionFileType)) {
            stringBuilder.append("Extraction FileType Empty\n");
        }
        if (PipelineUtil.isNull(targetFolder)) {
            stringBuilder.append("Target Folder Empty\n");
        }
        if (PipelineUtil.isNull(targetFileType)) {
            stringBuilder.append("Target FileType Empty\n");
        }
        if (PipelineUtil.isNull(extractionFolderPattern)) {
            stringBuilder.append("Extraction FolderPattern Empty\n");
        }
        if (PipelineUtil.isNull(extractionFilePattern)) {
            stringBuilder.append("Extraction FilePattern Empty\n");
        }
        if (PipelineUtil.isNull(targetFolderPattern)) {
            stringBuilder.append("Target FolderPattern Empty\n");
        }
        if (PipelineUtil.isNull(targetTaskFolderPattern)) {
            stringBuilder.append("Target TaskFolderPattern Empty\n");
        }
        if (PipelineUtil.isNull(targetFilePattern)) {
            stringBuilder.append("Target FilePattern Empty\n");
        }
        if (PipelineUtil.isNull(csvFiled)) {
            stringBuilder.append("Target CsvFiled Empty\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
