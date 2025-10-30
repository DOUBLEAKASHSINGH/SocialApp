import { inject } from '@angular/core';
import {Routes} from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./home/home.component').then(m => m.HomeComponent),
        pathMatch: 'full'
    },
    {
        path: 'login',
        loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
    },
    {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [() => inject(AuthGuard).canActivate()],
    },
    {
        path: 'messaging',
        loadComponent: () => import('./messaging/messaging.component').then(m => m.MessagingComponent),
        canActivate: [() => inject(AuthGuard).canActivate()],
    },
    {
        path: '**',
        redirectTo: '',
        pathMatch: 'full'
    }
];
