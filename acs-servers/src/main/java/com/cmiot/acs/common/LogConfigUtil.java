package com.cmiot.acs.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by zjial on 2016/5/25.
 */
public class LogConfigUtil extends CommonUtil {

    public static void initLogBack(String fileLocation) {
        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            File externalConfigFile = new File(fileLocation);
            if (!externalConfigFile.exists()) {
                throw new IOException("Logback External Config File Parameter does not reference a file that exists");
            } else {
                if (!externalConfigFile.isFile()) {
                    throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
                } else {
                    if (!externalConfigFile.canRead()) {
                        throw new IOException("Logback External Config File exists and is a file, but cannot be read.");
                    } else {
                        JoranConfigurator configurator = new JoranConfigurator();
                        configurator.setContext(lc);
                        lc.reset();
                        configurator.doConfigure(fileLocation);
                        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
                    }
                }
            }
        } catch (Exception e) {

        }

    }
}
