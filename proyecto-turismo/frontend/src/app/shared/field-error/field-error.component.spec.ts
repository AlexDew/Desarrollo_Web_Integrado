import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { FieldErrorComponent } from './field-error.component';

@Component({
  imports: [ReactiveFormsModule, FieldErrorComponent],
  template: `
    <form [formGroup]="form">
      <input formControlName="email">
      <app-field-error [control]="form.controls.email" />
    </form>
    <button type="button" (click)="form.controls.email.markAsTouched()">Touch field</button>
  `,
})
class FieldErrorHostComponent {
  readonly form = new FormGroup({
    email: new FormControl('', Validators.required),
  });
}

describe('FieldErrorComponent', () => {
  it('updates the displayed validation message when the field is touched', async () => {
    await TestBed.configureTestingModule({
      imports: [FieldErrorHostComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(FieldErrorHostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Este campo es obligatorio.');

    fixture.nativeElement.querySelector('button').click();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Este campo es obligatorio.');
  });
});
