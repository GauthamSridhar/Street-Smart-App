import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnDestroy,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of, Subscription, Observable } from 'rxjs';
import { Shop } from '../model/shop.model';
import { Review } from '../model/review.model';
import { ReviewService } from '../services/review.service';
import { FavoritesService } from '../services/favorite-service.service';
import { SessionService } from '../services/session.service';
import { apiError } from '../services/api-error';
import { ShopImageComponent } from './shop-image.component';
@Component({
  selector: 'app-shop-details',
  standalone: true,
  imports: [CommonModule, FormsModule, ShopImageComponent],
  templateUrl: './shop-details.component.html',
  styleUrls: ['./shop-details.component.css'],
})
export class ShopDetailsComponent implements OnChanges, OnDestroy {
  @Input() shop!: Shop;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() navigateToShop = new EventEmitter<Shop>();
  activeTab = 'Overview';
  tabs = ['Overview', 'Reviews', 'Images', 'Products'];
  isFavorite = false;
  favoriteReady = false;
  reviews: Review[] = [];
  userReview: Review | null = null;
  reviewForm = { rating: 5, review: '' };
  isEditingReview = false;
  isAddingReview = false;
  loadingReviews = false;
  showAvailableProducts = true;
  busy = false;
  errorMessage = '';
  reportMessage = '';
  summary = { average: 0, count: 0 };
  reviewPage = 0;
  private subscriptions = new Subscription();
  constructor(
    private reviewService: ReviewService,
    private favorites: FavoritesService,
    public session: SessionService,
  ) {}
  get currentUserId(): string {
    return this.session.id;
  }
  get canReview(): boolean {
    return this.session.role === 'USER';
  }
  get filteredProducts() {
    return this.shop.products.filter((p) => p.available === this.showAvailableProducts);
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['shop'] && this.shop) {
      this.subscriptions.unsubscribe();
      this.subscriptions = new Subscription();
      this.reviews = [];
      this.userReview = null;
      this.summary = { average: 0, count: 0 };
      this.activeTab = 'Overview';
      this.reviewPage = 0;
      this.errorMessage = '';
      this.busy = false;
      this.favoriteReady = false;
      this.isFavorite = false;
      this.cancelReviewForm();
      this.fetchReviews();
      if (this.canReview)
        this.subscriptions.add(
          this.favorites.isFavorite(this.shop.id, this.currentUserId).subscribe({
            next: (favorite) => {
              this.isFavorite = favorite;
              this.favoriteReady = true;
            },
            error: (error) => {
              this.errorMessage = apiError(error);
            },
          }),
        );
    }
  }
  fetchReviews(): void {
    this.loadingReviews = true;
    this.subscriptions.add(
      forkJoin({
        reviews: this.reviewService.getReviewsByShop(this.shop.id, this.reviewPage),
        summary: this.reviewService.summary(this.shop.id),
        mine: this.canReview ? this.reviewService.mine(this.shop.id) : of(null),
      }).subscribe({
        next: (result) => {
          this.reviews = result.reviews;
          this.summary = result.summary;
          this.userReview = result.mine;
          this.loadingReviews = false;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.loadingReviews = false;
        },
      }),
    );
  }
  pageReviews(delta: number): void {
    if (this.loadingReviews) return;
    this.reviewPage += delta;
    this.fetchReviews();
  }
  toggleFavorite(): void {
    if (!this.canReview || !this.favoriteReady || this.busy) return;
    const desired = !this.isFavorite;
    this.busy = true;
    this.errorMessage = '';
    const request: Observable<unknown> = desired
      ? this.favorites.addFavorite(this.shop.id, this.currentUserId)
      : this.favorites.removeFavoriteShop(this.shop.id, this.currentUserId);
    this.subscriptions.add(
      request.subscribe({
        next: () => {
          this.isFavorite = desired;
          this.busy = false;
        },
        error: (error) => {
          this.errorMessage = apiError(error);
          this.busy = false;
        },
      }),
    );
  }
  startAddingReview(): void {
    this.isAddingReview = true;
    this.reviewForm = { rating: 5, review: '' };
  }
  toggleEditMode(): void {
    if (this.isEditingReview) this.cancelReviewForm();
    else if (this.userReview) {
      this.isEditingReview = true;
      this.reviewForm = { rating: this.userReview.rating, review: this.userReview.review };
    }
  }
  cancelReviewForm(): void {
    this.isEditingReview = false;
    this.isAddingReview = false;
    this.reviewForm = { rating: 5, review: '' };
  }
  submitReview(): void {
    if (!this.canReview || this.busy) return;
    const value = { rating: Number(this.reviewForm.rating), review: this.reviewForm.review.trim() };
    if (
      !Number.isInteger(value.rating) ||
      value.rating < 1 ||
      value.rating > 5 ||
      value.review.length > 2000
    ) {
      this.errorMessage =
        'Choose a rating from 1 to 5 and a review no longer than 2000 characters.';
      return;
    }
    this.busy = true;
    this.errorMessage = '';
    const request =
      this.isEditingReview && this.userReview
        ? this.reviewService.updateReview(this.currentUserId, this.userReview.id, value)
        : this.reviewService.addReview(this.shop.id, value);
    this.subscriptions.add(
      request.subscribe({
        next: () => {
          this.busy = false;
          this.cancelReviewForm();
          this.reviewPage = 0;
          this.fetchReviews();
        },
        error: (error) => {
          this.busy = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  deleteReview(): void {
    if (!this.userReview || this.busy || !confirm('Delete your review?')) return;
    this.busy = true;
    this.subscriptions.add(
      this.reviewService.deleteReview(this.shop.id, this.userReview.id).subscribe({
        next: () => {
          this.busy = false;
          this.cancelReviewForm();
          this.reviewPage = 0;
          this.fetchReviews();
        },
        error: (error) => {
          this.busy = false;
          this.errorMessage = apiError(error);
        },
      }),
    );
  }
  reportReview(review: Review): void {
    if (!this.canReview || review.userId === this.currentUserId || this.busy) return;
    const reason = window.prompt('Why should this review be reviewed?');
    if (!reason?.trim()) return;
    if (reason.trim().length > 500) { this.errorMessage = 'Use at most 500 characters for the report reason.'; return; }
    this.errorMessage = '';
    this.reportMessage = '';
    this.busy = true;
    this.reviewService.reportReview(review.id, reason.trim()).subscribe({
      next: () => { this.busy = false; this.reportMessage = 'Review reported for moderation.'; },
      error: (error) => { this.busy = false; this.errorMessage = apiError(error); },
    });
  }
  filterProducts(status: 'available' | 'unavailable'): void {
    this.showAvailableProducts = status === 'available';
  }
  onNavigate(): void {
    this.navigateToShop.emit(this.shop);
  }
  closeSidebar(): void {
    this.close.emit();
  }
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
