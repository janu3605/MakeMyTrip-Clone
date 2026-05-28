import { useState, useRef, useEffect } from "react";
import { Info, X } from "lucide-react";

interface RecommendationTooltipProps {
  reason: string;
  reasonCode: string;
}

const REASON_ICONS: Record<string, string> = {
  REPEAT_DESTINATION: "🔁",
  SIMILAR_CATEGORY: "🏷️",
  PRICE_RANGE: "💰",
  COLLABORATIVE: "👥",
  TRENDING: "🔥",
  REVIEW_BASED: "⭐",
};

const REASON_LABELS: Record<string, string> = {
  REPEAT_DESTINATION: "Based on your travel history",
  SIMILAR_CATEGORY: "Matches your preferences",
  PRICE_RANGE: "Within your budget",
  COLLABORATIVE: "Collaborative insight",
  TRENDING: "Popular choice",
  REVIEW_BASED: "Based on your reviews",
};

export default function RecommendationTooltip({
  reason,
  reasonCode,
}: RecommendationTooltipProps) {
  const [isVisible, setIsVisible] = useState(false);
  const tooltipRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        tooltipRef.current &&
        !tooltipRef.current.contains(e.target as Node)
      ) {
        setIsVisible(false);
      }
    };
    if (isVisible) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isVisible]);

  const icon = REASON_ICONS[reasonCode] || "💡";
  const label = REASON_LABELS[reasonCode] || "Personalized for you";

  return (
    <div className="rec-tooltip-wrapper" ref={tooltipRef}>
      <button
        className="rec-tooltip-trigger"
        onClick={() => setIsVisible(!isVisible)}
        aria-label="Why this recommendation?"
      >
        <Info className="rec-tooltip-icon" size={14} />
        <span>Why this?</span>
      </button>

      {isVisible && (
        <div className="rec-tooltip-popover">
          <div className="rec-tooltip-header">
            <span className="rec-tooltip-emoji">{icon}</span>
            <span className="rec-tooltip-label">{label}</span>
            <button
              className="rec-tooltip-close"
              onClick={() => setIsVisible(false)}
              aria-label="Close tooltip"
            >
              <X size={12} />
            </button>
          </div>
          <p className="rec-tooltip-reason">{reason}</p>
        </div>
      )}
    </div>
  );
}
