import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { PlacesApiService } from '../../core/services/places-api.service';
import { TouristPlace } from '../../core/models/place.model';

@Component({
  imports: [RouterLink],
  templateUrl: './places-list.component.html',
  styleUrl: './places-list.component.scss',
})
export class PlacesListComponent implements OnInit {
  private readonly api = inject(PlacesApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly route = inject(ActivatedRoute);
  readonly auth = inject(AuthService);

  management = false;
  readonly places = signal<TouristPlace[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly deletingId = signal<number | null>(null);

  ngOnInit(): void {
    this.management = this.route.snapshot.data['management'] === true;
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    const request = this.management ? this.api.listOwned() : this.api.list();
    request.subscribe({
      next: (places) => {
        this.places.set(places);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errors.parse(error).message);
        this.loading.set(false);
      },
    });
  }

  remove(id: number): void {
    if (this.deletingId() !== null) return;
    if (!window.confirm('¿Eliminar este lugar?')) return;
    this.deletingId.set(id);
    this.api.delete(id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.load();
      },
      error: (error: unknown) => {
        this.deletingId.set(null);
        this.errorMessage.set(this.errors.parse(error).message);
      },
    });
  }
}