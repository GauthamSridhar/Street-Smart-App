import { Injectable } from '@angular/core';
import { LoginResponse } from '../model/loginResponse';
export type Role = 'USER' | 'SHOPKEEPER' | 'ADMIN';
@Injectable({ providedIn: 'root' })
export class SessionService {
  get token(): string | null {
    const token = sessionStorage.getItem('tokenId');
    if (!token) return null;
    try {
      const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const claims = JSON.parse(atob(part.padEnd(Math.ceil(part.length / 4) * 4, '=')));
      if (!Number.isFinite(claims.exp) || claims.exp * 1000 <= Date.now())
        throw new Error('Expired session');
      return token;
    } catch {
      this.clear();
      return null;
    }
  }
  get id(): string {
    return this.token ? sessionStorage.getItem('id') || '' : '';
  }
  get role(): Role | null {
    const role = this.token ? sessionStorage.getItem('role') : null;
    return role === 'USER' || role === 'SHOPKEEPER' || role === 'ADMIN' ? role : null;
  }
  save(login: LoginResponse): void {
    this.clear();
    sessionStorage.setItem('tokenId', login.jwt);
    sessionStorage.setItem('id', login.id);
    sessionStorage.setItem('role', login.role);
    sessionStorage.setItem('username', login.username);
    if (!this.token || !this.role || !this.id) {
      this.clear();
      throw new Error('Invalid session response');
    }
  }
  clear(): void {
    for (const key of ['tokenId', 'id', 'role', 'username', 'shopId'])
      sessionStorage.removeItem(key);
  }
  get home(): string {
    return this.role === 'ADMIN'
      ? '/admin-dashboard'
      : this.role === 'SHOPKEEPER'
        ? '/shop-dashboard'
        : '/dashboard';
  }
}
