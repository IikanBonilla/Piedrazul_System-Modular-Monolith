import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'configuracion',
    loadComponent: () =>
      import('./scheduling-config/component').then((m) => m.SchedulingConfigComponent)
  }
];
