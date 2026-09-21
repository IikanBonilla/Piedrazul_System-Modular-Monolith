import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterPatientRequest {
  username: string;
  password: string;
  fullName: string;
  documentNumber: string;
  phone: string;
}

export interface RegisterPatientResponseDTO {
  id: number;
  username: string;
  fullName: string;
  documentNumber: string;
  phone: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  register(payload: RegisterPatientRequest): Observable<RegisterPatientResponseDTO> {
    return this.http.post<RegisterPatientResponseDTO>(
      `${this.baseUrl}/patients/register`,
      payload
    );
  }
}
