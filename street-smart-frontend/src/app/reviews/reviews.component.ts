// src/app/reviews/reviews.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReviewService } from '../services/review.service';
import { Review } from '../model/review.model';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-reviews',
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class ReviewsComponent implements OnInit, OnDestroy {
  reviews: Review[] = [];
  sortOption: string = 'recent';
  private subscription: Subscription = new Subscription();
  shopId: string = '';

  ngOnInit(): void {
    this.shopId = sessionStorage.getItem('shopId') || '';
    if (this.shopId) {
      this.fetchReviews();
    }
  }

  constructor(private reviewService: ReviewService, private route: ActivatedRoute) {}

  /**
   * Fetch reviews for the shop.
   */
  fetchReviews(): void {
    this.subscription.add(
      this.reviewService.getReviewsByShop(this.shopId).subscribe(
        (data: Review[]) => {
          this.reviews = data.map(review => ({
            ...review,
            avatar: `https://i.pravatar.cc/150?img=${Math.floor(Math.random() * 70) + 1}`
          }));
          this.sortReviews();
        },
        (error) => {
          console.error('Failed to fetch reviews:', error);
        }
      )
    );
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

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
