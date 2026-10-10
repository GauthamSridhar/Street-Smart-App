import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ModerationComponent } from './moderation.component';
import { ReviewService } from '../services/review.service';

describe('Moderation pagination', () => {
  let component: ModerationComponent;
  let reviews: jasmine.SpyObj<ReviewService>;
  beforeEach(() => {
    reviews = jasmine.createSpyObj('ReviewService', ['reports', 'resolveReport']);
    TestBed.configureTestingModule({ providers: [{ provide: ReviewService, useValue: reviews }] });
    component = TestBed.runInInjectionContext(() => new ModerationComponent(reviews));
  });
  it('returns to the last available page after resolving its final report', () => {
    component.page = 1;
    component.loading = false;
    reviews.resolveReport.and.returnValue(of(undefined));
    reviews.reports.and.returnValues(
      of({ content: [], number: 1, size: 20, totalPages: 1, totalElements: 20 }),
      of({ content: [], number: 0, size: 20, totalPages: 1, totalElements: 20 }),
    );
    component.resolve('last-report', 'DISMISS');
    expect(reviews.reports.calls.allArgs()).toEqual([[1], [0]]);
    expect(component.page).toBe(0);
    expect(component.loading).toBeFalse();
  });
  it('keeps the current page and exposes retryable fetch errors', () => {
    component.page = 2;
    reviews.reports.and.returnValue(throwError(() => new Error('Unavailable')));
    component.load();
    expect(component.page).toBe(2);
    expect(component.loading).toBeFalse();
    expect(component.error).not.toBe('');
  });
});
