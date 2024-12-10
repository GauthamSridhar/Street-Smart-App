// src/app/model/registerResponse.ts

export interface RegisterResponse {
    message: string;
    id: string;
    email: string;
    fullName: string;
    phoneNumber: string;
    ratings: any[]; // Adjust the type based on actual structure
    favorites: any[]; // Adjust the type based on actual structure
  }
  