package com.lucky.arbaguette.substitute.dto.response;

import com.lucky.arbaguette.substitute.domain.Substitute;

public record SubstituteResponse(int substituteId,
                                 int scheduleId) {

    public static SubstituteResponse of(Substitute substitute) {
        return new SubstituteResponse(
                substitute.getSubstituteId(),
                substitute.getSchedule().getScheduleId()
        );
    }
}
