import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly errors = inject(ApiErrorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
    password: ['', Validators.required],
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
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting = false;
        const requested = this.route.snapshot.queryParamMap.get('returnUrl');
        const returnUrl = requested?.startsWith('/') && !requested.startsWith('//')
          ? requested
          : '/';
        void this.router.navigateByUrl(returnUrl);
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
