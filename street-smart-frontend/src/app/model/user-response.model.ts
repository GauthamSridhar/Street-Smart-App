export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  phoneNumber: string;
  verified: boolean;
}
export interface UserEdit {
  fullName: string;
  email: string;
  phoneNumber: string;
  currentPassword?: string;
  password?: string;
  phoneVerificationToken?: string;
}
