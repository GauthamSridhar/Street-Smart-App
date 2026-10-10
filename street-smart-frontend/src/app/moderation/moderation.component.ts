import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReviewService } from '../services/review.service';
import { apiError } from '../services/api-error';

@Component({ selector: 'app-moderation', standalone: true, imports: [CommonModule, NavbarComponent], templateUrl: './moderation.component.html' })
export class ModerationComponent implements OnInit {
  reports: Array<{ id: string; ratingId: string; reason: string; review: string; createdAt: string; status: string }> = [];
  loading = true;
  error = '';
  busy = false;
  page = 0;
  totalPages = 0;
  totalElements = 0;
  constructor(private reviews: ReviewService) {}
  ngOnInit(): void { this.load(); }
  load(page = this.page): void {
    if (this.busy) return;
    this.loading = true;
    this.error = '';
    this.reviews.reports(page).subscribe({
      next: result => {
        if (page > 0 && !result.content.length) {
          this.load(Math.max(0, result.totalPages - 1));
          return;
        }
        this.reports = result.content;
        this.page = result.number;
        this.totalPages = result.totalPages;
        this.totalElements = result.totalElements;
        this.loading = false;
      },
      error: error => { this.error = apiError(error); this.loading = false; },
    });
  }
  resolve(id: string, action: 'DISMISS' | 'REMOVE'): void {
    if (this.busy || this.loading) return;
    this.busy = true; this.error = '';
    this.reviews.resolveReport(id, action).subscribe({
      next: () => { this.busy = false; this.load(); },
      error: error => { this.busy = false; this.error = apiError(error); },
    });
  }
}
