import { Component, OnInit, inject } from '@angular/core';
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
  places: TouristPlace[] = [];
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.management = this.route.snapshot.data['management'] === true;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    const request = this.management ? this.api.listOwned() : this.api.list();
    request.subscribe({
      next: (places) => {
        this.places = places;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
        this.loading = false;
      },
    });
  }

  remove(id: number): void {
    if (!window.confirm('¿Eliminar este lugar?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(),
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
      },
    });
  }
}
