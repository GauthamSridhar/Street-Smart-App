import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShopRejectedComponent } from './shop-rejected.component';

describe('ShopRejectedComponent', () => {
  let component: ShopRejectedComponent;
  let fixture: ComponentFixture<ShopRejectedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      imports: [ShopRejectedComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ShopRejectedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
