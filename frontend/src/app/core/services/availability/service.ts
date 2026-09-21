import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  private baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  getDoctors(): Observable<DoctorDTO[]> {
    return this.http.get<DoctorDTO[]>(`${this.baseUrl}/doctors`);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<AvailableSlotsResultDTO> {
    return this.http.get<AvailableSlotsResultDTO>(
      `${this.baseUrl}/doctors/${doctorId}/slots?date=${date}`
    );
  }
}
