export const KEYCLOAK_BASE_URL = 'http://localhost:8081';
export const KEYCLOAK_REALM = 'piedrazul';
export const KEYCLOAK_CLIENT_ID = 'piedrazul-web';

export function keycloakTokenUrl(): string {
  return `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`;
}
