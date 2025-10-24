import { SessionService } from './session.service';

describe('SessionService', () => {
  let svc: SessionService;

  beforeEach(() => {
    // Ensure clean localStorage and runtime config
    window.localStorage.removeItem('userEmail');
    (window as any).RUNTIME_CONFIG = undefined;
    svc = new SessionService();
  });

  it('isLoggedIn false by default and true when email is set', () => {
    expect(svc.isLoggedIn).toBeFalse();
    svc.email = 'alice@example.com';
    expect(svc.isLoggedIn).toBeTrue();
    expect(svc.email).toBe('alice@example.com');
    expect(svc.currentEmailOrNull).toBe('alice@example.com');
  });

  it('logout clears email and logged-in state', () => {
    svc.email = 'bob@example.com';
    expect(svc.isLoggedIn).toBeTrue();
    svc.logout();
    expect(svc.isLoggedIn).toBeFalse();
    expect(svc.currentEmailOrNull).toBeNull();
    expect(svc.email).toBe('');
  });

  it('uses runtime config email when provided', () => {
    (window as any).RUNTIME_CONFIG = { USER_EMAIL: 'carol@example.com' };
    // Create a fresh instance to read runtime config
    svc = new SessionService();
    expect(svc.isLoggedIn).toBeTrue();
    expect(svc.email).toBe('carol@example.com');
  });
});
