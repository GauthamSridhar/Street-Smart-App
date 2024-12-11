// src/app/models/favorite-response-dto.model.ts
export interface FavoriteResponseDTO {
    id: string;       // UUID as string
    userId: string;   // UUID as string
    shopId: string;   // UUID as string
    shopName: string; // Shop name returned by the backend
  }
  