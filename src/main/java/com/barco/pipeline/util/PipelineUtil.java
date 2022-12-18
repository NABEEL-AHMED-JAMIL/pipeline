package com.barco.pipeline.util;


import org.apache.kafka.common.header.Headers;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.StreamSupport;

/**
 * @author Nabeel Ahmed
 */
public class PipelineUtil {

    public final static String JOB_QUEUE = "jobQueue";
    public final static String TASK_DETAIL = "taskDetail";
    public final static String PRIORITY = "Priority";
    public final static String SEGMENT_PROCESS_ANALYTICS = "1000";
    public static String QATAR_TIME_ZONE = "Asia/Qatar";
    public static String typeIdHeader(Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false)
            .filter(header -> header.key().equals("__TypeId__"))
            .findFirst().map(header -> new String(header.value())).orElse("N/A");
    }

    public static boolean isNull(Object payload) {
        return payload == null || payload == "" ? true : false;
    }

}