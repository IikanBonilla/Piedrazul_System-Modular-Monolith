import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../api/api-config';

export interface DoctorDTO {
  id: number;
  fullName: string;
  specialty: string;
  active: boolean;
}

export interface TimeSlotDTO {
  start: string;
}

export interface AvailableSlotsResultDTO {
  doctorId: number;
  doctorName: string;
  date: string;
  total: number;
  slots: TimeSlotDTO[];
  message?: string;
}

export interface TimeRangeDTO {
  startTime: string;
  endTime: string;
}

export interface DoctorSchedulingConfigDTO {
  doctorId?: number;
  bookingWindowWeeks: number | null;
  workingDays: string[];
  timeSlots: TimeRangeDTO[];
  slotIntervalMinutes: number | null;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  constructor(private http: HttpClient) {}

  getDoctors(): Observable<DoctorDTO[]> {
    return this.http.get<DoctorDTO[]>(`${API_BASE_URL}/doctors`);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<AvailableSlotsResultDTO> {
    return this.http.get<AvailableSlotsResultDTO>(
      `${API_BASE_URL}/doctors/${doctorId}/slots?date=${date}`
    );
  }

  getSchedulingConfig(doctorId: number): Observable<DoctorSchedulingConfigDTO> {
    return this.http.get<DoctorSchedulingConfigDTO>(
      `${API_BASE_URL}/admin/doctors/${doctorId}/scheduling-config`
    );
  }

  saveSchedulingConfig(
    doctorId: number,
    payload: DoctorSchedulingConfigDTO
  ): Observable<DoctorSchedulingConfigDTO> {
    return this.http.put<DoctorSchedulingConfigDTO>(
      `${API_BASE_URL}/admin/doctors/${doctorId}/scheduling-config`,
      payload
    );
  }
}
