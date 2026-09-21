// src/app/models/add-product-request.model.ts

export interface AddProductRequest {
    name: string;
    description?: string;
    price?: number | null;
    currency?: string | null;
    available: boolean;
  }
  
