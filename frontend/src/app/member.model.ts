// Aligne les noms de rôles sur le backend et l'énoncé (FR)
export type ProjectRole = 'Administrateur' | 'Membre' | 'Observateur';

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
