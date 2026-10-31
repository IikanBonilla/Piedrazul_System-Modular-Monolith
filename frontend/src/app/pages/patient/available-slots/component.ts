import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AvailabilityService,
  AvailableSlotsResultDTO,
  DoctorDTO
} from '../../../core/services/availability/service';
import { httpErrorMessage } from '../../../core/http/error-message';

@Component({
  selector: 'app-available-slots',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './component.html'
})
export class AvailableSlotsComponent implements OnInit {
  doctors: DoctorDTO[] = [];
  selectedDoctorId: number | null = null;
  selectedDate = '';
  result: AvailableSlotsResultDTO | null = null;
  loading = false;
  searched = false;
  formError = '';
  doctorsError = '';

  constructor(private availabilityService: AvailabilityService) {}

  ngOnInit() {
    this.availabilityService.getDoctors().subscribe({
      next: (doctors) => {
        this.doctors = doctors;
        this.doctorsError = doctors.length === 0
          ? 'No hay médicos activos. Reinicia el backend para cargar la semilla de la Dra. Maria Lopez.'
          : '';
      },
      error: (error) => {
        this.doctors = [];
        this.doctorsError = httpErrorMessage(error, 'No se pudo cargar la lista de médicos.');
      }
    });
  }

  consult() {
    this.formError = '';
    this.result = null;
    if (!this.selectedDoctorId) {
      this.formError = 'Debe seleccionar un medico o terapista antes de consultar disponibilidad.';
      return;
    }
    if (!this.selectedDate) {
      this.formError = 'Debe seleccionar una fecha antes de consultar disponibilidad.';
      return;
    }

    this.loading = true;
    this.searched = true;
    this.availabilityService
      .getAvailableSlots(this.selectedDoctorId, this.selectedDate)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => { this.result = response; },
        error: (err) => {
          this.formError = err.error?.message || 'No se pudo consultar la disponibilidad.';
        }
      });
  }
}
