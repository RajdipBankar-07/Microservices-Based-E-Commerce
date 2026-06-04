// src/components/SearchBar.tsx
"use client";
import React, { useState, useEffect, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Search } from "lucide-react";

// Inline custom debounce to avoid external dependency issues
function debounce<T extends (...args: any[]) => void>(func: T, wait: number) {
  let timeout: NodeJS.Timeout | null = null;
  const debounced = function(this: any, ...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => {
      func.apply(this, args);
    }, wait);
  };
  debounced.cancel = () => {
    if (timeout) clearTimeout(timeout);
  };
  return debounced;
}

export default function SearchBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialQuery = searchParams.get("q") || "";
  const [query, setQuery] = useState(initialQuery);

  const debouncedNavigate = useRef(
    debounce((q: string) => {
      const trimmed = q.trim();
      if (trimmed.length > 0) {
        router.push(`/?q=${encodeURIComponent(trimmed)}`);
      } else {
        router.push(`/`);
      }
    }, 400)
  ).current;

  // Sync state if URL search params change
  useEffect(() => {
    setQuery(searchParams.get("q") || "");
  }, [searchParams]);

  // Cleanup debounce on unmount
  useEffect(() => {
    return () => {
      debouncedNavigate.cancel();
    };
  }, [debouncedNavigate]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setQuery(val);
    debouncedNavigate(val);
  };

  return (
    <div className="relative w-full max-w-sm sm:max-w-md mx-auto group">
      <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-zinc-400 group-focus-within:text-indigo-400 transition-colors">
        <Search className="h-4 w-4" />
      </div>
      <input
        type="text"
        placeholder="Search products..."
        value={query}
        onChange={handleChange}
        className="w-full bg-white/[0.03] border border-white/[0.08] rounded-xl pl-9 pr-4 py-2 text-xs text-zinc-200 placeholder-zinc-500 focus:border-indigo-500/50 focus:bg-white/[0.05] focus:outline-none focus:ring-1 focus:ring-indigo-500/30 transition-all duration-200"
      />
    </div>
  );
}


