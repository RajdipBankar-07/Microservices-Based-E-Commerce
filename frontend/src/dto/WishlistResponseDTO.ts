import { Product } from "./Product";

export interface WishlistResponseDTO {
  id: number;
  userEmail: string;
  products: Product[];
}
