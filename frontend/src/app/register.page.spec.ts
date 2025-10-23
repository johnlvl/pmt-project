import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterPageComponent } from './register.page';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';

describe('RegisterPageComponent', () => {
  let fixture: ComponentFixture<RegisterPageComponent>;
  let http: HttpTestingController;
  let routerNavigateSpy = jasmine.createSpy('navigate');
  let routerNavigateByUrlSpy = jasmine.createSpy('navigateByUrl');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterPageComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: { navigate: routerNavigateSpy, navigateByUrl: routerNavigateByUrlSpy } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({}) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterPageComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('submits form and navigates to /projects on success', () => {
    const comp = fixture.componentInstance;
    comp.form.setValue({ username: 'alice', email: 'alice@example.com', password: 'secret123', confirmPassword: 'secret123' });
    comp.submit();
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/users/register'));
    req.flush({ id: 1, username: 'alice', email: 'alice@example.com' });
    expect(routerNavigateByUrlSpy).toHaveBeenCalledWith('/projects');
  });

  it('prevents submit when passwords do not match', () => {
    const comp = fixture.componentInstance;
    comp.form.setValue({ username: 'alice', email: 'alice@example.com', password: 'secret123', confirmPassword: 'different' });
    comp.submit();
    // No HTTP call should be made when invalid
    http.expectNone(() => true);
    expect(comp.form.invalid).toBeTrue();
  });

  it('redirects to target after successful signup when redirect param is present', () => {
    // Recreate component with redirect param
    TestBed.resetTestingModule();
    routerNavigateSpy = jasmine.createSpy('navigate');
    routerNavigateByUrlSpy = jasmine.createSpy('navigateByUrl');
    TestBed.configureTestingModule({
      imports: [RegisterPageComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: { navigate: routerNavigateSpy, navigateByUrl: routerNavigateByUrlSpy } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({ redirect: '/projects/123' }) } } }
      ]
    }).compileComponents();
    const fx = TestBed.createComponent(RegisterPageComponent);
    const http2 = TestBed.inject(HttpTestingController);
    fx.componentInstance.form.setValue({ username: 'bob', email: 'bob@example.com', password: 'secret123', confirmPassword: 'secret123' });
    fx.componentInstance.submit();
    const req = http2.expectOne(r => r.method==='POST' && r.url.endsWith('/api/users/register'));
    req.flush({ id: 10, username: 'bob', email: 'bob@example.com' });
    expect(routerNavigateByUrlSpy).toHaveBeenCalledWith('/projects/123');
    http2.verify();
  });
});
