import { Component, OnInit, inject } from '@angular/core';
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
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    tipo: ['', [Validators.required, Validators.maxLength(60)]],
    descripcion: ['', Validators.required],
    direccion: ['', [Validators.required, Validators.maxLength(255)]],
    horarios: ['', [Validators.required, Validators.maxLength(255)]],
    contacto: ['', [Validators.required, Validators.maxLength(180)]],
  });
  serverErrors: Record<string, string> = {};
  errorMessage = '';
  submitting = false;
  placeId: number | null = null;
  loading = false;

  get editing(): boolean {
    return this.placeId !== null;
  }

  ngOnInit(): void {
    const value = this.route.snapshot.paramMap.get('id');
    if (!value) return;

    this.placeId = Number(value);
    if (!Number.isInteger(this.placeId) || this.placeId < 1) {
      this.errorMessage = 'El identificador del lugar no es válido.';
      return;
    }

    this.loading = true;
    this.api.get(this.placeId).subscribe({
      next: (place) => {
        this.form.patchValue(place);
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

    const payload: PlacePayload = this.form.getRawValue();
    this.serverErrors = {};
    this.errorMessage = '';
    this.submitting = true;
    const request = this.placeId === null
      ? this.api.create(payload)
      : this.api.update(this.placeId, payload);
    request.subscribe({
      next: () => {
        this.submitting = false;
        void this.router.navigate(['/gestion/lugares']);
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
