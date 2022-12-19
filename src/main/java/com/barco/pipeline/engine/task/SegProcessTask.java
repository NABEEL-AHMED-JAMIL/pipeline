package com.barco.pipeline.engine.task;

import com.barco.pipeline.model.bean.CsvDataBean;
import com.barco.pipeline.model.dto.QueueMessageStatusDto;
import com.barco.pipeline.model.dto.SourceJobQueueDto;
import com.barco.pipeline.model.dto.SourceTaskDto;
import com.barco.pipeline.model.enums.FileStatus;
import com.barco.pipeline.model.enums.JobStatus;
import com.barco.pipeline.model.enums.Status;
import com.barco.pipeline.model.pojo.SegFiles;
import com.barco.pipeline.model.pojo.SegFolder;
import com.barco.pipeline.model.repository.SegFilesRepository;
import com.barco.pipeline.model.repository.SegFolderRepository;
import com.barco.pipeline.model.service.impl.LookupDataCacheService;
import com.barco.pipeline.util.ApiCaller;
import com.barco.pipeline.util.EfsFileUtil;
import com.barco.pipeline.util.PipelineUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.barco.pipeline.model.parser.ExtractionXmlParser;
import com.barco.pipeline.util.ExceptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Component
public class SegProcessTask implements Runnable {

    public Logger logger = LogManager.getLogger(SegProcessTask.class);

    private final String DASH = "-";
    private final String SLASH = "/";
    private final String COMMA = ",";
    private final String CSV_TYPE = "csv";
    private final String LOG_TYPE = "log";
    private final String SOURCE_JOB_ID = "{sourceJobId}";
    private final String TODAY_DATE = "{todayDate}";
    private final String PIPELINE_ID = "{pipelineId}";
    private final String CURRENT_FOLDER = "{currentFolder}";

    private Map<String, ?> data;
    @Autowired
    private ApiCaller apiCaller;
    @Autowired
    private EfsFileUtil efsFileUtil;
    @Autowired
    private SegFolderRepository segFolderRepository;
    @Autowired
    private SegFilesRepository segFilesRepository;
    @Autowired
    private LookupDataCacheService lookupDataCacheService;
    private ExtractionXmlParser extractionXmlParser;

    public SegProcessTask() { }

    public Map<String, ?> getData() {
        return data;
    }

    public void setData(Map<String, ?> data) {
        this.data = data;
    }

