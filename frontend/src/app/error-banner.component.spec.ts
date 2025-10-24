import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorBannerComponent } from './error-banner.component';
import { ErrorService } from './error.service';

describe('ErrorBannerComponent', () => {
  let fixture: ComponentFixture<ErrorBannerComponent>;
  let component: ErrorBannerComponent;
  let svc: ErrorService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorBannerComponent]
    }).compileComponents();
    svc = TestBed.inject(ErrorService);
    fixture = TestBed.createComponent(ErrorBannerComponent);
    component = fixture.componentInstance;
  });

  it('renders message and clears on click', () => {
    spyOn(svc, 'clear').and.callThrough();
    svc.set('Oops');
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Oops');
    const btn = el.querySelector('button') as HTMLButtonElement;
    btn.click();
    expect(svc.clear).toHaveBeenCalled();
  });
});
