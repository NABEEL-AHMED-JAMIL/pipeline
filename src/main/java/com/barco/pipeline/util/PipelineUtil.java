package com.barco.pipeline.util;


import com.barco.pipeline.model.parser.ExtractionXmlParser;
import org.apache.kafka.common.header.Headers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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

    /**
     * Method use to convert the xml to pojo object
     * @param xmlPayload
     * @return ExtractionXmlParser
     * */
    public static ExtractionXmlParser extractionXmlParser(String xmlPayload) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ExtractionXmlParser.class);
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();
        ExtractionXmlParser loopXmlParser = (ExtractionXmlParser) jaxbUnMarshaller.unmarshal(new StringReader(xmlPayload));
        return loopXmlParser;
    }

    public static boolean isNull(Object payload) {
        return payload == null || payload == "" ? true : false;
    }

}