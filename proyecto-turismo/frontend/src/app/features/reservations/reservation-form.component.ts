import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { ReservationsApiService } from '../../core/services/reservations-api.service';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './reservation-form.component.html',
  styleUrl: './reservation-form.component.scss',
})
export class ReservationFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly api = inject(ReservationsApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.group({
    fecha: ['', Validators.required],
    cantidadPersonas: [1, [Validators.required, Validators.min(1)]],
  });
  serviceId = 0;
  readonly minimumDate = this.tomorrow();
  serverErrors: Record<string, string> = {};
  errorMessage = '';
  submitting = false;

  ngOnInit(): void {
    this.serviceId = Number(this.route.snapshot.paramMap.get('servicioId'));
    if (!Number.isInteger(this.serviceId) || this.serviceId < 1) {
      this.errorMessage = 'El servicio seleccionado no es válido.';
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.serverErrors = {};
    this.errorMessage = '';
    this.submitting = true;
    this.api.create(this.serviceId, this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting = false;
        void this.router.navigate(['/reservas']);
      },
      error: (error: unknown) => {
        this.submitting = false;
        const parsed = this.errors.parse(error);
        this.serverErrors = parsed.fields;
        this.errorMessage = parsed.message;
      },
    });
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private tomorrow(): string {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    return this.formatLocalDate(date);
  }
}
