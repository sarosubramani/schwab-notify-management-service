package com.schwab.nms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableAsync
public class NmsApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(NmsApplication.class);

	public static void main(String[] args) {
        LOGGER.info("Enter: main");
        try {
            SpringApplication.run(NmsApplication.class, args);
            LOGGER.info("Exit: main");
        } catch (Exception e) {
            LOGGER.error("Error in main", e);
            throw e;
        }
    }

}
