package com.campusguide.personal.ai.atlas.streaming;

public interface StreamSubscription {
    String getSubscriptionId();
    String getSessionId();
    String getLastEventId();
    boolean isActive();
    void emitEvent(AtlasStreamEvent event);
    void emitError(Throwable error);
    void complete();
    void cancel();
}
