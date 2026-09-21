export interface RegisterPayload {
  email: string;
  phoneNumber: string;
  fullName: string;
  password: string;
  role: 'USER' | 'SHOPKEEPER';
  phoneVerificationToken?: string;
}
