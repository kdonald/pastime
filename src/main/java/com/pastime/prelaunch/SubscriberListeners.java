package com.pastime.prelaunch;

import java.util.LinkedList;
import java.util.List;

public class SubscriberListeners implements SubscriberListener {

    private List<SubscriberListener> listeners = new LinkedList<SubscriberListener>();
    
    public void add(SubscriberListener listener) {
        this.listeners.add(listener);
    }
    
    public void subscriberAdded(Subscriber subscriber) {
        for (SubscriberListener listener : listeners) {
            listener.subscriberAdded(subscriber);
        }
    }

}
