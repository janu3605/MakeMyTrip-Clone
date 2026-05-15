import React, { useEffect, useState } from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { getPriceHistory } from "@/api";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";

interface PriceHistoryEntry {
  id: string;
  entityId: string;
  entityType: string;
  oldPrice: number;
  newPrice: number;
  multiplier: number;
  reason: string;
  timestamp: string;
}

interface Props {
  entityId: string;
  entityType: "FLIGHT" | "HOTEL";
  currentPrice?: number;
  basePrice?: number;
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-white/95 backdrop-blur border border-gray-200 rounded-lg shadow-lg p-3 text-sm">
        <p className="font-semibold text-gray-900">₹{data.price?.toLocaleString("en-IN")}</p>
        <p className="text-gray-500 text-xs mt-1">{data.formattedDate}</p>
        {data.reason && data.reason !== "BASE_PRICE" && (
          <p className="text-xs mt-1 text-orange-600 font-medium">
            {data.reason.replace(/_/g, " ")}
          </p>
        )}
      </div>
    );
  }
  return null;
};

export const PriceHistoryChart = ({ entityId, entityType, currentPrice, basePrice }: Props) => {
  const [history, setHistory] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const data: PriceHistoryEntry[] = await getPriceHistory(entityId, entityType);
        const chartData = data.map((entry) => ({
          price: entry.newPrice,
          formattedDate: new Date(entry.timestamp).toLocaleDateString("en-IN", {
            day: "numeric",
            month: "short",
            hour: "2-digit",
            minute: "2-digit",
          }),
          timestamp: new Date(entry.timestamp).getTime(),
          reason: entry.reason,
          multiplier: entry.multiplier,
        }));
        setHistory(chartData);
      } catch (e) {
        console.error("Failed to load price history", e);
      } finally {
        setLoading(false);
      }
    };
    if (entityId) fetchHistory();
  }, [entityId, entityType]);

  // Calculate trend
  const trendPercent =
    basePrice && currentPrice
      ? Math.round(((currentPrice - basePrice) / basePrice) * 100)
      : 0;

  const isUp = trendPercent > 0;
  const isDown = trendPercent < 0;

  if (loading) {
    return (
      <div className="bg-white rounded-xl shadow-sm p-6 animate-pulse">
        <div className="h-4 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="h-48 bg-gray-100 rounded"></div>
      </div>
    );
  }

  if (history.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-sm p-6">
        <h3 className="text-lg font-bold mb-2 flex items-center gap-2">
          <TrendingUp className="w-5 h-5 text-gray-400" />
          Price History
        </h3>
        <p className="text-sm text-gray-500">
          No price changes recorded yet. The pricing engine updates every minute.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold flex items-center gap-2">
          <TrendingUp className="w-5 h-5 text-blue-600" />
          Price History
        </h3>
        {trendPercent !== 0 && (
          <div
            className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold ${
              isUp
                ? "bg-red-50 text-red-600"
                : "bg-green-50 text-green-600"
            }`}
          >
            {isUp ? (
              <TrendingUp className="w-3.5 h-3.5" />
            ) : (
              <TrendingDown className="w-3.5 h-3.5" />
            )}
            {isUp ? "+" : ""}
            {trendPercent}% from base
          </div>
        )}
        {trendPercent === 0 && basePrice && (
          <div className="flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold bg-gray-50 text-gray-600">
            <Minus className="w-3.5 h-3.5" />
            At base price
          </div>
        )}
      </div>

      <ResponsiveContainer width="100%" height={200}>
        <AreaChart data={history} margin={{ top: 5, right: 5, left: 5, bottom: 5 }}>
          <defs>
            <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={isUp ? "#ef4444" : "#22c55e"} stopOpacity={0.3} />
              <stop offset="95%" stopColor={isUp ? "#ef4444" : "#22c55e"} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis
            dataKey="formattedDate"
            tick={{ fontSize: 10, fill: "#9ca3af" }}
            axisLine={false}
            tickLine={false}
          />
          <YAxis
            tick={{ fontSize: 10, fill: "#9ca3af" }}
            axisLine={false}
            tickLine={false}
            tickFormatter={(v) => `₹${v.toLocaleString("en-IN")}`}
            width={70}
          />
          <Tooltip content={<CustomTooltip />} />
          <Area
            type="monotone"
            dataKey="price"
            stroke={isUp ? "#ef4444" : "#22c55e"}
            strokeWidth={2}
            fill="url(#priceGradient)"
            animationDuration={1000}
          />
        </AreaChart>
      </ResponsiveContainer>

      {basePrice && currentPrice && (
        <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100 text-sm">
          <span className="text-gray-500">
            Base: <span className="font-medium text-gray-700">₹{basePrice.toLocaleString("en-IN")}</span>
          </span>
          <span className="text-gray-500">
            Current: <span className="font-bold text-gray-900">₹{currentPrice.toLocaleString("en-IN")}</span>
          </span>
        </div>
      )}
    </div>
  );
};
