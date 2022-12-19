package com.barco.pipeline;

import com.barco.pipeline.util.PipelineUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class PipelineApplication {

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
	}

}
