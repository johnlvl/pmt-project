import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProjectMemberService } from './project-member.service';

describe('ProjectMemberService', () => {
  let svc: ProjectMemberService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    svc = TestBed.inject(ProjectMemberService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists members', () => {
    svc.list(1).subscribe(ms => expect(ms).toBeTruthy());
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/projects/1/members'));
    req.flush([]);
  });

  it('invites a member', () => {
    svc.invite(1, { email: 'a@b.com', role: 'MEMBER' }).subscribe(() => expect(true).toBeTrue());
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/projects/1/invitations'));
    req.flush({});
  });

  it('changes role', () => {
    svc.changeRole(1, 2, 'OWNER').subscribe(() => expect(true).toBeTrue());
    const req = http.expectOne(r => r.method==='PATCH' && r.url.endsWith('/api/projects/1/members/2'));
    req.flush({});
  });

  it('removes a member', () => {
    svc.remove(1, 2).subscribe(() => expect(true).toBeTrue());
    const req = http.expectOne(r => r.method==='DELETE' && r.url.endsWith('/api/projects/1/members/2'));
    req.flush({});
  });
});
