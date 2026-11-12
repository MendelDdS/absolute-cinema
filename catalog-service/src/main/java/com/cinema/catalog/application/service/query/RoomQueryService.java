package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.port.in.query.RoomQueryUseCase;
import com.cinema.catalog.application.port.out.query.RoomQueryRepository;
import com.cinema.catalog.application.query.RoomView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomQueryService implements RoomQueryUseCase {

    private final RoomQueryRepository roomRepository;

    public RoomQueryService(RoomQueryRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<RoomView> list() {
        return roomRepository.findAll();
    }
}
