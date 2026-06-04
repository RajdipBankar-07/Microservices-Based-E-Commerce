"use client";

import React, { useState } from "react";
import { useCart } from "@/context/CartContext";
import { ShoppingCart, X, Plus, Minus, Trash2, ArrowRight } from "lucide-react";
import { useRouter } from "next/navigation";

export default function CartDrawer() {
  const router = useRouter();
  const { cartItems, totalPrice, updateQuantity, removeFromCart, clearCart } = useCart();
  const [isOpen, setIsOpen] = useState(false);

  const handleCheckout = () => {
    setIsOpen(false);
    router.push("/checkout");
  };

  return (
    <>
      {/* Floating Toggle Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-tr from-indigo-500 to-purple-600 text-white shadow-[0_6px_20px_rgba(99,102,241,0.4)] hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer"
        title="View Shopping Cart"
      >
        <div className="relative">
          <ShoppingCart className="h-6 w-6" />
          {cartItems.length > 0 && (
            <span className="absolute -top-2.5 -right-2.5 flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-black text-white shadow-md animate-bounce">
              {cartItems.reduce((acc, item) => acc + item.quantity, 0)}
            </span>
          )}
        </div>
      </button>

      {/* Backdrop */}
      {isOpen && (
        <div
          onClick={() => setIsOpen(false)}
          className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs transition-opacity duration-300"
        />
      )}

      {/* Drawer Panel */}
      <div
        className={`fixed top-0 right-0 z-50 h-full w-full max-w-md bg-zinc-950/90 border-l border-white/[0.08] backdrop-blur-xl shadow-2xl transition-transform duration-300 transform flex flex-col justify-between ${
          isOpen ? "translate-x-0" : "translate-x-full"
        }`}
      >
        {/* Drawer Header */}
        <div className="p-6 border-b border-white/[0.06] flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              <ShoppingCart className="h-4.5 w-4.5" />
            </div>
            <h3 className="text-lg font-bold text-white tracking-tight">Your Cart</h3>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            className="p-1.5 rounded-lg hover:bg-zinc-900 text-zinc-400 hover:text-white transition-colors cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Drawer Body (Cart Items) */}
        <div className="flex-1 overflow-y-auto p-6 space-y-5 scrollbar-thin">
          {cartItems.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center space-y-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-zinc-900 text-zinc-500 border border-white/[0.05]">
                <ShoppingCart className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm font-bold text-zinc-300">Your cart is empty</p>
                <p className="text-xs text-zinc-500 mt-1">Add items from the marketplace to get started.</p>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {cartItems.map((item) => (
                <div
                  key={item.id}
                  className="flex gap-4 border-b border-white/[0.04] pb-4 last:border-b-0 items-start justify-between"
                >
                  <div className="flex gap-3">
                    {item.product.imageUrl ? (
                      <img
                        src={item.product.imageUrl}
                        alt={item.product.name}
                        className="h-12 w-12 rounded-lg object-cover border border-white/[0.08]"
                      />
                    ) : (
                      <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-zinc-800 border border-white/[0.08] text-zinc-500 text-xs">
                        Item
                      </div>
                    )}
                    <div className="space-y-1">
                      <h4 className="text-xs font-bold text-zinc-200 line-clamp-1">{item.product.name}</h4>
                      <p className="text-xs text-indigo-400 font-extrabold">₹{item.product.price.toLocaleString("en-IN")}</p>
                      
                      {/* Quantity Toggles */}
                      <div className="flex items-center gap-2 mt-2">
                        <button
                          onClick={() => updateQuantity(item.id, Math.max(1, item.quantity - 1))}
                          className="p-1 rounded bg-white/[0.02] border border-white/[0.06] text-zinc-450 hover:text-white cursor-pointer"
                        >
                          <Minus className="h-3 w-3" />
                        </button>
                        <span className="text-xs font-extrabold text-white px-1.5">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity + 1)}
                          className="p-1 rounded bg-white/[0.02] border border-white/[0.06] text-zinc-450 hover:text-white cursor-pointer"
                        >
                          <Plus className="h-3 w-3" />
                        </button>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={() => removeFromCart(item.id)}
                    className="p-1.5 rounded-lg border border-rose-500/10 hover:bg-rose-500/10 text-rose-400 shrink-0 cursor-pointer"
                    title="Remove item"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Drawer Footer */}
        {cartItems.length > 0 && (
          <div className="p-6 border-t border-white/[0.06] bg-zinc-950 space-y-4">
            <div className="flex items-center justify-between text-sm">
              <span className="font-bold text-zinc-400">Total Price</span>
              <span className="text-lg font-black text-indigo-450">₹{totalPrice.toLocaleString("en-IN")}</span>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-2">
              <button
                onClick={clearCart}
                className="w-full py-3 rounded-xl border border-white/[0.06] hover:bg-white/[0.02] text-xs font-bold text-zinc-400 hover:text-white transition-colors cursor-pointer"
              >
                Clear Cart
              </button>
              <button
                onClick={handleCheckout}
                className="w-full py-3 rounded-xl bg-gradient-to-tr from-indigo-500 to-purple-600 text-xs font-bold text-white shadow-md hover:scale-[1.01] transition-transform cursor-pointer flex items-center justify-center gap-1.5"
              >
                <span>Checkout</span>
                <ArrowRight className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
