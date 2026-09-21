import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginForm } from '../model/loginForm';
import { environment } from '../environment';
import { LoginResponse } from '../model/loginResponse';
@Injectable({ providedIn: 'root' })
export class LoginService {
  constructor(private http: HttpClient) {}
  validateUser(credentials: LoginForm) {
    return this.http.post<LoginResponse>(environment.apiBaseUrl + '/users/login', credentials);
  }
}
