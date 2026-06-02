"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { 
  ShoppingCart, 
  Heart, 
  User, 
  LogOut, 
  Menu, 
  X, 
  LayoutDashboard, 
  ShoppingBag 
} from "lucide-react";

export default function Navbar() {
  const { user, logout, loading } = useAuth();
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);

  const isActive = (path: string) => pathname === path;

  const handleLogout = async () => {
    setUserDropdownOpen(false);
    setMobileMenuOpen(false);
    await logout();
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-white/[0.05] bg-zinc-950/65 backdrop-blur-xl transition-all">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        
        {/* Brand Group */}
        <div className="flex items-center gap-10">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-tr from-indigo-500 to-purple-600 text-white shadow-[0_4px_15px_rgba(99,102,241,0.3)] group-hover:scale-105 transition-transform duration-200">
              <ShoppingBag className="h-5 w-5" />
            </div>
            <span className="bg-gradient-to-r from-white via-zinc-200 to-zinc-400 bg-clip-text text-xl font-extrabold tracking-tight text-transparent">
              ShopEasy
            </span>
          </Link>

          {/* Desktop Nav Items */}
          <nav className="hidden md:flex items-center gap-8">
            <Link 
              href="/" 
              className={`text-sm font-semibold tracking-wide relative py-2 transition-colors hover:text-white ${
                isActive("/") ? "text-indigo-400" : "text-zinc-400"
              }`}
            >
              Shop
              {isActive("/") && (
                <span className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
              )}
            </Link>
            {user && (
              <>
                <Link 
                  href="/wishlist" 
                  className={`text-sm font-semibold tracking-wide relative py-2 transition-colors hover:text-white ${
                    isActive("/wishlist") ? "text-indigo-400" : "text-zinc-400"
                  }`}
                >
                  Wishlist
                  {isActive("/wishlist") && (
                    <span className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
                  )}
                </Link>
                <Link 
                  href="/orders" 
                  className={`text-sm font-semibold tracking-wide relative py-2 transition-colors hover:text-white ${
                    isActive("/orders") ? "text-indigo-400" : "text-zinc-400"
                  }`}
                >
                  My Orders
                  {isActive("/orders") && (
                    <span className="absolute bottom-0 left-0 right-0 h-[2px] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
                  )}
                </Link>
              </>
            )}
          </nav>
        </div>

        {/* Action Widgets */}
        <div className="hidden md:flex items-center gap-4">
          {loading ? (
            <div className="h-8 w-8 animate-pulse rounded-full bg-zinc-800" />
          ) : user ? (
            <div className="relative">
              {/* Profile Avatar trigger */}
              <button 
                onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                className="flex items-center gap-2.5 rounded-full border border-white/[0.06] bg-white/[0.02] p-1.5 pr-4 hover:bg-white/[0.05] hover:border-white/[0.1] transition-all duration-200 cursor-pointer"
              >
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-bold text-sm">
                  {user.name.split(" ").map(n => n[0]).join("").toUpperCase()}
                </div>
                <span className="text-sm font-bold text-zinc-300">
                  {user.name}
                </span>
              </button>

              {/* Session Dropdown menu */}
              {userDropdownOpen && (
                <>
                  <div className="fixed inset-0 z-10" onClick={() => setUserDropdownOpen(false)} />
                  <div className="absolute right-0 mt-2.5 w-60 origin-top-right rounded-xl border border-white/[0.08] bg-zinc-950 p-2.5 shadow-[0_10px_30px_rgba(0,0,0,0.5)] ring-1 ring-black/5 z-20">
                    <div className="px-3 py-2.5 border-b border-white/[0.05] mb-2">
                      <p className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Account Signed In</p>
                      <p className="text-sm font-semibold text-zinc-200 truncate mt-0.5">{user.email}</p>
                      <span className="mt-1.5 inline-flex items-center rounded-full bg-indigo-500/10 px-2 py-0.5 text-[10px] font-bold text-indigo-400 border border-indigo-500/20">
                        {user.role}
                      </span>
                    </div>

                    {user.role === "ADMIN" && (
                      <Link 
                        href="/admin" 
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-zinc-400 hover:bg-white/[0.04] hover:text-white transition-colors"
                      >
                        <LayoutDashboard className="h-4 w-4" />
                        Admin Dashboard
                      </Link>
                    )}

                    <Link 
                      href="/cart" 
                      onClick={() => setUserDropdownOpen(false)}
                      className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-zinc-400 hover:bg-white/[0.04] hover:text-white transition-colors"
                    >
                      <ShoppingCart className="h-4 w-4" />
                      Shopping Cart
                    </Link>

                    <Link 
                      href="/wishlist" 
                      onClick={() => setUserDropdownOpen(false)}
                      className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-zinc-400 hover:bg-white/[0.04] hover:text-white transition-colors"
                    >
                      <Heart className="h-4 w-4" />
                      My Wishlist
                    </Link>

                    <button 
                      onClick={handleLogout}
                      className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 transition-colors text-left cursor-pointer"
                    >
                      <LogOut className="h-4 w-4" />
                      Sign Out
                    </button>
                  </div>
                </>
              )}
            </div>
          ) : (
            <div className="flex items-center gap-4">
              <Link 
                href="/login" 
                className="text-sm font-semibold text-zinc-400 hover:text-white transition-colors"
              >
                Sign In
              </Link>
              <Link 
                href="/register" 
                className="inline-flex h-9 items-center justify-center rounded-xl bg-indigo-600 px-5 text-xs font-bold text-white shadow-md hover:bg-indigo-500 active:scale-[0.98] transition-all cursor-pointer"
              >
                Register
              </Link>
            </div>
          )}
        </div>

        {/* Mobile menu icon */}
        <div className="flex md:hidden">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="inline-flex items-center justify-center rounded-lg p-2 text-zinc-400 hover:bg-zinc-900 hover:text-white focus:outline-none cursor-pointer"
          >
            {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-white/[0.06] bg-zinc-950 px-4 py-4 space-y-4">
          <nav className="flex flex-col gap-1.5">
            <Link 
              href="/" 
              onClick={() => setMobileMenuOpen(false)}
              className={`rounded-lg px-3 py-2 text-base font-semibold transition-colors ${
                isActive("/") ? "bg-white/[0.04] text-white" : "text-zinc-400 hover:bg-white/[0.02]"
              }`}
            >
              Shop
            </Link>
            {user && (
              <>
                <Link 
                  href="/wishlist" 
                  onClick={() => setMobileMenuOpen(false)}
                  className={`rounded-lg px-3 py-2 text-base font-semibold transition-colors ${
                    isActive("/wishlist") ? "bg-white/[0.04] text-white" : "text-zinc-400 hover:bg-white/[0.02]"
                  }`}
                >
                  Wishlist
                </Link>
                <Link 
                  href="/cart" 
                  onClick={() => setMobileMenuOpen(false)}
                  className={`rounded-lg px-3 py-2 text-base font-semibold transition-colors ${
                    isActive("/cart") ? "bg-white/[0.04] text-white" : "text-zinc-400 hover:bg-white/[0.02]"
                  }`}
                >
                  Cart
                </Link>
                <Link 
                  href="/orders" 
                  onClick={() => setMobileMenuOpen(false)}
                  className={`rounded-lg px-3 py-2 text-base font-semibold transition-colors ${
                    isActive("/orders") ? "bg-white/[0.04] text-white" : "text-zinc-400 hover:bg-white/[0.02]"
                  }`}
                >
                  My Orders
                </Link>
              </>
            )}
          </nav>

          <div className="border-t border-white/[0.06] pt-4">
            {user ? (
              <div className="space-y-2">
                <div className="flex items-center gap-3 px-3 py-1 mb-2">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-500/10 text-indigo-400 font-bold border border-indigo-500/20">
                    {user.name.split(" ").map(n => n[0]).join("").toUpperCase()}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white">{user.name}</p>
                    <p className="text-xs text-zinc-500 truncate max-w-[200px]">{user.email}</p>
                  </div>
                </div>
                {user.role === "ADMIN" && (
                  <Link
                    href="/admin"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-zinc-400 hover:bg-white/[0.04] hover:text-white"
                  >
                    <LayoutDashboard className="h-4 w-4" />
                    Admin Dashboard
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 cursor-pointer"
                >
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-3 px-2">
                <Link 
                  href="/login" 
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center justify-center rounded-xl border border-white/[0.06] py-2 text-sm font-semibold text-zinc-400 hover:text-white hover:bg-white/[0.02]"
                >
                  Sign In
                </Link>
                <Link 
                  href="/register" 
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center justify-center rounded-xl bg-indigo-600 py-2 text-sm font-semibold text-white hover:bg-indigo-500 transition-colors"
                >
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
