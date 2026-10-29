import { useCallback, useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, Button, ScrollView, StyleSheet, Text, View } from 'react-native';

import * as auth from './services/auth';
import * as api from './services/api';

export default function App() {
  const [signedIn, setSignedIn] = useState(auth.isSignedIn());
  const [claims, setClaims] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const handleSignIn = useCallback(async () => {
    setBusy(true);
    setError('');
    try {
      await auth.signIn();
      setSignedIn(true);
      setClaims(await api.me());
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }, []);

  const handleSignOut = useCallback(async () => {
    await auth.signOut();
    setSignedIn(false);
    setClaims(null);
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>CIAM — Mobile Identity</Text>
      <Text style={styles.subtitle}>Authorization Code + PKCE</Text>

      {!signedIn ? (
        <View style={styles.card}>
          <Button title="Sign in with CIAM" onPress={handleSignIn} disabled={busy} />
          {busy && <ActivityIndicator style={styles.busy} />}
        </View>
      ) : (
        <View style={styles.card}>
          <Text style={styles.subtitle}>Signed in — /api/me claims:</Text>
          <ScrollView style={styles.claims}>
            <Text style={styles.claimsText}>{claims ? JSON.stringify(claims, null, 2) : 'Loading…'}</Text>
          </ScrollView>
          <Button title="Sign out" onPress={handleSignOut} />
        </View>
      )}

      {!!error && <Text style={styles.error}>{error}</Text>}
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  title: { fontSize: 22, fontWeight: '700' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 16 },
  card: { width: '100%', maxWidth: 420 },
  busy: { marginTop: 16 },
  claims: {
    maxHeight: 240,
    marginVertical: 12,
    backgroundColor: '#f4f4f4',
    borderRadius: 6,
    padding: 8,
  },
  claimsText: { fontFamily: 'monospace', fontSize: 12 },
  error: { color: '#b00020', marginTop: 16 },
});

