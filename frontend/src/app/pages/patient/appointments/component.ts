import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppointmentResponseDTO, AppointmentService } from '../../../core/services/appointment/service';
import { httpErrorMessage } from '../../../core/http/error-message';

@Component({
  selector: 'app-patient-appointments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './component.html'
})
export class PatientAppointmentsComponent implements OnInit {
  appointments: AppointmentResponseDTO[] = [];
  loading = false;
  cancelling = false;
  loadError = '';
  formError = '';
  success = '';
  selectedId: number | null = null;
  justification = '';

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.loadError = '';
    this.appointmentService.getMyAppointments()
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (result) => { this.appointments = result.appointments ?? []; },
        error: (error) => {
          this.appointments = [];
          this.loadError = httpErrorMessage(error, 'No se pudieron cargar tus citas.');
        }
      });
  }

  canCancel(appointment: AppointmentResponseDTO): boolean {
    if (appointment.status !== 'PENDING' && appointment.status !== 'CONFIRMED') {
      return false;
    }
    const day = appointment.appointmentDate?.slice(0, 10);
    return !!day && day >= this.minimumDate();
  }

  startCancel(appointment: AppointmentResponseDTO): void {
    this.selectedId = appointment.id;
    this.justification = '';
    this.formError = '';
    this.success = '';
  }

  closeCancel(): void {
    if (this.cancelling) {
      return;
    }
    this.selectedId = null;
    this.justification = '';
    this.formError = '';
  }

  confirmCancel(): void {
    if (this.cancelling || this.selectedId == null) {
      return;
    }
    const reason = this.justification.trim().replace(/\s+/g, ' ');
    if (!reason) {
      this.formError = 'La justificación es obligatoria.';
      return;
    }
    if (reason.length < 10 || reason.length > 300) {
      this.formError = 'La justificación debe tener entre 10 y 300 caracteres.';
      return;
    }
    this.cancelling = true;
    this.formError = '';
    this.appointmentService.cancelAppointment(this.selectedId, reason)
      .pipe(finalize(() => { this.cancelling = false; }))
      .subscribe({
        next: (result) => {
          this.success = result.message;
          this.selectedId = null;
          this.justification = '';
          this.load();
        },
        error: (error) => {
          this.formError = httpErrorMessage(error, 'No se pudo cancelar la cita.');
        }
      });
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

  private minimumDate(): string {
    const date = new Date();
    date.setDate(date.getDate() + 2);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
