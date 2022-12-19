package com.barco.pipeline.util;

import com.barco.pipeline.model.dto.QueueMessageStatusDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Nabeel.amd
 */
@Component
@Scope("prototype")
public class ApiCaller {

    public Logger logger = LogManager.getLogger(ApiCaller.class);

    @Autowired
    private RestTemplate restTemplate;

    private ResponseEntity<?> response;

    public ResponseEntity<?> apiCaller(Class<?> responseType, Object body, HttpMethod httpMethod, String url) throws Exception {
        logger.info(">>>>>>> ApiCaller := apiCaller Start ");
        HttpHeaders headers = new HttpHeaders();
        this.response = this.restTemplate.exchange(url, httpMethod,
            new HttpEntity<>(!httpMethod.equals(HttpMethod.GET) ? body : null, headers), responseType);
        logger.debug("Response := ActiveStatus " + this.response.getStatusCode());
        logger.debug("Response := Body " + this.response.getBody());
        return this.response;
    }

    public ResponseEntity<?> apiCaller(Class<?> responseType, Object body, HttpMethod httpMethod, String url,
        Map<String, ?> headerMap) throws Exception {
        logger.info(">>>>>>> ApiCaller := apiCaller Start ");
        HttpHeaders headers = new HttpHeaders();
        this.fillHeader(headers, headerMap);
        this.response = this.restTemplate.exchange(url, httpMethod,
            new HttpEntity<>(!httpMethod.equals(HttpMethod.GET) ? body : null, headers), responseType);
        logger.debug("Response := ActiveStatus " + this.response.getStatusCode());
        logger.debug("Response := Body " + this.response.getBody());
        return this.response;
    }

    private void fillHeader(HttpHeaders headers, Map<String, ?> headerMap) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (headerMap != null && headerMap.size() > 0) {
            Iterator<? extends Entry<String, ?>> it = headerMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry pair = it.next();
                headers.add(String.valueOf(pair.getKey()), String.valueOf(pair.getValue()));
            }
        }
    }

    /**
     * Method use to send the event status
     * @param apiUrl
     * @param queueMessageStatus
     * */
    public void sendStatusEvent(String apiUrl, QueueMessageStatusDto queueMessageStatus) {
        try {
            logger.info("Api Caller Response :- " + this.apiCaller(Object.class, queueMessageStatus, HttpMethod.PUT, apiUrl));
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }
}