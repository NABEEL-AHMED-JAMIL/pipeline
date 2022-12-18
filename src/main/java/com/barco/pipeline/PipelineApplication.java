package com.barco.pipeline;

import com.barco.pipeline.engine.async.executor.AsyncDALTaskExecutor;
import com.barco.pipeline.engine.task.SegProcessTask;
import com.barco.pipeline.util.PipelineUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class PipelineApplication {

	@Autowired
	private SegProcessTask segProcessTask;
	@Autowired
	private AsyncDALTaskExecutor asyncDALTaskExecutor;

	public static void main(String[] args) {
		SpringApplication.run(PipelineApplication.class, args);
	}

	/**
	 * Method run on the start to set the time
	 * zone for application Karachi
	 * */
	@PostConstruct
	public void started() {
		// default system timezone for application
		TimeZone.setDefault(TimeZone.getTimeZone(PipelineUtil.QATAR_TIME_ZONE));
		this.asyncDALTaskExecutor.addTask(this.segProcessTask, 1);
	}

}
