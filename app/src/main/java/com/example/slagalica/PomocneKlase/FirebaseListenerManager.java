package com.example.slagalica.PomocneKlase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class FirebaseListenerManager {

    private List<Pair<DatabaseReference, ValueEventListener>> valueEventListeners = new ArrayList<>();
    private List<Pair<DatabaseReference, ChildEventListener>> childEventListeners = new ArrayList<>();

    public void addValueEventListener(DatabaseReference reference, ValueEventListener listener) {
        valueEventListeners.add(new Pair<>(reference, listener));
        reference.addValueEventListener(listener);
    }

    public void addChildEventListener(DatabaseReference reference, ChildEventListener listener) {
        childEventListeners.add(new Pair<>(reference, listener));
        reference.addChildEventListener(listener);
    }

    public void removeValueEventListener(DatabaseReference reference, ValueEventListener listener) {
        for (Pair<DatabaseReference, ValueEventListener> pair : valueEventListeners) {
            if (pair.first.equals(reference) && pair.second.equals(listener)) {
                reference.removeEventListener(listener);
                valueEventListeners.remove(pair);
                break;
            }
        }
    }

    public void removeChildEventListener(DatabaseReference reference, ChildEventListener listener) {
        for (Pair<DatabaseReference, ChildEventListener> pair : childEventListeners) {
            if (pair.first.equals(reference) && pair.second.equals(listener)) {
                reference.removeEventListener(listener);
                childEventListeners.remove(pair);
                break;
            }
        }
    }

    public void removeAllListeners() {
        for (Pair<DatabaseReference, ValueEventListener> pair : valueEventListeners) {
            pair.first.removeEventListener(pair.second);
        }
        valueEventListeners.clear();

        for (Pair<DatabaseReference, ChildEventListener> pair : childEventListeners) {
            pair.first.removeEventListener(pair.second);
        }
        childEventListeners.clear();

    }
}
