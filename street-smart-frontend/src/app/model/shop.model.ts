export interface Shop {
  id: string;
  name: string;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  ratings: number[];
  category: string;
  // Backend doesn't provide images, we'll add them later
  images?: { url: string }[];
}
