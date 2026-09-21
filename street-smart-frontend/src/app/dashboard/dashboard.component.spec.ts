import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import { Shop } from '../model/shop.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      imports: [DashboardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens directions without a map or location permission', () => {
    const open = spyOn(window, 'open');
    component.onNavigateToShop({ latitude: 10, longitude: 76 } as Shop);
    expect(open).toHaveBeenCalledWith(
      'https://www.google.com/maps/dir/?api=1&destination=10%2C76',
      '_blank',
      'noopener,noreferrer',
    );
  });
});
