import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { MyInvitationsPageComponent } from './my-invitations.page';
import { InvitationService } from './invitation.service';

describe('MyInvitationsPageComponent', () => {
  it('should create and load invitations', () => {
    const svcMock = {
      listMine: jasmine.createSpy('listMine').and.returnValue(of([{ id:1, projectId:42, projectName:'Demo', email:'u@e.com', status:'PENDING', createdAt: new Date().toISOString() }]))
    } as Partial<InvitationService> as InvitationService;

    TestBed.configureTestingModule({
      imports: [MyInvitationsPageComponent, RouterTestingModule],
      providers: [{ provide: InvitationService, useValue: svcMock }]
    });

    const fixture = TestBed.createComponent(MyInvitationsPageComponent);
    fixture.detectChanges();

    const comp = fixture.componentInstance;
    expect(comp).toBeTruthy();
    expect(svcMock.listMine).toHaveBeenCalled();
    expect(comp.invitations.length).toBe(1);
  });
});
