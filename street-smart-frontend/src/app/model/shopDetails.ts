// src/app/model/shopDetails.ts

export interface ShopDetails {
  id: string; // UUID of the shop
  name: string; // Name of the shop
  description: string; // Description of the shop
  address: string; // Address of the shop
  latitude: number; // Latitude of the shop's location
  longitude: number; // Longitude of the shop's location
  status: string; // Status of the shop (e.g., Verified, Pending, Rejected)
  ownerId: string; // UUID of the owner/shopkeeper
  products: ProductResponseDTO[]; // List of product response DTOs
  images: ImageResponseDTO[]; // List of image response DTOs
  ratings: string[]; // List of UUIDs representing ratings
}

// Define ProductResponseDTO (if not already defined)
export interface ProductResponseDTO {
  id: string; // UUID of the product
  name: string; // Name of the product
  description: string; // Description of the product
  price: number; // Price of the product
  // Add additional fields if needed
}

// Define ImageResponseDTO (if not already defined)
export interface ImageResponseDTO {
  id: string; // UUID of the image
  url: string; // URL of the image
  // Add additional fields if needed
}
