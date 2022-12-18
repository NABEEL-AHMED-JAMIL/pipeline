package com.barco.pipeline.engine.task;

import com.barco.pipeline.model.bean.CsvDataBean;
import com.barco.pipeline.model.enums.FileStatus;
import com.barco.pipeline.model.enums.Status;
import com.barco.pipeline.model.pojo.SegFiles;
import com.barco.pipeline.model.pojo.SegFolder;
import com.barco.pipeline.model.repository.SegFilesRepository;
import com.barco.pipeline.model.repository.SegFolderRepository;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDate;
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
        //SourceJobQueueDto jobQueue = (SourceJobQueueDto) this.getData().get(PipelineUtil.JOB_QUEUE);
        //SourceTaskDto sourceTaskDto = (SourceTaskDto) this.getData().get(PipelineUtil.TASK_DETAIL);
        try {
            // call api for send the status for running
            ExtractionXmlParser extractionXmlParser = this.extractionXmlParser(xmlPayload());
            String validParserObject = extractionXmlParser.isValidParserObject();
            if (!PipelineUtil.isNull(validParserObject) && validParserObject.length() > 1) {
                // change the status into the fail status
                return;
            }
            File listFilesFolder = this.efsFileUtil.listFilesForFolder(extractionXmlParser.getExtractionFolder());
            List<SegFolder> segFolders = this.segFolderRepository.findAllSegFolderBySourceJobIdAndPipelineId(1077L, 1000L);
            if (segFolders.isEmpty()) {
                logger.info("No Previous seg folder exist");
                this.processLogsToCsvFile(segFolders, listFilesFolder, extractionXmlParser, 1077L, 1000L);
            } else {
                // file exit use below use case
                List<String> localTargetFolderName = Arrays.asList(listFilesFolder.list());
                // get those file which are not delete
                List<String> storeTargetFolderName = this.getStoreTargetFolderName(segFolders, false);
                storeTargetFolderName.removeAll(localTargetFolderName);
                if (!storeTargetFolderName.isEmpty()) {
                    // update file/folder status these deleted
                    this.segFolderRepository.updateFolderBySourceJobIdAndPipelineIdAndTargetFolderNameIn
                        (FileStatus.Delete.ordinal(),1077L, 1000L,storeTargetFolderName);
                    this.segFilesRepository.updateFileBySourceJobIdAndPipelineIdAndTargetFolderNameIn
                        (FileStatus.Delete.ordinal(),1077L, 1000L,storeTargetFolderName);
                    logger.info(String.format("Folders %s deleting with sourceJobId[%d] Or pipelineId[%d]",
                        storeTargetFolderName, 1077L, 1000L));
                }
                this.processLogsToCsvFile(segFolders, listFilesFolder, extractionXmlParser, 1077L, 1000L);
            }
            // change the status into the complete status
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
            // change the status into the fail status
        }
    }

    /**
     * Method use to process the new file
     * @param folderFiles
     * @param extractionXmlParser
     * @param sourceJobId
     * */
    private void processLogsToCsvFile(List<SegFolder> segFolders, File folderFiles,
        ExtractionXmlParser extractionXmlParser, Long sourceJobId, Long pipelineId) {
        List<String> storeTargetFolderName = this.getStoreTargetFolderName(segFolders, true);
        for (File folderFile: folderFiles.listFiles()) {
            logger.info(String.format("Current folder[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                folderFile.getName(), sourceJobId, pipelineId));
            Pattern pattern = Pattern.compile(extractionXmlParser.getExtractionFolderPattern());
            Matcher matcher = pattern.matcher(folderFile.getName());
            if (!matcher.matches()) {
                logger.info(String.format("Pattern not match skipping folder[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(), sourceJobId, pipelineId));
                // send the logs detail folder not match
                continue;
            }
            // for new file process below
            String writingLocation = extractionXmlParser.getTargetTaskFolderPattern()
                .replace(SOURCE_JOB_ID, String.valueOf(sourceJobId)).replace(TODAY_DATE, String.valueOf(LocalDate.now()))
                .replace(PIPELINE_ID, String.valueOf(pipelineId)).replace(CURRENT_FOLDER,folderFile.getName());
            writingLocation = extractionXmlParser.getTargetFolder() + writingLocation;
            if (!storeTargetFolderName.contains(folderFile.getName()) && this.efsFileUtil.makeDir(writingLocation)
                && (extractionXmlParser.getExtractionFileType().equals(LOG_TYPE) && extractionXmlParser.getTargetFileType().equals(CSV_TYPE)) ) {
                SegFolder segFolder =  this.createNewSegFolder(folderFile, sourceJobId, pipelineId);
                segFolder.setTargetFolderValidFiles(this.getTotalValidFileCount(folderFile, extractionXmlParser));
                segFolder.setExtFolderLocation(writingLocation);
                this.segFolderRepository.save(segFolder);
                logger.info(String.format("Current folder[%s] save into db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(),segFolder.getFolderId(), sourceJobId, pipelineId));
                this.logsToCsvFile(folderFile, segFolder, extractionXmlParser, sourceJobId, pipelineId, false);
            // for old file and deleted file process below
            } else if (extractionXmlParser.getExtractionFileType().equals(LOG_TYPE) && extractionXmlParser.getTargetFileType().equals(CSV_TYPE)) {
                // this case will check db folder match with the local folder and both have same size its mean it's not update
                SegFolder oldSegFolder = segFolders.stream().filter(pSegFolder -> pSegFolder.getSourceJobId().equals(sourceJobId)
                    && pSegFolder.getPipelineId().equals(pipelineId) && pSegFolder.getTargetFolderName()
                    .equals(folderFile.getName())).findFirst().orElse(null);
                // change the status to old
                if (!PipelineUtil.isNull(oldSegFolder) && (oldSegFolder.getTargetFolderStatus().equals(FileStatus.Delete) ||
                    oldSegFolder.getTargetFolderStatus().equals(FileStatus.New)) && oldSegFolder.getTargetLastModified()
                    .equals(folderFile.lastModified())) {
                    logger.info(String.format("Folder[%s] is old in db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                        oldSegFolder.getTargetFolderName(), oldSegFolder.getFolderId(), sourceJobId, pipelineId));
                    oldSegFolder.setTargetFolderStatus(FileStatus.OLD);
                    this.segFolderRepository.save(oldSegFolder);
                    this.segFilesRepository.updateFileBySourceJobIdAndPipelineIdAndTargetFolderName(
                        FileStatus.OLD.ordinal(), sourceJobId, pipelineId, folderFile.getName());
                } else { // modified case
                    this.efsFileUtil.makeDir(writingLocation);
                    oldSegFolder.setTargetFolderStatus(FileStatus.Modified);
                    oldSegFolder.setTargetLastModified(folderFile.lastModified());
                    oldSegFolder.setTargetFolderValidFiles(this.getTotalValidFileCount(folderFile, extractionXmlParser));
                    oldSegFolder.setExtFolderLocation(writingLocation);
                    oldSegFolder.setTargetFolderTotalFiles(Arrays.stream(folderFile.listFiles()).count());
                    this.segFolderRepository.save(oldSegFolder);
                    logger.info(String.format("Folder[%s] is modified in db with folderId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                        oldSegFolder.getTargetFolderName(), oldSegFolder.getFolderId(), sourceJobId, pipelineId));
                    this.logsToCsvFile(folderFile, oldSegFolder, extractionXmlParser, sourceJobId, pipelineId, true);
                }
            }
        }
    }

    private void logsToCsvFile(File folderFile, SegFolder segFolder, ExtractionXmlParser extractionXmlParser,
        Long sourceJobId, Long pipelineId, boolean isModified) {
        if (isModified) {
            this.segFilesRepository.deleteFileBySourceJobIdAndPipelineIdAndFolderId(
                sourceJobId, pipelineId, segFolder.getFolderId());
        }
        for (File logFile: folderFile.listFiles()) {
            try {
                Pattern pattern = Pattern.compile(extractionXmlParser.getExtractionFilePattern());
                Matcher matcher = pattern.matcher(logFile.getName());
                if (!matcher.matches()) {
                    logger.info(String.format("Pattern not match skipping folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                        folderFile.getName(), logFile.getName(), sourceJobId, pipelineId));
                    // send the logs detail file not match
                    continue;
                }
                // reading file detail log file start
                FileInputStream fileInputStream = new FileInputStream(logFile.getAbsoluteFile());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                logger.info(String.format("File reading folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    folderFile.getName(), logFile.getName(), sourceJobId, pipelineId));
                // writer file detail csv start
                String writerFileName = logFile.getName().replace(extractionXmlParser.getExtractionFileType(), extractionXmlParser.getTargetFileType());
                File csvFile = new File(segFolder.getExtFolderLocation()+SLASH+writerFileName);
                ICsvBeanWriter csvBeanWriter = new CsvBeanWriter(new FileWriter(csvFile), CsvPreference.STANDARD_PREFERENCE);
                logger.info(String.format("File writing folder[%s]=>file[%s] processing with sourceJobId[%d] Or pipelineId[%d]",
                    segFolder.getExtFolderLocation(), writerFileName, sourceJobId, pipelineId));
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
                            CsvDataBean csvDataBean = new CsvDataBean(colData[0],colData[1],colData[2],colData[3],
                                colData[4],colData[5],colData[6],colData[7]);
                            csvBeanWriter.write(csvDataBean, header, processors);
                        }
                    }
                }
                bufferedReader.close();
                if(!PipelineUtil.isNull(csvBeanWriter)) { csvBeanWriter.close(); }
                if (!isDeviceAdded) {
                    logger.info(String.format("Current folder[%s]=>file[%s] not have any device deleting from storage",
                        segFolder.getExtFolderLocation(), writerFileName));
                    // reduce the count also from the folder table
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
                logger.info(String.format("Current folder[%s]=>file[%s] save into db with fileId[%d] and sourceJobId[%d] Or pipelineId[%d]",
                    segFolder.getExtFolderLocation(), writerFileName, segFiles.getFileId(), sourceJobId, pipelineId));
            } catch (Exception ex) {
                logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
                // change the status into the fail status
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

    /**
     * Method use to convert the xml to pojo object
     * @param xmlPayload
     * @return ExtractionXmlParser
     * */
    private ExtractionXmlParser extractionXmlParser(String xmlPayload) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ExtractionXmlParser.class);
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();
        ExtractionXmlParser loopXmlParser = (ExtractionXmlParser) jaxbUnMarshaller.unmarshal(new StringReader(xmlPayload));
        return loopXmlParser;
    }

    private String xmlPayload() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<extractionXmlParser>\n" +
            "  <jobStatusUrl>JOB_STATUS_AND_LOG_API</jobStatusUrl>\n" +
            "  <extractionLinePattern>==&gt;&gt;&gt;&gt; Inpt line</extractionLinePattern>\n" +
            "  <extractionFolder>D:\\efs\\logs-folder</extractionFolder>\n" +
            "  <extractionFileType>log</extractionFileType>\n" +
            "  <targetFolder>D:\\efs\\csv-folder\\</targetFolder>\n" +
            "  <targetFileType>csv</targetFileType>\n" +
            "  <targetTaskFolderPattern>{sourceJobId}/{todayDate}-{pipelineId}/{currentFolder}</targetTaskFolderPattern>\n" +
            "  <extractionFolderPattern>^([0-9]{4}|[0-9]{2})[-]([0]?[1-9]|[1][0-2])[-]([0]?[1-9]|[1|2][0-9]|[3][0|1])$</extractionFolderPattern>\n" +
            "  <extractionFilePattern>^appnexus[-]([0-9]{4}|[0-9]{2})[-]([0]?[1-9]|[1][0-2])[-]([0]?[1-9]|[1|2][0-9]|[3][0|1])[-]([0-9]{4}|[0-9]{3}|[0-9]{2}|[0-9]{1}).log$</extractionFilePattern>\n" +
            "  <targetFolderPattern>^([0-9]{4}|[0-9]{2})[-]([0]?[1-9]|[1][0-2])[-]([0]?[1-9]|[1|2][0-9]|[3][0|1])[-]([0-9]{4})$</targetFolderPattern>\n" +
            "  <targetFilePattern>^appnexus[-]([0-9]{4}|[0-9]{2})[-]([0]?[1-9]|[1][0-2])[-]([0]?[1-9]|[1|2][0-9]|[3][0|1])[-]([0-9]{4}|[0-9]{3}|[0-9]{2}|[0-9]{1}).csv$</targetFilePattern>\n" +
            "  <csvFiled>deviceId</csvFiled>\n" +
            "  <csvFiled>deviceType</csvFiled>\n" +
            "  <csvFiled>driveByTime</csvFiled>\n" +
            "  <csvFiled>latitude</csvFiled>\n" +
            "  <csvFiled>longitude</csvFiled>\n" +
            "  <csvFiled>driveByTimeOut</csvFiled>\n" +
            "  <csvFiled>latitudeOut</csvFiled>\n" +
            "  <csvFiled>longitudeOut</csvFiled>\n" +
            "</extractionXmlParser>\n";
    }

}
