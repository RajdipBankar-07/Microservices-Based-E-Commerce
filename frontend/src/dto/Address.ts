export interface Address {
  id: number;
  label: string; // HOME, WORK, OTHER
  street: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  isDefault: boolean;
}
