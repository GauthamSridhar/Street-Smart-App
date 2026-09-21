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
  constructor(private reviews: ReviewService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.loading = true; this.reviews.reports().subscribe({ next: reports => { this.reports = reports; this.loading = false; }, error: error => { this.error = apiError(error); this.loading = false; } }); }
  resolve(id: string, action: 'DISMISS' | 'REMOVE'): void {
    if (this.busy) return;
    this.busy = true; this.error = '';
    this.reviews.resolveReport(id, action).subscribe({
      next: () => { this.busy = false; this.reports = this.reports.filter(r => r.id !== id); },
      error: error => { this.busy = false; this.error = apiError(error); },
    });
  }
}
