import { MessageSquare } from "lucide-react";
import { StarRating } from "./StarRating";
import { ReviewCard } from "./ReviewCard";

interface Reply {
  id: string;
  userId: string;
  userName: string;
  content: string;
  createdAt: string;
}

interface Review {
  id: string;
  userId: string;
  userName: string;
  entityId: string;
  entityType: string;
  rating: number;
  title: string;
  content: string;
  photos: string[];
  replies: Reply[];
  helpfulCount: number;
  helpfulVoters: string[];
  flagged: boolean;
  moderationStatus: string;
  createdAt: string;
}

interface ReviewListProps {
  reviews: Review[];
  ratingSummary: {
    averageRating: number;
    totalReviews: number;
    ratingDistribution: Record<number, number>;
  };
  sortBy: string;
  onSortChange: (sort: string) => void;
  currentUserId?: string;
  onReviewUpdate: () => void;
}

const SORT_OPTIONS = [
  { value: "helpful", label: "Most Helpful" },
  { value: "newest", label: "Newest" },
  { value: "highest", label: "Highest Rated" },
  { value: "lowest", label: "Lowest Rated" },
];

export const ReviewList = ({
  reviews,
  ratingSummary,
  sortBy,
  onSortChange,
  currentUserId,
  onReviewUpdate,
}: ReviewListProps) => {
  const { averageRating, totalReviews, ratingDistribution } = ratingSummary;

  // Find the max count for proportional bar widths
  const maxCount = Math.max(
    ...Object.values(ratingDistribution || {}).map(Number),
    1
  );

  return (
    <div className="space-y-6">
      {/* Rating Summary */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div className="flex flex-col md:flex-row gap-8">
          {/* Average Rating */}
          <div className="flex items-center gap-4">
            <div className="text-5xl font-bold text-gray-900">
              {averageRating ? averageRating.toFixed(1) : "0.0"}
            </div>
            <div>
              <StarRating
                rating={Math.round(averageRating || 0)}
                size="md"
                readOnly
              />
              <p className="text-sm text-gray-500 mt-1">
                Based on {totalReviews} review{totalReviews !== 1 ? "s" : ""}
              </p>
            </div>
          </div>

          {/* Rating Distribution Bars */}
          <div className="flex-1 space-y-2">
            {[5, 4, 3, 2, 1].map((star) => {
              const count = Number(ratingDistribution?.[star]) || 0;
              const percentage = maxCount > 0 ? (count / maxCount) * 100 : 0;
              return (
                <div key={star} className="flex items-center gap-3">
                  <span className="text-sm font-medium text-gray-600 w-4">
                    {star}
                  </span>
                  <StarRating rating={1} maxRating={1} size="sm" readOnly />
                  <div className="flex-1 h-2.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-amber-400 rounded-full transition-all duration-500"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                  <span className="text-sm text-gray-500 w-8 text-right">
                    {count}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Sort Controls */}
      <div className="flex flex-wrap gap-2">
        {SORT_OPTIONS.map((option) => (
          <button
            key={option.value}
            onClick={() => onSortChange(option.value)}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
              sortBy === option.value
                ? "bg-blue-500 text-white shadow-md"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>

      {/* Review Cards */}
      {reviews.length > 0 ? (
        <div className="space-y-4">
          {reviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              currentUserId={currentUserId}
              onReviewUpdate={onReviewUpdate}
            />
          ))}
        </div>
      ) : (
        /* Empty State */
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
          <MessageSquare className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-700 mb-2">
            No reviews yet
          </h3>
          <p className="text-gray-500">
            Be the first to share your experience!
          </p>
        </div>
      )}
    </div>
  );
};

export default ReviewList;
