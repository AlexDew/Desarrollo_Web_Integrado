import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TouristService } from '../../core/models/service.model';

@Component({
  selector: 'app-service-card',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './service-card.component.html',
  styleUrl: './service-card.component.scss',
})
export class ServiceCardComponent {
  @Input({ required: true }) servicio!: TouristService;
  @Input() management = false;
  @Input() deleting = false;
  @Output() deleteService = new EventEmitter<number>();
}
