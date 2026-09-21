import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'agendador', pathMatch: 'full' },
  {
    path: 'agendador',
    loadChildren: () =>
      import('./pages/scheduler/routes').then((m) => m.SCHEDULER_ROUTES)
  },
  {
    path: 'paciente',
    loadChildren: () =>
      import('./pages/patient/routes').then((m) => m.PATIENT_ROUTES)
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./pages/admin/routes').then((m) => m.ADMIN_ROUTES)
  }
];
