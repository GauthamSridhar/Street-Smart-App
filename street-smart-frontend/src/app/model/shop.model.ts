import { ProductResponseDTO } from './product-response-dto.model';
export type ShopStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE' | 'INACTIVE';
export interface ShopImage {
  id: string;
  fileName: string;
  fileType: string;
  fileSizeInBytes: number;
  shopId: string;
}
export interface Shop {
  id: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  category: string;
  openingHours?: string | null;
  status: ShopStatus;
  ownerId: string;
  products: ProductResponseDTO[];
  images: ShopImage[];
  /** Product names from the current discovery page, used only for map marker context. */
  mapProductNames?: string[];
}
export type ShopEdit = Pick<
  Shop,
  'name' | 'description' | 'address' | 'latitude' | 'longitude' | 'category' | 'openingHours'
>;
