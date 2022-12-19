package com.barco.pipeline.util;

import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import java.io.*;

/**
 * @author Nabeel Ahmed
 */
@Component
public class EfsFileUtil {

    private Logger logger = LogManager.getLogger(EfsFileUtil.class);

    public EfsFileUtil() {}

    /**
     * Method use to retrieve the all folder in the base directory
     * @param basePath
     * @return File
     * */
    public File listFilesForFolder(String basePath) throws Exception {
        File file = new File(basePath);
        if (file.exists()) {
            return file;
        }
        throw new Exception(String.format("Wrong base path define folder not exit [%s] ", basePath));
    }

    /**
     * Method use to create the directory in the target path
     * @param basePath
     * @return boolean (true|false)
     * */
    public Boolean makeDir(String basePath) {
        try {
            File finalDir = new File(basePath);
            if (!finalDir.exists()) {
                logger.info("Making New Directory at path [ " + basePath + " ]");
                return finalDir.mkdirs();
            } else {
                logger.info("Directory Already Exist At Path [ " + basePath + " ]");
                return false;
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
        return false;
    }

    /**
     * Method use to save the file in the target directory-> folder
     * @param byteArrayOutputStream
     * @param basePathDire
     * @param targetFileName
     * @return 0
     * */
    public void saveFile(ByteArrayOutputStream byteArrayOutputStream, String basePathDire, String targetFileName) throws Exception {
        if (byteArrayOutputStream != null && byteArrayOutputStream.size() > 0) {
            try (OutputStream outputStream = new FileOutputStream(basePathDire.concat(targetFileName))) {
                byteArrayOutputStream.writeTo(outputStream);
            } finally {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            }
            logger.info("File Store into local path");
        }
    }

    /**
     * Method use to retrieve the file base on target filename
     * @param targetFileName
     * @return InputStream
     * */
    public InputStream getFile(String targetFileName) throws Exception {
        return new FileInputStream(targetFileName);
    }

    /**
     * Method use to delete the directory
     * @param basePath
     * */
    public void deleteDir(String basePath) {
        try {
            File file = new File(basePath);
            if (file.exists()) {
                logger.info("Deleting Directory At Path [ " + basePath + " ]");
                FileUtils.deleteDirectory(file);
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    /**
     * Method use to clean the directory
     * @param basePath
     * */
    public void cleanDir(String basePath) {
        try {
            File file = new File(basePath);
            if (file.exists()) {
                logger.info("Cleaning Directory At Path [ " + basePath + " ]");
                FileUtils.cleanDirectory(file);
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
