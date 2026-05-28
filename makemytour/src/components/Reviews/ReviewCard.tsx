import { useState } from "react";
import { ThumbsUp, MessageCircle, Flag, Send } from "lucide-react";
import { toast } from "sonner";
import { StarRating } from "./StarRating";
import { PhotoGallery } from "./PhotoGallery";
import { voteHelpful, flagReview, addReviewReply } from "@/api";
import { useSelector } from "react-redux";

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

interface ReviewCardProps {
  review: Review;
  currentUserId?: string;
  onReviewUpdate: () => void;
}

const AVATAR_COLORS = [
  "bg-blue-500",
  "bg-emerald-500",
  "bg-purple-500",
  "bg-rose-500",
  "bg-amber-500",
  "bg-cyan-500",
  "bg-indigo-500",
  "bg-pink-500",
];

const FLAG_REASONS = ["Spam", "Inappropriate", "Misleading", "Other"];

const getAvatarColor = (name: string): string => {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
};

const getRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);
  const diffWeeks = Math.floor(diffDays / 7);
  const diffMonths = Math.floor(diffDays / 30);
  const diffYears = Math.floor(diffDays / 365);

  if (diffYears > 0) return `${diffYears} year${diffYears > 1 ? "s" : ""} ago`;
  if (diffMonths > 0) return `${diffMonths} month${diffMonths > 1 ? "s" : ""} ago`;
  if (diffWeeks > 0) return `${diffWeeks} week${diffWeeks > 1 ? "s" : ""} ago`;
  if (diffDays > 0) return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;
  if (diffHours > 0) return `${diffHours} hour${diffHours > 1 ? "s" : ""} ago`;
  if (diffMinutes > 0) return `${diffMinutes} minute${diffMinutes > 1 ? "s" : ""} ago`;
  return "Just now";
};

