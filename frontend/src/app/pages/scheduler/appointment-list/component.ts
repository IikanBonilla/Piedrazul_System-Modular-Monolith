import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AppointmentService,
  AppointmentResponseDTO
} from '../../../core/services/appointment/service';
import { AvailabilityService, DoctorDTO } from '../../../core/services/availability/service';

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

  constructor(
    private appointmentService: AppointmentService,
    private availabilityService: AvailabilityService
  ) {}

  ngOnInit() {
    this.loadDoctors();
  }

  private loadDoctors() {
    this.availabilityService.getDoctors().subscribe({
      next: (doctors) => { this.doctors = doctors; }
    });
  }

  search() {
    this.searchError = '';

    if (!this.selectedDoctorId) {
      this.searchError = 'Debe seleccionar un medico o terapista antes de realizar la busqueda.';
      return;
    }
    if (!this.selectedDate) {
      this.searchError = 'Debe seleccionar una fecha antes de realizar la busqueda.';
      return;
    }

    this.loading = true;
    this.searched = true;

    this.appointmentService
      .getAppointmentsByDoctorAndDate(this.selectedDoctorId, this.selectedDate)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          this.appointments = response.appointments;
          this.total = response.total;
        },
        error: (err) => {
          this.appointments = [];
          this.total = 0;
          this.searchError = err.error?.message || 'No se pudo realizar la busqueda.';
        }
      });
  }
}
