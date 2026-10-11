import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

// Clientside Validtaion mirrors Backend-validation attributes
export const MAX_LENGTH = 255;

// checks whitespace only input; ".required" allows only whitespace input
export const notBlank: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  typeof control.value === 'string' && control.value.trim().length === 0 ? { notBlank: true } : null;

export const documentValidators = {
  title: [Validators.required, notBlank, Validators.maxLength(MAX_LENGTH)],
  filename: [Validators.required, notBlank, Validators.maxLength(MAX_LENGTH)],
};

export function fieldError(control: AbstractControl | null): string | null {
  if (!control || !control.errors || !(control.touched || control.dirty)) {
    return null;
  }
  if (control.errors['required'] || control.errors['notBlank']) {
    return 'This field is required.';
  }
  if (control.errors['maxlength']) {
    return `Maximum ${MAX_LENGTH} characters.`;
  }
  return 'Invalid value.';
}