    @Override
    public void run() {
        // change the status into the running status
        SourceJobQueueDto jobQueue = (SourceJobQueueDto) this.getData().get(PipelineUtil.JOB_QUEUE);
        SourceTaskDto sourceTask = (SourceTaskDto) this.getData().get(PipelineUtil.TASK_DETAIL);
        try {
            // call api for send the status for running
            this.extractionXmlParser = PipelineUtil.extractionXmlParser(sourceTask.getTaskPayload());
            this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                new QueueMessageStatusDto(jobQueue.getJobId(), JobStatus.Running, jobQueue.getJobQueueId(), String.format("Job %s now in the running.",
                jobQueue.getJobId()), QueueMessageStatusDto.QUEUE_DETAIL));
            String validParserObject = this.extractionXmlParser.isValidParserObject();
            if (!PipelineUtil.isNull(validParserObject) && validParserObject.length() > 1) {
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobId(), JobStatus.Failed, jobQueue.getJobQueueId(), String.format("Job %s fail due to %s .",
                    jobQueue.getJobId(), "Pattern Not Valid"), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
                return;
            }
            File listFilesFolder = this.efsFileUtil.listFilesForFolder(this.extractionXmlParser.getExtractionFolder());
            List<SegFolder> segFolders = this.segFolderRepository.findAllSegFolderBySourceJobIdAndPipelineId(
                jobQueue.getJobId(), Long.valueOf(sourceTask.getPipelineId()));
            if (segFolders.isEmpty()) {
                this.processLogsToCsvFile(segFolders, listFilesFolder, this.extractionXmlParser, jobQueue, Long.valueOf(sourceTask.getPipelineId()));
            } else {
                List<String> localTargetFolderName = Arrays.asList(listFilesFolder.list());
                List<String> storeTargetFolderName = this.getStoreTargetFolderName(segFolders, false);
                storeTargetFolderName.removeAll(localTargetFolderName);
                if (!storeTargetFolderName.isEmpty()) {
                    this.segFolderRepository.updateFolderBySourceJobIdAndPipelineIdAndTargetFolderNameIn
                        (FileStatus.Delete.ordinal(), jobQueue.getJobId(), Long.valueOf(sourceTask.getPipelineId()), storeTargetFolderName);
                    this.segFilesRepository.updateFileBySourceJobIdAndPipelineIdAndTargetFolderNameIn
                        (FileStatus.Delete.ordinal(), jobQueue.getJobId(), Long.valueOf(sourceTask.getPipelineId()), storeTargetFolderName);
                    this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                        new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Folders %s deleting with sourceJobId[%d] Or pipelineId[%d]",
                        storeTargetFolderName, jobQueue.getJobId(), Long.valueOf(sourceTask.getPipelineId())), QueueMessageStatusDto.AUDIT_LOG));
                }
                this.processLogsToCsvFile(segFolders, listFilesFolder, this.extractionXmlParser, jobQueue, Long.valueOf(sourceTask.getPipelineId()));
            }
            this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                new QueueMessageStatusDto(jobQueue.getJobId(), JobStatus.Completed, jobQueue.getJobQueueId(), String.format("Job %s now complete.",
                jobQueue.getJobId()), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
            this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                new QueueMessageStatusDto(jobQueue.getJobId(), JobStatus.Failed, jobQueue.getJobQueueId(), String.format("Job %s fail due to %s .", jobQueue.getJobId(),
                ExceptionUtil.getRootCauseMessage(ex)), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
        }
    }

    /**
     * Method use to process the new file
     * @param folderFiles
     * @param extractionXmlParser
     * @param jobQueue
     * */
    private void processLogsToCsvFile(List<SegFolder> segFolders, File folderFiles, ExtractionXmlParser extractionXmlParser,
        SourceJobQueueDto jobQueue, Long pipelineId) {
        List<String> storeTargetFolderName = this.getStoreTargetFolderName(segFolders, true);
        for (File folderFile: folderFiles.listFiles()) {
            this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
               new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Current folder[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
               folderFile.getName(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
            Pattern pattern = Pattern.compile(extractionXmlParser.getExtractionFolderPattern());
            Matcher matcher = pattern.matcher(folderFile.getName());
            if (!matcher.matches()) {
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Pattern not match skipping folder[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                continue;
            }
            String writingLocation = extractionXmlParser.getTargetTaskFolderPattern()
                .replace(SOURCE_JOB_ID, String.valueOf(jobQueue.getJobId())).replace(TODAY_DATE, String.valueOf(LocalDate.now()))
                .replace(PIPELINE_ID, String.valueOf(pipelineId)).replace(CURRENT_FOLDER,folderFile.getName());
            writingLocation = extractionXmlParser.getTargetFolder() + writingLocation;
            if (!storeTargetFolderName.contains(folderFile.getName()) && this.efsFileUtil.makeDir(writingLocation)
                && (extractionXmlParser.getExtractionFileType().equals(LOG_TYPE) && extractionXmlParser.getTargetFileType().equals(CSV_TYPE)) ) {
                SegFolder segFolder =  this.createNewSegFolder(folderFile, jobQueue.getJobId(), pipelineId);
                segFolder.setTargetFolderValidFiles(this.getTotalValidFileCount(folderFile, extractionXmlParser));
                segFolder.setExtFolderLocation(writingLocation);
                this.segFolderRepository.save(segFolder);
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Current folder[%s] save into db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(),segFolder.getFolderId(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                this.logsToCsvFile(folderFile, segFolder, extractionXmlParser, jobQueue, pipelineId, false);
            } else if (extractionXmlParser.getExtractionFileType().equals(LOG_TYPE) && extractionXmlParser.getTargetFileType().equals(CSV_TYPE)) {
                SegFolder oldSegFolder = segFolders.stream().filter(pSegFolder -> pSegFolder.getSourceJobId().equals(jobQueue.getJobId())
                    && pSegFolder.getPipelineId().equals(pipelineId) && pSegFolder.getTargetFolderName()
                    .equals(folderFile.getName())).findFirst().orElse(null);
                if (!PipelineUtil.isNull(oldSegFolder) && (oldSegFolder.getTargetFolderStatus().equals(FileStatus.Delete) ||
                    oldSegFolder.getTargetFolderStatus().equals(FileStatus.New)) &&
                    oldSegFolder.getTargetLastModified().equals(folderFile.lastModified())) {
                    oldSegFolder.setTargetFolderStatus(FileStatus.OLD);
                    this.segFolderRepository.save(oldSegFolder);
                    this.segFilesRepository.updateFileBySourceJobIdAndPipelineIdAndTargetFolderName(FileStatus.OLD.ordinal(),
                        jobQueue.getJobId(), pipelineId, folderFile.getName());
                    this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                        new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Folder[%s] is old in db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                        oldSegFolder.getTargetFolderName(), oldSegFolder.getFolderId(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                } else if (!oldSegFolder.getTargetLastModified().equals(folderFile.lastModified())) {
                    this.efsFileUtil.makeDir(writingLocation);
                    oldSegFolder.setTargetFolderStatus(FileStatus.Modified);
                    oldSegFolder.setTargetLastModified(folderFile.lastModified());
                    oldSegFolder.setTargetFolderValidFiles(this.getTotalValidFileCount(folderFile, extractionXmlParser));
                    oldSegFolder.setExtFolderLocation(writingLocation);
                    oldSegFolder.setTargetFolderTotalFiles(Arrays.stream(folderFile.listFiles()).count());
                    this.segFolderRepository.save(oldSegFolder);
                    this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                        new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Folder[%s] is modified in db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                        oldSegFolder.getTargetFolderName(), oldSegFolder.getFolderId(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                    this.logsToCsvFile(folderFile, oldSegFolder, extractionXmlParser, jobQueue, pipelineId, true);
                }
            }
        }
    }

    private void logsToCsvFile(File folderFile, SegFolder segFolder, ExtractionXmlParser extractionXmlParser,
        SourceJobQueueDto jobQueue, Long pipelineId, boolean isModified) {
        if (isModified) {
            this.segFilesRepository.deleteFileBySourceJobIdAndPipelineIdAndFolderId(jobQueue.getJobId(), pipelineId, segFolder.getFolderId());
            this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("File deleting from db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                    segFolder.getFolderId(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
        }
        for (File logFile: folderFile.listFiles()) {
            try {
                Pattern pattern = Pattern.compile(extractionXmlParser.getExtractionFilePattern());
                Matcher matcher = pattern.matcher(logFile.getName());
                if (!matcher.matches()) {
                    this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                        new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Pattern not match skipping folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                        folderFile.getName(), logFile.getName(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                    continue;
                }
                // reading file detail log file start
                FileInputStream fileInputStream = new FileInputStream(logFile.getAbsoluteFile());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("File reading folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(), logFile.getName(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                String writerFileName = logFile.getName().replace(extractionXmlParser.getExtractionFileType(), extractionXmlParser.getTargetFileType());
                File csvFile = new File(segFolder.getExtFolderLocation()+SLASH+writerFileName);
                ICsvBeanWriter csvBeanWriter = new CsvBeanWriter(new FileWriter(csvFile), CsvPreference.STANDARD_PREFERENCE);
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("File writing folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    segFolder.getExtFolderLocation(), writerFileName, jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
                final String[] header = extractionXmlParser.getCsvFiled().stream().toArray(String[] ::new);
                final CellProcessor[] processors = getProcessors();
                csvBeanWriter.writeHeader(header);
                String strLine;
                boolean isDeviceAdded = false;
                while ((strLine = bufferedReader.readLine()) != null)   {
                    pattern = Pattern.compile(extractionXmlParser.getExtractionLinePattern());
                    matcher = pattern.matcher(strLine);
                    if (matcher.find()) {
                        strLine = strLine.substring(strLine.indexOf(extractionXmlParser.getExtractionLinePattern())+
                            extractionXmlParser.getExtractionLinePattern().length()).trim();
                        String colData[] = strLine.split(COMMA);
                        if (colData.length == 8) {
                            if (!isDeviceAdded) { isDeviceAdded = true; }
                            int index = 0;
                            CsvDataBean csvDataBean = new CsvDataBean(colData[index],colData[++index],colData[++index],colData[++index],
                                colData[++index],colData[++index],colData[++index],colData[++index]);
                            csvBeanWriter.write(csvDataBean, header, processors);
                        }
                    }
                }
                bufferedReader.close();
                if(!PipelineUtil.isNull(csvBeanWriter)) { csvBeanWriter.close(); }
                if (!isDeviceAdded) {
                    this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                        new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Current folder[%s]=>file[%s] not have any device deleting from storage",
                        segFolder.getExtFolderLocation(), writerFileName), QueueMessageStatusDto.AUDIT_LOG));
                    Optional<SegFolder> reduceSegFolderCount = this.segFolderRepository.findById(segFolder.getFolderId());
                    reduceSegFolderCount.get().setTargetFolderValidFiles(reduceSegFolderCount.get()
                        .getTargetFolderValidFiles() != 0 ? reduceSegFolderCount.get().getTargetFolderValidFiles() -1 : 0);
                    this.segFolderRepository.save(reduceSegFolderCount.get());
                    csvFile.delete();
                    continue;
                }
                SegFiles segFiles = new SegFiles();
                segFiles.setFolderId(segFolder.getFolderId());
                segFiles.setTargetFileName(logFile.getAbsolutePath());
                segFiles.setTargetFileSize(logFile.length());
                segFiles.setTargetFileLastModified(logFile.lastModified());
                if (isModified) {
                    segFiles.setTargetFileStatus(FileStatus.Modified);
                } else {
                    segFiles.setTargetFileStatus(FileStatus.New);
                }
                segFiles.setExtFileName(csvFile.getName());
                segFiles.setExtFileSize(csvFile.length());
                segFiles.setExtFileLastModified(csvFile.lastModified());
                segFiles.setStatus(Status.Active);
                this.segFilesRepository.save(segFiles);
                this.apiCaller.sendStatusEvent(this.lookupDataCacheService.getParentLookupById(this.extractionXmlParser.getJobStatusUrl()).getLookupValue(),
                    new QueueMessageStatusDto(jobQueue.getJobQueueId(), String.format("Current folder[%s]=>file[%s] save into db with fileId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                    segFolder.getExtFolderLocation(), writerFileName, segFiles.getFileId(), jobQueue.getJobId(), pipelineId), QueueMessageStatusDto.AUDIT_LOG));
            } catch (Exception ex) {
                logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
            }
        }
    }

    /**
     * Method use to get folder name from the db
     * @param segFolders
     * @param isNeedAll
     * @return List<String>
     * */
    private List<String> getStoreTargetFolderName(List<SegFolder> segFolders, boolean isNeedAll) {
        return segFolders.stream().filter(segFolder -> {
            return isNeedAll ? isNeedAll : !segFolder.getTargetFolderStatus().equals(FileStatus.Delete);
        }).map(segFolder -> {
           return segFolder.getTargetFolderName();
        }).collect(Collectors.toList());
    }

    /**
     * @return CellProcessor[]
     * */
    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
            new NotNull(), new NotNull(), new NotNull(), new NotNull(),
            new NotNull(), new NotNull(), new NotNull(), new NotNull()
        };
        return processors;
    }

    /**
     * Method use to create the new seg object for enter the new folder
     * @param file
     * @param sourceJobId
     * @param pipelineId
     * @return SegFolder
     * */
    private SegFolder createNewSegFolder(File file, Long sourceJobId, Long pipelineId) {
        SegFolder segFolder = new SegFolder();
        segFolder.setSourceJobId(sourceJobId);
        segFolder.setPipelineId(pipelineId);
        segFolder.setTargetFolderName(file.getName());
        segFolder.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        segFolder.setExtFolderLocation(file.getAbsolutePath());
        segFolder.setTargetLastModified(file.lastModified());
        segFolder.setTargetFolderTotalFiles(Arrays.stream(file.listFiles()).count());
        segFolder.setStatus(Status.Active);
        segFolder.setTargetFolderStatus(FileStatus.New);
        return segFolder;
    }

    /**
     * Method return valid file count in the folder
     * @param folderFiles
     * @param extractionXmlParser
     * @return Long
     * */
    private Long getTotalValidFileCount(File folderFiles, ExtractionXmlParser extractionXmlParser) {
        Long validFileCount = 0L;
        for (File folderFile: folderFiles.listFiles()) {
            Pattern pattern = Pattern.compile(extractionXmlParser.getExtractionFilePattern());
            Matcher matcher = pattern.matcher(folderFile.getName());
            if (!matcher.matches()) { continue; }
            validFileCount = validFileCount+1;
        }
        return validFileCount;
    }

}
