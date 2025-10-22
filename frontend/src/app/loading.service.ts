import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private counter = 0;
  private readonly _loading$ = new BehaviorSubject<boolean>(false);
  readonly loading$ = this._loading$.asObservable();

  start() {
    this.counter++;
    if (this.counter === 1) this._loading$.next(true);
  }

  stop() {
    if (this.counter > 0) this.counter--;
    if (this.counter === 0) this._loading$.next(false);
  }
}
