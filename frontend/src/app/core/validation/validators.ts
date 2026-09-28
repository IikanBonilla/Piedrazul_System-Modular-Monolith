export function personNameError(value: string): string | null {
  const normalized = normalizeSpaces(value);
  if (!normalized) {
    return 'Este campo es obligatorio.';
  }
  if (normalized.length < 2 || normalized.length > 80) {
    return 'El nombre debe tener entre 2 y 80 caracteres.';
  }
  if (!/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?:[ -][A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$/.test(normalized)) {
    return 'Usa solo letras. Se permiten espacios, tildes y la eñe.';
  }
  return null;
}

export function usernameError(value: string): string | null {
  const normalized = value.trim().toLowerCase();
  if (!normalized) {
    return 'Este campo es obligatorio.';
  }
  if (!/^[a-z0-9._]{4,30}$/.test(normalized)) {
    return 'El usuario debe tener entre 4 y 30 caracteres: letras, números, punto o guion bajo.';
  }
  return null;
}

export function emailError(value: string): string | null {
  const normalized = value.trim().toLowerCase();
  if (!normalized) {
    return 'Este campo es obligatorio.';
  }
  if (normalized.length > 120 || /\s/.test(normalized) || !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(normalized)) {
    return 'Ingresa un correo electrónico válido.';
  }
  return null;
}

export function passwordError(value: string): string | null {
  if (!value || !value.trim()) {
    return 'Este campo es obligatorio.';
  }
  if (value.length < 8 || value.length > 64) {
    return 'La contraseña debe tener entre 8 y 64 caracteres.';
  }
  if (!/[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]/.test(value) || !/\d/.test(value)) {
    return 'La contraseña debe incluir al menos una letra y un número.';
  }
  return null;
}

export function phoneError(value: string): string | null {
  if (!value || !value.trim()) {
    return 'Este campo es obligatorio.';
  }
  if (value !== value.trim() || /\s/.test(value)) {
    return 'El número de teléfono no debe contener espacios.';
  }
  if (!/^3\d{9}$/.test(value)) {
    return 'El número de teléfono debe tener 10 dígitos y comenzar por 3.';
  }
  return null;
}

export function documentError(value: string): string | null {
  const normalized = value.trim();
  if (!normalized) {
    return 'Este campo es obligatorio.';
  }
  if (!/^\d{6,15}$/.test(normalized)) {
    return 'El documento debe tener entre 6 y 15 dígitos, sin letras ni espacios.';
  }
  return null;
}

export function birthDateError(value: string, today = new Date()): string | null {
  if (!value || !value.trim()) {
    return 'Este campo es obligatorio.';
  }
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || !isRealDate(value)) {
    return 'Ingresa una fecha de nacimiento válida.';
  }
  const todayIso = toIsoDate(today);
  if (value > todayIso) {
    return 'La fecha de nacimiento no puede ser posterior a hoy.';
  }
  const oldest = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate());
  if (value < toIsoDate(oldest)) {
    return 'La fecha de nacimiento no es válida.';
  }
  return null;
}

export function appointmentDateError(value: string): string | null {
  if (!value || !value.trim()) {
    return 'Selecciona una fecha.';
  }
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || !isRealDate(value)) {
    return 'Ingresa una fecha válida.';
  }
  return null;
}

export function timeRangeError(start: string, end: string): string | null {
  if (!start || !end) {
    return 'Indica la hora de inicio y la hora de fin.';
  }
  if (!/^([01]\d|2[0-3]):[0-5]\d$/.test(start) || !/^([01]\d|2[0-3]):[0-5]\d$/.test(end)) {
    return 'Ingresa un horario válido en formato HH:mm.';
  }
  if (start >= end) {
    return 'La hora de fin debe ser posterior a la hora de inicio.';
  }
  return null;
}

export function specialtyError(value: string): string | null {
  const normalized = normalizeSpaces(value);
  if (!normalized) {
    return 'Indica la especialidad del médico.';
  }
  if (
    normalized.length < 3 ||
    normalized.length > 60 ||
    !/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?:[ -][A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$/.test(normalized)
  ) {
    return 'La especialidad solo puede contener letras y debe tener entre 3 y 60 caracteres.';
  }
  return null;
}

export function normalizeSpaces(value: string): string {
  return value.trim().replace(/\s+/g, ' ');
}

export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

function isRealDate(iso: string): boolean {
  const [year, month, day] = iso.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
}
