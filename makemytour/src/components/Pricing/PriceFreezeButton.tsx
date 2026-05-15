import React, { useEffect, useState, useCallback } from "react";
import { useSelector } from "react-redux";
import { createPriceFreeze, checkPriceFreeze, getFreezeFee } from "@/api";
import { toast } from "sonner";
import { Lock, Snowflake, Clock, Loader2 } from "lucide-react";

interface Props {
  entityId: string;
  entityType: "FLIGHT" | "HOTEL";
  currentPrice: number;
  quantity?: number;
}

export const PriceFreezeButton = ({ entityId, entityType, currentPrice, quantity = 1 }: Props) => {
  const user = useSelector((state: any) => state.user.user);
  const [freeze, setFreeze] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(true);
  const [fee, setFee] = useState(99);
  const [countdown, setCountdown] = useState("");
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  // Check if user already has a freeze
  useEffect(() => {
    const check = async () => {
      if (!user?.id || !entityId) {
        setChecking(false);
        return;
      }
      try {
        const existing = await checkPriceFreeze(user.id, entityId);
        if (existing) setFreeze(existing);
      } catch (e) {
        // No freeze exists
      } finally {
        setChecking(false);
      }
    };
    check();
  }, [user, entityId]);

  // Fetch fee based on quantity
  useEffect(() => {
    const fetchFee = async () => {
      const f = await getFreezeFee(quantity);
      setFee(f);
    };
    fetchFee();
  }, [quantity]);

  // Countdown timer
  useEffect(() => {
    if (!freeze?.expiresAt) return;

    const update = () => {
      const now = new Date();
      const expires = new Date(freeze.expiresAt);
      const diff = expires.getTime() - now.getTime();

      if (diff <= 0) {
        setCountdown("Expired");
        setFreeze(null);
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      setCountdown(`${hours}h ${minutes}m`);
    };

    update();
    const interval = setInterval(update, 60000);
    return () => clearInterval(interval);
  }, [freeze]);

  const handleFreeze = useCallback(async () => {
    if (!user?.id) {
      toast.error("Please log in to freeze this price");
      return;
    }

    setLoading(true);
    try {
      const result = await createPriceFreeze(user.id, entityId, entityType, quantity);
      setFreeze(result);
      toast.success("Price Frozen! 🔒", {
        description: `₹${currentPrice.toLocaleString("en-IN")} locked for 24 hours. Fee: ₹${fee}`,
      });
    } catch (error: any) {
      const msg = error?.response?.data || "Failed to freeze price";
      toast.error("Freeze Failed", { description: String(msg) });
    } finally {
      setLoading(false);
    }
  }, [user, entityId, entityType, quantity, currentPrice, fee]);

  if (!isMounted || !user) return null;
  if (checking) {
    return (
      <div className="flex items-center gap-2 text-gray-400 text-sm py-2">
        <Loader2 className="w-4 h-4 animate-spin" />
        Checking freeze status...
      </div>
    );
  }

  // Active freeze state
  if (freeze) {
    return (
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-4 mt-4">
        <div className="flex items-center gap-2 mb-2">
          <div className="bg-blue-100 p-1.5 rounded-full">
            <Snowflake className="w-4 h-4 text-blue-600" />
          </div>
          <span className="font-semibold text-blue-900 text-sm">Price Frozen</span>
        </div>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xl font-bold text-blue-900">
              ₹{freeze.frozenPrice?.toLocaleString("en-IN")}
            </p>
            <p className="text-xs text-blue-600 mt-0.5">
              Locked price • Fee paid: ₹{freeze.freezeFee}
            </p>
          </div>
          <div className="text-right">
            <div className="flex items-center gap-1 text-blue-700 text-sm font-medium">
              <Clock className="w-3.5 h-3.5" />
              {countdown}
            </div>
            <p className="text-[10px] text-blue-500 uppercase tracking-wider mt-0.5">
              Remaining
            </p>
          </div>
        </div>
        {currentPrice > freeze.frozenPrice && (
          <div className="mt-2 bg-green-50 text-green-700 text-xs p-2 rounded font-medium">
            You're saving ₹{(currentPrice - freeze.frozenPrice).toLocaleString("en-IN")} with this freeze!
          </div>
        )}
      </div>
    );
  }

  // Freeze button state
  return (
    <div className="mt-4">
      <button
        onClick={handleFreeze}
        disabled={loading}
        className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 
                   hover:from-blue-700 hover:to-indigo-700 text-white py-3 px-4 rounded-lg 
                   font-medium text-sm transition-all duration-200 
                   disabled:opacity-50 disabled:cursor-not-allowed
                   shadow-md hover:shadow-lg active:scale-[0.98]"
      >
        {loading ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          <Lock className="w-4 h-4" />
        )}
        {loading ? "Freezing..." : `Freeze This Price for ₹${fee}`}
      </button>
      <p className="text-[11px] text-gray-500 text-center mt-2">
        Lock ₹{currentPrice.toLocaleString("en-IN")} for 24 hours • Non-refundable fee
      </p>
    </div>
  );
};
