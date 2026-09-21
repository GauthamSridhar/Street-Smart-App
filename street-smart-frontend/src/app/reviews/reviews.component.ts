import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReviewService } from '../services/review.service';
import { ShopService } from '../services/shop.service';
import { SessionService } from '../services/session.service';
import { Review } from '../model/review.model';
import { apiError } from '../services/api-error';
@Component({
  selector: 'app-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css'],
})
export class ReviewsComponent implements OnInit, OnDestroy {
  reviews: Review[] = [];
  sortOption = 'recent';
  shopId = '';
  errorMessage = '';
  page = 0;
  loading = false;
  private subscriptions = new Subscription();
  constructor(
    private reviewService: ReviewService,
    private shops: ShopService,
    private session: SessionService,
  ) {}
  ngOnInit(): void {
    this.subscriptions.add(
      this.shops.getShopByShopkeeperId(this.session.id).subscribe({
        next: (shop) => {
          this.shopId = shop.id;
          this.fetchReviews();
        },
        error: (error) => {
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  fetchReviews(): void {
    this.loading = true;
    this.subscriptions.add(
      this.reviewService.getReviewsByShop(this.shopId, this.page).subscribe({
        next: (reviews) => {
          this.reviews = reviews;
          this.sortReviews();
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.loading = false;
        },
      }),
    );
  }
  move(delta: number): void {
    if (!this.loading) {
      this.page += delta;
      this.fetchReviews();
    }
  }
  sortReviews(): void {
    this.reviews.sort((a, b) =>
      this.sortOption === 'high-to-low'
        ? b.rating - a.rating
        : this.sortOption === 'low-to-high'
          ? a.rating - b.rating
          : new Date(b.date).getTime() - new Date(a.date).getTime(),
    );
  }
  getStarsArray(rating: number): number[] {
    return Array(Math.max(0, Math.min(5, rating))).fill(0);
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
