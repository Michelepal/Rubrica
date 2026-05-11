import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { ErrorComponent } from './pages/error/error.component';
import { ContactsComponent } from './pages/contacts/contacts.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { TagsComponent } from './pages/tags/tags.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'contacts', component: ContactsComponent, canActivate: [authGuard] },
  { path: 'tags', component: TagsComponent, canActivate: [authGuard] },
  { path: 'error', component: ErrorComponent },
  { path: '**', redirectTo: 'login' }
];
