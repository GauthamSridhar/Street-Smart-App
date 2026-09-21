import { Component, Input, OnChanges, OnDestroy } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { environment } from '../environment';
@Component({
  selector: 'app-shop-image',
  standalone: true,
  imports: [],
  template:
    '@if (url) {<img [src]="url" [alt]="alt" class="w-full rounded-xl object-cover shadow-sm" />}@if (error) {<span class="text-sm text-slate-400">Image unavailable</span>}',
})
export class ShopImageComponent implements OnChanges, OnDestroy {
  @Input() imageId = '';
  @Input() alt = 'Shop image';
  url = '';
  error = false;
  private request?: Subscription;
  constructor(private http: HttpClient) {}
  ngOnChanges(): void {
    this.cleanup();
    this.error = false;
    if (!this.imageId) return;
    this.request = this.http
      .get(environment.apiBaseUrl + '/images/' + this.imageId + '/download', {
        responseType: 'blob',
      })
      .subscribe({
        next: (blob) => {
          this.url = URL.createObjectURL(blob);
        },
        error: () => {
          this.error = true;
        },
      });
  }
  private cleanup(): void {
    this.request?.unsubscribe();
    if (this.url) URL.revokeObjectURL(this.url);
    this.url = '';
  }
  ngOnDestroy(): void {
    this.cleanup();
  }
}
