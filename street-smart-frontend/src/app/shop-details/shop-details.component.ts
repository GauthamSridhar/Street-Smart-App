// src/app/shop-details/shop-details.component.ts

import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Shop } from '../model/shop.model';
import { FormsModule } from '@angular/forms';
import { Review } from '../model/review.model'; // Use the new Review model
import { ReviewService } from '../services/review.service';
import { FavoritesService } from '../services/favorite-service.service';
import { Subscription } from 'rxjs';
import { ProductResponseDTO } from '../model/product-response-dto.model';
import { ProductsService } from '../services/products.service';

@Component({
  selector: 'app-shop-details',
  templateUrl: './shop-details.component.html',
  styleUrls: ['./shop-details.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class ShopDetailsComponent implements OnInit, OnDestroy, OnChanges {
  @Input() shop!: Shop;
  @Input() isOpen = false;

  @Output() close = new EventEmitter<void>();
  @Output() navigateToShop = new EventEmitter<Shop>();
  @Output() cancelNavigationEvent = new EventEmitter<void>();

  activeTab = 'Overview';
  tabs = ['Overview', 'Reviews', 'Images', 'Products'];

  navigationActive = false;
  isFavorite = false;

  reviews: Review[] = [];
  userReview: Review | null = null;
  
  // Adjusted to match Review model: replace `comment` with `review`
  reviewForm: Partial<Review> = { rating: 5, review: '' };

  isEditingReview = false;
  isAddingReview = false;
  loadingReviews = false;
  products: ProductResponseDTO[] = []; // Replace the hardcoded array with this dynamic list
loadingProducts: boolean = false; // Add a loading state for products
  currentUserId: string = sessionStorage.getItem('id') || ''; // Example user ID
  currentUserName: string = 'John Doe'; // Example user name


  showAvailableProducts = true;

  private subscriptions: Subscription[] = [];

  constructor(
    private reviewService: ReviewService,
    private favoritesService: FavoritesService,
    private productService: ProductsService
  ) {}

  ngOnInit(): void {
    if (this.shop) {
      this.checkIfFavorite();
      this.fetchReviews();
      this.getCurrentUser();
      this.fetchProducts();
      this.filterProducts('available');

    }
  }
  filterProducts(status: 'available' | 'unavailable'): void {
    this.showAvailableProducts = status === 'available';
  }

  get filteredProducts() {
    return this.products.filter(
      (product) => product.available === this.showAvailableProducts
    );
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['shop'] && !changes['shop'].firstChange) {
      this.fetchReviews();
      this.fetchProducts(); // Refetch products if the shop changes
      this.checkIfFavorite();
    }
    if (changes['isOpen']) {
      if (changes['isOpen'].currentValue === false) {
        this.resetNavigationState();
      } else if (changes['isOpen'].currentValue === true) {
        this.navigationActive = false;
        console.log('Sidebar opened. Navigation state reset.');
      }
    }

    if (changes['shop'] && !changes['shop'].firstChange) {
      this.fetchReviews();
      this.checkIfFavorite();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  calculateAverageRating(ratings: number[]): number {
    return ratings.length ? ratings.reduce((a, b) => a + b, 0) / ratings.length : 0;
  }

  onNavigate(): void {
    this.navigateToShop.emit(this.shop);
    this.navigationActive = true;
    console.log('Navigation initiated.');
  }

  onCancelNavigation(): void {
    this.cancelNavigationEvent.emit();
    this.navigationActive = false;
  }

  checkIfFavorite(): void {
    if (!this.shop?.id) return;
    const sub = this.favoritesService.isFavorite(this.shop.id, this.currentUserId).subscribe({
      next: (isFav) => this.isFavorite = isFav,
      error: (err) => console.error('Error checking favorite:', err)
    });
    this.subscriptions.push(sub);
  }

  toggleFavorite(): void {
    if (!this.shop?.id) return;

    const action$ = this.isFavorite
      ? this.favoritesService.removeFavoriteShop(this.shop.id, this.currentUserId)
      : this.favoritesService.addFavorite(this.shop.id, this.currentUserId);

    const sub = action$.subscribe({
      next: () => {
        this.isFavorite = !this.isFavorite;
        console.log(`Shop is now ${this.isFavorite ? 'a favorite' : 'not a favorite'}.`);
      },
      error: (err) => console.error('Error toggling favorite:', err)
    });
    this.subscriptions.push(sub);
  }

  resetNavigationState(): void {
    this.navigationActive = false;
    console.log('Navigation state reset.');
  }

  closeSidebar(): void {
    this.resetNavigationState();
    this.close.emit();
  }

  toggleProductFilter(): void {
    this.showAvailableProducts = !this.showAvailableProducts;
  }


  getCurrentUser(): void {
    // If integrated with AuthService:
    // const currentUser = this.authService.getCurrentUser();
    // this.currentUserId = currentUser.id;
    // this.currentUserName = currentUser.name;
  }
  fetchProducts(): void {
    if (!this.shop?.id) return;
  
    this.loadingProducts = true; // Set loading state to true
    const sub = this.productService.getProductsByShop(this.shop.id).subscribe({
      next: (fetchedProducts) => {
        this.products = fetchedProducts; // Assign the fetched products to the component variable
        this.loadingProducts = false; // Reset loading state
        console.log('Products loaded:', this.products);
      },
      error: (err) => {
        console.error('Error fetching products:', err);
        this.loadingProducts = false; // Reset loading state on error
      },
    });
  
    this.subscriptions.push(sub); // Add the subscription to manage lifecycle
  }

  fetchReviews(): void {
    if (!this.shop || !this.shop.id) return;
    this.loadingReviews = true;
    const sub = this.reviewService.getReviewsByShop(this.shop.id).subscribe({
      next: (fetchedReviews) => {
        this.reviews = fetchedReviews;
        this.userReview = this.reviews.find(review => review.userId === this.currentUserId) || null;
        this.loadingReviews = false;
      },
      error: (err) => {
        console.error('Error fetching reviews:', err);
        this.loadingReviews = false;
      }
    });
    this.subscriptions.push(sub);
  }

  startAddingReview(): void {
    this.isAddingReview = true;
    this.reviewForm = { rating: 5, review: '' };
  }

  toggleEditMode(): void {
    if (this.isEditingReview) {
      // Cancel editing
      this.isEditingReview = false;
      this.reviewForm = { rating: 5, review: '' };
    } else if (this.userReview) {
      // Start editing
      this.isEditingReview = true;
      this.reviewForm = { rating: this.userReview.rating, review: this.userReview.review };
    }
  }

  cancelReviewForm(): void {
    this.isAddingReview = false;
    this.isEditingReview = false;
    this.reviewForm = { rating: 5, review: '' };
  }

  submitReview(): void {
    if (this.isEditingReview && this.userReview) {
      this.editReview();
    } else if (this.isAddingReview) {
      this.addReview();
    }
  }

  addReview(): void {
    if (!this.reviewForm.rating || !this.reviewForm.review?.trim() || !this.shop?.id) {
      alert('Please provide both rating and review text.');
      return;
    }

    const sub = this.reviewService
    .addReview(this.shop.id, {
      rating: this.reviewForm.rating!,
      review: this.reviewForm.review!.trim(),
      userId: this.currentUserId,
      userName: this.currentUserName,
    })
    .subscribe({
      next: (newReview) => {
        this.reviews.unshift(newReview); // Add the new review to the beginning of the reviews array
        this.userReview = newReview; // Update the user's review
        this.reviewForm = { rating: 5, review: '' }; // Reset the review form
        this.isAddingReview = false; // Exit adding review mode
        console.log('Review added successfully:', newReview);
      },
      error: (err) => console.error('Error adding review:', err),
    });
  
  this.subscriptions.push(sub);
  }  

  editReview(): void {
    if (!this.userReview || !this.shop?.id) return;
    if (!this.reviewForm.rating || !this.reviewForm.review?.trim()) {
      alert('Please provide both rating and review text.');
      return;
    }
  
    const sub = this.reviewService
      .updateReview(this.currentUserId, this.userReview.id, {
        rating: this.reviewForm.rating!,
        review: this.reviewForm.review!.trim(),
      })
      .subscribe({
        next: (updatedReview) => {
          const index = this.reviews.findIndex((r) => r.id === updatedReview.id);
          if (index > -1) {
            this.reviews[index] = updatedReview;
          }
          this.userReview = updatedReview;
          this.isEditingReview = false;
          console.log('Review updated successfully:', updatedReview);
        },
        error: (err) => console.error('Error updating review:', err),
      });
  
    this.subscriptions.push(sub);
  }
  

  deleteReview(): void {
    if (!this.userReview || !this.shop?.id) return;
    if (!confirm('Are you sure you want to delete your review?')) return;

    const sub = this.reviewService.deleteReview(this.shop.id, this.userReview.id).subscribe({
      next: () => {
        this.reviews = this.reviews.filter(review => review.id !== this.userReview?.id);
        this.userReview = null;
        this.isEditingReview = false;
        console.log('Review deleted successfully.');
      },
      error: (err) => console.error('Error deleting review:', err)
    });
    this.subscriptions.push(sub);
  }
}
