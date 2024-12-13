// src/app/models/shop-response.model.ts

export interface Product {
  id: string;
  name: string;
  available: boolean;
  shopId: string | null;
}

export interface Image {
  url: string; // Changed from 'any' to 'string' for better type safety
  altText?: string; // Optional property for accessibility
}

export interface Rating {
  score: number; // e.g., 1-5 scale
  reviewer: string; // e.g., userId or name
}

// Interface representing the backend response
export interface Shop {
  id: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  category: string;
  // Optional properties that might be missing in the backend response
  status?: string; 
  ownerId?: string;
  products?: Product[];
  images?: Image[];
  ratings?: Rating[];
}

// Interface representing what the frontend expects
export interface ShopResponse {
  id: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  status: string; 
  ownerId: string;
  category: string;
  products: Product[];
  images: Image[];
  ratings: Rating[];
}
