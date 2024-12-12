import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShopRejectedComponent } from './shop-rejected.component';

describe('ShopRejectedComponent', () => {
  let component: ShopRejectedComponent;
  let fixture: ComponentFixture<ShopRejectedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopRejectedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShopRejectedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
