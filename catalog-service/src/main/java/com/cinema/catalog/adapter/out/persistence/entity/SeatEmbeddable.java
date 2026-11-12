package com.cinema.catalog.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SeatEmbeddable {

    @Column(name = "seat_row", nullable = false, length = 1)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    protected SeatEmbeddable() {
    }

    public SeatEmbeddable(String seatRow, int seatNumber) {
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public int getSeatNumber() {
        return seatNumber;
    }
}