export const ReviewCard = ({
  review,
  currentUserId,
  onReviewUpdate,
}: ReviewCardProps) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState("");
  const [showFlagMenu, setShowFlagMenu] = useState(false);
  const [isSubmittingReply, setIsSubmittingReply] = useState(false);
  const [isVoting, setIsVoting] = useState(false);
  const user = useSelector((state: any) => state.user.user);

  const hasVoted = currentUserId
    ? review.helpfulVoters?.includes(currentUserId)
    : false;

  const isOwnReview = currentUserId === review.userId;

  const handleVoteHelpful = async () => {
    if (!currentUserId) {
      toast.error("Please log in to vote");
      return;
    }
    if (isVoting) return;
    setIsVoting(true);
    try {
      await voteHelpful(review.id, currentUserId);
      toast.success(hasVoted ? "Vote removed" : "Marked as helpful");
      onReviewUpdate();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Failed to vote");
    } finally {
      setIsVoting(false);
    }
  };

  const handleSubmitReply = async () => {
    if (!replyContent.trim()) return;
    if (!user) {
      toast.error("Please log in to reply");
      return;
    }
    setIsSubmittingReply(true);
    try {
      await addReviewReply(review.id, {
        userId: user.id,
        userName: `${user.firstName} ${user.lastName}`,
        content: replyContent.trim(),
      });
      toast.success("Reply posted successfully");
      setReplyContent("");
      setShowReplyForm(false);
      onReviewUpdate();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Failed to post reply");
    } finally {
      setIsSubmittingReply(false);
    }
  };

  const handleFlag = async (reason: string) => {
    if (!currentUserId) {
      toast.error("Please log in to report");
      return;
    }
    try {
      await flagReview(review.id, {
        userId: currentUserId,
        reason,
      });
      toast.success("Review reported successfully");
      setShowFlagMenu(false);
      onReviewUpdate();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Failed to report review");
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-all duration-300">
      {/* Header: Avatar, Name, Date */}
      <div className="flex items-center gap-3 mb-3">
        <div
          className={`w-10 h-10 rounded-full ${getAvatarColor(
            review.userName
          )} flex items-center justify-center text-white font-semibold text-sm`}
        >
          {review.userName?.charAt(0)?.toUpperCase() || "U"}
        </div>
        <div className="flex-1">
          <div className="font-bold text-gray-900">{review.userName}</div>
          <div className="text-xs text-gray-500">
            {getRelativeTime(review.createdAt)}
          </div>
        </div>
      </div>

      {/* Star Rating */}
      <div className="mb-2">
        <StarRating rating={review.rating} size="sm" readOnly />
      </div>

      {/* Title */}
      {review.title && (
        <h4 className="font-semibold text-lg text-gray-900 mb-1">
          {review.title}
        </h4>
      )}

      {/* Content */}
      <p className="text-gray-700 mb-3 leading-relaxed">{review.content}</p>

      {/* Photo Gallery */}
      {review.photos && review.photos.length > 0 && (
        <PhotoGallery photos={review.photos} />
      )}

      {/* Action Bar */}
      <div className="flex items-center gap-4 mt-4 pt-3 border-t border-gray-100">
        {/* Helpful Button */}
        <button
          onClick={handleVoteHelpful}
          disabled={isVoting}
          className={`flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-lg transition-all duration-200 ${
            hasVoted
              ? "text-blue-600 bg-blue-50 hover:bg-blue-100"
              : "text-gray-600 hover:text-blue-600 hover:bg-gray-50"
          }`}
        >
          <ThumbsUp className={`w-4 h-4 ${hasVoted ? "fill-current" : ""}`} />
          <span>Helpful</span>
          {review.helpfulCount > 0 && (
            <span className="text-xs bg-gray-100 px-1.5 py-0.5 rounded-full">
              {review.helpfulCount}
            </span>
          )}
        </button>

        {/* Reply Button */}
        <button
          onClick={() => setShowReplyForm(!showReplyForm)}
          className="flex items-center gap-1.5 text-sm font-medium text-gray-600 hover:text-blue-600 px-3 py-1.5 rounded-lg hover:bg-gray-50 transition-all duration-200"
        >
          <MessageCircle className="w-4 h-4" />
          <span>Reply</span>
        </button>

        {/* Flag Button */}
        {currentUserId && !isOwnReview && (
          <div className="relative ml-auto">
            <button
              onClick={() => setShowFlagMenu(!showFlagMenu)}
              className="flex items-center gap-1.5 text-sm font-medium text-gray-400 hover:text-red-500 px-3 py-1.5 rounded-lg hover:bg-red-50 transition-all duration-200"
            >
              <Flag className="w-4 h-4" />
              <span>Flag</span>
            </button>

            {/* Flag Dropdown */}
            {showFlagMenu && (
              <div className="absolute right-0 top-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-20 py-1 min-w-[160px]">
                <div className="px-3 py-1.5 text-xs font-semibold text-gray-500 uppercase">
                  Report reason
                </div>
                {FLAG_REASONS.map((reason) => (
                  <button
                    key={reason}
                    onClick={() => handleFlag(reason)}
                    className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-red-50 hover:text-red-600 transition-colors"
                  >
                    {reason}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Reply Form */}
      {showReplyForm && (
        <div className="mt-4 pt-3 border-t border-gray-100">
          <div className="flex gap-2">
            <textarea
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="Write a reply..."
              rows={2}
              className="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none transition-all"
            />
            <button
              onClick={handleSubmitReply}
              disabled={!replyContent.trim() || isSubmittingReply}
              className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed self-end"
            >
              {isSubmittingReply ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <Send className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>
      )}

      {/* Reply Thread */}
      {review.replies && review.replies.length > 0 && (
        <div className="mt-4 space-y-3">
          {review.replies.map((reply) => (
            <div
              key={reply.id}
              className="border-l-2 border-blue-200 ml-8 pl-4 py-2"
            >
              <div className="flex items-center gap-2 mb-1">
                <div
                  className={`w-7 h-7 rounded-full ${getAvatarColor(
                    reply.userName
                  )} flex items-center justify-center text-white text-xs font-semibold`}
                >
                  {reply.userName?.charAt(0)?.toUpperCase() || "U"}
                </div>
                <span className="font-semibold text-sm text-gray-900">
                  {reply.userName}
                </span>
                <span className="text-xs text-gray-500">
                  {getRelativeTime(reply.createdAt)}
                </span>
              </div>
              <p className="text-sm text-gray-700 ml-9">{reply.content}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ReviewCard;
