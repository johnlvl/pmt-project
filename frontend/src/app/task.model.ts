export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface TaskItem {
  id: number;
  projectId: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority?: TaskPriority;
  assigneeEmail?: string;
  assigneeId?: number;
  dueDate?: string; // ISO date
}

export interface CreateTaskDto {
  projectId: number;
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeEmail?: string;
  assigneeId?: number;
  dueDate?: string;
}

export interface UpdateTaskDto {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeEmail?: string;
  assigneeId?: number;
  dueDate?: string;
}

export interface TaskHistoryItem {
  id: number;
  taskId: number;
  type?: 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'STATUS_CHANGED' | 'COMMENT' | string;
  message?: string;
  createdAt: string; // ISO datetime
  actorName?: string;
  actorId?: number;
}
