package org.failuretest.failurecore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.*;


public class ModelVerificationHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ModelVerificationHelper.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static Map<String, TreeSet<LogAction.LogEntry>> loadLogFile() {
        Map<String, TreeSet<LogAction.LogEntry>> clientResult = new HashMap<>();
        try {
            // parse logs/log.txt, group by workers, for each worker sort by start time
            List<String> lines = IOUtils.readLines(new FileReader("logs/log.txt"));
            LOG.info("{}", lines);
            for (String line : lines) {
                LogAction.LogEntry logEntry = OBJECT_MAPPER.readValue(line, LogAction.LogEntry.class);
                LOG.info("{}", logEntry);
                clientResult.computeIfAbsent(
                        logEntry.getWorkerName(),
                        k -> new TreeSet<>(
                                new Comparator<LogAction.LogEntry>() {
                                    @Override
                                    public int compare(LogAction.LogEntry o1, LogAction.LogEntry o2) {
                                        return (int) (o1.getStartTime() - o2.getStartTime());
                                    }
                                }
                        )
                ).add(logEntry);
            }
        } catch (Exception e) {
            LOG.error("parse log error", e);
        }
        return clientResult;
    }

    /**
     * called it to show timeline chart optionally.
     */
    public static void showTimeLine() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        loadLogFile().forEach(
                (worker, results) -> {
                    for (LogAction.LogEntry logEntry : results) {
                        // generate string:
                        // [ 'client-0', 'QueryContactAction', new Date(1547870105648),  new Date(1547870108648) ]
                        StringBuilder stringBuilder = new StringBuilder();
                        String target = logEntry.getTarget();
                        if (target == null || target.equalsIgnoreCase("")) {
                            target = "";
                        } else if (!target.equalsIgnoreCase("")) {
                            target = "-" + target;
                        }
                        stringBuilder
                                .append("[")
                                .append("'" + worker + "',")
                                .append("'" + logEntry.getAction() + target + "',")
                                .append("new Date(" + logEntry.getStartTime() + "),")
                                .append("new Date(" + logEntry.getEndTime() + ")],");
                        builder.append(stringBuilder.toString());

                    }

                }
        );
        String jsonData = builder.append("]").toString();
        // load template
        try {
            String template = IOUtils.toString(
                    ModelVerificationHelper.class.getClassLoader().getResourceAsStream("template.html"),
                    "UTF-8"
            );
            String replaced = template.replace("to-be-replaced", jsonData);
            FileUtils.writeStringToFile(
                    new File("logs/result.html"),
                    replaced,
                    "UTF-8");
        } catch (Exception e) {
            LOG.error("process template error", e);
        }
        try {
            File htmlFile = new File("logs/result.html");
            java.awt.Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (Exception e) {
            LOG.error("open browser error", e);
        }
    }

}
