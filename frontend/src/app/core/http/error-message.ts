export function httpErrorMessage(error: unknown, fallback: string): string {
  const parsed = asHttpError(error);
  if (!parsed) {
    return fallback;
  }
  if (parsed.status === 0) {
    return 'No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo.';
  }
  if (parsed.status === 401) {
    if (parsed.code === 'SESSION_EXPIRED') {
      return 'Tu sesión expiró. Ingresa de nuevo.';
    }
    return parsed.message || 'Usuario o contraseña incorrectos.';
  }
  if (parsed.status === 403) {
    return parsed.message || 'No tienes permiso para realizar esta acción.';
  }
  if (parsed.status === 404) {
    return parsed.message || 'No se encontró la información solicitada.';
  }
  if (parsed.status >= 500) {
    return 'Ocurrió un error en el servidor. Intenta de nuevo más tarde.';
  }
  return parsed.message || fallback;
}

export function httpFieldErrors(error: unknown): Record<string, string> {
  const body = asHttpError(error)?.body;
  const fields = body?.['fields'];
  if (!fields || typeof fields !== 'object') {
    return {};
  }
  return Object.fromEntries(
    Object.entries(fields).filter((entry): entry is [string, string] => typeof entry[1] === 'string')
  );
}

function asHttpError(error: unknown): { status: number; message: string; code: string; body: Record<string, unknown> } | null {
  if (!error || typeof error !== 'object') {
    return null;
  }
  const candidate = error as { status?: number; error?: unknown };
  const status = typeof candidate.status === 'number' ? candidate.status : 0;
  const body = candidate.error && typeof candidate.error === 'object'
    ? candidate.error as Record<string, unknown>
    : {};
  return {
    status,
    message: typeof body['message'] === 'string' ? body['message'] : '',
    code: typeof body['code'] === 'string' ? body['code'] : '',
    body
  };
}
