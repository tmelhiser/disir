package com.raveer.disir.listeners;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.raveer.disir.singletons.PropertiesContainer;

@WebListener
public class ScheduledJobs implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(ScheduledJobs.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
    	LOGGER.info("Starting Disir Cache Monitor");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(PropertiesContainer.INSTANCE.refreshNameSpaces(), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    	LOGGER.info("Shutting Down Disir Cache Monitor");
        scheduler.shutdownNow();
    }

}