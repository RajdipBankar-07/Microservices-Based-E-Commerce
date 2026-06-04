// src/dto/Product.ts
export interface Product {
  id: number;
  name: string;
  price: number;
  quantity: number;
  category?: { id: number; name: string; description?: string } | null;
  description?: string;
  imageUrl?: string;
  stockQuantity?: number;
  deactivateAt?: string; // ISO datetime string
}

