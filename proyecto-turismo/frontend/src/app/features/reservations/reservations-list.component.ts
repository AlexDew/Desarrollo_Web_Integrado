import { Component, OnInit, inject, signal } from '@angular/core';
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
  readonly reservations = signal<Reservation[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly actionInProgress = signal(false);

  ngOnInit(): void {
    this.management = this.route.snapshot.data['management'] === true;
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    const request = this.management ? this.api.listForManagedServices() : this.api.listMine();
    request.subscribe({
      next: (reservations) => {
        this.reservations.set(reservations);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errors.parse(error).message);
        this.loading.set(false);
      },
    });
  }

  cancel(id: number): void {
    if (this.actionInProgress()) return;
    if (!window.confirm('¿Cancelar esta solicitud pendiente?')) return;
    this.beginAction();
    this.api.cancel(id).subscribe({
      next: () => {
        this.actionInProgress.set(false);
        this.load();
      },
      error: (error: unknown) => {
        this.failAction(error);
      },
    });
  }

  decide(id: number, status: 'CONFIRMADA' | 'RECHAZADA'): void {
    if (this.actionInProgress()) return;
    this.beginAction();
    this.api.changeStatus(id, status).subscribe({
      next: () => {
        this.actionInProgress.set(false);
        this.load();
      },
      error: (error: unknown) => {
        this.failAction(error);
      },
    });
  }

  private beginAction(): void {
    this.errorMessage.set('');
    this.actionInProgress.set(true);
  }

  private failAction(error: unknown): void {
    this.actionInProgress.set(false);
    this.errorMessage.set(this.errors.parse(error).message);
  }
}
