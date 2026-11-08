import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, FieldErrorComponent],
  templateUrl: './request-role.component.html',
  styleUrl: './request-role.component.scss',
})
export class RequestRoleComponent {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly errors = inject(ApiErrorService);

  readonly form = this.fb.group({
    rol: this.fb.control<'GUIA' | 'PROPIETARIO'>('GUIA', Validators.required),
  });
  errorMessage = '';
  successMessage = '';
  serverErrors: Record<string, string> = {};
  submitting = false;

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.serverErrors = {};
    this.submitting = true;
    this.auth.addRole(this.form.controls.rol.value).subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = 'El rol se añadió a tu cuenta.';
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
