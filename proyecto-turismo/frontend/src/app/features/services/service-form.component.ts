import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { ServicesApiService } from '../../core/services/services-api.service';
import { ServicePayload } from '../../core/models/service.model';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './service-form.component.html',
  styleUrl: './service-form.component.scss',
})
export class ServiceFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly api = inject(ServicesApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    tipo: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(60)]],
    descripcion: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(2000)]],
    horarios: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    precio: [0, [
      Validators.required,
      Validators.min(0),
      Validators.max(99_999_999.99),
      Validators.pattern(/^\d+(\.\d{1,2})?$/),
    ]],
    ruta: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    contacto: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(180)]],
    aceptaReservas: [false],
  });
  readonly serverErrors = signal<Record<string, string>>({});
  readonly errorMessage = signal('');
  readonly submitting = signal(false);
  serviceId: number | null = null;
  readonly loading = signal(false);
  readonly loadFailed = signal(false);

  get editing(): boolean {
    return this.serviceId !== null;
  }

  ngOnInit(): void {
    const value = this.route.snapshot.paramMap.get('id');
    if (!value) return;

    this.serviceId = Number(value);
    if (!Number.isInteger(this.serviceId) || this.serviceId < 1) {
      this.errorMessage.set('El identificador del servicio no es válido.');
      this.loadFailed.set(true);
      return;
    }

    this.loading.set(true);
    this.api.listManaged().subscribe({
      next: (services) => {
        const service = services.find((item) => item.id === this.serviceId);
        if (!service) {
          this.errorMessage.set('No se encontró un servicio tuyo con ese identificador.');
          this.loadFailed.set(true);
        } else {
          this.form.patchValue(service);
        }
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errors.parse(error).message);
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.loading() || this.loadFailed()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Ingrese datos válidos antes de publicar el servicio.');
      return;
    }

    const payload: ServicePayload = this.form.getRawValue();
    this.serverErrors.set({});
    this.errorMessage.set('');
    this.submitting.set(true);
    const request = this.serviceId === null
      ? this.api.create(payload)
      : this.api.update(this.serviceId, payload);
    request.subscribe({
      next: () => {
        this.submitting.set(false);
        void this.router.navigate(['/gestion/servicios']);
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
