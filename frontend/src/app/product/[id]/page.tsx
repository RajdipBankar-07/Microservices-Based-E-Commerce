// src/app/product/[id]/page.tsx
"use client";

import React, { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { api } from "@/utils/api";
import ProductDetailCard from "@/components/ProductDetailCard";
import ReviewPanel from "@/components/ReviewPanel";
import CartDrawer from "@/components/CartDrawer";
import { Product } from "@/dto/Product";

export default function ProductPage() {
  const { id } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchProduct() {
      try {
        const data = await api.get<Product>(`/products/${id}`);
        setProduct(data);
      } catch (e) {
        console.error("Failed to fetch product", e);
      } finally {
        setLoading(false);
      }
    }
    if (id) fetchProduct();
  }, [id]);

  if (loading) return <div className="flex justify-center items-center h-64">Loading…</div>;
  if (!product) return <div className="p-4 text-red-500">Product not found.</div>;

  return (
    <section className="container mx-auto p-6">
      <ProductDetailCard product={product} />
      <ReviewPanel productId={product.id} />
      <CartDrawer />
    </section>
  );
}
