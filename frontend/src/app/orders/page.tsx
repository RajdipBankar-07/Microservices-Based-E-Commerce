"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { api } from "@/utils/api";
import { ShoppingBag, ArrowLeft, Clock, RefreshCw, XCircle, CheckCircle, Package } from "lucide-react";
import Link from "next/link";

interface Order {
  id: number;
  product: {
    id: number;
    name: string;
    price: number;
    imageUrl?: string;
  };
  quantity: number;
  status: string;
  orderDate: string;
}

export default function OrdersPage() {
  const router = useRouter();
  const { user } = useAuth();
  const { addToCart } = useCart();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  const fetchOrders = async () => {
    if (!user) return;
    try {
      const data = await api.get<Order[]>(`/orders/user/${user.id}`);
      // Sort orders by id or date descending (latest first)
      const sorted = (data || []).sort((a, b) => b.id - a.id);
      setOrders(sorted);
    } catch (err) {
      console.error("Failed to load orders", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [user]);

  const handleCancelOrder = async (orderId: number) => {
    if (!confirm("Are you sure you want to cancel this order?")) return;
    setActionLoading(orderId);
    try {
      await api.put(`/orders/${orderId}/cancel`);
      // Refresh the orders
      await fetchOrders();
    } catch (err: any) {
      alert(err.message || "Failed to cancel order.");
    } finally {
      setActionLoading(null);
    }
  };

  const handleReorder = async (productId: number) => {
    setActionLoading(productId);
    try {
      await addToCart(productId, 1);
      router.push("/checkout");
    } catch (err: any) {
      alert(err.message || "Failed to reorder item.");
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toUpperCase()) {
      case "PLACED":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-indigo-500/10 px-2.5 py-0.5 text-xs font-bold text-indigo-400 border border-indigo-500/20">
            <Clock className="h-3 w-3 animate-pulse" />
            Placed
          </span>
        );
      case "CANCELLED":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-bold text-rose-400 border border-rose-500/20">
            <XCircle className="h-3 w-3" />
            Cancelled
          </span>
        );
      case "REFUNDED":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-bold text-amber-400 border border-amber-500/20">
            <RefreshCw className="h-3 w-3" />
            Refunded
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-bold text-emerald-400 border border-emerald-500/20">
            <CheckCircle className="h-3 w-3" />
            Completed
          </span>
        );
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
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Package className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight leading-none">Order History</h1>
            <p className="text-zinc-500 text-xs mt-1">Review your recent purchases and check transaction status.</p>
          </div>
        </div>

        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-32 rounded-2xl border border-white/[0.05] bg-zinc-900/20 animate-pulse" />
            ))}
          </div>
        ) : orders.length === 0 ? (
          <div className="rounded-2xl border border-white/[0.06] bg-zinc-900/20 backdrop-blur-xl p-12 text-center max-w-xl mx-auto space-y-4">
            <div className="inline-flex h-16 w-16 items-center justify-center rounded-2xl bg-zinc-900 text-zinc-600 border border-white/[0.05] mb-2">
              <ShoppingBag className="h-8 w-8" />
            </div>
            <h3 className="text-lg font-bold text-white">No orders found</h3>
            <p className="text-zinc-500 text-sm">You have not placed any orders yet. Head back to the store to start shopping!</p>
            <Link 
              href="/" 
              className="inline-flex items-center justify-center rounded-xl bg-indigo-600 hover:bg-indigo-500 px-6 py-3 text-xs font-bold text-white shadow-md transition-all cursor-pointer"
            >
              Start Browsing
            </Link>
          </div>
        ) : (
          <div className="space-y-6">
            {orders.map((order) => (
              <div 
                key={order.id} 
                className="rounded-2xl border border-white/[0.06] bg-zinc-900/40 backdrop-blur-xl p-6 shadow-xl relative overflow-hidden transition-transform duration-200 hover:scale-[1.005]"
              >
                {/* Glow border depending on status */}
                <div className={`absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r ${
                  order.status === "CANCELLED" 
                    ? "from-rose-500/50 to-transparent" 
                    : order.status === "REFUNDED"
                    ? "from-amber-500/50 to-transparent"
                    : "from-indigo-500/50 to-transparent"
                }`} />

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                  
                  {/* Left block: Product details */}
                  <div className="flex items-center gap-4">
                    {order.product.imageUrl ? (
                      <img
                        src={order.product.imageUrl}
                        alt={order.product.name}
                        className="h-16 w-16 rounded-xl object-cover border border-white/[0.08]"
                      />
                    ) : (
                      <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-zinc-800 border border-white/[0.08] text-zinc-500 text-xs">
                        Item
                      </div>
                    )}
                    <div className="space-y-1">
                      <h4 className="text-base font-bold text-white">{order.product.name}</h4>
                      <p className="text-xs text-zinc-500">
                        Placed on {new Date(order.orderDate).toLocaleDateString("en-IN", {
                          year: "numeric",
                          month: "long",
                          day: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>
                      <div className="flex items-center gap-3 mt-1.5">
                        <span className="text-xs text-zinc-400">Qty: <span className="font-bold text-zinc-200">{order.quantity}</span></span>
                        <span className="text-xs text-zinc-500">|</span>
                        <span className="text-xs text-zinc-400">Total Price: <span className="font-bold text-indigo-400">₹{(order.product.price * order.quantity).toLocaleString()}</span></span>
                      </div>
                    </div>
                  </div>

                  {/* Right block: status badge & action buttons */}
                  <div className="flex flex-wrap items-center gap-4 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 border-white/[0.04] pt-4 md:pt-0">
                    <div className="flex flex-col gap-1 md:items-end">
                      <span className="text-[10px] text-zinc-500 font-bold uppercase tracking-wider">Order ID: #{order.id}</span>
                      <div className="mt-1">{getStatusBadge(order.status)}</div>
                    </div>

                    <div className="flex items-center gap-3">
                      {order.status === "PLACED" && (
                        <button
                          onClick={() => handleCancelOrder(order.id)}
                          disabled={actionLoading === order.id}
                          className="rounded-xl border border-rose-500/20 bg-rose-500/5 px-4 py-2.5 text-xs font-bold text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 disabled:opacity-40 transition-all cursor-pointer"
                        >
                          {actionLoading === order.id ? "Cancelling..." : "Cancel Order"}
                        </button>
                      )}
                      
                      <button
                        onClick={() => handleReorder(order.product.id)}
                        disabled={actionLoading === order.product.id}
                        className="rounded-xl bg-white/[0.03] border border-white/[0.08] px-4 py-2.5 text-xs font-bold text-zinc-300 hover:bg-white/[0.06] hover:text-white transition-colors cursor-pointer"
                      >
                        Reorder Item
                      </button>
                    </div>
                  </div>

                </div>

              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
}
