package com.groupsoft.piedrazul.availability.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "doctor_scheduling_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedulingConfig {

    public static final int DEFAULT_BOOKING_WINDOW_WEEKS = 4;
    public static final int DEFAULT_SLOT_INTERVAL_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false, unique = true)
    private Long doctorId;

    @Column(name = "booking_window_weeks", nullable = false)
    private int bookingWindowWeeks;

    @Column(name = "slot_interval_minutes", nullable = false)
    private int slotIntervalMinutes;

    @ElementCollection
    @CollectionTable(name = "doctor_working_days", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> workingDays = EnumSet.noneOf(DayOfWeek.class);

    @ElementCollection
    @CollectionTable(name = "doctor_time_ranges", joinColumns = @JoinColumn(name = "config_id"))
    @OrderColumn(name = "range_order")
    @Builder.Default
    private List<TimeRange> timeSlots = new ArrayList<>();

    public static DoctorSchedulingConfig defaultFor(Long doctorId) {
        List<TimeRange> ranges = new ArrayList<>();
        ranges.add(new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)));
        ranges.add(new TimeRange(LocalTime.of(14, 0), LocalTime.of(18, 0)));
        return DoctorSchedulingConfig.builder()
                .doctorId(doctorId)
                .bookingWindowWeeks(DEFAULT_BOOKING_WINDOW_WEEKS)
                .slotIntervalMinutes(DEFAULT_SLOT_INTERVAL_MINUTES)
                .workingDays(EnumSet.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY
                ))
                .timeSlots(ranges)
                .build();
    }
}
