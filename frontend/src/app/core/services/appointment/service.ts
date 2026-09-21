import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AppointmentResponseDTO {
  id: number;
  patientId: number;
  patientName?: string;
  patientDocument?: string;
  doctorId: number;
  doctorName?: string;
  appointmentDate: string;
  status: string;
  whatsappNumber: string;
  notes: string;
}

export interface AppointmentSearchResultDTO {
  doctorId: number;
  doctorName: string;
  date: string;
  total: number;
  appointments: AppointmentResponseDTO[];
}

export interface ScheduleAppointmentRequest {
  documentNumber: string;
  doctorId: number;
  slot: string;
  confirmed: boolean;
  notes?: string;
}

export interface ScheduleAppointmentConfirmationDTO {
  id: number;
  patientName: string;
  patientDocument: string;
  doctorName: string;
  appointmentDate: string;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  getAppointmentsByDoctorAndDate(
    doctorId: number,
    date: string
  ): Observable<AppointmentSearchResultDTO> {
    return this.http.get<AppointmentSearchResultDTO>(
      `${this.baseUrl}/appointments/doctor/${doctorId}?date=${date}`
    );
  }

  scheduleAppointment(
    payload: ScheduleAppointmentRequest
  ): Observable<ScheduleAppointmentConfirmationDTO> {
    return this.http.post<ScheduleAppointmentConfirmationDTO>(
      `${this.baseUrl}/appointments`,
      payload
    );
  }
}
