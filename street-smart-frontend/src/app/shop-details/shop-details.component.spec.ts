import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShopDetailsComponent } from './shop-details.component';

describe('ShopDetailsComponent', () => {
  let component: ShopDetailsComponent;
  let fixture: ComponentFixture<ShopDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      imports: [ShopDetailsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ShopDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not claim external navigation is controlled by the sidebar', () => {
    const emit = spyOn(component.navigateToShop, 'emit');
    component.onNavigate();
    expect(emit).toHaveBeenCalledWith(component.shop);
  });
});
