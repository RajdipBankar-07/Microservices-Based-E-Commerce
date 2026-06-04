"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { api } from "@/utils/api";
import { Address } from "@/dto/Address";
import { 
  CreditCard, 
  ShoppingBag, 
  Tag, 
  Truck, 
  CheckCircle, 
  ArrowLeft,
  Plus,
  Trash2,
  Save,
  MapPin
} from "lucide-react";
import Link from "next/link";

interface CheckoutResponse {
  orderIds: number[];
  totalBeforeDiscount: number;
  discountAmount: number;
  finalTotal: number;
  message: string;
}

interface ApiResponse<T> {
  message: string;
  data: T;
}


export default function CheckoutPage() {
  const router = useRouter();
  const { token, user } = useAuth();
  const { cartItems, totalPrice, clearCart } = useCart();
  const [loading, setLoading] = useState(false);
  const [couponCode, setCouponCode] = useState("");
  const [discountPercent, setDiscountPercent] = useState<number>(0);
  const [couponMessage, setCouponMessage] = useState<{ text: string; isError: boolean } | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Address book state
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | string>("new");
  const [saveToProfile, setSaveToProfile] = useState(false);
  const [addressUpdating, setAddressUpdating] = useState(false);

  // Shipping & Payment Form State
  const [form, setForm] = useState({
    name: user?.name || "",
    email: user?.email || "",
    phone: "",
    address: "",
    city: "",
    state: "",
    zip: "",
    country: "India",
    cardNumber: "",
    expiry: "",
    cvc: "",
    paymentMethod: "CREDIT_CARD"
  });

  const fetchAddresses = async () => {
    if (!user) return;
    try {
      const res = await api.get<ApiResponse<Address[]>>(`/users/${user.id}/addresses`);
      if (res && res.data) {
        setAddresses(res.data);
      }
    } catch (err: any) {
      console.error("Failed to fetch saved addresses:", err);
    }
  };

  useEffect(() => {
    fetchAddresses();
  }, [user]);

  const handleSelectAddress = (addr: Address) => {
    setSelectedAddressId(addr.id);
    setForm((prev) => ({
      ...prev,
      address: addr.street,
      city: addr.city,
      state: addr.state,
      zip: addr.pincode,
      country: addr.country,
    }));
    setSaveToProfile(false);
  };

  const handleNewAddressSelect = () => {
    setSelectedAddressId("new");
    setForm((prev) => ({
      ...prev,
      address: "",
      city: "",
      state: "",
      zip: "",
      country: "India",
    }));
  };

  const isAddressModified = () => {
    if (selectedAddressId === "new") return false;
    const addr = addresses.find((a) => a.id === selectedAddressId);
    if (!addr) return false;
    return (
      form.address !== addr.street ||
      form.city !== addr.city ||
      form.state !== addr.state ||
      form.zip !== addr.pincode ||
      form.country !== addr.country
    );
  };

  const handleUpdateAddress = async () => {
    if (selectedAddressId === "new" || !user) return;
    setAddressUpdating(true);
    setErrorMessage(null);
    try {
      const payload = {
        street: form.address,
        city: form.city,
        state: form.state,
        pincode: form.zip,
        country: form.country,
        label: addresses.find((a) => a.id === selectedAddressId)?.label || "HOME",
        isDefault: addresses.find((a) => a.id === selectedAddressId)?.isDefault || false,
      };
      await api.put(`/users/${user.id}/addresses/${selectedAddressId}`, payload);
      await fetchAddresses();
      alert("Address updated successfully in your profile!");
    } catch (err: any) {
      setErrorMessage(err.message || "Failed to update address.");
    } finally {
      setAddressUpdating(false);
    }
  };

  const handleDeleteAddress = async (addrId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!user) return;
    if (!confirm("Are you sure you want to delete this address from your profile?")) return;
    try {
      await api.delete(`/users/${user.id}/addresses/${addrId}`);
      if (selectedAddressId === addrId) {
        handleNewAddressSelect();
      }
      await fetchAddresses();
    } catch (err: any) {
      alert(err.message || "Failed to delete address.");
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const applyCoupon = async () => {
    if (!couponCode) return;
    setCouponMessage(null);
    try {
      const res = await api.post<any>("/coupons/validate", {
        code: couponCode,
        userId: user?.id,
        orderAmount: totalPrice
      });

      if (res && res.data) {
        const discountValue = res.data.discountValue || 0.1;
        setDiscountPercent(discountValue);
        setCouponMessage({
          text: `Coupon applied successfully! ${discountValue * 100}% discount.`,
          isError: false,
        });
      } else {
        setCouponMessage({ text: "Coupon code is invalid or expired.", isError: true });
      }
    } catch (err: any) {
      setCouponMessage({ text: err.message || "Failed to validate coupon.", isError: true });
    }
  };

  const discountAmount = totalPrice * discountPercent;
  const finalTotal = Math.max(0, totalPrice - discountAmount);

  const placeOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (cartItems.length === 0) {
      setErrorMessage("Your cart is empty.");
      return;
    }

    if (!form.address || !form.city || !form.zip || !form.phone) {
      setErrorMessage("Please complete all shipping details.");
      return;
    }

    if (form.paymentMethod === "CREDIT_CARD" && (!form.cardNumber || !form.expiry || !form.cvc)) {
      setErrorMessage("Please fill in credit card details.");
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      // 1. Save new address if checked
      if (selectedAddressId === "new" && saveToProfile && user) {
        try {
          const addressPayload = {
            street: form.address,
            city: form.city,
            state: form.state,
            pincode: form.zip,
            country: form.country,
            label: "HOME",
            isDefault: addresses.length === 0
          };
          await api.post(`/users/${user.id}/addresses`, addressPayload);
          await fetchAddresses();
        } catch (addrErr: any) {
          console.warn("Could not save address to profile:", addrErr);
        }
      }

      // 2. Submit checkout request
      const payload = {
        items: cartItems,
        shippingAddress: `${form.address}, ${form.state || ""}`,
        city: form.city,
        zipCode: form.zip,
        country: form.country,
        phone: form.phone,
        couponCode: couponCode || null,
        paymentMethod: form.paymentMethod
      };

      const response = await api.post<any>("/checkout", payload);
      setSuccessMessage("Order placed successfully! Redirecting...");
      clearCart();
      setTimeout(() => {
        router.push("/orders");
      }, 2000);
    } catch (err: any) {
      setErrorMessage(err.message || "Order placement failed. Please verify stock or try again.");
    } finally {
      setLoading(false);
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

      <div className="grid grid-cols-1 gap-10 lg:grid-cols-12 items-start">
        
        {/* Left Column: Checkout Forms */}
        <form onSubmit={placeOrder} className="lg:col-span-7 space-y-8">
          
          {/* Shipping Address (Glassmorphic) */}
          <div className="rounded-2xl border border-white/[0.08] bg-zinc-900/40 backdrop-blur-xl p-6 md:p-8 space-y-6 shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500" />
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                  <Truck className="h-5 w-5" />
                </div>
                <h2 className="text-xl font-bold text-white tracking-tight">Shipping Information</h2>
              </div>
            </div>

            {/* Saved Addresses list */}
            {user && (
              <div className="space-y-3 pt-2">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Select Saved Address or Add New</label>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                  {/* Saved Address Cards */}
                  {addresses.map((addr) => (
                    <div
                      key={addr.id}
                      onClick={() => handleSelectAddress(addr)}
                      className={`relative p-4 rounded-xl border transition-all cursor-pointer flex flex-col justify-between h-36 text-left ${
                        selectedAddressId === addr.id
                          ? "border-indigo-500 bg-indigo-500/5 text-white"
                          : "border-white/[0.06] bg-white/[0.02] hover:bg-white/[0.04] text-zinc-400"
                      }`}
                    >
                      <div>
                        <div className="flex items-center justify-between mb-2">
                          <span className={`inline-flex items-center gap-1 text-[9px] font-bold px-2 py-0.5 rounded-full ${
                            selectedAddressId === addr.id
                              ? "bg-indigo-500 text-white"
                              : "bg-zinc-800 text-zinc-300 border border-white/[0.05]"
                          }`}>
                            <MapPin className="h-2.5 w-2.5" />
                            {addr.label}
                          </span>
                          
                          {/* Trash button */}
                          <button
                            type="button"
                            onClick={(e) => handleDeleteAddress(addr.id, e)}
                            className="p-1 rounded text-zinc-500 hover:text-rose-400 hover:bg-white/[0.04] transition-colors cursor-pointer"
                            title="Delete Saved Address"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                        <p className="text-xs font-semibold line-clamp-2 mt-1">{addr.street}</p>
                        <p className="text-[11px] text-zinc-500 mt-1 truncate">
                          {addr.city}, {addr.state} - {addr.pincode}
                        </p>
                      </div>

                      {selectedAddressId === addr.id && (
                        <span className="text-[10px] text-indigo-400 font-bold self-end">Selected</span>
                      )}
                    </div>
                  ))}

                  {/* Add New Address Card */}
                  <div
                    onClick={handleNewAddressSelect}
                    className={`p-4 rounded-xl border border-dashed transition-all cursor-pointer flex flex-col items-center justify-center h-36 ${
                      selectedAddressId === "new"
                        ? "border-indigo-500 bg-indigo-500/5 text-white"
                        : "border-white/[0.12] bg-white/[0.01] hover:bg-white/[0.03] text-zinc-500"
                    }`}
                  >
                    <Plus className="h-6 w-6 mb-2" />
                    <span className="text-xs font-bold">New Shipping Address</span>
                    {selectedAddressId === "new" && (
                      <span className="text-[10px] text-indigo-400 font-bold mt-1">Form Enabled</span>
                    )}
                  </div>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 pt-4 border-t border-white/[0.04]">
              {/* Address modified warning & Save changes button */}
              {isAddressModified() && (
                <div className="sm:col-span-2 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 p-3.5 rounded-xl border border-amber-500/20 bg-amber-500/5 text-amber-300">
                  <div className="text-xs font-semibold">
                    You have modified this address's details. Save changes to your profile?
                  </div>
                  <button
                    type="button"
                    disabled={addressUpdating}
                    onClick={handleUpdateAddress}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-amber-500 hover:bg-amber-450 px-3.5 py-2 text-xs font-bold text-zinc-950 shadow transition-all cursor-pointer hover:scale-102 active:scale-98"
                  >
                    <Save className="h-3.5 w-3.5" />
                    {addressUpdating ? "Saving..." : "Save Changes"}
                  </button>
                </div>
              )}

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Full Name</label>
                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  required
                  placeholder="John Doe"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Email Address</label>
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  required
                  placeholder="john@example.com"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="sm:col-span-2 space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Street Address</label>
                <input
                  type="text"
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  required
                  placeholder="123 Luxury Avenue, Penthouse 4"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">City</label>
                <input
                  type="text"
                  name="city"
                  value={form.city}
                  onChange={handleChange}
                  required
                  placeholder="Mumbai"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">State / Province</label>
                <input
                  type="text"
                  name="state"
                  value={form.state}
                  onChange={handleChange}
                  placeholder="Maharashtra"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Postal ZIP Code</label>
                <input
                  type="text"
                  name="zip"
                  value={form.zip}
                  onChange={handleChange}
                  required
                  placeholder="400001"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Phone Number</label>
                <input
                  type="tel"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  required
                  placeholder="+91 9876543210"
                  className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                />
              </div>

              {selectedAddressId === "new" && (
                <div className="sm:col-span-2 flex items-center gap-2.5 pt-2">
                  <input
                    type="checkbox"
                    id="saveToProfile"
                    checked={saveToProfile}
                    onChange={(e) => setSaveToProfile(e.target.checked)}
                    className="h-4 w-4 rounded border-white/[0.1] bg-zinc-900 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                  />
                  <label htmlFor="saveToProfile" className="text-xs font-bold text-zinc-400 uppercase tracking-wider cursor-pointer select-none">
                    Save this address to my profile address book
                  </label>
                </div>
              )}
            </div>
          </div>

          {/* Payment Method & Gateway Stub */}
          <div className="rounded-2xl border border-white/[0.08] bg-zinc-900/40 backdrop-blur-xl p-6 md:p-8 space-y-6 shadow-2xl relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
                  <CreditCard className="h-5 w-5" />
                </div>
                <h2 className="text-xl font-bold text-white tracking-tight">Secure Payment</h2>
              </div>
              <span className="text-[10px] uppercase tracking-wider font-extrabold text-indigo-400 bg-indigo-500/10 border border-indigo-500/20 px-2 py-0.5 rounded-full">
                Sandbox Mode
              </span>
            </div>

            <div className="space-y-4">
              <div className="flex items-center gap-4 border-b border-white/[0.06] pb-4 mb-4">
                <label className="flex items-center gap-3 cursor-pointer">
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="CREDIT_CARD"
                    checked={form.paymentMethod === "CREDIT_CARD"}
                    onChange={handleChange}
                    className="h-4 w-4 border-white/[0.1] bg-zinc-900 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="text-sm font-semibold text-zinc-200">Stripe Payment Gateway Stub</span>
                </label>
              </div>

              {form.paymentMethod === "CREDIT_CARD" && (
                <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
                  <div className="sm:col-span-3 space-y-1.5">
                    <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Card Number</label>
                    <div className="relative">
                      <input
                        type="text"
                        name="cardNumber"
                        value={form.cardNumber}
                        onChange={handleChange}
                        maxLength={19}
                        placeholder="4242 4242 4242 4242"
                        className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                      />
                      <div className="absolute right-3.5 top-3.5 flex gap-1">
                        <div className="h-5 w-8 rounded bg-zinc-800 border border-white/10" />
                      </div>
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Expiry Date</label>
                    <input
                      type="text"
                      name="expiry"
                      value={form.expiry}
                      onChange={handleChange}
                      placeholder="MM/YY"
                      maxLength={5}
                      className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-zinc-400 uppercase tracking-wider">Security CVC</label>
                    <input
                      type="text"
                      name="cvc"
                      value={form.cvc}
                      onChange={handleChange}
                      placeholder="123"
                      maxLength={3}
                      className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3 text-sm text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all duration-200"
                    />
                  </div>
                </div>
              )}
            </div>
          </div>
        </form>

        {/* Right Column: Order Summary & Coupon Code */}
        <div className="lg:col-span-5 space-y-6">
          <div className="rounded-2xl border border-white/[0.08] bg-zinc-900/40 backdrop-blur-xl p-6 md:p-8 space-y-6 shadow-2xl relative">
            
            {/* Title */}
            <div className="flex items-center gap-3 border-b border-white/[0.06] pb-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-pink-500/10 text-pink-400 border border-pink-500/20">
                <ShoppingBag className="h-5 w-5" />
              </div>
              <h2 className="text-xl font-bold text-white tracking-tight">Order Summary</h2>
            </div>

            {/* Cart Items List */}
            {cartItems.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-sm text-zinc-500">Your cart is empty.</p>
                <Link href="/" className="mt-4 inline-block text-xs font-bold text-indigo-400 hover:text-indigo-300">
                  Shop Products
                </Link>
              </div>
            ) : (
              <div className="max-h-60 overflow-y-auto space-y-4 pr-1 scrollbar-thin scrollbar-thumb-zinc-800 scrollbar-track-transparent">
                {cartItems.map((item) => (
                  <div key={item.id} className="flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      {item.product.imageUrl ? (
                        <img
                          src={item.product.imageUrl}
                          alt={item.product.name}
                          className="h-12 w-12 rounded-xl object-cover border border-white/[0.08]"
                        />
                      ) : (
                        <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-zinc-800 border border-white/[0.08] text-zinc-400 text-xs">
                          Item
                        </div>
                      )}
                      <div>
                        <p className="text-sm font-semibold text-zinc-200 line-clamp-1">{item.product.name}</p>
                        <p className="text-xs text-zinc-500">Qty: {item.quantity}</p>
                      </div>
                    </div>
                    <span className="text-sm font-bold text-zinc-300">
                      ₹{(item.product.price * item.quantity).toLocaleString()}
                    </span>
                  </div>
                ))}
              </div>
            )}

            {/* Coupon Code Section */}
            <div className="border-t border-white/[0.06] pt-5 space-y-3">
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Tag className="absolute left-3.5 top-3.5 h-4 w-4 text-zinc-500" />
                  <input
                    type="text"
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                    placeholder="PROMO CODE (e.g. WELCOME10)"
                    className="w-full rounded-xl border border-white/[0.06] bg-white/[0.03] pl-10 pr-4 py-3 text-xs text-zinc-200 placeholder-zinc-600 focus:border-indigo-500 focus:bg-white/[0.05] focus:outline-none transition-all"
                  />
                </div>
                <button
                  type="button"
                  onClick={applyCoupon}
                  className="rounded-xl bg-white/[0.04] border border-white/[0.08] px-4 py-3 text-xs font-bold text-zinc-300 hover:bg-white/[0.08] hover:text-white transition-colors cursor-pointer"
                >
                  Apply
                </button>
              </div>
              {couponMessage && (
                <p className={`text-[11px] font-semibold ${couponMessage.isError ? "text-rose-400" : "text-emerald-400"}`}>
                  {couponMessage.text}
                </p>
              )}
            </div>

            {/* Price Calculations */}
            <div className="border-t border-white/[0.06] pt-5 space-y-3 text-sm">
              <div className="flex justify-between text-zinc-400">
                <span>Subtotal</span>
                <span className="font-semibold text-zinc-300">₹{totalPrice.toLocaleString()}</span>
              </div>
              {discountPercent > 0 && (
                <div className="flex justify-between text-emerald-400 font-medium">
                  <span>Promo Discount ({discountPercent * 100}%)</span>
                  <span>-₹{discountAmount.toLocaleString()}</span>
                </div>
              )}
              <div className="flex justify-between text-zinc-400">
                <span>Shipping</span>
                <span className="font-semibold text-emerald-400">FREE</span>
              </div>
              <div className="flex justify-between text-base font-bold text-white pt-2 border-t border-white/[0.04]">
                <span>Total Amount</span>
                <span className="text-indigo-400">₹{finalTotal.toLocaleString()}</span>
              </div>
            </div>

            {/* Error / Success Notifications */}
            {errorMessage && (
              <div className="rounded-xl bg-rose-500/10 border border-rose-500/20 p-3 text-xs font-semibold text-rose-400 text-center">
                {errorMessage}
              </div>
            )}
            {successMessage && (
              <div className="rounded-xl bg-emerald-500/10 border border-emerald-500/20 p-3 text-xs font-semibold text-emerald-400 text-center flex items-center justify-center gap-2">
                <CheckCircle className="h-4 w-4 animate-bounce" />
                {successMessage}
              </div>
            )}

            {/* Submit Button */}
            <button
              onClick={placeOrder}
              disabled={loading || cartItems.length === 0}
              className="w-full py-4 rounded-xl text-sm font-bold text-white bg-gradient-to-tr from-indigo-500 to-purple-600 shadow-[0_4px_20px_rgba(99,102,241,0.25)] hover:scale-[1.01] active:scale-[0.99] disabled:opacity-40 disabled:pointer-events-none transition-all duration-200 cursor-pointer flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  <span>Processing secure checkout...</span>
                </>
              ) : (
                <span>Complete Purchase</span>
              )}
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
