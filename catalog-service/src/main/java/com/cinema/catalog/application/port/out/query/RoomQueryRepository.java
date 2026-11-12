package com.cinema.catalog.application.port.out.query;

import com.cinema.catalog.application.query.RoomView;

import java.util.List;

public interface RoomQueryRepository {
    List<RoomView> findAll();
}
