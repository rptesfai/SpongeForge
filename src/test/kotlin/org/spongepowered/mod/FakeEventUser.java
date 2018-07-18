package org.spongepowered.mod;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;

public class FakeEventUser<T extends Event> implements EventListener<T> {

    private final Class<T> eventClass;
    protected final EventListener<T> listener;

    public FakeEventUser(Class<T> eventClass, EventListener<T> listener) {
        this.eventClass = eventClass;
        this.listener = listener;
    }

    @Override
    public void handle(T event) throws Exception {

    }
}
