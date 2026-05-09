import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { AppComponent } from './app/app.component';
import { LoginComponent } from './app/pages/login/login.component';
import { HomeComponent } from './app/pages/home/home.component';
import { ErrorComponent } from './app/pages/error/error.component';
import { authGuard } from './app/core/auth/auth.guard';
import { authInterceptor } from './app/core/auth/auth.interceptor';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'contacts', component: HomeComponent, canActivate: [authGuard] },
  { path: 'error', component: ErrorComponent },
  { path: '**', redirectTo: 'login' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
}).catch(error => console.error(error));

