package com.barco.pipeline.util;

import com.barco.pipeline.model.bean.SegFileBean;
import com.barco.pipeline.model.bean.SegFolderBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import java.io.FileReader;

public class CsvToFolder {

    public Logger logger = LogManager.getLogger(CsvToFolder.class);

    private String folderRootPath = "D:\\efs\\seg-folder";

    private CellProcessor[] getSegFolderProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
            new NotNull(), new NotNull(), new NotNull(),
        };
        return processors;
    }

    private CellProcessor[] getSegFileProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
            new NotNull()
        };
        return processors;
    }

    private void readWithCsvBeanReader(String filePath, String fileId) throws Exception {
        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(new FileReader(filePath), CsvPreference.STANDARD_PREFERENCE);
            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            CellProcessor[] processors = null;
            if (fileId.equals("1000")) {
                processors  = getSegFolderProcessors();
            } else if (fileId.equals("1001")) {
                processors = getSegFileProcessors();
            }
            if (fileId.equals("1000"))  {
                SegFolderBean segFolderBean;
                while( (segFolderBean = beanReader.read(SegFolderBean.class, header, processors)) != null ) {
                    //System.out.println(String.format("lineNo=%s, rowNo=%s, SegFolder=%s", beanReader.getLineNumber(), beanReader.getRowNumber(), segFolderBean));
                }
            } else if (fileId.equals("1001")) {
                SegFileBean segFileBean;
                while( (segFileBean = beanReader.read(SegFileBean.class, header, processors)) != null ) {
                    if (segFileBean.getKey().startsWith("S-")) {
                        logger.info(segFileBean.getKey());
                        //System.out.println(String.format("lineNo=%s, rowNo=%s, SegFile=%s", beanReader.getLineNumber(), beanReader.getRowNumber(), segFileBean));
                    }
                }
            }
        }
        finally {
            if( beanReader != null ) {
                beanReader.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CsvToFolder csvToFolder = new CsvToFolder();
        csvToFolder.readWithCsvBeanReader("C:\\Users\\97431\\Downloads\\bucket-folder.csv", "1000");
        csvToFolder.readWithCsvBeanReader("C:\\Users\\97431\\Downloads\\bucket-key.csv", "1001");
    }
}
