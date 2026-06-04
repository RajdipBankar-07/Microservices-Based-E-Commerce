import { Product } from "./Product";

export interface CartItem {
  id: number;
  user: {
    id: number;
    name?: string;
    email?: string;
    role?: string;
  };
  product: Product;
  quantity: number;
}
