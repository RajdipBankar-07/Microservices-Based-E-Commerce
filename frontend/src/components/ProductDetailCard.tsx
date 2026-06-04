"use client";

import React, { useState } from "react";
import { Product } from "@/dto/Product";
import { useCart } from "@/context/CartContext";
import { useWishlist } from "@/context/WishlistContext";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { ShoppingCart, CheckCircle, AlertTriangle, ShieldCheck, Tag, Heart } from "lucide-react";

interface ProductDetailCardProps {
  product: Product;
}

export default function ProductDetailCard({ product }: ProductDetailCardProps) {
  const { user } = useAuth();
  const router = useRouter();
  const { addToCart } = useCart();
  const { addToWishlist, removeFromWishlist, isInWishlist } = useWishlist();
  const [qty, setQty] = useState(1);
  const [adding, setAdding] = useState(false);
  const [wishlistLoading, setWishlistLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const handleAdd = async () => {
    setAdding(true);
    setMessage(null);
    try {
      await addToCart(product.id, qty);
      setMessage("Item added to cart successfully!");
      setTimeout(() => setMessage(null), 3000);
    } catch (err: any) {
      setMessage(err.message || "Failed to add item to cart.");
    } finally {
      setAdding(false);
    }
  };

  const toggleWishlist = async () => {
    if (!user) {
      setMessage("Please log in to add items to your wishlist.");
      setTimeout(() => {
        setMessage(null);
        router.push("/login");
      }, 2000);
      return;
    }
    setWishlistLoading(true);
    try {
      if (isInWishlist(product.id)) {
        await removeFromWishlist(product.id);
      } else {
        await addToWishlist(product.id);
      }
    } catch (err: any) {
      setMessage(err.message || "Failed to update wishlist.");
      setTimeout(() => setMessage(null), 3500);
    } finally {
      setWishlistLoading(false);
    }
  };

  const isLowStock = product.quantity > 0 && product.quantity < 10;
  const isOutOfStock = product.quantity === 0;

  return (
    <div className="rounded-3xl border border-white/[0.08] bg-zinc-900/40 backdrop-blur-xl p-6 md:p-8 shadow-2xl relative overflow-hidden grid grid-cols-1 md:grid-cols-12 gap-8 items-start mb-12">
      <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500" />
      
      {/* Product Image */}
      <div className="md:col-span-5 w-full h-80 md:h-96 relative rounded-2xl overflow-hidden bg-zinc-950 border border-white/[0.04] flex items-center justify-center">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="text-zinc-650 text-sm font-bold uppercase tracking-wider">No Product Image</div>
        )}
        <span className="absolute top-4 left-4 inline-flex items-center rounded-full bg-black/60 backdrop-blur-md px-3.5 py-1 text-xs font-bold text-indigo-400 border border-white/[0.08]">
          {product.category?.name || "Premium Catalog"}
        </span>
      </div>

      {/* Product Details */}
      <div className="md:col-span-7 space-y-6 flex flex-col justify-between h-full">
        <div className="space-y-4">
          <div className="space-y-1">
            <h1 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight">{product.name}</h1>
            <p className="text-xs text-zinc-500 font-bold uppercase tracking-wider">Product SKU: #{product.id}</p>
          </div>

          <div className="flex flex-wrap items-center gap-4">
            <span className="text-3xl font-black text-indigo-400">
              ₹{product.price.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </span>
            {isOutOfStock ? (
              <span className="inline-flex items-center gap-1 rounded-full bg-rose-500/10 px-3 py-1 text-xs font-bold text-rose-450 border border-rose-500/20">
                <AlertTriangle className="h-3.5 w-3.5" />
                Out of Stock
              </span>
            ) : isLowStock ? (
              <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/10 px-3 py-1 text-xs font-bold text-amber-400 border border-amber-500/20">
                <AlertTriangle className="h-3.5 w-3.5 animate-bounce" />
                Only {product.quantity} left!
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-3 py-1 text-xs font-bold text-emerald-400 border border-emerald-500/20">
                <CheckCircle className="h-3.5 w-3.5" />
                In Stock ({product.quantity} units)
              </span>
            )}
          </div>

          <p className="text-zinc-400 text-sm leading-relaxed font-medium">
            {product.description || "Explore this premium addition to our curated catalog. Crafted with meticulous detail to offer visual appeal and functional superiority."}
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-white/[0.04] text-xs text-zinc-500 font-bold uppercase tracking-wider">
            <div className="flex items-center gap-2">
              <ShieldCheck className="h-4.5 w-4.5 text-indigo-400 shrink-0" />
              <span>Full Sandbox Warranty</span>
            </div>
            <div className="flex items-center gap-2">
              <Tag className="h-4.5 w-4.5 text-pink-400 shrink-0" />
              <span>Apply Coupons at Checkout</span>
            </div>
          </div>
        </div>

        {/* Add to Cart Actions */}
        <div className="space-y-4 pt-6 border-t border-white/[0.04] mt-6">
          {!isOutOfStock && (
            <div className="flex items-center gap-4">
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Quantity</span>
                <div className="flex items-center border border-white/[0.08] bg-white/[0.03] rounded-xl p-1">
                  <button
                    type="button"
                    disabled={qty <= 1}
                    onClick={() => setQty((q) => q - 1)}
                    className="h-8 w-8 flex items-center justify-center text-zinc-400 hover:text-white disabled:opacity-30 cursor-pointer"
                  >
                    -
                  </button>
                  <span className="px-4 text-sm font-bold text-white min-w-8 text-center">{qty}</span>
                  <button
                    type="button"
                    disabled={qty >= product.quantity}
                    onClick={() => setQty((q) => q + 1)}
                    className="h-8 w-8 flex items-center justify-center text-zinc-400 hover:text-white disabled:opacity-30 cursor-pointer"
                  >
                    +
                  </button>
                </div>
              </div>

              <div className="flex-1 pt-5 flex gap-3">
                <button
                  type="button"
                  disabled={adding}
                  onClick={handleAdd}
                  className="flex-1 h-11 inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-sm font-bold text-white shadow-lg shadow-indigo-600/25 transition-all cursor-pointer disabled:opacity-40"
                >
                  {adding ? (
                    <>
                      <div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      <span>Adding...</span>
                    </>
                  ) : (
                    <>
                      <ShoppingCart className="h-4.5 w-4.5" />
                      <span>Add to Cart</span>
                    </>
                  )}
                </button>

                <button
                  type="button"
                  disabled={wishlistLoading}
                  onClick={toggleWishlist}
                  className={`h-11 w-11 inline-flex items-center justify-center rounded-xl border transition-all cursor-pointer ${
                    isInWishlist(product.id)
                      ? "border-rose-500/30 bg-rose-500/10 text-rose-500 hover:bg-rose-500/20"
                      : "border-white/[0.08] bg-white/[0.03] text-zinc-450 hover:text-white hover:bg-white/[0.06]"
                  }`}
                  title={isInWishlist(product.id) ? "Remove from Wishlist" : "Add to Wishlist"}
                >
                  <Heart className={`h-4.5 w-4.5 ${isInWishlist(product.id) ? "fill-rose-500" : ""}`} />
                </button>
              </div>
            </div>
          )}

          {message && (
            <div className={`p-3.5 rounded-xl text-xs font-bold text-center border ${
              message.includes("successfully") 
                ? "bg-emerald-500/10 border-emerald-500/20 text-emerald-450" 
                : "bg-rose-500/10 border-rose-500/20 text-rose-450"
            }`}>
              {message}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

