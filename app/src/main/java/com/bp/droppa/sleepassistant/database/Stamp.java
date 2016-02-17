package com.bp.droppa.sleepassistant.database;

/**
 * Created by Roman Droppa on 16.4.2015.
 */
/** Definuje znacky ukladane do DB*/
public class Stamp {

    private float x;
    private float y;
    private float z;
    private long date;
    private int count;

    public Stamp(float x, float y, float z, long date,int count) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.date = date;
        this.count = count;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getDate() {
        return date;
    }

    public int getCount() {
        return count;
    }
}
