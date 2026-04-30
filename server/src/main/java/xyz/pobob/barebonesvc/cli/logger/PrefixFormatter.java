package xyz.pobob.barebonesvc.cli.logger;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PrefixFormatter extends Formatter {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String time = LocalTime.now().format(TIME_FORMAT);
        String level = record.getLevel().getName();

        return String.format(
                "[%s %s] %s%n",
                time,
                level,
                formatMessage(record)
        );
    }
}