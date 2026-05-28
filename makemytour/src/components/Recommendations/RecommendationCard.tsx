import { useRouter } from "next/router";
import { Plane, Hotel, MapPin, TrendingUp } from "lucide-react";
import RecommendationTooltip from "./RecommendationTooltip";
import FeedbackWidget from "./FeedbackWidget";

interface RecommendationCardProps {
  id: string;
  entityId: string;
  entityType: string;
  entityName: string;
  entityLocation: string;
  entityPrice: number;
  entityImageUrl: string;
  entityCategory: string | null;
  score: number;
  reason: string;
  reasonCode: string;
  feedback: string | null;
  userId: string;
}

const CATEGORY_GRADIENTS: Record<string, string> = {
  beach: "linear-gradient(135deg, #0ea5e9 0%, #06b6d4 50%, #14b8a6 100%)",
  mountain: "linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%)",
  city: "linear-gradient(135deg, #8b5cf6 0%, #7c3aed 50%, #6d28d9 100%)",
  heritage: "linear-gradient(135deg, #f59e0b 0%, #d97706 50%, #b45309 100%)",
};

const CATEGORY_LABELS: Record<string, string> = {
  beach: "🏖️ Beach",
  mountain: "🏔️ Mountain",
  city: "🌆 City",
  heritage: "🏛️ Heritage",
};

export default function RecommendationCard({
  id,
  entityId,
  entityType,
  entityName,
  entityLocation,
  entityPrice,
  entityImageUrl,
  entityCategory,
  score,
  reason,
  reasonCode,
  feedback,
  userId,
}: RecommendationCardProps) {
  const router = useRouter();

  const handleBookNow = () => {
    if (entityType === "FLIGHT") {
      router.push(`/book-flight/${entityId}`);
    } else {
      router.push(`/book-hotel/${entityId}`);
    }
  };

  const gradient =
    CATEGORY_GRADIENTS[entityCategory || "city"] || CATEGORY_GRADIENTS.city;
  const categoryLabel =
    CATEGORY_LABELS[entityCategory || "city"] || "🌍 Destination";

  const scoreLabel =
    score >= 80
      ? "Top Match"
      : score >= 60
      ? "Great Pick"
      : score >= 40
      ? "Popular"
      : "Discover";

  return (
    <div className="rec-card">
      {/* Image Section */}
      <div className="rec-card-image-wrapper">
        <img
          src={entityImageUrl}
          alt={entityName}
          className="rec-card-image"
          loading="lazy"
        />
        <div className="rec-card-image-overlay" style={{ background: gradient }} />

        {/* Score Badge */}
        <div className="rec-card-score-badge">
          <TrendingUp size={12} />
          <span>{scoreLabel}</span>
        </div>

        {/* Category Tag */}
        <div className="rec-card-category-tag">{categoryLabel}</div>

        {/* Type Icon */}
        <div className="rec-card-type-icon">
          {entityType === "FLIGHT" ? (
            <Plane size={16} />
          ) : (
            <Hotel size={16} />
          )}
        </div>
      </div>

      {/* Content Section */}
      <div className="rec-card-content">
        <h3 className="rec-card-title">{entityName}</h3>

        <div className="rec-card-location">
          <MapPin size={13} />
          <span>{entityLocation}</span>
        </div>

        <div className="rec-card-price-row">
          <div className="rec-card-price">
            <span className="rec-card-currency">₹</span>
            <span className="rec-card-price-value">
              {entityPrice.toLocaleString("en-IN")}
            </span>
            <span className="rec-card-price-unit">
              {entityType === "HOTEL" ? "/night" : ""}
            </span>
          </div>
        </div>

        {/* Why This Tooltip */}
        <RecommendationTooltip reason={reason} reasonCode={reasonCode} />

        {/* Feedback Widget */}
        <FeedbackWidget
          recommendationId={id}
          userId={userId}
          currentFeedback={feedback}
        />

        {/* Book Now Button */}
        <button className="rec-card-book-btn" onClick={handleBookNow}>
          {entityType === "FLIGHT" ? "Book Flight" : "Book Hotel"}
        </button>
      </div>
    </div>
  );
}
