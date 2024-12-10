// src/app/models/shop-response.model.ts

export interface Product {
    id: string;
    name: string;
    available: boolean;
    shopId: string | null;
  }
  
  export interface Image {
    url: any;
    // Currently empty since images array is empty in the response
  }
  
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
    ratings: any[];
  }
  