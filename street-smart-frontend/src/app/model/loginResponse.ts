// src/app/model/loginResponse.ts

export interface LoginResponse {
  jwt: string;
  username: string;
  role: 'USER' | 'SHOPKEEPER' | 'ADMIN';
  id: string; // Unique identifier for the user
}
