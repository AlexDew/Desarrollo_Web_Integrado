import { Component, OnInit, inject, signal } from '@angular/core';
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
  readonly services = signal<TouristService[]>([]);
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
    const request = this.management ? this.api.listManaged() : this.api.listPublic();
    request.subscribe({
      next: (services) => {
        this.services.set(services);
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
    if (!window.confirm('¿Eliminar este servicio?')) return;
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