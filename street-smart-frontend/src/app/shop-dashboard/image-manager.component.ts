import { Component, Input, OnChanges, OnDestroy } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Subscription, finalize } from 'rxjs';
import { environment } from '../environment';
import { ShopImage } from '../model/shop.model';
import { ShopImageComponent } from '../shop-details/shop-image.component';
import { apiError } from '../services/api-error';

@Component({
  selector: 'app-image-manager',
  standalone: true,
  imports: [ShopImageComponent],
  template: `
    <section aria-label="Shop images" class="mt-8 border-t border-slate-100 pt-6">
      <h2 class="text-xl font-bold text-ink-950">Shop images</h2>
      <p class="mb-3 text-sm text-slate-500">JPEG or PNG, up to 5 MB each. Maximum 20 images.</p>
      <input aria-label="Upload shop image" type="file" accept="image/jpeg,image/png"
        class="ss-file" [disabled]="busy || images.length >= 20" (change)="upload($event)" />
      @if (error) {
        <p role="alert" class="ss-alert-error mt-3">{{ error }}</p>
      }
      @if (busy) {
        <p role="status" class="ss-alert-info mt-3">Updating images...</p>
      }
      @for (image of images; track image) {
        <div class="my-4 max-w-sm rounded-xl border border-slate-100 p-3">
          <app-shop-image [imageId]="image.id" />
          <button class="ss-btn-danger mt-2" [disabled]="busy" (click)="remove(image.id)">Delete image</button>
        </div>
      }
    </section>`,
})
export class ImageManagerComponent implements OnChanges, OnDestroy {
  @Input() shopId = '';
  images: ShopImage[] = [];
  busy = false;
  error = '';
  private requests = new Subscription();
  private readonly base = environment.apiBaseUrl + '/images';
  constructor(private http: HttpClient) {}
  ngOnChanges(): void {
    this.requests.unsubscribe();
    this.requests = new Subscription();
    this.images = [];
    this.busy = false;
    if (this.shopId) this.requests.add(this.http.get<ShopImage[]>(this.base + '/shops/' + this.shopId)
      .subscribe({ next: images => this.images = images, error: err => this.error = apiError(err) }));
  }
  upload(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.busy) return;
    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) {
      this.error = 'Choose a JPEG or PNG image no larger than 5 MB.';
      return;
    }
    const body = new FormData();
    body.append('file', file);
    this.busy = true;
    this.error = '';
    this.requests.add(this.http.post<ShopImage>(this.base + '/upload', body, { params: { shopId: this.shopId } })
      .pipe(finalize(() => this.busy = false)).subscribe({
        next: image => this.images = [...this.images, image], error: err => this.error = apiError(err),
      }));
  }
  remove(id: string): void {
    if (this.busy || !window.confirm('Delete this shop image? This cannot be undone.')) return;
    this.busy = true;
    this.error = '';
    this.requests.add(this.http.delete(this.base + '/' + id).pipe(finalize(() => this.busy = false))
      .subscribe({ next: () => this.images = this.images.filter(image => image.id !== id),
        error: err => this.error = apiError(err) }));
  }
  ngOnDestroy(): void { this.requests.unsubscribe(); }
}
