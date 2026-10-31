import { Component, OnDestroy, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { httpErrorMessage } from '../../../core/http/error-message';
import {
  AppointmentResponseDTO,
  AppointmentService,
  PatientSuggestionDTO
} from '../../../core/services/appointment/service';
import { documentError } from '../../../core/validation/validators';

@Component({
  selector: 'app-patient-history',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './component.html'
})
export class PatientHistoryComponent implements OnDestroy {
  documentQuery = '';
  suggestions: PatientSuggestionDTO[] = [];
  appointments: AppointmentResponseDTO[] = [];
  patientName = '';
  patientDocument = '';
  total = 0;
  loading = false;
  suggesting = false;
  searched = false;
  searchError = '';
  documentFieldError = '';
  private suggestTimer: ReturnType<typeof setTimeout> | null = null;
  private skipNextSuggest = false;
  private auth = inject(AuthService);
  private appointmentsApi = inject(AppointmentService);

  ngOnDestroy() {
    if (this.suggestTimer) {
      clearTimeout(this.suggestTimer);
    }
  }

  audience(): string {
    return this.auth.session()?.role === 'DOCTOR' ? 'Médico / terapista' : 'Agendador';
  }

  onDocumentInput() {
    if (this.skipNextSuggest) {
      this.skipNextSuggest = false;
      return;
    }
    this.documentFieldError = '';
    this.searchError = '';
    if (this.suggestTimer) {
      clearTimeout(this.suggestTimer);
    }
    const digits = this.documentQuery.trim();
    if (!/^\d{3,}$/.test(digits)) {
      this.suggestions = [];
      return;
    }
    this.suggestTimer = setTimeout(() => this.loadSuggestions(digits), 250);
  }

  selectSuggestion(suggestion: PatientSuggestionDTO) {
    this.skipNextSuggest = true;
    this.documentQuery = suggestion.documentNumber;
    this.suggestions = [];
    this.search();
  }

  closeSuggestions() {
    setTimeout(() => { this.suggestions = []; }, 150);
  }

  search() {
    if (this.loading) {
      return;
    }
    this.suggestions = [];
    this.searchError = '';
    this.documentFieldError = documentError(this.documentQuery) ?? '';
    if (this.documentFieldError) {
      return;
    }

    this.loading = true;
    this.searched = true;
    this.appointmentsApi.getAppointmentsByPatientDocument(this.documentQuery.trim())
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          this.patientName = response.patientName;
          this.patientDocument = response.patientDocument;
          this.total = response.total;
          this.appointments = response.appointments;
        },
        error: (error) => {
          this.patientName = '';
          this.patientDocument = '';
          this.total = 0;
          this.appointments = [];
          this.searchError = httpErrorMessage(error, 'No se pudo consultar el historial.');
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

  private loadSuggestions(prefix: string) {
    this.suggesting = true;
    this.appointmentsApi.suggestPatients(prefix)
      .pipe(finalize(() => { this.suggesting = false; }))
      .subscribe({
        next: (suggestions) => { this.suggestions = suggestions; },
        error: () => { this.suggestions = []; }
      });
  }
}
