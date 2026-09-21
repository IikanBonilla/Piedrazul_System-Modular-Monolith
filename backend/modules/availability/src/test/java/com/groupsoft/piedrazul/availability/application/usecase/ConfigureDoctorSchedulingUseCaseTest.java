package com.groupsoft.piedrazul.availability.application.usecase;

import com.groupsoft.piedrazul.availability.application.dto.DoctorSchedulingConfigDTO;
import com.groupsoft.piedrazul.availability.application.dto.TimeRangeDTO;
import com.groupsoft.piedrazul.availability.application.exception.InvalidSchedulingConfigException;
import com.groupsoft.piedrazul.availability.application.mapper.DoctorSchedulingConfigMapper;
import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorSchedulingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigureDoctorSchedulingUseCaseTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorSchedulingConfigRepository configRepository;

    private ConfigureDoctorSchedulingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfigureDoctorSchedulingUseCase(
                doctorRepository, configRepository, new DoctorSchedulingConfigMapper());
    }

    @Test
    void hu31_shouldSaveValidBookingWindowWeeks() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(configRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(1L, validRequest().bookingWindowWeeks(6).build());

        assertEquals(6, result.getBookingWindowWeeks());
        assertTrue(result.getMessage().toLowerCase().contains("guardada"));
        verify(configRepository).save(any(DoctorSchedulingConfig.class));
    }

    @Test
    void hu31_shouldRejectZeroOrNegativeWeeks() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        InvalidSchedulingConfigException zero = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, validRequest().bookingWindowWeeks(0).build())
        );
        assertEquals("INVALID_BOOKING_WINDOW", zero.getCode());

        InvalidSchedulingConfigException negative = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, validRequest().bookingWindowWeeks(-2).build())
        );
        assertEquals("INVALID_BOOKING_WINDOW", negative.getCode());
        verify(configRepository, never()).save(any());
    }

    @Test
    void hu32_shouldSaveSelectedWorkingDays() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(configRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        var result = useCase.execute(1L, validRequest().workingDays(days).build());

        assertEquals(days, result.getWorkingDays());
        ArgumentCaptor<DoctorSchedulingConfig> captor = ArgumentCaptor.forClass(DoctorSchedulingConfig.class);
        verify(configRepository).save(captor.capture());
        assertEquals(days, captor.getValue().getWorkingDays());
    }

    @Test
    void hu32_shouldRejectWhenNoWorkingDaysAreSelected() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        InvalidSchedulingConfigException ex = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, validRequest().workingDays(Set.of()).build())
        );

        assertEquals("NO_WORKING_DAYS", ex.getCode());
        verify(configRepository, never()).save(any());
    }

    @Test
    void hu33_shouldSaveMultipleValidTimeRanges() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(configRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(1L, validRequest().build());

        assertEquals(2, result.getTimeSlots().size());
        assertEquals(LocalTime.of(8, 0), result.getTimeSlots().get(0).getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getTimeSlots().get(0).getEndTime());
        assertEquals(LocalTime.of(14, 0), result.getTimeSlots().get(1).getStartTime());
    }

    @Test
    void hu33_shouldRejectWhenStartIsNotBeforeEnd() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        DoctorSchedulingConfigDTO request = validRequest()
                .timeSlots(List.of(TimeRangeDTO.builder()
                        .startTime(LocalTime.of(12, 0))
                        .endTime(LocalTime.of(8, 0))
                        .build()))
                .build();

        InvalidSchedulingConfigException ex = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, request)
        );

        assertEquals("INVALID_TIME_RANGE", ex.getCode());
        verify(configRepository, never()).save(any());
    }

    @Test
    void hu34_shouldSaveValidSlotInterval() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(configRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(1L, validRequest().slotIntervalMinutes(20).build());

        assertEquals(20, result.getSlotIntervalMinutes());
    }

    @Test
    void hu34_shouldRejectIntervalEqualOrLessThanZero() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        InvalidSchedulingConfigException zero = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, validRequest().slotIntervalMinutes(0).build())
        );
        assertEquals("INVALID_SLOT_INTERVAL", zero.getCode());

        InvalidSchedulingConfigException negative = assertThrows(
                InvalidSchedulingConfigException.class,
                () -> useCase.execute(1L, validRequest().slotIntervalMinutes(-15).build())
        );
        assertEquals("INVALID_SLOT_INTERVAL", negative.getCode());
        verify(configRepository, never()).save(any());
    }

    @Test
    void hu35_shouldPersistCompleteConfigurationAtomically() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(configRepository.findByDoctorId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorSchedulingConfigDTO request = validRequest()
                .bookingWindowWeeks(3)
                .workingDays(EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
                .slotIntervalMinutes(45)
                .build();

        var result = useCase.execute(1L, request);

        assertEquals(3, result.getBookingWindowWeeks());
        assertEquals(EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), result.getWorkingDays());
        assertEquals(45, result.getSlotIntervalMinutes());
        assertEquals(2, result.getTimeSlots().size());
        verify(configRepository, times(1)).save(any());
    }

    private DoctorSchedulingConfigDTO.DoctorSchedulingConfigDTOBuilder validRequest() {
        return DoctorSchedulingConfigDTO.builder()
                .bookingWindowWeeks(4)
                .workingDays(EnumSet.of(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))
                .timeSlots(List.of(
                        TimeRangeDTO.builder().startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build(),
                        TimeRangeDTO.builder().startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(18, 0)).build()
                ))
                .slotIntervalMinutes(30);
    }
}
