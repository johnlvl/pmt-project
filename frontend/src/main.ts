import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { apiBaseUrlInterceptor, errorInterceptor, loadingInterceptor } from './app/http-interceptors';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(
      withInterceptors([
        apiBaseUrlInterceptor,
        loadingInterceptor,
        errorInterceptor
      ])
    ),
    provideRouter(routes)
  ]
}).catch(err => console.error(err));
