// src/app/models/user-response.model.ts
export interface UserResponse {
    id: string; // UUID as string
    email: string;
    fullName: string;
    phoneNumber: string;
    ratings: string[]; // Array of UUIDs as strings
    favorites: string[]; // Array of UUIDs as strings
  }
  