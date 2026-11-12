import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TouristService } from '../../core/models/service.model';
import { RatingControlComponent } from '../rating-control/rating-control.component';

@Component({
  selector: 'app-service-card',
  imports: [DecimalPipe, RouterLink, RatingControlComponent],
  templateUrl: './service-card.component.html',
  styleUrl: './service-card.component.scss',
})
export class ServiceCardComponent {
  @Input({ required: true }) servicio!: TouristService;
  @Input() management = false;
  @Input() deleting = false;
  @Input() canRate = false;
  @Input() ratingPending = false;
  @Output() deleteService = new EventEmitter<number>();
  @Output() rateService = new EventEmitter<number>();
}
