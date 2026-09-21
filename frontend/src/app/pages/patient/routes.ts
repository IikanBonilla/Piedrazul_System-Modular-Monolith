import { Routes } from '@angular/router';

export const PATIENT_ROUTES: Routes = [
  {
    path: 'disponibilidad',
    loadComponent: () =>
      import('./available-slots/component').then((m) => m.AvailableSlotsComponent)
  }
];
