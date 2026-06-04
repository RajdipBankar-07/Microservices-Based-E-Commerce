"use client";

import React, { useEffect, useState } from "react";
import { api } from "@/utils/api";
import { useAuth } from "@/context/AuthContext";
import { Star, MessageSquare, AlertCircle, CheckCircle, Send } from "lucide-react";
import Link from "next/link";


interface Review {
  id: number;
  userId: number;
  userName: string;
  rating: number;
  comment?: string;
  verifiedPurchase: boolean;
  reviewDate: string;
}

interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingBreakdown: Record<number, number>;
  reviews: Review[];
}

interface ReviewPanelProps {
  productId: number;
}

export default function ReviewPanel({ productId }: ReviewPanelProps) {
  const { user } = useAuth();
  const [summary, setSummary] = useState<ReviewSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  
  // Submit Form States
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const [submitMessage, setSubmitMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const fetchReviews = async () => {
    try {
      const res = await api.get<{ message: string; data: any }>(`/reviews/product/${productId}`);
      if (res && res.data) {
        // Map backend properties to summary interface
        setSummary({
          averageRating: res.data.averageRating || 0,
          totalReviews: res.data.totalReviews || 0,
          ratingBreakdown: res.data.ratingBreakdown || {},
          reviews: res.data.reviews || []
        });
      }
    } catch (err) {
      console.error("Failed to fetch product reviews", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [productId]);

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      setSubmitMessage({ text: "Please log in to submit a review.", isError: true });
      return;
    }

    setSubmitting(true);
    setSubmitMessage(null);

    try {
      await api.post("/reviews", {
        productId,
        userId: user.id,
        userName: user.name,
        rating,
        comment: comment.trim() || undefined
      });
      
      setSubmitMessage({ text: "Review submitted successfully!", isError: false });
      setComment("");
      setRating(5);
      await fetchReviews();
    } catch (err: any) {
      setSubmitMessage({ text: err.message || "Failed to submit review. You can only review a product once.", isError: true });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="rounded-3xl border border-white/[0.08] bg-zinc-900/40 backdrop-blur-xl p-6 md:p-8 shadow-2xl space-y-8 relative overflow-hidden mb-12">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
          <MessageSquare className="h-5 w-5" />
        </div>
        <h2 className="text-xl font-bold text-white tracking-tight">Customer Reviews</h2>
      </div>

      {loading ? (
        <div className="space-y-4 animate-pulse">
          <div className="h-10 bg-zinc-800 rounded-lg w-1/4" />
          <div className="h-20 bg-zinc-800 rounded-xl" />
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          
          {/* Review Summary Breakdown */}
          <div className="lg:col-span-4 space-y-6">
            <div className="bg-white/[0.02] border border-white/[0.04] rounded-2xl p-6 text-center space-y-4">
              <p className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">Average Rating</p>
              <div className="space-y-1">
                <h3 className="text-5xl font-black text-white">{(summary?.averageRating || 0).toFixed(1)}</h3>
                <div className="flex justify-center text-amber-400">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      className={`h-5 w-5 ${
                        i < Math.round(summary?.averageRating || 0) ? "fill-amber-400" : "text-zinc-700"
                      }`}
                    />
                  ))}
                </div>
              </div>
              <p className="text-xs text-zinc-500 font-medium">Based on {summary?.totalReviews || 0} customer ratings</p>
            </div>
          </div>

          {/* Reviews List & Submission Form */}
          <div className="lg:col-span-8 space-y-8">
            
            {/* Submit New Review Form */}
            {user ? (
              <form onSubmit={handleSubmitReview} className="bg-white/[0.02] border border-white/[0.04] rounded-2xl p-5 space-y-4">
                <h4 className="text-sm font-bold text-white uppercase tracking-wider">Write a Review</h4>
                
                {/* Star rating selector */}
                <div className="flex items-center gap-2">
                  <span className="text-xs text-zinc-400 font-medium">Your Rating:</span>
                  <div className="flex text-zinc-650">
                    {Array.from({ length: 5 }).map((_, i) => {
                      const starVal = i + 1;
                      return (
                        <button
                          key={i}
                          type="button"
                          onClick={() => setRating(starVal)}
                          onMouseEnter={() => setHoverRating(starVal)}
                          onMouseLeave={() => setHoverRating(null)}
                          className="p-1 hover:scale-110 active:scale-95 transition-transform cursor-pointer"
                        >
                          <Star
                            className={`h-6 w-6 ${
                              starVal <= (hoverRating ?? rating) ? "fill-amber-400 text-amber-400" : "text-zinc-750"
                            }`}
                          />
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Comment area */}
                <div className="relative">
                  <textarea
                    rows={3}
                    placeholder="Tell us what you liked or disliked about this product..."
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    className="w-full py-3 px-4 rounded-xl border border-zinc-700 bg-zinc-900/60 text-white placeholder-zinc-500 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-all resize-none"
                  />
                </div>

                <div className="flex justify-between items-center">
                  <div>
                    {submitMessage && (
                      <p className={`text-[11px] font-semibold flex items-center gap-1.5 ${submitMessage.isError ? "text-rose-400" : "text-emerald-400"}`}>
                        {submitMessage.isError ? <AlertCircle className="h-3.5 w-3.5" /> : <CheckCircle className="h-3.5 w-3.5" />}
                        {submitMessage.text}
                      </p>
                    )}
                  </div>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 px-5 py-2.5 text-xs font-bold text-white shadow-md transition-all cursor-pointer disabled:opacity-40"
                  >
                    {submitting ? "Submitting..." : (
                      <>
                        <span>Submit Review</span>
                        <Send className="h-3.5 w-3.5" />
                      </>
                    )}
                  </button>
                </div>
              </form>
            ) : (
              <div className="rounded-2xl border border-white/[0.04] bg-white/[0.01] p-6 text-center text-sm text-zinc-500">
                Please <Link href="/login" className="text-indigo-400 font-bold hover:underline">sign in</Link> to share your review and rating.
              </div>
            )}

            {/* List of Reviews */}
            <div className="space-y-5">
              <h4 className="text-xs font-bold text-zinc-500 uppercase tracking-wider">Review List ({summary?.reviews?.length || 0})</h4>
              {summary?.reviews && summary.reviews.length > 0 ? (
                <div className="space-y-4">
                  {summary.reviews.map((rev) => (
                    <div key={rev.id} className="border-b border-white/[0.04] pb-4 space-y-2">
                      <div className="flex justify-between items-start gap-4">
                        <div>
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-sm font-bold text-zinc-200">{rev.userName || "Customer"}</span>
                            {rev.verifiedPurchase && (
                              <span className="inline-flex items-center gap-1 rounded bg-emerald-500/10 px-2 py-0.5 text-[9px] font-bold text-emerald-400 border border-emerald-500/20">
                                Verified Purchase
                              </span>
                            )}
                          </div>
                          <span className="text-[10px] text-zinc-500 font-medium">Reviewed on {new Date(rev.reviewDate).toLocaleDateString("en-IN")}</span>
                        </div>
                        
                        {/* Rating Display */}
                        <div className="flex text-amber-400 shrink-0">
                          {Array.from({ length: 5 }).map((_, i) => (
                            <Star
                              key={i}
                              className={`h-3.5 w-3.5 ${
                                i < rev.rating ? "fill-amber-400" : "text-zinc-800"
                              }`}
                            />
                          ))}
                        </div>
                      </div>
                      <p className="text-sm text-zinc-400 leading-relaxed font-medium">{rev.comment || "No written review comment provided."}</p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-zinc-500">Be the first to review this product!</p>
              )}
            </div>

          </div>

        </div>
      )}
    </div>
  );
}
