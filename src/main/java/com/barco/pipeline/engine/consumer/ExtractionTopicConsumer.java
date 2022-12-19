package com.barco.pipeline.engine.consumer;

import com.barco.pipeline.model.dto.QueueMessageStatusDto;
import com.barco.pipeline.model.dto.SourceJobQueueDto;
import com.barco.pipeline.model.dto.SourceTaskDto;
import com.barco.pipeline.engine.async.executor.AsyncDALTaskExecutor;
import com.barco.pipeline.engine.task.SegProcessTask;
import com.barco.pipeline.model.enums.JobStatus;
import com.barco.pipeline.model.parser.ExtractionXmlParser;
import com.barco.pipeline.util.ApiCaller;
import com.barco.pipeline.util.ExceptionUtil;
import com.barco.pipeline.util.PipelineUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nabeel Ahmed
 */
@Component
public class ExtractionTopicConsumer {

    public Logger logger = LogManager.getLogger(ExtractionTopicConsumer.class);

    @Autowired
    private ApiCaller apiCaller;
    @Autowired
    private SegProcessTask segProcessTask;
    @Autowired
    private AsyncDALTaskExecutor asyncDALTaskExecutor;

    private SourceJobQueueDto jobQueue;
    private SourceTaskDto sourceTask;
    private ExtractionXmlParser extractionXmlParser;

    /**
     * Consumer user to handle only extraction-topic source with all-partition * for test-topic
     * alter use can use the batch message consumer using the below one KafkaListener
     * */
    @KafkaListener(topics = "extraction-topic", clientIdPrefix = "string", groupId = "tpd-process",
        containerFactory = "kafkaListenerContainerFactory")
    public void extractionTopicConsumerListener(ConsumerRecord<String, String> consumerRecord, @Payload String payload) {
        try {
            Thread.sleep(1000);
            logger.info("ExtractionTopicConsumerListener [String] received key {}: Type [{}] | Payload: {} | Record: {}",
                consumerRecord.key(), PipelineUtil.typeIdHeader(consumerRecord.headers()), payload, consumerRecord.toString());
            JsonObject convertedObject = new Gson().fromJson(payload, JsonObject.class);
            Map<String, Object> taskPayloadInfo = new HashMap<>();
            this.jobQueue = new Gson().fromJson(convertedObject.get(PipelineUtil.JOB_QUEUE), SourceJobQueueDto.class);
            taskPayloadInfo.put(PipelineUtil.JOB_QUEUE,  jobQueue);
            this.sourceTask = new Gson().fromJson(convertedObject.get(PipelineUtil.TASK_DETAIL), SourceTaskDto.class);
            taskPayloadInfo.put(PipelineUtil.TASK_DETAIL, sourceTask);
            if (!PipelineUtil.isNull(sourceTask.getPipelineId()) &&
                PipelineUtil.SEGMENT_PROCESS_ANALYTICS.equals(sourceTask.getPipelineId())) {
                this.segProcessTask.setData(taskPayloadInfo);
                this.asyncDALTaskExecutor.addTask(this.segProcessTask, convertedObject.get(PipelineUtil.PRIORITY).getAsInt());
            } else {
                // call api with fail status
                this.apiCaller.sendStatusEvent(this.extractionXmlParser.getJobStatusUrl(), new QueueMessageStatusDto(jobQueue.getJobId(),
                    JobStatus.Failed, jobQueue.getJobQueueId(), String.format("Job %s fail due to %s .", jobQueue.getJobId(),
                    "No Pipeline id found"), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
            }
        } catch (InterruptedException ex) {
            logger.error("Exception in ExtractionTopicConsumerListener ", ExceptionUtil.getRootCauseMessage(ex));
            this.apiCaller.sendStatusEvent(this.extractionXmlParser.getJobStatusUrl(), new QueueMessageStatusDto(jobQueue.getJobId(),
                JobStatus.Failed, jobQueue.getJobQueueId(), String.format("Job %s fail due to %s .", jobQueue.getJobId(),
                ExceptionUtil.getRootCauseMessage(ex)), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
        } catch (Exception ex) {
            logger.error("Exception in ExtractionTopicConsumerListener ", ExceptionUtil.getRootCauseMessage(ex));
            this.apiCaller.sendStatusEvent(this.extractionXmlParser.getJobStatusUrl(), new QueueMessageStatusDto(jobQueue.getJobId(),
                JobStatus.Failed, jobQueue.getJobQueueId(), String.format("Job %s fail due to %s .", jobQueue.getJobId(),
                ExceptionUtil.getRootCauseMessage(ex)), LocalDateTime.now(), QueueMessageStatusDto.QUEUE_DETAIL));
        }
    }

}
