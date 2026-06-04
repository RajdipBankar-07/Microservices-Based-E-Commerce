import type { Metadata } from "next";
import { Outfit } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { CartProvider } from "@/context/CartContext";
import { WishlistProvider } from "@/context/WishlistContext";
import Navbar from "@/components/Navbar";


const outfit = Outfit({
  subsets: ["latin"],
  variable: "--font-outfit",
});

export const metadata: Metadata = {
  title: "ShopEasy | Premium E-Commerce Experience",
  description: "Explore our premium marketplace featuring the latest gadgets, fashion apparel, home decors, and exclusive coupons.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${outfit.variable} h-full antialiased dark`}
      style={{ colorScheme: "dark" }}
    >
      <body className="min-h-full flex flex-col bg-zinc-950 text-zinc-100 font-sans selection:bg-indigo-500/30 selection:text-indigo-200 relative">
        
        {/* Global ambient background grid and light spots */}
        <div className="fixed inset-0 bg-[radial-gradient(ellipse_at_top,rgba(16,16,24,0.3)_0%,rgba(9,9,11,1)_80%)] pointer-events-none z-0" />
        <div className="fixed inset-0 bg-grid-pattern opacity-[0.02] pointer-events-none z-0" />
        
        <AuthProvider>
          <CartProvider>
            <WishlistProvider>
              <div className="relative z-10 flex-1 flex flex-col">
                <Navbar />
                <main className="flex-1 flex flex-col">
                  {children}
                </main>
                
                {/* Elegant SaaS Footer */}
                <footer className="border-t border-white/[0.04] bg-zinc-950/60 backdrop-blur-md py-8">
                  <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center md:flex md:items-center md:justify-between">
                    <div className="flex justify-center space-x-6 md:order-2">
                      <span className="text-zinc-500 text-xs hover:text-indigo-400 transition-colors cursor-pointer">Privacy</span>
                      <span className="text-zinc-500 text-xs hover:text-indigo-400 transition-colors cursor-pointer">Terms</span>
                      <span className="text-zinc-500 text-xs hover:text-indigo-400 transition-colors cursor-pointer">Support API</span>
                    </div>
                    <div className="mt-4 md:mt-0 md:order-1">
                      <p className="text-xs text-zinc-600">
                        &copy; {new Date().getFullYear()} ShopEasy. Designed for premium aesthetics and fluid interactions.
                      </p>
                    </div>
                  </div>
                </footer>
              </div>
            </WishlistProvider>
          </CartProvider>
        </AuthProvider>

      </body>
    </html>
  );
}

