import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiErrorService } from '../../core/services/api-error.service';
import { ReservationsApiService } from '../../core/services/reservations-api.service';
import { Reservation } from '../../core/models/reservation.model';

@Component({
  templateUrl: './reservations-list.component.html',
  styleUrl: './reservations-list.component.scss',
})
export class ReservationsListComponent implements OnInit {
  private readonly api = inject(ReservationsApiService);
  private readonly errors = inject(ApiErrorService);
  private readonly route = inject(ActivatedRoute);

  management = false;
  reservations: Reservation[] = [];
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.management = this.route.snapshot.data['management'] === true;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    const request = this.management ? this.api.listForManagedServices() : this.api.listMine();
    request.subscribe({
      next: (reservations) => {
        this.reservations = reservations;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
        this.loading = false;
      },
    });
  }

  cancel(id: number): void {
    if (!window.confirm('¿Cancelar esta solicitud pendiente?')) return;
    this.api.cancel(id).subscribe({
      next: () => this.load(),
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
      },
    });
  }

  decide(id: number, status: 'CONFIRMADA' | 'RECHAZADA'): void {
    this.api.changeStatus(id, status).subscribe({
      next: () => this.load(),
      error: (error: unknown) => {
        this.errorMessage = this.errors.parse(error).message;
      },
    });
  }
}
