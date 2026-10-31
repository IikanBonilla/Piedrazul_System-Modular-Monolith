import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AppointmentService,
  AppointmentResponseDTO
} from '../../../core/services/appointment/service';
import { AvailabilityService, DoctorDTO } from '../../../core/services/availability/service';
import { httpErrorMessage } from '../../../core/http/error-message';
import { appointmentDateError } from '../../../core/validation/validators';
import { AuthService } from '../../../core/auth/auth.service';

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
  exportError = '';
  exporting = false;
  doctorError = '';
  dateError = '';
  doctorsError = '';
  private auth = inject(AuthService);

  constructor(
    private appointmentService: AppointmentService,
    private availabilityService: AvailabilityService
  ) {}

  ngOnInit() {
    this.loadDoctors();
  }

  audience(): string {
    return this.auth.session()?.role === 'DOCTOR' ? 'Médico / terapista' : 'Agendador';
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
      next: (doctors) => {
        this.doctors = doctors;
        const ownDoctorId = this.auth.session()?.doctorId;
        if (ownDoctorId && doctors.some((doctor) => doctor.id === ownDoctorId)) {
          this.selectedDoctorId = ownDoctorId;
        }
      },
      error: (error) => {
        this.doctorsError = httpErrorMessage(error, 'No se pudo cargar la lista de médicos.');
      }
    });
  }

  private filtersAreValid(): boolean {
    this.doctorError = this.selectedDoctorId ? '' : 'Selecciona un médico o terapista.';
    this.dateError = appointmentDateError(this.selectedDate) ?? '';
    return !this.doctorError && !this.dateError;
  }

  search() {
    if (this.loading) {
      return;
    }
    this.searchError = '';
    if (!this.filtersAreValid()) {
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

  exportCsv() {
    if (this.exporting) {
      return;
    }
    this.exportError = '';
    if (!this.filtersAreValid()) {
      return;
    }

    const doctorId = this.selectedDoctorId!;
    const date = this.selectedDate;
    this.exporting = true;
    this.appointmentService.exportAppointments(doctorId, date)
      .pipe(finalize(() => { this.exporting = false; }))
      .subscribe({
        next: (blob) => this.downloadCsv(blob, doctorId, date),
        error: (err) => { void this.readExportError(err); }
      });
  }

  private downloadCsv(blob: Blob, doctorId: number, date: string) {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `citas-${doctorId}-${date}.csv`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private async readExportError(error: unknown) {
    const blob = error && typeof error === 'object' && 'error' in error
      ? (error as { error?: unknown }).error
      : null;
    if (blob instanceof Blob) {
      try {
        const parsed = JSON.parse(await blob.text()) as { message?: string };
        this.exportError = parsed.message || 'No se pudo exportar el listado.';
        return;
      } catch {
        this.exportError = 'No se pudo exportar el listado.';
        return;
      }
    }
    this.exportError = httpErrorMessage(error, 'No se pudo exportar el listado.');
  }
}
