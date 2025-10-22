import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, finalize, map, of, throwError } from 'rxjs';
import { LoadingService } from './loading.service';
import { ErrorService } from './error.service';

function getApiBase(): string {
  const base = (typeof window !== 'undefined' ? (window as any).RUNTIME_CONFIG?.API_BASE_URL : undefined) || '/api';
  return base.replace(/\/+$/,'');
}

// Prefix '/api' requests with runtime base URL
export const apiBaseUrlInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> => {
  if (/^\//.test(req.url) && req.url.startsWith('/api')) {
    const cloned = req.clone({ url: `${getApiBase()}${req.url.replace(/^\/api/, '')}` });
    return next(cloned);
  }
  return next(req);
};

// Global loading spinner
export const loadingInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> => {
  const loading = inject(LoadingService);
  loading.start();
  return next(req).pipe(finalize(() => loading.stop()));
};

// Error mapping to banner
export const errorInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> => {
  const errors = inject(ErrorService);
  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        const msg = err.error?.message || `${err.status} ${err.statusText || 'Erreur réseau'}`;
        errors.set(msg);
      } else {
        errors.set('Erreur inconnue');
      }
      return throwError(() => err);
    })
  );
};
