import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { PlacesApiService } from '../../core/services/places-api.service';
import { PlacePayload } from '../../core/models/place.model';
import { FieldErrorComponent } from '../../shared/field-error/field-error.component';

@Component({
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './place-form.component.html',
  styleUrl: './place-form.component.scss',
})
export class PlaceFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly api = inject(PlacesApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    tipo: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(60)]],
    descripcion: ['', [Validators.required, Validators.pattern(/\S/)]],
    direccion: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    horarios: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    contacto: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(180)]],
  });
  readonly serverErrors = signal<Record<string, string>>({});
  readonly errorMessage = signal('');
  readonly submitting = signal(false);
  readonly loading = signal(false);
  readonly loadFailed = signal(false);
  placeId: number | null = null;

  get editing(): boolean {
    return this.placeId !== null;
  }

  ngOnInit(): void {
    const value = this.route.snapshot.paramMap.get('id');
    if (!value) return;

    this.placeId = Number(value);
    if (!Number.isInteger(this.placeId) || this.placeId < 1) {
      this.errorMessage.set('El identificador del lugar no es válido.');
      this.loadFailed.set(true);
      return;
    }

    this.loading.set(true);
    this.api.get(this.placeId).subscribe({
      next: (place) => {
        this.form.patchValue(place);
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
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Ingrese datos válidos antes de guardar el lugar.');
      return;
    }

    const payload: PlacePayload = this.form.getRawValue();
    this.serverErrors.set({});
    this.errorMessage.set('');
    this.submitting.set(true);
    const request = this.placeId === null
      ? this.api.create(payload)
      : this.api.update(this.placeId, payload);
    request.subscribe({
      next: () => {
        this.submitting.set(false);
        void this.router.navigate(['/gestion/lugares']);
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