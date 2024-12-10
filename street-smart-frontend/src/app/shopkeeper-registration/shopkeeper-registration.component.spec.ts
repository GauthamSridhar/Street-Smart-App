import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShopkeeperRegistrationComponent } from './shopkeeper-registration.component';

describe('ShopkeeperRegistrationComponent', () => {
  let component: ShopkeeperRegistrationComponent;
  let fixture: ComponentFixture<ShopkeeperRegistrationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopkeeperRegistrationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShopkeeperRegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
