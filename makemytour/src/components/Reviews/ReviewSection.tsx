import { useState, useEffect } from "react";
import { Star, PenLine, X } from "lucide-react";
import { toast } from "sonner";
import { useSelector } from "react-redux";
import { getReviews, getRatingSummary } from "@/api";
import { ReviewForm } from "./ReviewForm";
import { ReviewList } from "./ReviewList";

interface ReviewSectionProps {
  entityId: string;
  entityType: string;
}

export const ReviewSection = ({ entityId, entityType }: ReviewSectionProps) => {
  const [reviews, setReviews] = useState<any[]>([]);
  const [ratingSummary, setRatingSummary] = useState({
    averageRating: 0,
    totalReviews: 0,
    ratingDistribution: {} as Record<number, number>,
  });
  const [sortBy, setSortBy] = useState("newest");
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const user = useSelector((state: any) => state.user.user);
  const currentUserId = user?.id;

  const fetchData = async () => {
    setLoading(true);
    try {
      const [reviewsData, summaryData] = await Promise.all([
        getReviews(entityId, entityType, sortBy),
        getRatingSummary(entityId, entityType),
      ]);
      setReviews(Array.isArray(reviewsData) ? reviewsData : []);
      setRatingSummary(
        summaryData || {
          averageRating: 0,
          totalReviews: 0,
          ratingDistribution: {},
        }
      );
    } catch (error) {
      console.error("Error fetching review data:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (entityId) {
      fetchData();
    }
  }, [entityId, entityType, sortBy]);

  const handleSubmitSuccess = () => {
    toast.success("Your review has been published!");
    setShowForm(false);
    fetchData();
  };

  const handleReviewUpdate = () => {
    fetchData();
  };

  const handleSortChange = (newSort: string) => {
    setSortBy(newSort);
  };

  return (
    <div id="reviews" className="py-8">
      {/* Section Heading */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Star className="w-6 h-6 text-amber-400 fill-current" />
          Reviews & Ratings
        </h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-lg font-medium text-sm transition-all duration-200 ${
            showForm
              ? "bg-gray-100 text-gray-700 hover:bg-gray-200"
              : "bg-gradient-to-r from-blue-500 to-blue-600 text-white hover:from-blue-600 hover:to-blue-700 shadow-md hover:shadow-lg"
          }`}
        >
          {showForm ? (
            <>
              <X className="w-4 h-4" />
              Cancel
            </>
          ) : (
            <>
              <PenLine className="w-4 h-4" />
              Write a Review
            </>
          )}
        </button>
      </div>

      {/* Review Form (Conditionally Rendered) */}
      <div
        className={`overflow-hidden transition-all duration-500 ease-in-out ${
          showForm ? "max-h-[800px] opacity-100 mb-8" : "max-h-0 opacity-0"
        }`}
      >
        <ReviewForm
          entityId={entityId}
          entityType={entityType}
          onSubmitSuccess={handleSubmitSuccess}
        />
      </div>

      {/* Loading State */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        /* Review List */
        <ReviewList
          reviews={reviews}
          ratingSummary={ratingSummary}
          sortBy={sortBy}
          onSortChange={handleSortChange}
          currentUserId={currentUserId}
          onReviewUpdate={handleReviewUpdate}
        />
      )}
    </div>
  );
};

export default ReviewSection;
