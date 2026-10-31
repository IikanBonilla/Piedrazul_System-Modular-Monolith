import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../api/api-config';

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

export interface PatientAppointmentHistoryDTO {
  patientId: number;
  patientName: string;
  patientDocument: string;
  total: number;
  appointments: AppointmentResponseDTO[];
}

export interface PatientSuggestionDTO {
  id: number;
  fullName: string;
  documentNumber: string;
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
  constructor(private http: HttpClient) {}

  getAppointmentsByDoctorAndDate(
    doctorId: number,
    date: string
  ): Observable<AppointmentSearchResultDTO> {
    return this.http.get<AppointmentSearchResultDTO>(
      `${API_BASE_URL}/appointments/doctor/${doctorId}?date=${date}`
    );
  }

  scheduleAppointment(
    payload: ScheduleAppointmentRequest
  ): Observable<ScheduleAppointmentConfirmationDTO> {
    return this.http.post<ScheduleAppointmentConfirmationDTO>(
      `${API_BASE_URL}/appointments`,
      payload
    );
  }

  exportAppointments(doctorId: number, date: string): Observable<Blob> {
    return this.http.get(
      `${API_BASE_URL}/appointments/doctor/${doctorId}/export?date=${date}`,
      { responseType: 'blob' }
    );
  }

  getAppointmentsByPatientDocument(documentNumber: string): Observable<PatientAppointmentHistoryDTO> {
    return this.http.get<PatientAppointmentHistoryDTO>(
      `${API_BASE_URL}/appointments/patient`,
      { params: { document: documentNumber } }
    );
  }

  suggestPatients(documentPrefix: string): Observable<PatientSuggestionDTO[]> {
    return this.http.get<PatientSuggestionDTO[]>(
      `${API_BASE_URL}/patients/suggest`,
      { params: { document: documentPrefix } }
    );
  }
}
