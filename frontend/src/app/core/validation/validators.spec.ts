import { birthDateError, emailError, personNameError, phoneError, timeRangeError } from './validators';

describe('validators', () => {
  const today = new Date(2026, 8, 26);

  it('accepts a spanish person name and rejects digits', () => {
    expect(personNameError('Juan Carlos')).toBeNull();
    expect(personNameError('Ángel Núñez')).toBeNull();
    expect(personNameError('Juan123')).toBe('Usa solo letras. Se permiten espacios, tildes y la eñe.');
  });

  it('validates email structure', () => {
    expect(emailError('usuario@gmail.com')).toBeNull();
    expect(emailError('usuario@')).toBe('Ingresa un correo electrónico válido.');
    expect(emailError('')).toBe('Este campo es obligatorio.');
  });

  it('requires a 10 digit phone starting with 3', () => {
    expect(phoneError('3001234567')).toBeNull();
    expect(phoneError('2001234567')).toBe(
      'El número de teléfono debe tener 10 dígitos y comenzar por 3.'
    );
    expect(phoneError('30012345')).toBe(
      'El número de teléfono debe tener 10 dígitos y comenzar por 3.'
    );
    expect(phoneError('30012345678')).toBe(
      'El número de teléfono debe tener 10 dígitos y comenzar por 3.'
    );
  });

  it('accepts today and past birth dates and rejects future dates', () => {
    expect(birthDateError('2026-09-26', today)).toBeNull();
    expect(birthDateError('1990-01-15', today)).toBeNull();
    expect(birthDateError('2026-09-27', today)).toBe(
      'La fecha de nacimiento no puede ser posterior a hoy.'
    );
  });

  it('requires the end time to be later than the start time', () => {
    expect(timeRangeError('08:00', '12:00')).toBeNull();
    expect(timeRangeError('12:00', '08:00')).toBe(
      'La hora de fin debe ser posterior a la hora de inicio.'
    );
    expect(timeRangeError('10:00', '10:00')).toBe(
      'La hora de fin debe ser posterior a la hora de inicio.'
    );
  });
});
