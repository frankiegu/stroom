package stroom.annotation.shared;

import java.util.List;

public class CreateEntryRequest {
    private Annotation annotation;
    private String type;
    private String data;
    private List<EventId> linkedEvents;

    public CreateEntryRequest() {
    }

    public CreateEntryRequest(final Annotation annotation,
                              final String type,
                              final String data) {
        this.annotation = annotation;
        this.type = type;
        this.data = data;
    }

    public CreateEntryRequest(final Annotation annotation, final String type, final String data, final List<EventId> linkedEvents) {
        this.annotation = annotation;
        this.type = type;
        this.data = data;
        this.linkedEvents = linkedEvents;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(final Annotation annotation) {
        this.annotation = annotation;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public List<EventId> getLinkedEvents() {
        return linkedEvents;
    }
}
