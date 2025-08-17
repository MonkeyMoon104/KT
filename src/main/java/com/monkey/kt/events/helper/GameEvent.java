package com.monkey.kt.events.helper;

public interface GameEvent {
    void endEvent(boolean timeExpired);
    long getRemainingTimeSeconds();
    String getRemainingTimeFormatted();
    String getEventName();
}

