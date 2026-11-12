import { Component, inject, signal } from '@angular/core';
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
  readonly auth = inject(AuthService);
  private readonly errors = inject(ApiErrorService);

  readonly form = this.fb.group({
    rol: this.fb.control<'GUIA' | 'PROPIETARIO'>('GUIA', Validators.required),
  });
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly serverErrors = signal<Record<string, string>>({});
  readonly submitting = signal(false);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.successMessage.set('');
    this.serverErrors.set({});
    this.submitting.set(true);
    this.auth.addRole(this.form.controls.rol.value).subscribe({
      next: () => {
        this.submitting.set(false);
        this.successMessage.set('El rol se añadió a tu cuenta.');
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        const parsed = this.errors.parse(error);
        this.serverErrors.set(parsed.fields);
        this.errorMessage.set(parsed.message);
      },
    });
  }
}
