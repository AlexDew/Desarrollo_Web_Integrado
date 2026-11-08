import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly errors = inject(ApiErrorService);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });
  serverErrors: Record<string, string> = {};
  errorMessage = '';
  submitting = false;

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.serverErrors = {};
    this.errorMessage = '';
    this.submitting = true;
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting = false;
        void this.router.navigate(['/']);
      },
      error: (error: unknown) => {
        this.submitting = false;
        const parsed = this.errors.parse(error);
        this.serverErrors = parsed.fields;
        this.errorMessage = parsed.message;
      },
    });
  }
}
