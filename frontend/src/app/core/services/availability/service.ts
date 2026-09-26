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

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  private baseUrl = API_BASE_URL;

  constructor(private http: HttpClient) {}

  getDoctors(): Observable<DoctorDTO[]> {
    return this.http.get<DoctorDTO[]>(`${this.baseUrl}/doctors`);
  }
}
