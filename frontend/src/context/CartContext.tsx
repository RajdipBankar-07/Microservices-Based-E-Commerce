"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { api } from "@/utils/api";
import { CartItem } from "@/dto/CartItem";
import { useAuth } from "./AuthContext";

interface CartSummaryResponse {
  items: CartItem[];
  totalPrice: number;
}

interface ApiResponse<T> {
  message: string;
  data: T;
}

interface CartContextType {
  cartItems: CartItem[];
  totalPrice: number;
  loading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (productId: number, quantity: number) => Promise<void>;
  updateQuantity: (cartItemId: number, quantity: number) => Promise<void>;
  removeFromCart: (cartItemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [totalPrice, setTotalPrice] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchCart = async () => {
    if (!user) {
      setCartItems([]);
      setTotalPrice(0);
      return;
    }
    setLoading(true);
    try {
      const res = await api.get<ApiResponse<CartSummaryResponse>>(`/cart/${user.id}`);
      if (res.data) {
        setCartItems(res.data.items || []);
        setTotalPrice(res.data.totalPrice || 0);
      }
    } catch (error) {
      console.error("Failed to fetch cart:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [user]);

  const addToCart = async (productId: number, quantity: number) => {
    if (!user) return;
    try {
      await api.post<ApiResponse<CartItem>>("/cart", {
        userId: user.id,
        productId,
        quantity,
      });
      await fetchCart();
    } catch (error) {
      console.error("Failed to add to cart:", error);
      throw error;
    }
  };

  const updateQuantity = async (cartItemId: number, quantity: number) => {
    try {
      await api.put<ApiResponse<CartItem>>(`/cart/${cartItemId}?quantity=${quantity}`);
      await fetchCart();
    } catch (error) {
      console.error("Failed to update quantity:", error);
      throw error;
    }
  };

  const removeFromCart = async (cartItemId: number) => {
    try {
      await api.delete<ApiResponse<string>>(`/cart/item/${cartItemId}`);
      await fetchCart();
    } catch (error) {
      console.error("Failed to remove item:", error);
      throw error;
    }
  };

  const clearCart = async () => {
    if (!user) return;
    try {
      await api.delete<ApiResponse<string>>(`/cart/${user.id}/clear`);
      setCartItems([]);
      setTotalPrice(0);
    } catch (error) {
      console.error("Failed to clear cart:", error);
      throw error;
    }
  };

  return (
    <CartContext.Provider
      value={{
        cartItems,
        totalPrice,
        loading,
        fetchCart,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
