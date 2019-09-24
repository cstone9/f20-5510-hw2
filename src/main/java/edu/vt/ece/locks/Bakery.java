package edu.vt.ece.locks;

import edu.vt.ece.locks.Lock;

public class Bakery implements Lock {

    public Bakery() {
        this(2);
    }

    public Bakery(int n) {
    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }
}
