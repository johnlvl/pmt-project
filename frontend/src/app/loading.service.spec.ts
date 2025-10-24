import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let svc: LoadingService;

  beforeEach(() => {
    svc = new LoadingService();
  });

  it('emits true only on first start and false when counter returns to 0', () => {
    const emissions: boolean[] = [];
    const sub = svc.loading$.subscribe(v => emissions.push(v));

    // initial value is false
    expect(emissions[0]).toBeFalse();

    svc.start(); // counter=1 -> emits true
    svc.start(); // counter=2 -> no emission expected
    svc.stop();  // counter=1 -> no emission expected
    svc.stop();  // counter=0 -> emits false

    // Expect sequence: false (initial), true (first start), false (when back to 0)
    expect(emissions).toEqual([false, true, false]);
    sub.unsubscribe();
  });

  it('stop when counter is already 0 does not go negative (may emit false again)', () => {
    const emissions: boolean[] = [];
    const sub = svc.loading$.subscribe(v => emissions.push(v));
    expect(emissions).toEqual([false]);

    svc.stop(); // counter stays at 0, service emits false again per implementation
    expect(emissions).toEqual([false, false]);
    sub.unsubscribe();
  });
});
