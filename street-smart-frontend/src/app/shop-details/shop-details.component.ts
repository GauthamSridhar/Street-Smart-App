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
import { FormsModule, NgModel } from '@angular/forms';
// Mock Interfaces (Replace with actual models as needed)
interface Product {
  name: string;
  available: boolean;
}

interface Review {
  id: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  date: Date;
}

@Component({
  selector: 'app-shop-details',
  templateUrl: './shop-details.component.html',
  styleUrls: ['./shop-details.component.css'],
  standalone: true,
  imports: [CommonModule,FormsModule],
})
export class ShopDetailsComponent implements OnInit, OnDestroy, OnChanges {
  @Input() shop!: Shop; // Ensure 'shop' is provided
  @Input() isOpen = false;

  @Output() close = new EventEmitter<void>();
  @Output() navigateToShop = new EventEmitter<Shop>();
  @Output() cancelNavigationEvent = new EventEmitter<void>();

  activeTab = 'Overview';
  tabs = ['Overview', 'Reviews', 'Images', 'Products'];

  navigationActive = false;
  isFavorite = false;

  products: Product[] = [
    { name: 'Product A', available: true },
    { name: 'Product B', available: false },
    { name: 'Product C', available: true },
    { name: 'Product D', available: false }
  ];
  showAvailableProducts = true; // Toggle for available/unavailable products

  // Reviews related properties
  reviews: Review[] = [];
  userReview: Review | null = null;
  newReview: Partial<Review> = { rating: 5, comment: '' };
  isEditingReview: boolean = false;
  isAddingReview: boolean = false;
  loadingReviews: boolean = false;
  currentUserId: string = 'user123'; // Mock current user ID
  currentUserName: string = 'John Doe'; // Mock current user name

  // New Form Model
  reviewForm: Partial<Review> = { rating: 5, comment: '' };

  constructor(
    // private reviewService: ReviewService, // Uncomment when integrating backend
    // private authService: AuthService // Uncomment when integrating backend
  ) {}

  ngOnInit(): void {
    if (this.shop) {
      this.checkIfFavorite();
      this.fetchReviews();
      this.getCurrentUser();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
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
    }
  }

  ngOnDestroy(): void {
    // Cleanup if necessary
  }

  calculateAverageRating(ratings: number[]): number {
    return ratings.length
      ? ratings.reduce((a, b) => a + b, 0) / ratings.length
      : 0;
  }

  onNavigate(): void {
    this.navigateToShop.emit(this.shop);
    this.navigationActive = true;
    console.log('Navigation initiated.');
  }

  onCancelNavigation(): void {
    this.cancelNavigationEvent.emit();
    this.navigationActive = false;
    console.log('Navigation cancelled.');
  }

  checkIfFavorite(): void {
    this.isFavorite = false;
    // Mock logic to check if the shop is a favorite
    // Example:
    // this.isFavorite = this.favoritesService.isFavorite(this.shop.id);
  }

  toggleFavorite(): void {
    this.isFavorite = !this.isFavorite;
    console.log(`Shop is now ${this.isFavorite ? 'a favorite' : 'not a favorite'}.`);
    // Mock logic to add/remove from favorites
    // Example:
    // if (this.isFavorite) {
    //   this.favoritesService.addFavorite(this.shop.id);
    // } else {
    //   this.favoritesService.removeFavorite(this.shop.id);
    // }
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

  get filteredProducts(): Product[] {
    return this.products.filter(
      product => product.available === this.showAvailableProducts
    );
  }

  // Review Methods

  getCurrentUser(): void {
    // Mock current user retrieval
    // const currentUser = this.authService.getCurrentUser();
    // if (currentUser) {
    //   this.currentUserId = currentUser.id;
    //   this.currentUserName = currentUser.name;
    // }
  }

  fetchReviews(): void {
    if (!this.shop || !this.shop.id) return;

    this.loadingReviews = true;

    // Mock fetching reviews
    setTimeout(() => {
      // Sample mock reviews
      this.reviews = [
        {
          id: 'rev1',
          userId: 'user123',
          userName: 'John Doe',
          rating: 5,
          comment: 'Excellent service and quality!',
          date: new Date('2023-08-15T10:00:00')
        },
        {
          id: 'rev2',
          userId: 'user456',
          userName: 'Jane Smith',
          rating: 4,
          comment: 'Great products but delivery was slow.',
          date: new Date('2023-08-14T12:30:00')
        },
        {
          id: 'rev3',
          userId: 'user789',
          userName: 'Alice Johnson',
          rating: 3,
          comment: 'Average experience.',
          date: new Date('2023-08-13T09:15:00')
        }
      ];

      // Mock user's own review
      this.userReview = this.reviews.find(review => review.userId === this.currentUserId) || null;

      this.loadingReviews = false;
    }, 1000); // Simulate API delay
  }

  startAddingReview(): void {
    this.isAddingReview = true;
    this.reviewForm = { rating: 5, comment: '' };
  }

  toggleEditMode(): void {
    if (this.isEditingReview) {
      // Cancel editing
      this.isEditingReview = false;
      this.reviewForm = { rating: 5, comment: '' };
    } else if (this.userReview) {
      // Start editing
      this.isEditingReview = true;
      this.reviewForm = { ...this.userReview };
    }
  }

  cancelReviewForm(): void {
    this.isAddingReview = false;
    this.isEditingReview = false;
    this.reviewForm = { rating: 5, comment: '' };
  }

  submitReview(): void {
    if (this.isEditingReview && this.userReview) {
      this.editReview();
    } else if (this.isAddingReview) {
      this.addReview();
    }
  }

  addReview(): void {
    if (!this.reviewForm.rating || !this.reviewForm.comment?.trim()) {
      alert('Please provide both rating and comment.');
      return;
    }

    // Mock adding a review
    const addedReview: Review = {
      id: `rev${this.reviews.length + 1}`,
      userId: this.currentUserId,
      userName: this.currentUserName,
      rating: this.reviewForm.rating!,
      comment: this.reviewForm.comment.trim(),
      date: new Date()
    };

    // Update reviews list
    this.reviews.unshift(addedReview);
    this.userReview = addedReview;
    this.reviewForm = { rating: 5, comment: '' };
    this.isAddingReview = false;
    console.log('Review added successfully.');
  }

  editReview(): void {
    if (!this.userReview) return;

    if (!this.reviewForm.rating || !this.reviewForm.comment?.trim()) {
      alert('Please provide both rating and comment.');
      return;
    }

    // Mock updating a review
    this.userReview.rating = this.reviewForm.rating!;
    this.userReview.comment = this.reviewForm.comment.trim();
    this.isEditingReview = false;
    console.log('Review updated successfully.');
  }

  deleteReview(): void {
    if (!this.userReview) return;

    if (!confirm('Are you sure you want to delete your review?')) return;

    // Mock deleting a review
    this.reviews = this.reviews.filter(review => review.id !== this.userReview?.id);
    this.userReview = null;
    this.isEditingReview = false;
    console.log('Review deleted successfully.');
  }
}
