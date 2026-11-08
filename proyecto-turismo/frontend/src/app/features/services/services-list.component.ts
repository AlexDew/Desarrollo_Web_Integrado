import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { ServicesApiService } from '../../core/services/services-api.service';
import { TouristService } from '../../core/models/service.model';
import { ServiceCardComponent } from '../../shared/service-card/service-card.component';

@Component({
  imports: [RouterLink, ServiceCardComponent],
  templateUrl: './services-list.component.html',
  styleUrl: './services-list.component.scss',
})
export class ServicesListComponent implements OnInit {
  private readonly api = inject(ServicesApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly route = inject(ActivatedRoute);
  management = false;
  services: TouristService[] = [];
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.management = this.route.snapshot.data['management'] === true;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    const request = this.management ? this.api.listManaged() : this.api.listPublic();
    request.subscribe({
      next: (services) => {
        this.services = services;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
        this.loading = false;
      },
    });
  }

  remove(id: number): void {
    if (!window.confirm('¿Eliminar este servicio?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(),
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
      },
    });
  }
}
