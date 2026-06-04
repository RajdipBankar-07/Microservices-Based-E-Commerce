"use client";

import React from "react";
import Link from "next/link";
import { useWishlist } from "@/context/WishlistContext";
import { useCart } from "@/context/CartContext";
import { Heart, ShoppingCart, ArrowLeft, Trash2, ShoppingBag } from "lucide-react";

export default function WishlistPage() {
  const { wishlistItems, removeFromWishlist, loading } = useWishlist();
  const { addToCart } = useCart();

  const handleAddToCart = async (productId: number) => {
    try {
      await addToCart(productId, 1);
      alert("Added to cart successfully!");
    } catch (err: any) {
      alert(err.message || "Failed to add to cart.");
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8 relative z-10">
      
      {/* Back to shop */}
      <div className="mb-8">
        <Link href="/" className="inline-flex items-center gap-2 text-zinc-400 hover:text-white transition-colors group">
          <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
          <span>Back to Marketplace</span>
        </Link>
      </div>

      <div className="space-y-8">
        {/* Header Title */}
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-rose-500/10 text-rose-450 border border-rose-500/20">
            <Heart className="h-6 w-6 fill-rose-500 text-rose-500" />
          </div>
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight leading-none">My Wishlist</h1>
            <p className="text-zinc-500 text-xs mt-1">Your saved products list – easily add items to your cart.</p>
          </div>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-80 rounded-2xl border border-white/[0.05] bg-zinc-900/20 animate-pulse" />
            ))}
          </div>
        ) : wishlistItems.length === 0 ? (
          <div className="rounded-2xl border border-white/[0.06] bg-zinc-900/20 backdrop-blur-xl p-12 text-center max-w-xl mx-auto space-y-4">
            <div className="inline-flex h-16 w-16 items-center justify-center rounded-2xl bg-zinc-900 text-zinc-650 border border-white/[0.05] mb-2">
              <Heart className="h-8 w-8 text-zinc-655" />
            </div>
            <h3 className="text-lg font-bold text-white">Your wishlist is empty</h3>
            <p className="text-zinc-500 text-sm">Save products to your wishlist while browsing to track favorites here.</p>
            <Link 
              href="/" 
              className="inline-flex items-center justify-center rounded-xl bg-indigo-600 hover:bg-indigo-500 px-6 py-3 text-xs font-bold text-white shadow-md transition-all cursor-pointer"
            >
              Discover Products
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
            {wishlistItems.map((product) => (
              <div 
                key={product.id} 
                className="glass-premium-card group relative rounded-2xl border border-white/[0.06] p-5 flex flex-col justify-between overflow-hidden shadow-lg transition-all hover:scale-[1.01]"
              >
                <div>
                  {/* Product Image Cover Container */}
                  <div className="w-full h-44 relative rounded-xl overflow-hidden mb-4 bg-zinc-900 border border-white/[0.04]">
                    {product.imageUrl ? (
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-zinc-650 text-xs font-bold">
                        No Image
                      </div>
                    )}
                    
                    {/* Category Badge */}
                    <span className="absolute top-2.5 left-2.5 z-10 inline-flex items-center rounded-full bg-black/60 backdrop-blur-md px-2.5 py-0.5 text-[10px] font-bold text-indigo-400 border border-white/[0.08]">
                      {product.category?.name || "Uncategorized"}
                    </span>

                    {/* Delete from wishlist button */}
                    <button
                      onClick={() => removeFromWishlist(product.id)}
                      className="absolute top-2.5 right-2.5 z-10 p-2 rounded-lg bg-black/60 backdrop-blur-md text-zinc-450 hover:text-rose-400 border border-white/[0.08] hover:scale-105 active:scale-95 transition-all cursor-pointer"
                      title="Remove from Wishlist"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>

                  <h3 className="text-sm font-bold text-zinc-200 line-clamp-1">{product.name}</h3>
                  <p className="text-xs text-zinc-500 line-clamp-2 mt-1.5 leading-relaxed">
                    {product.description || "Explore this premium addition to our catalog."}
                  </p>
                </div>

                <div className="flex justify-between items-center mt-6 pt-4 border-t border-white/[0.04]">
                  <div>
                    <p className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Price</p>
                    <p className="text-base font-extrabold text-zinc-100 mt-0.5">
                      ₹{product.price.toLocaleString("en-IN")}
                    </p>
                  </div>

                  <button
                    onClick={() => handleAddToCart(product.id)}
                    className="p-3 rounded-xl bg-indigo-600 text-white shadow-md hover:bg-indigo-500 hover:scale-105 active:scale-95 transition-all cursor-pointer"
                    title="Add to Shopping Cart"
                  >
                    <ShoppingCart className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
}
