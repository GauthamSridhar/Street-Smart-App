import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';

interface Review {
  userName: string;
  rating: number;
  comment: string;
  date: string;
  avatar: string; // URL to avatar image
}

interface RatingCount {
  value: number;
  count: number;
}

@Component({
  selector: 'app-reviews',
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css'],
  standalone: true,
  imports:[CommonModule,FormsModule,NavbarComponent]
})
export class ReviewsComponent implements OnInit {
  reviews: Review[] = [];
  sortOption: string = 'recent';
  ratings: RatingCount[] = [];

  ngOnInit(): void {
    this.fetchReviews();
  }

  /**
   * Fetch reviews for the shop.
   * Replace the dummy data with backend logic when ready.
   */
  fetchReviews(): void {
    // Backend API logic (commented for future implementation)
    /*
    this.http.get<Review[]>('https://your-backend-api.com/api/reviews')
      .subscribe((data) => {
        this.reviews = data;
        this.calculateRatings();
        this.sortReviews(); // Apply initial sorting
      }, error => {
        console.error('Failed to fetch reviews:', error);
      });
    */

    // Dummy data for now
    this.reviews = [
      {
        userName: 'John Doe',
        rating: 5,
        comment: 'Excellent service and great products!',
        date: '2024-12-01',
        avatar: 'https://i.pravatar.cc/150?img=1',
      },
      {
        userName: 'Jane Smith',
        rating: 4,
        comment: 'Loved the experience, but room for improvement.',
        date: '2024-12-02',
        avatar: 'https://i.pravatar.cc/150?img=2',
      },
      {
        userName: 'Alice Brown',
        rating: 3,
        comment: 'Average service, not too bad.',
        date: '2024-12-03',
        avatar: 'https://i.pravatar.cc/150?img=3',
      },
      {
        userName: 'Bob Green',
        rating: 2,
        comment: 'Could have been better. Not satisfied.',
        date: '2024-12-04',
        avatar: 'https://i.pravatar.cc/150?img=4',
      },
      {
        userName: 'Charlie Black',
        rating: 1,
        comment: 'Very poor service. Highly disappointed.',
        date: '2024-12-05',
        avatar: 'https://i.pravatar.cc/150?img=5',
      },
      // Add more dummy reviews as needed
    ];

    this.calculateRatings();
    this.sortReviews(); // Apply initial sorting
  }

  /**
   * Calculate the count of each rating (1 to 5 stars).
   */
  calculateRatings(): void {
    const counts = [0, 0, 0, 0, 0]; // Index 0 for 1 star, ..., Index 4 for 5 stars
    this.reviews.forEach(review => {
      if (review.rating >= 1 && review.rating <=5) {
        counts[review.rating - 1]++;
      }
    });

    this.ratings = counts.map((count, index) => ({
      value: index + 1,
      count: count
    }));
  }

  /**
   * Sort reviews based on the selected sort option.
   */
  sortReviews(): void {
    if (this.sortOption === 'high-to-low') {
      this.reviews.sort((a, b) => b.rating - a.rating);
    } else if (this.sortOption === 'low-to-high') {
      this.reviews.sort((a, b) => a.rating - b.rating);
    } else if (this.sortOption === 'recent') {
      this.reviews.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    }
  }

  /**
   * Generates an array for the number of stars to display.
   * @param rating - The rating value.
   * @returns number[] - An array representing stars.
   */
  getStarsArray(rating: number): number[] {
    return Array(rating).fill(0);
  }

  /**
   * Calculate percentage for the rating bar.
   * @param count - Number of reviews for the rating.
   * @returns number - Percentage width.
   */
  calculatePercentage(count: number): number {
    const total = this.reviews.length;
    return total === 0 ? 0 : (count / total) * 100;
  }
}
