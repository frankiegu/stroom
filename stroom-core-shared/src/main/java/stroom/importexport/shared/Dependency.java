package stroom.importexport.shared;

import stroom.docref.DocRef;
import stroom.docref.SharedObject;

public class Dependency implements SharedObject {
    private DocRef from;
    private DocRef to;
    private boolean ok;

    public Dependency() {
        // For GWT
    }

    public Dependency(final DocRef from, final DocRef to, final boolean ok) {
        this.from = from;
        this.to = to;
        this.ok = ok;
    }

    public DocRef getFrom() {
        return from;
    }

    public DocRef getTo() {
        return to;
    }

    public boolean isOk() {
        return ok;
    }
}
