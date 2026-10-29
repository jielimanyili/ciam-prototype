import * as AuthSession from 'expo-auth-session';
import * as SecureStore from 'expo-secure-store';
import * as WebBrowser from 'expo-web-browser';

const ISSUER = 'http://localhost:8080/realms/ciam-demo';
const CLIENT_ID = 'mobile-identity';
const REFRESH_KEY = 'mobileIdentity.refreshToken';

// Lets the auth session complete in Expo Go / dev builds.
WebBrowser.maybeCompleteAuthSession();

let accessToken: string | null = null;

export function isSignedIn(): boolean {
  return accessToken !== null;
}

export function getAccessToken(): string | null {
  return accessToken;
}

export async function signIn(): Promise<void> {
  const discovery = await AuthSession.fetchDiscoveryAsync(ISSUER);
  if (!discovery) {
    throw new Error('OIDC discovery failed');
  }

  const redirectUri = AuthSession.makeRedirectUri({ scheme: 'mobileidentity', path: 'callback' });
  const request = new AuthSession.AuthRequest({
    clientId: CLIENT_ID,
    redirectUri,
    scopes: ['openid'],
    responseType: AuthSession.ResponseType.Code,
    usePKCE: true,
    codeChallengeMethod: AuthSession.CodeChallengeMethod.S256,
  });

  const result = await request.promptAsync(discovery);
  if (result.type !== 'success' || !request.codeVerifier) {
    throw new Error(result.type === 'cancel' ? 'Sign in cancelled' : `Sign in failed (${result.type})`);
  }

  const tokenResponse = await AuthSession.exchangeCodeAsync(
    {
      clientId: CLIENT_ID,
      redirectUri,
      code: result.params.code,
      extraParams: { code_verifier: request.codeVerifier },
    },
    discovery,
  );

  accessToken = tokenResponse.accessToken;
  if (tokenResponse.refreshToken) {
    await SecureStore.setItemAsync(REFRESH_KEY, tokenResponse.refreshToken);
  }
}

export async function signOut(): Promise<void> {
  accessToken = null;
  await SecureStore.deleteItemAsync(REFRESH_KEY);
}
