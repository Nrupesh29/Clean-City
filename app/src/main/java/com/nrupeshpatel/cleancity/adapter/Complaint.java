package com.nrupeshpatel.cleancity.adapter;

public class Complaint {

    private boolean star;
    private int id;

    public Complaint() {

    }

    public Complaint(int id, boolean star) {
        this.id = id;
        this.star = star;
    }


    void setStar(boolean star) {
        this.star = star;
    }

    boolean getStar() {
        return this.star;
    }

    public int getId() {
        return this.id;
    }

}