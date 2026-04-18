package com.medtracker.observer;

public interface ReminderObserver {

    void update(String message);
}

package com.medtracker.observer;

import java.util.ArrayList;
import java.util.List;

public class ReminderSubject {

    private List<ReminderObserver> observers = new ArrayList<>();

    public void addObserver(ReminderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ReminderObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (ReminderObserver observer : observers) {
            observer.update(message);
        }
    }
}