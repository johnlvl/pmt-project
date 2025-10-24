import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginPageComponent } from './login.page';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';

describe('LoginPageComponent', () => {
  let fixture: ComponentFixture<LoginPageComponent>;
  let http: HttpTestingController;
  let routerNavigateSpy = jasmine.createSpy('navigate');
  let routerNavigateByUrlSpy = jasmine.createSpy('navigateByUrl');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginPageComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: { navigate: routerNavigateSpy, navigateByUrl: routerNavigateByUrlSpy } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({}) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPageComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('submits form and navigates to /projects on success', () => {
    const comp = fixture.componentInstance;
    comp.form.setValue({ email: 'alice@example.com', password: 'secret123' });
    comp.submit();
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/users/login'));
    req.flush({ id: 1, username: 'alice', email: 'alice@example.com' });
    expect(routerNavigateByUrlSpy).toHaveBeenCalledWith('/projects');
  });

  it('respects redirect query param after login', () => {
    // Recreate component with redirect param
    TestBed.resetTestingModule();
    routerNavigateSpy = jasmine.createSpy('navigate');
    routerNavigateByUrlSpy = jasmine.createSpy('navigateByUrl');
    TestBed.configureTestingModule({
      imports: [LoginPageComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: { navigate: routerNavigateSpy, navigateByUrl: routerNavigateByUrlSpy } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({ redirect: '/board' }) } } }
      ]
    }).compileComponents();
    const fx = TestBed.createComponent(LoginPageComponent);
    const http2 = TestBed.inject(HttpTestingController);
    fx.componentInstance.form.setValue({ email: 'a@b.c', password: 'xxxxxx' });
    fx.componentInstance.submit();
    const req = http2.expectOne(r => r.method==='POST' && r.url.endsWith('/api/users/login'));
    req.flush({ id: 2, username: 'a', email: 'a@b.c' });
    expect(routerNavigateByUrlSpy).toHaveBeenCalledWith('/board');
    http2.verify();
  });
});
