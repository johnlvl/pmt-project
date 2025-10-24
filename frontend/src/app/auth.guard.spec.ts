import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { authGuard } from './auth.guard';
import { SessionService } from './session.service';

describe('authGuard', () => {
  function runGuard(): any {
    const state = { url: '/protected' } as any;
    return TestBed.runInInjectionContext(() => authGuard({} as any, state));
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])],
      providers: [
        { provide: SessionService, useValue: { get isLoggedIn() { return false; } } }
      ]
    });
  });

  it('allows navigation when logged in', () => {
    TestBed.overrideProvider(SessionService, { useValue: { get isLoggedIn() { return true; } } });
    const result = runGuard();
    expect(result).toBeTrue();
  });

  it('redirects to /login when not logged in', () => {
    const router = TestBed.inject(Router);
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    const tree = result as UrlTree;
    const url = router.serializeUrl(tree);
    expect(url.startsWith('/login')).toBeTrue();
    expect(url).toContain('redirect=%2Fprotected');
  });
});
