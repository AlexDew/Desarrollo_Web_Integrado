import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-rating-control',
  templateUrl: './rating-control.component.html',
  styleUrl: './rating-control.component.scss',
})
export class RatingControlComponent {
  @Input() calificacion: number | undefined;
  @Input() pending = false;
  @Output() rate = new EventEmitter<number>();

  selected = 5;

  updateSelection(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    if (Number.isInteger(value) && value >= 1 && value <= 5) {
      this.selected = value;
    }
  }
}
