// El navegador pide el token al mismo origen. ng serve lo reenvía a Keycloak.
export const KEYCLOAK_BASE_URL = '';
export const KEYCLOAK_REALM = 'piedrazul';
export const KEYCLOAK_CLIENT_ID = 'piedrazul-web';

export function keycloakTokenUrl(): string {
  return `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`;
}
