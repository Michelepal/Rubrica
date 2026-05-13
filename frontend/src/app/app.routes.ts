import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'home', loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent), canActivate: [authGuard] },
  { path: 'contacts', loadComponent: () => import('./pages/contacts/contacts.component').then(m => m.ContactsComponent), canActivate: [authGuard] },
  { path: 'tags', loadComponent: () => import('./pages/tags/tags.component').then(m => m.TagsComponent), canActivate: [authGuard] },
  { path: 'error', loadComponent: () => import('./pages/error/error.component').then(m => m.ErrorComponent) },
  { path: '**', redirectTo: 'login' }
];
