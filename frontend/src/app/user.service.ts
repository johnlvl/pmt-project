import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserRegistrationDto {
  username: string;
  email: string;
  password: string;
}

export interface UserResponseDto {
  id: number;
  username: string;
  email: string;
}

export interface UserLoginDto {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  register(dto: UserRegistrationDto): Observable<UserResponseDto> {
    return this.http.post<UserResponseDto>(`/api/users/register`, dto);
  }

  login(dto: UserLoginDto): Observable<UserResponseDto> {
    return this.http.post<UserResponseDto>(`/api/users/login`, dto);
  }
}
