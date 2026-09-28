import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AvailabilityService,
  DoctorDTO,
  DoctorSchedulingConfigDTO,
  TimeRangeDTO
} from '../../../core/services/availability/service';

interface WeekDayOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-scheduling-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './component.html'
})
export class SchedulingConfigComponent implements OnInit {
  readonly weekDays: WeekDayOption[] = [
    { value: 'MONDAY', label: 'Lunes' },
    { value: 'TUESDAY', label: 'Martes' },
    { value: 'WEDNESDAY', label: 'Miercoles' },
    { value: 'THURSDAY', label: 'Jueves' },
    { value: 'FRIDAY', label: 'Viernes' },
    { value: 'SATURDAY', label: 'Sabado' },
    { value: 'SUNDAY', label: 'Domingo' }
  ];

  doctors: DoctorDTO[] = [];
  selectedDoctorId: number | null = null;
  bookingWindowWeeks: number | null = 4;
  selectedDays: Record<string, boolean> = {};
  timeSlots: TimeRangeDTO[] = [
    { startTime: '08:00', endTime: '12:00' },
    { startTime: '14:00', endTime: '18:00' }
  ];
  slotIntervalMinutes: number | null = 30;
  loading = false;
  formError = '';
  fieldErrors: string[] = [];
  successMessage = '';

  constructor(private availabilityService: AvailabilityService) {}

  ngOnInit() {
    this.weekDays.forEach((day) => {
      this.selectedDays[day.value] = day.value !== 'SUNDAY';
    });
    this.availabilityService.getDoctors().subscribe({
      next: (doctors) => { this.doctors = doctors; }
    });
  }

  onDoctorChange() {
    this.formError = '';
    this.successMessage = '';
    this.fieldErrors = [];
    if (!this.selectedDoctorId) {
      return;
    }
    this.availabilityService.getSchedulingConfig(this.selectedDoctorId).subscribe({
      next: (config) => this.applyConfig(config),
      error: (err) => {
        this.formError = err.error?.message || 'No se pudo cargar la configuracion.';
      }
    });
  }

  addTimeSlot() {
    this.timeSlots = [...this.timeSlots, { startTime: '08:00', endTime: '12:00' }];
  }

  removeTimeSlot(index: number) {
    this.timeSlots = this.timeSlots.filter((_, i) => i !== index);
    if (this.timeSlots.length === 0) {
      this.addTimeSlot();
    }
  }

  save() {
    this.formError = '';
    this.successMessage = '';
    this.fieldErrors = [];

    if (!this.selectedDoctorId) {
      this.formError = 'Debe seleccionar un medico o terapista.';
      return;
    }

    const errors = this.validate();
    if (errors.length > 0) {
      this.fieldErrors = errors;
      this.formError = errors[0];
      return;
    }

    const payload: DoctorSchedulingConfigDTO = {
      bookingWindowWeeks: Number(this.bookingWindowWeeks),
      workingDays: this.selectedWorkingDays(),
      timeSlots: this.timeSlots.map((slot) => ({
        startTime: this.normalizeTime(slot.startTime),
        endTime: this.normalizeTime(slot.endTime)
      })),
      slotIntervalMinutes: Number(this.slotIntervalMinutes)
    };

    this.loading = true;
    this.availabilityService
      .saveSchedulingConfig(this.selectedDoctorId, payload)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Configuracion de agendamiento guardada correctamente.';
          this.applyConfig(response);
        },
        error: (err) => {
          this.formError = err.error?.message || 'No se pudo guardar la configuracion.';
        }
      });
  }

  private validate(): string[] {
    const errors: string[] = [];
    if (!this.isPositiveInteger(this.bookingWindowWeeks)) {
      errors.push('La ventana de semanas debe ser un numero entero mayor a 0.');
    }
    if (this.selectedWorkingDays().length === 0) {
      errors.push('Debe seleccionar al menos un dia de atencion.');
    }
    const hasInvalidRange = this.timeSlots.some((slot) => {
      if (!slot.startTime || !slot.endTime) {
        return true;
      }
      return this.normalizeTime(slot.startTime) >= this.normalizeTime(slot.endTime);
    });
    if (hasInvalidRange || this.timeSlots.length === 0) {
      errors.push('La hora de inicio debe ser anterior a la hora de fin en cada franja.');
    }
    if (!this.isPositiveInteger(this.slotIntervalMinutes)) {
      errors.push('El intervalo entre citas debe ser un numero entero mayor a 0.');
    }
    return errors;
  }

  private selectedWorkingDays(): string[] {
    return this.weekDays
      .filter((day) => this.selectedDays[day.value])
      .map((day) => day.value);
  }

  private isPositiveInteger(value: number | null): boolean {
    if (value === null || value === undefined || value === ('' as unknown as number)) {
      return false;
    }
    const numeric = Number(value);
    return Number.isInteger(numeric) && numeric > 0;
  }

  private normalizeTime(value: string): string {
    return value.length === 5 ? `${value}:00` : value;
  }

  private applyConfig(config: DoctorSchedulingConfigDTO) {
    this.bookingWindowWeeks = config.bookingWindowWeeks;
    this.slotIntervalMinutes = config.slotIntervalMinutes;
    this.weekDays.forEach((day) => {
      this.selectedDays[day.value] = (config.workingDays || []).includes(day.value);
    });
    this.timeSlots = (config.timeSlots || []).map((slot) => ({
      startTime: (slot.startTime || '').substring(0, 5),
      endTime: (slot.endTime || '').substring(0, 5)
    }));
    if (this.timeSlots.length === 0) {
      this.addTimeSlot();
    }
  }
}
