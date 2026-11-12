import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-field-error',
  templateUrl: './field-error.component.html',
  styleUrl: './field-error.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class FieldErrorComponent {
  @Input() control: AbstractControl | null = null;
  @Input() serverError: string | null = null;

  get message(): string | null {
    if (this.serverError) return this.serverError;
    if (!this.control || !(this.control.dirty || this.control.touched)) return null;
    if (this.control.hasError('required')) return 'Este campo es obligatorio.';
    if (this.control.hasError('email')) return 'Ingresa un correo válido.';
    if (this.control.hasError('minlength')) return 'El valor es demasiado corto.';
    if (this.control.hasError('maxlength')) return 'El valor supera la longitud permitida.';
    if (this.control.hasError('min')) return 'El valor está por debajo del mínimo permitido.';
    if (this.control.hasError('max')) return 'El valor supera el máximo permitido.';
    if (this.control.hasError('minDate')) return 'Selecciona una fecha futura.';
    if (this.control.hasError('pattern')) {
      const requiredPattern: string = this.control.getError('pattern')?.requiredPattern ?? '';
      return requiredPattern === '\\S'
        ? 'El campo no puede contener solo espacios en blanco.'
        : 'El formato no es válido.';
    }
    return null;
  }
}
