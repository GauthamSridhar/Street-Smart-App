export interface Review {
  id: string;
  userId: string;
  shopId: string;
  rating: number;
  review: string;
  date: string;
  userName?: string;
  avatar?: string;
}
