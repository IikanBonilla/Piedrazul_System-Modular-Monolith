import { Routes } from '@angular/router';
import { HomeRedirectComponent, homeRedirectGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [homeRedirectGuard],
    component: HomeRedirectComponent
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/auth/login/component').then((m) => m.LoginComponent)
  },
  {
    path: 'registro',
    loadComponent: () => import('./pages/auth/register/component').then((m) => m.RegisterComponent)
  },
  {
    path: 'acceso-denegado',
    loadComponent: () => import('./pages/auth/forbidden/component').then((m) => m.ForbiddenComponent)
  },
  {
    path: 'agendador',
    canActivate: [roleGuard('ADMINISTRATOR', 'SCHEDULER')],
    loadChildren: () => import('./pages/scheduler/routes').then((m) => m.SCHEDULER_ROUTES)
  },
  {
    path: 'medico',
    canActivate: [roleGuard('DOCTOR')],
    loadChildren: () => import('./pages/scheduler/routes').then((m) => m.SCHEDULER_ROUTES)
  },
  {
    path: 'paciente',
    canActivate: [roleGuard('PATIENT')],
    loadComponent: () => import('./pages/patient/home/component').then((m) => m.PatientHomeComponent)
  }
];
