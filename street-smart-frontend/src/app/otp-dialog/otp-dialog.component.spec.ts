import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { OtpDialogComponent } from './otp-dialog.component';
describe('Server-side phone verification', () => {
  it('sends only the phone and never accepts a code without server verification', () => {
    TestBed.configureTestingModule({
      imports: [OtpDialogComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(OtpDialogComponent);
    const component = fixture.componentInstance;
    component.contactInfo = '+919876543210';
    fixture.detectChanges();
    const send = http.expectOne('/api/sms/send');
    expect(send.request.body).toEqual({ phoneNumber: '+919876543210' });
    send.flush({});
    const close = spyOn(component.close, 'emit');
    component.otpCode = '369715';
    component.verifyOtp();
    const verify = http.expectOne('/api/sms/verify');
    expect(close).not.toHaveBeenCalled();
    verify.flush({}, { status: 400, statusText: 'Invalid code' });
    expect(close).not.toHaveBeenCalled();
    component.otpCode = '123456';
    component.verifyOtp();
    http.expectOne('/api/sms/verify').flush({ verificationToken: 'server-proof' });
    expect(close).toHaveBeenCalledWith(
      jasmine.objectContaining({ verificationToken: 'server-proof', phoneNumber: '+919876543210' }),
    );
    fixture.destroy();
    http.verify();
  });
});
