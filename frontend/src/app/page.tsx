"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/utils/api";
import { 
  Search, 
  ShoppingCart, 
  Heart, 
  Sparkles, 
  AlertCircle, 
  ArrowRight, 
  Grid, 
  CheckCircle, 
  Tag, 
  TrendingUp,
  PackageCheck,
  X
} from "lucide-react";

interface Category {
  id: number;
  name: string;
  description: string;
}

interface Product {
  id: number;
  name: string;
  price: number;
  quantity: number;
  category: Category | null;
  description?: string;
  imageUrl?: string;
}

export default function HomePage() {
  const { user, loading: authLoading } = useAuth();
  
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedProductDetails, setSelectedProductDetails] = useState<Product | null>(null);

  // Storefront Announcements States
  const [announcements, setAnnouncements] = useState<any[]>([]);
  const [activeAnnIdx, setActiveAnnIdx] = useState(0);
  const [dismissedAnns, setDismissedAnns] = useState<number[]>([]);

  // Fetch catalog data when authenticated user is loaded
  useEffect(() => {
    if (user) {
      fetchCatalog();
    }
  }, [user]);

  const fetchCatalog = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch categories (wrapped in ApiResponse DTO)
      const catResponse = await api.get<{ message: string; data: Category[] }>("/categories");
      setCategories(catResponse?.data || []);

      // 2. Fetch products (direct list representation)
      const prodResponse = await api.get<Product[]>("/products");
      setProducts(prodResponse || []);

      // 3. Fetch announcements
      try {
        const annResponse = await api.get<{ message: string; data: any[] }>("/announcements");
        setAnnouncements(annResponse?.data || []);
      } catch (annErr) {
        console.error("Failed to load storefront announcements", annErr);
      }
    } catch (err: any) {
      setError(err?.message || "Failed to load catalog data from the server.");
    } finally {
      setLoading(false);
    }
  };

  // Client-side filtering logic
  const filteredProducts = products.filter(product => {
    const matchesCategory = selectedCategory === null || product.category?.id === selectedCategory;
    const matchesSearch = product.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (product.category?.name && product.category.name.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  // Render Skeleton cards
  const renderSkeletons = () => (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="glass-premium-card rounded-2xl p-5 border border-white/[0.05] h-80 flex flex-col justify-between shimmer-premium">
          <div className="space-y-4">
            <div className="h-6 w-24 bg-zinc-800 rounded-full" />
            <div className="h-8 w-4/5 bg-zinc-800 rounded-lg" />
            <div className="h-4 w-1/2 bg-zinc-800 rounded" />
          </div>
          <div className="flex justify-between items-center mt-4">
            <div className="h-8 w-20 bg-zinc-800 rounded-lg" />
            <div className="h-10 w-10 bg-zinc-800 rounded-xl" />
          </div>
        </div>
      ))}
    </div>
  );

  // 1. Anonymous State View (Stunning SaaS Landing Page)
  if (!authLoading && !user) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center min-h-[85vh] px-4 py-16 relative overflow-hidden bg-zinc-950">
        
        {/* Glow ambient background lights */}
        <div className="absolute top-1/4 left-1/4 -translate-x-1/2 w-96 h-96 bg-indigo-500/10 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute bottom-1/4 right-1/4 translate-x-1/2 w-96 h-96 bg-purple-500/10 rounded-full blur-[120px] pointer-events-none" />

        <div className="max-w-4xl text-center relative z-10 space-y-8">
          
          {/* Sparkle Badge */}
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-indigo-500/30 bg-indigo-500/5 text-indigo-300 text-xs font-bold uppercase tracking-wider mb-2 animate-pulse">
            <Sparkles className="h-4 w-4" />
            E-Commerce Portal Sandbox
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight leading-tight">
            <span className="text-gradient-indigo">
              Elevate Your Shopping
            </span>
            <br />
            <span className="text-gradient-purple">
              Experience with ShopEasy
            </span>
          </h1>

          <p className="max-w-xl mx-auto text-base sm:text-lg text-zinc-400 leading-relaxed font-medium">
            Register or Sign in with our pre-populated mock credentials to browse products, apply coupons, and simulate secure payments.
          </p>

          {/* SaaS Buttons */}
          <div className="flex flex-col sm:flex-row justify-center items-center gap-4 pt-4">
            <Link 
              href="/login" 
              className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-8 py-3.5 text-base font-bold text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-500 hover:scale-[1.01] active:scale-[0.99] transition-all cursor-pointer min-w-[200px] h-12"
            >
              Sign In Now
              <ArrowRight className="h-5 w-5" />
            </Link>
            <Link 
              href="/register" 
              className="inline-flex items-center justify-center rounded-xl border border-zinc-700 bg-zinc-900/60 px-8 py-3.5 text-base font-bold text-zinc-300 hover:text-white hover:bg-zinc-800 transition-all min-w-[200px] h-12"
            >
              Create Account
            </Link>
          </div>

          {/* Features highlight grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 pt-12 mt-16 text-left border-t border-white/[0.06]">
            
            <div className="glass-premium rounded-2xl p-6 border border-white/[0.04]">
              <div className="h-10 w-10 flex items-center justify-center rounded-xl bg-indigo-500/10 text-indigo-400 mb-4 border border-indigo-500/20">
                <CheckCircle className="h-5 w-5" />
              </div>
              <h3 className="text-lg font-bold text-zinc-200 mb-2">Pre-populated Sandbox</h3>
              <p className="text-sm text-zinc-400 leading-relaxed">
                Log in and browse pre-loaded Electronics, Apparel, and Coupons instantly.
              </p>
            </div>
            
            <div className="glass-premium rounded-2xl p-6 border border-white/[0.04]">
              <div className="h-10 w-10 flex items-center justify-center rounded-xl bg-purple-500/10 text-purple-400 mb-4 border border-purple-500/20">
                <Tag className="h-5 w-5" />
              </div>
              <h3 className="text-lg font-bold text-zinc-200 mb-2">Simulated Promos</h3>
              <p className="text-sm text-zinc-400 leading-relaxed">
                Test checkout totals by applying percentage or flat coupon code calculations.
              </p>
            </div>

            <div className="glass-premium rounded-2xl p-6 border border-white/[0.04]">
              <div className="h-10 w-10 flex items-center justify-center rounded-xl bg-emerald-500/10 text-emerald-400 mb-4 border border-emerald-500/20">
                <ShoppingCart className="h-5 w-5" />
              </div>
              <h3 className="text-lg font-bold text-zinc-200 mb-2">Shopping Portals</h3>
              <p className="text-sm text-zinc-400 leading-relaxed">
                Save to personal wishlists, update cart items, and view completed orders.
              </p>
            </div>
          </div>

        </div>
      </div>
    );
  }

  // 2. Authenticated State View (Stunning Shopping Catalog)
  return (
    <div className="flex-1 bg-zinc-950 px-4 py-8 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full relative">
      
      {/* Background ambient glowing light */}
      <div className="absolute top-10 right-10 w-80 h-80 bg-indigo-500/5 rounded-full blur-[100px] pointer-events-none" />

      {/* Active Promotion & Announcement Banner */}
      {announcements.length > 0 && announcements.filter(a => !dismissedAnns.includes(a.id)).length > 0 && (
        <div className="relative mb-8 rounded-2xl overflow-hidden border border-indigo-500/30 bg-indigo-500/5 p-4 shadow-[0_8px_32px_rgba(99,102,241,0.12)] backdrop-blur-md transition-all duration-300">
          {/* Ambient glow line */}
          <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-500 animate-shimmer" />

          {(() => {
            const visibleAnns = announcements.filter(a => !dismissedAnns.includes(a.id));
            const currentAnn = visibleAnns[activeAnnIdx % visibleAnns.length] || visibleAnns[0];
            if (!currentAnn) return null;

            return (
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div className="flex items-start gap-3.5 pr-8">
                  <div className="h-10 w-10 shrink-0 flex items-center justify-center rounded-xl bg-indigo-500/15 text-indigo-400 border border-indigo-500/25 shadow-[0_4px_12px_rgba(99,102,241,0.15)] animate-pulse">
                    <Sparkles className="h-5 w-5" />
                  </div>
                  <div className="space-y-1">
                    <h4 className="text-sm font-bold text-white flex items-center gap-2">
                      {currentAnn.title}
                      {currentAnn.displayUntil && (
                        <span className="inline-flex text-[9px] bg-rose-500/10 text-rose-400 border border-rose-500/20 px-1.5 py-0.5 rounded font-bold uppercase tracking-wider">
                          Limited Offer
                        </span>
                      )}
                    </h4>
                    <p className="text-xs text-zinc-300 leading-relaxed font-medium">
                      {currentAnn.message}
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3 shrink-0 self-end sm:self-auto pl-13 sm:pl-0">
                  {currentAnn.product && (
                    <button
                      onClick={() => setSelectedProductDetails(currentAnn.product)}
                      className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 px-3.5 py-2 text-xs font-bold text-white shadow-md shadow-indigo-600/15 transition-all cursor-pointer hover:scale-102 active:scale-98"
                    >
                      View Sale Product
                      <ArrowRight className="h-3.5 w-3.5" />
                    </button>
                  )}
                  
                  {visibleAnns.length > 1 && (
                    <div className="flex border border-white/[0.06] bg-zinc-900/60 rounded-lg p-0.5">
                      <button
                        onClick={() => setActiveAnnIdx((prev) => (prev > 0 ? prev - 1 : visibleAnns.length - 1))}
                        className="p-1.5 rounded hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer"
                        title="Previous Alert"
                      >
                        <ArrowRight className="h-3.5 w-3.5 rotate-180" />
                      </button>
                      <button
                        onClick={() => setActiveAnnIdx((prev) => (prev + 1) % visibleAnns.length)}
                        className="p-1.5 rounded hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer"
                        title="Next Alert"
                      >
                        <ArrowRight className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  )}

                  <button
                    onClick={() => {
                      setDismissedAnns((prev) => [...prev, currentAnn.id]);
                      setActiveAnnIdx(0);
                    }}
                    className="p-2 rounded-lg bg-white/[0.02] border border-white/[0.04] text-zinc-400 hover:text-white hover:bg-white/[0.06] transition-all cursor-pointer"
                    title="Dismiss Announcement"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            );
          })()}
        </div>
      )}

      {/* Hero Header Search Banner */}
      <section className="relative rounded-3xl border border-white/[0.05] bg-gradient-to-br from-zinc-900/60 to-zinc-950/80 p-8 sm:p-12 mb-12 overflow-hidden shadow-2xl">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,rgba(99,102,241,0.06),transparent_50%)]" />
        <div className="absolute inset-0 bg-grid-pattern opacity-[0.015] pointer-events-none" />
        
        <div className="relative z-10 max-w-2xl space-y-4">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-indigo-500/10 px-3 py-1 text-xs font-bold text-indigo-400 border border-indigo-500/20">
            <PackageCheck className="h-3.5 w-3.5" />
            Discover Catalog
          </span>
          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white leading-none">
            Find Your Next Purchase
          </h1>
          <p className="text-zinc-400 text-sm sm:text-base leading-relaxed font-medium">
            Browse items across our active categories. Add them directly to your wishlist, save them in your cart, and head over to checkout.
          </p>

          {/* Search Bar */}
          <div className="pt-4 max-w-md">
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 text-zinc-400">
                <Search className="h-5 w-5" />
              </span>
              <input
                type="text"
                placeholder="Search products or categories..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{ paddingLeft: "2.75rem" }}
                className="w-full py-3 rounded-xl border border-zinc-700 bg-zinc-900/90 text-white placeholder-zinc-500 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Main Catalog Workspace */}
      <section className="space-y-6">
        
        {/* Categories capsule selection list */}
        <div className="flex items-center gap-2 overflow-x-auto pb-3 scrollbar-none border-b border-white/[0.05]">
          <button
            onClick={() => setSelectedCategory(null)}
            className={`rounded-full px-5 py-2 text-xs font-bold tracking-wide uppercase transition-all cursor-pointer ${
              selectedCategory === null 
                ? "bg-indigo-600 text-white shadow-lg shadow-indigo-600/30" 
                : "border border-zinc-800 bg-zinc-900/40 text-zinc-400 hover:text-white hover:bg-zinc-800"
            }`}
          >
            All Products
          </button>
          
          {categories.map((category) => (
            <button
              key={category.id}
              onClick={() => setSelectedCategory(category.id)}
              className={`rounded-full px-5 py-2 text-xs font-bold tracking-wide uppercase whitespace-nowrap transition-all cursor-pointer ${
                selectedCategory === category.id 
                  ? "bg-indigo-600 text-white shadow-lg shadow-indigo-600/30" 
                  : "border border-zinc-800 bg-zinc-900/40 text-zinc-400 hover:text-white hover:bg-zinc-800"
              }`}
            >
              {category.name}
            </button>
          ))}
        </div>

        {/* Error Alert Box */}
        {error && (
          <div className="flex items-center gap-3 rounded-xl bg-rose-500/10 border border-rose-500/20 p-5 text-rose-300">
            <AlertCircle className="h-6 w-6 shrink-0" />
            <div className="flex-1 text-sm">{error}</div>
            <button 
              onClick={fetchCatalog}
              className="text-xs font-bold bg-rose-500/20 px-3 py-1.5 rounded-lg hover:bg-rose-500/30 transition-colors"
            >
              Retry
            </button>
          </div>
        )}

        {/* Catalog Grid View */}
        {loading ? (
          renderSkeletons()
        ) : filteredProducts.length > 0 ? (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
            {filteredProducts.map((product) => {
              const isLowStock = product.quantity > 0 && product.quantity < 10;
              const isOutOfStock = product.quantity === 0;

              return (
                <div 
                  key={product.id} 
                  className="glass-premium-card group relative rounded-2xl border border-white/[0.06] p-5 flex flex-col justify-between overflow-hidden shadow-lg transition-all hover:scale-[1.01]"
                >
                  <div className="relative">
                    
                    {/* Product Image Cover Container */}
                    <div 
                      onClick={() => setSelectedProductDetails(product)}
                      className="w-full h-44 relative rounded-xl overflow-hidden mb-4 bg-zinc-900 border border-white/[0.04] cursor-pointer"
                    >
                      {/* Floating Category badge */}
                      <span className="absolute top-2.5 left-2.5 z-10 inline-flex items-center rounded-full bg-black/60 backdrop-blur-md px-2.5 py-0.5 text-[10px] font-bold text-indigo-400 border border-white/[0.08]">
                        {product.category?.name || "Uncategorized"}
                      </span>

                      {/* Floating Wishlist button */}
                      <button 
                        onClick={(e) => {
                          e.stopPropagation();
                          alert("Saved to wishlist!");
                        }}
                        className="absolute top-2.5 right-2.5 z-10 p-2 rounded-lg bg-black/60 backdrop-blur-md text-zinc-400 hover:text-rose-400 border border-white/[0.08] hover:scale-105 active:scale-95 transition-all cursor-pointer"
                        title="Save to Wishlist"
                      >
                        <Heart className="h-3.5 w-3.5" />
                      </button>

                      {product.imageUrl ? (
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                        />
                      ) : (
                        <div className="w-full h-full flex flex-col items-center justify-center bg-gradient-to-br from-zinc-900 to-indigo-950/20 text-zinc-600 gap-1.5">
                          <ShoppingCart className="h-10 w-10 opacity-30" />
                          <span className="text-[9px] font-bold uppercase tracking-wider text-zinc-500">ShopEasy</span>
                        </div>
                      )}
                    </div>

                    {/* Product Metadata */}
                    <h3 
                      onClick={() => setSelectedProductDetails(product)}
                      className="text-base font-bold text-zinc-200 group-hover:text-white transition-colors line-clamp-2 cursor-pointer hover:underline"
                    >
                      {product.name}
                    </h3>
                    
                    {/* Stock Alert Label indicator */}
                    <div className="mt-2">
                      {isOutOfStock ? (
                        <span className="inline-flex text-[11px] font-bold text-rose-400">
                          Out of stock
                        </span>
                      ) : isLowStock ? (
                        <span className="inline-flex text-[11px] font-bold text-amber-400">
                          Low Stock ({product.quantity} left)
                        </span>
                      ) : (
                        <span className="inline-flex text-[11px] font-bold text-emerald-400">
                          In stock ({product.quantity} units)
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex justify-between items-center mt-6 pt-4 border-t border-white/[0.04]">
                    {/* Price tag */}
                    <div>
                      <p className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Price</p>
                      <p className="text-lg font-extrabold text-zinc-100 mt-0.5">
                        ₹{product.price.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                      </p>
                    </div>

                    {/* Add to Cart button trigger */}
                    <button
                      disabled={isOutOfStock}
                      onClick={() => alert("Added to cart successfully!")}
                      className="p-3 rounded-xl bg-indigo-600 text-white shadow-md shadow-indigo-600/20 hover:bg-indigo-500 hover:scale-105 active:scale-95 disabled:opacity-30 disabled:pointer-events-none transition-all cursor-pointer"
                      title="Add to Shopping Cart"
                    >
                      <ShoppingCart className="h-5 w-5" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          !loading && (
            <div className="text-center py-20 rounded-3xl border border-dashed border-white/[0.06] bg-white/[0.01]">
              <Grid className="h-10 w-10 text-zinc-600 mx-auto mb-4" />
              <h3 className="text-lg font-bold text-zinc-400">No products found</h3>
              <p className="text-sm text-zinc-500 mt-1 max-w-sm mx-auto">
                No matches found for your search query. Try switching filters or categories.
              </p>
            </div>
          )
        )}
      </section>

      {/* Product Details Modal Overlay */}
      {selectedProductDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-sm overflow-y-auto">
          <div className="relative w-full max-w-2xl glass-premium rounded-3xl p-6 border border-white/[0.08] shadow-[0_20px_50px_rgba(0,0,0,0.6)] my-8">
            
            {/* Modal Close Button */}
            <button
              onClick={() => setSelectedProductDetails(null)}
              className="absolute top-4 right-4 p-2 rounded-xl bg-zinc-900 border border-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer z-10"
            >
              <X className="h-5 w-5" />
            </button>

            {/* Modal Content Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4">
              
              {/* Product Image preview */}
              <div className="relative h-64 md:h-full min-h-[260px] w-full rounded-2xl overflow-hidden bg-zinc-900 border border-white/[0.06] flex items-center justify-center">
                {selectedProductDetails.imageUrl ? (
                  <img
                    src={selectedProductDetails.imageUrl}
                    alt={selectedProductDetails.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="flex flex-col items-center justify-center text-zinc-600 gap-2">
                    <ShoppingCart className="h-16 w-16 opacity-30" />
                    <span className="text-[10px] font-bold uppercase tracking-wider text-zinc-500">No Image Preview</span>
                  </div>
                )}
              </div>

              {/* Product Specifications & Description */}
              <div className="flex flex-col justify-between space-y-5">
                <div className="space-y-3">
                  <span className="inline-flex items-center rounded-full bg-indigo-500/10 px-3 py-1 text-xs font-bold text-indigo-400 border border-indigo-500/20">
                    {selectedProductDetails.category?.name || "Uncategorized"}
                  </span>

                  <h2 className="text-2xl font-extrabold text-white leading-tight">
                    {selectedProductDetails.name}
                  </h2>

                  <div className="pt-1">
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold border ${
                      selectedProductDetails.quantity === 0
                        ? "bg-rose-500/10 text-rose-400 border-rose-500/20"
                        : selectedProductDetails.quantity < 10
                        ? "bg-amber-500/10 text-amber-400 border-amber-500/20"
                        : "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                    }`}>
                      {selectedProductDetails.quantity === 0
                        ? "Out of Stock"
                        : selectedProductDetails.quantity < 10
                        ? `Only ${selectedProductDetails.quantity} left in stock!`
                        : `In Stock: ${selectedProductDetails.quantity} units`}
                    </span>
                  </div>

                  <div className="pt-4 border-t border-white/[0.06] space-y-1.5">
                    <h4 className="text-xs font-bold text-zinc-500 uppercase tracking-wider">Product Description</h4>
                    <p className="text-sm text-zinc-400 leading-relaxed font-medium">
                      {selectedProductDetails.description || "Discover premium quality and exceptional aesthetics. This item is fully integrated with ShopEasy sandbox checkout metrics to provide simulated purchasing and inventory updates."}
                    </p>
                  </div>
                </div>

                <div className="pt-6 border-t border-white/[0.06] flex items-center justify-between gap-4">
                  <div>
                    <p className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Price</p>
                    <p className="text-2xl font-black text-zinc-100 mt-0.5">
                      ₹{selectedProductDetails.price.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                    </p>
                  </div>

                  <button
                    disabled={selectedProductDetails.quantity === 0}
                    onClick={() => {
                      alert("Added to cart successfully!");
                      setSelectedProductDetails(null);
                    }}
                    className="flex-1 inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 py-3.5 text-sm font-semibold text-white shadow-lg shadow-indigo-600/25 transition-all cursor-pointer disabled:opacity-30 disabled:pointer-events-none"
                  >
                    <ShoppingCart className="h-5 w-5" />
                    Add To Cart
                  </button>
                </div>

              </div>

            </div>

          </div>
        </div>
      )}

    </div>
  );
}
