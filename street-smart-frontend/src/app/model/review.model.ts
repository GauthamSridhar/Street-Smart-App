// src/app/models/review.model.ts
export interface Review {
  id: string; // UUID as string
  userId: string; // UUID as string
  userName?: string; // Optional: Will be populated after fetching user details
  rating: number;
  review: string;
  date: string; // ISO date string (use created_at or updated_at as per your backend)
    avatar: string; // URL to avatar image

}
