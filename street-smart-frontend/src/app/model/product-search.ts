import { ShopStatus } from './shop.model';
export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ProductMatch {
  id: string;
  name: string;
  available: boolean;
  shopId: string;
  shopName: string;
  category: string;
  address: string;
  latitude: number;
  longitude: number;
  shopStatus: ShopStatus;
  price?: number | null;
  currency?: string | null;
  updatedAt?: string;
}
export interface ProductQuery {
  fuzzy?: boolean;
  minPrice?: number;
  maxPrice?: number;
  currency?: string;
  latitude?: number;
  longitude?: number;
  radiusKm?: number;
  q: string;
  category: string;
  availableOnly: boolean;
  page: number;
  size: number;
}
