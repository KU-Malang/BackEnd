package server.enums;

import java.util.HashMap;
import java.util.Map;

public enum Topic {

    CULTURE("CULTURE"),
    HISTORY("HISTORY"),
    CURRENT_AFFAIRS("CURRENT_AFFAIRS"),
    GEOGRAPHY("GEOGRAPHY"),
    COMPUTER("COMPUTER"),
    GENERAL_KNOWLEDGE("GENERAL_KNOWLEDGE"),
    PRACTICE("PRACTICE"),
    REDEMPTION("REDEMPTION"),

    ;

    private final String value;

    private static final Map<String, Topic> TOPIC_MAP = new HashMap<>();

    static {
        for (Topic topic : Topic.values()) {
            TOPIC_MAP.put(topic.getValue(), topic);
        }
    }

    Topic(String value) {
        this.value = value;
    }

    public static Topic fromValue(String value) {
        return TOPIC_MAP.get(value);
    }

    public String getValue() {
        return value;
    }
}
