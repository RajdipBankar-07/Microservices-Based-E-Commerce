"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { api } from "@/utils/api";
import { Product } from "@/dto/Product";
import { WishlistResponseDTO } from "@/dto/WishlistResponseDTO";
import { useAuth } from "./AuthContext";

interface ApiResponse<T> {
  message: string;
  data: T;
}

interface WishlistContextType {
  wishlistItems: Product[];
  loading: boolean;
  fetchWishlist: () => Promise<void>;
  addToWishlist: (productId: number) => Promise<void>;
  removeFromWishlist: (productId: number) => Promise<void>;
  isInWishlist: (productId: number) => boolean;
  clearWishlist: () => Promise<void>;
}

const WishlistContext = createContext<WishlistContextType | undefined>(undefined);

export function WishlistProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [wishlistItems, setWishlistItems] = useState<Product[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchWishlist = async () => {
    if (!user) {
      setWishlistItems([]);
      return;
    }
    setLoading(true);
    try {
      const res = await api.get<ApiResponse<WishlistResponseDTO>>("/wishlist");
      if (res && res.data) {
        setWishlistItems(res.data.products || []);
      }
    } catch (error) {
      console.error("Failed to fetch wishlist:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWishlist();
  }, [user]);

  const addToWishlist = async (productId: number) => {
    if (!user) {
      throw new Error("Please log in to add items to your wishlist.");
    }
    try {
      const res = await api.post<ApiResponse<WishlistResponseDTO>>(`/wishlist/add/${productId}`);
      if (res && res.data) {
        setWishlistItems(res.data.products || []);
      }
    } catch (error) {
      console.error("Failed to add to wishlist:", error);
      throw error;
    }
  };

  const removeFromWishlist = async (productId: number) => {
    if (!user) {
      throw new Error("Please log in to manage your wishlist.");
    }
    try {
      const res = await api.delete<ApiResponse<WishlistResponseDTO>>(`/wishlist/remove/${productId}`);
      if (res && res.data) {
        setWishlistItems(res.data.products || []);
      }
    } catch (error) {
      console.error("Failed to remove from wishlist:", error);
      throw error;
    }
  };

  const isInWishlist = (productId: number) => {
    return wishlistItems.some((item) => item.id === productId);
  };

  const clearWishlist = async () => {
    if (!user) {
      throw new Error("Please log in to manage your wishlist.");
    }
    try {
      await api.delete<ApiResponse<string>>("/wishlist/clear");
      setWishlistItems([]);
    } catch (error) {
      console.error("Failed to clear wishlist:", error);
      throw error;
    }
  };

  return (
    <WishlistContext.Provider
      value={{
        wishlistItems,
        loading,
        fetchWishlist,
        addToWishlist,
        removeFromWishlist,
        isInWishlist,
        clearWishlist,
      }}
    >
      {children}
    </WishlistContext.Provider>
  );
}

export function useWishlist() {
  const context = useContext(WishlistContext);
  if (context === undefined) {
    throw new Error("useWishlist must be used within a WishlistProvider");
  }
  return context;
}
