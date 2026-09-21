// src/app/models/product-response-dto.model.ts

export interface ProductResponseDTO {
    description?: string;
    price?: number | null;
    currency?: string | null;
    updatedAt?: string;
    id: string;
    name: string;
    available: boolean;
    shopId: string;
  }
  
