import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppointmentService, ScheduleAppointmentConfirmationDTO } from '../../../core/services/appointment/service';
import {
  AvailabilityService,
  AvailableSlotsResultDTO,
  DoctorDTO
} from '../../../core/services/availability/service';

@Component({
  selector: 'app-patient-schedule',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './component.html'
})
export class PatientScheduleComponent implements OnInit {
  doctors: DoctorDTO[] = [];
  documentNumber = sessionStorage.getItem('patientDocument') || '';
  selectedDoctorId: number | null = null;
  selectedDate = '';
  selectedSlot = '';
  slotsResult: AvailableSlotsResultDTO | null = null;
  loadingSlots = false;
  scheduling = false;
  searched = false;
  formError = '';
  confirmation?: ScheduleAppointmentConfirmationDTO;
  showConfirmModal = false;

  constructor(
    private availabilityService: AvailabilityService,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit() {
    this.availabilityService.getDoctors().subscribe({
      next: (doctors) => { this.doctors = doctors; }
    });
  }

  selectedDoctorName(): string {
    return this.doctors.find((doctor) => doctor.id === this.selectedDoctorId)?.fullName || '';
  }

  consultAvailability() {
    this.formError = '';
    this.confirmation = undefined;
    this.selectedSlot = '';
    this.slotsResult = null;

    if (!this.selectedDoctorId) {
      this.formError = 'Debe seleccionar un medico o terapista antes de consultar disponibilidad.';
      return;
    }
    if (!this.selectedDate) {
      this.formError = 'Debe seleccionar una fecha antes de consultar disponibilidad.';
      return;
    }

    this.loadingSlots = true;
    this.searched = true;
    this.loadSlots();
  }

  private loadSlots() {
    if (!this.selectedDoctorId || !this.selectedDate) {
      return;
    }

    this.loadingSlots = true;
    this.availabilityService
      .getAvailableSlots(this.selectedDoctorId, this.selectedDate)
      .pipe(finalize(() => { this.loadingSlots = false; }))
      .subscribe({
        next: (result) => {
          this.slotsResult = result;
          if (result.slots.every((slot) => slot.start !== this.selectedSlot)) {
            this.selectedSlot = '';
          }
        },
        error: (err) => {
          this.formError = err.error?.message || 'No se pudo consultar la disponibilidad.';
        }
      });
  }

  requestSchedule() {
    this.formError = '';
    this.confirmation = undefined;

    if (!this.documentNumber.trim()) {
      this.formError = 'Debe indicar el documento del paciente registrado.';
      return;
    }
    if (!this.selectedDoctorId) {
      this.formError = 'Debe seleccionar un medico o terapista antes de agendar.';
      return;
    }
    if (!this.selectedSlot) {
      this.formError = 'Debe seleccionar una franja horaria disponible.';
      return;
    }

    this.showConfirmModal = true;
  }

  cancelConfirmation() {
    this.showConfirmModal = false;
  }

  confirmSchedule() {
    if (!this.selectedDoctorId || !this.selectedSlot) {
      return;
    }

    this.scheduling = true;
    this.appointmentService
      .scheduleAppointment({
        documentNumber: this.documentNumber.trim(),
        doctorId: this.selectedDoctorId,
        slot: this.selectedSlot,
        confirmed: true
      })
      .pipe(finalize(() => { this.scheduling = false; }))
      .subscribe({
        next: (response) => {
          this.showConfirmModal = false;
          this.confirmation = response;
          this.loadSlots();
        },
        error: (err) => {
          this.showConfirmModal = false;
          this.formError = err.error?.message || 'No se pudo agendar la cita.';
        }
      });
  }
}
