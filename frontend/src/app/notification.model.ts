export interface NotificationItem {
  id: number;
  message?: string; // primary text
  title?: string;   // optional alternative field used by backend
  createdAt: string; // ISO datetime
  read: boolean;
}
