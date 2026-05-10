import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorService } from '../../core/errors/error.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly errorService = inject(ErrorService);
  private readonly router = inject(Router);

  errorMessage = '';

  form = this.fb.nonNullable.group({
    username: ['admin', [Validators.required]],
    password: ['admin', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.errorMessage = 'Inserisci username e password.';
      return;
    }
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/home']).catch(error => console.error('Navigazione alla home fallita dopo il login.', error));
      },
      error: error => {
        console.error('Login fallito.', { username: this.form.controls.username.value, error });
        this.errorMessage = this.errorService.toMessage(error).message;
      }
    });
  }

  dismissError(): void {
    this.errorMessage = '';
  }
}
