import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorService } from '../../core/errors/error.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        ErrorService,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('navigates home after a successful login', () => {
    authService.login.and.returnValue(of({ token: 'jwt', tokenType: 'Bearer', username: 'admin' }));

    component.submit();

    expect(authService.login).toHaveBeenCalledWith({ username: 'admin', password: 'admin' });
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('shows a dismissible error and stays on login after a failed login', () => {
    spyOn(console, 'error');
    authService.login.and.returnValue(throwError(() => new HttpErrorResponse({ status: 401 })));

    component.submit();

    expect(component.errorMessage).toContain('Sessione');
    expect(router.navigate).not.toHaveBeenCalledWith(['/error'], jasmine.anything());
    expect(console.error).toHaveBeenCalledWith('Login fallito.', jasmine.anything());

    component.dismissError();

    expect(component.errorMessage).toBe('');
  });

  it('validates required credentials before calling the API', () => {
    component.form.setValue({ username: '', password: '' });

    component.submit();

    expect(component.errorMessage).toBe('Inserisci username e password.');
    expect(authService.login).not.toHaveBeenCalled();
  });
});
