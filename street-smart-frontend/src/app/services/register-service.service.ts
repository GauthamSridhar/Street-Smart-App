import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RegisterPayload } from '../model/registerPayload';
import { RegisterResponse } from '../model/RegisterResponse';
import { environment } from '../environment';
@Injectable({ providedIn: 'root' })
export class RegisterService {
  constructor(private http: HttpClient) {}
  registerUser(payload: RegisterPayload) {
    return this.http.post<RegisterResponse>(environment.apiBaseUrl + '/users/register', payload);
  }
  config() {
    return this.http.get<{ enabled: boolean }>(environment.apiBaseUrl + '/sms/config');
  }
  send(phoneNumber: string) {
    return this.http.post(environment.apiBaseUrl + '/sms/send', { phoneNumber });
  }
  verify(phoneNumber: string, otpCode: string) {
    return this.http.post<{ verificationToken: string }>(environment.apiBaseUrl + '/sms/verify', {
      phoneNumber,
      otpCode,
    });
  }
}
