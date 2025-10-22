export type ProjectRole = 'OWNER' | 'MAINTAINER' | 'MEMBER';

export interface ProjectMember {
  userId: number;
  name: string;
  email: string;
  role: ProjectRole;
}

export interface InviteMemberDto {
  email: string;
  role: ProjectRole;
}
