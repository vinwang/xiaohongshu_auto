package com.xhs;

import com.xhs.browser.BrowserAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableScheduling
public class XhsAiPublisherApplication {

    @Autowired
    private BrowserAutomationService browserAutomationService;

    public static void main(String[] args) {
        SpringApplication.run(XhsAiPublisherApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("应用已启动，准备就绪！");
    }
}