import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
      imports: [RegisterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens phone verification for a valid registration when SMS is enabled', () => {
    component.configLoaded = true;
    component.smsEnabled = true;
    component.registerForm.setValue({
      phoneCountryCode: '+91',
      phoneNumber: '9876543210',
      email: 'person@example.test',
      fullName: 'Test Person',
      role: 'USER',
      password: 'correct-password',
      confirmPassword: 'correct-password',
    });

    component.registerUser();

    expect(component.showOtpDialog).toBeTrue();
    expect(component.contactInfo).toBe('+919876543210');
  });
});
