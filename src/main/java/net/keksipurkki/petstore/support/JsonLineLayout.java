package net.keksipurkki.petstore.support;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.Instant.ofEpochMilli;
import static java.util.Objects.isNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class JsonLineLayout extends LayoutBase<ILoggingEvent> {

    // Hard coding is the way to, especially in public cloud context
    private final static ZoneId LOGGING_TIMEZONE = ZoneId.of("Europe/Helsinki");
    private final static DateTimeFormatter LOGGING_TIMESTAMP_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(LOGGING_TIMEZONE);
    private final static String LINE_SEPARATOR = System.lineSeparator();

    private final static ThrowableProxyConverter converter = new ThrowableProxyConverter();

    private boolean prettyPrint = true;

    @Override
    public String doLayout(ILoggingEvent event) {
        var jsonl = JsonLine.from(event, VertxMDC.getAll());
        return Json.stringify(jsonl, prettyPrint) + LINE_SEPARATOR;
    }

    @Override
    public void start() {
        super.start();
        converter.start();
    }

    @Data
    private static class JsonLine implements JsonSerialization {

        private final JsonObject json = new JsonObject();

        private static final String MESSAGE = "message";
        private static final String LEVEL = "level";
        private static final String LOGGER = "logger";
        private static final String TIMESTAMP = "timestamp";
        private static final String STACKTRACE = "stackTrace";

        public static JsonLine from(ILoggingEvent event, Map<String, String> context) {
            var line = new JsonLine();
            line.json.put(MESSAGE, event.getFormattedMessage());
            line.json.put(LEVEL, event.getLevel().toString());
            line.json.put(LOGGER, event.getLoggerName());
            line.json.put(TIMESTAMP, LOGGING_TIMESTAMP_FORMAT.format(ofEpochMilli(event.getTimeStamp())));
            line.json.put(STACKTRACE, stackTraceFrom(event));
            line.json.mergeIn(JsonObject.mapFrom(context));
            return line;
        }

        @JsonValue
        public JsonObject toJson() {
            return json;
        }

        private static String[] stackTraceFrom(ILoggingEvent event) {
            if (isNull(event.getThrowableProxy())) {
                return null;
            }
            var stackTrace = converter.convert(event);
            return stackTrace.split("\n\t");
        }
    }

}
