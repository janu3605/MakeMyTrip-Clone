import { useState } from "react";
import { ThumbsUp, ThumbsDown, Check } from "lucide-react";
import { submitRecommendationFeedback } from "@/api";

interface FeedbackWidgetProps {
  recommendationId: string;
  userId: string;
  currentFeedback: string | null;
}

export default function FeedbackWidget({
  recommendationId,
  userId,
  currentFeedback,
}: FeedbackWidgetProps) {
  const [feedback, setFeedback] = useState<string | null>(currentFeedback);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showToast, setShowToast] = useState(false);

  const handleFeedback = async (type: "HELPFUL" | "IRRELEVANT") => {
    if (feedback === type || isSubmitting) return;
    setIsSubmitting(true);
    try {
      await submitRecommendationFeedback(recommendationId, userId, type);
      setFeedback(type);
      setShowToast(true);
      setTimeout(() => setShowToast(false), 2500);
    } catch (err) {
      console.error("Feedback error:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (feedback) {
    return (
      <div className="rec-feedback-submitted">
        <Check size={14} className="rec-feedback-check" />
        <span>
          {feedback === "HELPFUL" ? "Glad you liked it!" : "Noted, we'll improve!"}
        </span>

        {showToast && (
          <div className="rec-feedback-toast">
            Thanks! We'll refine your suggestions.
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="rec-feedback-widget">
      <span className="rec-feedback-label">Was this helpful?</span>
      <div className="rec-feedback-buttons">
        <button
          className="rec-feedback-btn rec-feedback-btn-up"
          onClick={() => handleFeedback("HELPFUL")}
          disabled={isSubmitting}
          aria-label="Mark as helpful"
        >
          <ThumbsUp size={14} />
        </button>
        <button
          className="rec-feedback-btn rec-feedback-btn-down"
          onClick={() => handleFeedback("IRRELEVANT")}
          disabled={isSubmitting}
          aria-label="Mark as irrelevant"
        >
          <ThumbsDown size={14} />
        </button>
      </div>
    </div>
  );
}
