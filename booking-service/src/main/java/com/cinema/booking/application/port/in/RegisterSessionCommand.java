package com.cinema.booking.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegisterSessionCommand (UUID sessionId, Instant startsAt, BigDecimal price, List<String> seatCodes) { }
