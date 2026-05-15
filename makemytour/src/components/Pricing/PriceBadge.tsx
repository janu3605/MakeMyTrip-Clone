import React from "react";
import { TrendingUp } from "lucide-react";

interface Props {
  currentPrice: number;
  basePrice: number;
}

export const PriceBadge = ({ currentPrice, basePrice }: Props) => {
  if (!basePrice || basePrice <= 0) return null;

  const diff = currentPrice - basePrice;
  const percent = Math.round((diff / basePrice) * 100);

  if (percent === 0) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-green-50 text-green-700 border border-green-200">
        Base price
      </span>
    );
  }

  if (percent > 0) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-red-50 text-red-600 border border-red-200 animate-pulse">
        <TrendingUp className="w-3 h-3" />
        ↑ {percent}% surge
      </span>
    );
  }

  // Price went down (rare but possible)
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-green-50 text-green-700 border border-green-200">
      ↓ {Math.abs(percent)}% off
    </span>
  );
};
