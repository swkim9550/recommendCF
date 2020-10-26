package com.enliple.recom3.common;

import java.io.File;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

public class CustomLoggerFactory {
	public static Logger createLoggerGivenFileName(String loggerName, String filepath , boolean clearFile) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		File logFile = new File(filepath);
		if(clearFile & logFile.exists()) {
			logFile.delete();
		}
		
		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
		rollingFileAppender.setFile(filepath);
		rollingFileAppender.setContext(lc);

		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setContext(lc);
		rollingPolicy.setParent(rollingFileAppender);
		rollingPolicy
				.setFileNamePattern(filepath + ".%i");
		rollingPolicy.start();

		SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
		triggeringPolicy.setMaxFileSize(FileSize.valueOf("100MB"));
		triggeringPolicy.start();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern("%d{HH:mm:ss.SSS},%msg%n");
		encoder.setContext(lc);
		encoder.start();

		rollingFileAppender.setEncoder(encoder);
		rollingFileAppender.setRollingPolicy(rollingPolicy);
		rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
		rollingFileAppender.start();

		Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
		logger.addAppender(rollingFileAppender);
		logger.setLevel(Level.INFO);
		logger.setAdditive(false);
		return logger;
	}
}