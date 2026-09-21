import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
import { UserResponse, UserEdit } from '../model/user-response.model';
@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
  me() {
    return this.http.get<UserResponse>(environment.apiBaseUrl + '/users/me');
  }
  getUser(id: string) {
    return this.http.get<UserResponse>(environment.apiBaseUrl + '/users/' + id);
  }
  update(id: string, value: UserEdit) {
    return this.http.put<UserResponse>(environment.apiBaseUrl + '/users/' + id, value);
  }
  sessions() {
    return this.http.get<Array<{ id: string; createdAt: string; expiresAt: string; current: boolean }>>(
      environment.apiBaseUrl + '/users/sessions',
    );
  }
  revokeSession(id: string) {
    return this.http.delete(environment.apiBaseUrl + '/users/sessions/' + id);
  }
  logoutAll() {
    return this.http.post(environment.apiBaseUrl + '/users/logout-all', null);
  }
}
