package com.jannik_kuehn.common.storage.model;

import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;

import java.time.Duration;
import java.util.Objects;

/** Bounded request for canonical network statistics. */
public record StatisticsRequest(TimeRange range, TimeScope scope, Duration bounceThreshold) {
    public StatisticsRequest {
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(bounceThreshold, "bounceThreshold");
        if (bounceThreshold.isZero() || bounceThreshold.isNegative()) {
            throw new IllegalArgumentException("bounceThreshold must be positive");
        }
    }
}
