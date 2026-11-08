import { Component, OnInit, inject } from '@angular/core';
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
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    tipo: ['', [Validators.required, Validators.maxLength(60)]],
    descripcion: ['', [Validators.required, Validators.maxLength(2000)]],
    horarios: ['', [Validators.required, Validators.maxLength(255)]],
    precio: [0, [Validators.required, Validators.min(0)]],
    ruta: ['', [Validators.required, Validators.maxLength(255)]],
    contacto: ['', [Validators.required, Validators.maxLength(180)]],
    aceptaReservas: [false],
  });
  serverErrors: Record<string, string> = {};
  errorMessage = '';
  submitting = false;
  serviceId: number | null = null;
  loading = false;

  get editing(): boolean {
    return this.serviceId !== null;
  }

  ngOnInit(): void {
    const value = this.route.snapshot.paramMap.get('id');
    if (!value) return;

    this.serviceId = Number(value);
    if (!Number.isInteger(this.serviceId) || this.serviceId < 1) {
      this.errorMessage = 'El identificador del servicio no es válido.';
      return;
    }

    this.loading = true;
    this.api.listManaged().subscribe({
      next: (services) => {
        const service = services.find((item) => item.id === this.serviceId);
        if (!service) {
          this.errorMessage = 'No se encontró un servicio tuyo con ese identificador.';
        } else {
          this.form.patchValue(service);
        }
        this.loading = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
        this.loading = false;
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: ServicePayload = this.form.getRawValue();
    this.serverErrors = {};
    this.errorMessage = '';
    this.submitting = true;
    const request = this.serviceId === null
      ? this.api.create(payload)
      : this.api.update(this.serviceId, payload);
    request.subscribe({
      next: () => {
        this.submitting = false;
        void this.router.navigate(['/gestion/servicios']);
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
