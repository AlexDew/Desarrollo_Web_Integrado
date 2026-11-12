import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { ReservationsApiService } from '../../core/services/reservations-api.service';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

function minimumDateValidator(minimumDate: string) {
  return (control: { value: unknown }) => {
    const value = control.value;
    return typeof value === 'string' && value !== '' && value < minimumDate
      ? { minDate: true }
      : null;
  };
}

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

  readonly minimumDate = this.tomorrow();
  readonly form = this.fb.group({
    fecha: ['', [
      Validators.required,
      Validators.pattern(/^\d{4}-\d{2}-\d{2}$/),
      minimumDateValidator(this.minimumDate),
    ]],
    cantidadPersonas: [1, [
      Validators.required,
      Validators.min(1),
      Validators.max(2_147_483_647),
      Validators.pattern(/^\d+$/),
    ]],
  });
  serviceId = 0;
  placeId = 0;
  readonly serverErrors = signal<Record<string, string>>({});
  readonly errorMessage = signal('');
  readonly submitting = signal(false);

  ngOnInit(): void {
    const placeId = this.route.snapshot.paramMap.get('lugarId');
    const serviceId = this.route.snapshot.paramMap.get('servicioId');
    if (placeId !== null) {
      this.placeId = Number(placeId);
      if (!Number.isInteger(this.placeId) || this.placeId < 1) {
        this.errorMessage.set('El lugar seleccionado no es válido.');
      }
    } else {
      this.serviceId = Number(serviceId);
      if (!Number.isInteger(this.serviceId) || this.serviceId < 1) {
        this.errorMessage.set('El servicio seleccionado no es válido.');
      }
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Ingrese datos válidos antes de enviar la solicitud.');
      return;
    }
    if (this.serviceId < 1 && this.placeId < 1) {
      return;
    }

    this.serverErrors.set({});
    this.errorMessage.set('');
    this.submitting.set(true);
    const request = this.placeId > 0
      ? this.api.createForPlace(this.placeId, this.form.getRawValue())
      : this.api.create(this.serviceId, this.form.getRawValue());
    request.subscribe({
      next: () => {
        this.submitting.set(false);
        void this.router.navigate(['/reservas']);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        const parsed = this.errors.parse(error);
        this.serverErrors.set(parsed.fields);
        this.errorMessage.set(parsed.message);
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
