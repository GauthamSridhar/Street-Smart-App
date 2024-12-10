// src/app/models/add-product-request.model.ts

export interface AddProductRequest {
    name: string;
    description?: string;
    available: boolean;
  }
  