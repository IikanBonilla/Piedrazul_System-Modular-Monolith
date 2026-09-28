import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  AppointmentService,
  AppointmentResponseDTO
} from '../../../core/services/appointment/service';
import { AvailabilityService, DoctorDTO } from '../../../core/services/availability/service';
import { httpErrorMessage } from '../../../core/http/error-message';
import { appointmentDateError } from '../../../core/validation/validators';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './component.html'
})
export class AppointmentListComponent implements OnInit {
  doctors: DoctorDTO[] = [];
  selectedDoctorId: number | null = null;
  selectedDate = '';
  appointments: AppointmentResponseDTO[] = [];
  total = 0;
  loading = false;
  searched = false;
  searchError = '';
  doctorError = '';
  dateError = '';
  doctorsError = '';

  private router = inject(Router);

  constructor(
    private appointmentService: AppointmentService,
    private availabilityService: AvailabilityService
  ) {}

  ngOnInit() {
    this.loadDoctors();
  }

  audience(): string {
    return this.router.url.startsWith('/medico') ? 'Médico' : 'Administración';
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      CONFIRMED: 'Confirmada',
      PENDING: 'Pendiente',
      CANCELLED: 'Cancelada',
      COMPLETED: 'Completada',
      RESCHEDULED: 'Reprogramada'
    };
    return labels[status] ?? status;
  }

  statusClass(status: string): string {
    const classes: Record<string, string> = {
      CONFIRMED: 'text-bg-success',
      PENDING: 'text-bg-warning',
      CANCELLED: 'text-bg-danger',
      COMPLETED: 'text-bg-primary',
      RESCHEDULED: 'text-bg-secondary'
    };
    return classes[status] ?? 'text-bg-secondary';
  }

  private loadDoctors() {
    this.availabilityService.getDoctors().subscribe({
      next: (doctors) => { this.doctors = doctors; },
      error: (error) => {
        this.doctorsError = httpErrorMessage(error, 'No se pudo cargar la lista de médicos.');
      }
    });
  }

  search() {
    if (this.loading) {
      return;
    }
    this.searchError = '';
    this.doctorError = this.selectedDoctorId ? '' : 'Selecciona un médico o terapista.';
    this.dateError = appointmentDateError(this.selectedDate) ?? '';
    if (this.doctorError || this.dateError) {
      return;
    }

    this.loading = true;
    this.searched = true;

    this.appointmentService
      .getAppointmentsByDoctorAndDate(this.selectedDoctorId!, this.selectedDate)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          this.appointments = response.appointments;
          this.total = response.total;
        },
        error: (err) => {
          this.appointments = [];
          this.total = 0;
          this.searchError = httpErrorMessage(err, 'No se pudo realizar la búsqueda.');
        }
      });
  }
}
