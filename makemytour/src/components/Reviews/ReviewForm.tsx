import { useState, useRef } from "react";
import { Camera, X, Edit3, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { StarRating } from "./StarRating";
import { createReview } from "@/api";
import { useSelector } from "react-redux";
import SignupDialog from "@/components/SignupDialog";

interface ReviewFormProps {
  entityId: string;
  entityType: string;
  onSubmitSuccess: () => void;
}

export const ReviewForm = ({
  entityId,
  entityType,
  onSubmitSuccess,
}: ReviewFormProps) => {
  const user = useSelector((state: any) => state.user.user);
  const [rating, setRating] = useState(0);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [photos, setPhotos] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [ratingError, setRatingError] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!user) {
    return (
      <div className="bg-white rounded-xl shadow-lg p-6 text-center">
        <p className="text-gray-600 mb-4">Please log in to write a review</p>
        <SignupDialog />
      </div>
    );
  }

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const remainingSlots = 5 - photos.length;
    if (remainingSlots <= 0) {
      toast.error("Maximum 5 photos allowed");
      return;
    }

    const filesToProcess = Array.from(files).slice(0, remainingSlots);

    filesToProcess.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotos((prev) => {
          if (prev.length >= 5) return prev;
          return [...prev, reader.result as string];
        });
      };
      reader.readAsDataURL(file);
    });

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const removePhoto = (index: number) => {
    setPhotos((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (rating === 0) {
      setRatingError(true);
      toast.error("Please select a rating");
      return;
    }

    if (content.length < 10) {
      toast.error("Review content must be at least 10 characters");
      return;
    }

    setIsSubmitting(true);
    try {
      await createReview({
        userId: user.id,
        userName: `${user.firstName} ${user.lastName}`,
        entityId,
        entityType,
        rating,
        title: title.trim(),
        content: content.trim(),
        photos,
      });
      toast.success("Review submitted successfully!");
      setRating(0);
      setTitle("");
      setContent("");
      setPhotos([]);
      setRatingError(false);
      onSubmitSuccess();
    } catch (error: any) {
      const message =
        error?.response?.data?.message ||
        error?.response?.data ||
        "Failed to submit review";
      toast.error(
        typeof message === "string" ? message : "You have already reviewed this"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const isValid = rating > 0 && content.length >= 10;

  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2 mb-6">
        <Edit3 className="w-5 h-5 text-blue-500" />
        Write a Review
      </h3>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Star Rating */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Your Rating <span className="text-red-500">*</span>
          </label>
          <StarRating
            rating={rating}
            size="lg"
            onChange={(val) => {
              setRating(val);
              setRatingError(false);
            }}
          />
          {ratingError && (
            <p className="text-red-500 text-xs mt-1">Please select a rating</p>
          )}
        </div>

        {/* Title */}
        <div>
          <label
            htmlFor="review-title"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Title
          </label>
          <input
            id="review-title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Summarize your experience"
            maxLength={100}
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all text-sm"
          />
          <p className="text-xs text-gray-400 mt-1 text-right">
            {title.length}/100
          </p>
        </div>

        {/* Content */}
        <div>
          <label
            htmlFor="review-content"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Your Review <span className="text-red-500">*</span>
          </label>
          <textarea
            id="review-content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Share details of your experience..."
            rows={4}
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all text-sm resize-none"
          />
          {content.length > 0 && content.length < 10 && (
            <p className="text-red-500 text-xs mt-1">
              Minimum 10 characters required ({10 - content.length} more)
            </p>
          )}
        </div>

        {/* Photo Upload */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Photos
          </label>

          {/* Upload Area */}
          <div
            onClick={() => fileInputRef.current?.click()}
            className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center cursor-pointer hover:border-blue-400 hover:bg-blue-50/30 transition-all duration-200"
          >
            <Camera className="w-8 h-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm text-gray-600">
              Add Photos{" "}
              <span className="text-gray-400">(max 5)</span>
            </p>
            <p className="text-xs text-gray-400 mt-1">
              Click to upload images
            </p>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={handlePhotoUpload}
            className="hidden"
          />

          {/* Photo Thumbnails */}
          {photos.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-3">
              {photos.map((photo, index) => (
                <div key={index} className="relative group">
                  <img
                    src={photo}
                    alt={`Upload ${index + 1}`}
                    className="w-16 h-16 object-cover rounded-lg border border-gray-200"
                  />
                  <button
                    type="button"
                    onClick={() => removePhoto(index)}
                    className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={!isValid || isSubmitting}
          className="w-full py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              Submitting...
            </>
          ) : (
            "Submit Review"
          )}
        </button>
      </form>
    </div>
  );
};

export default ReviewForm;
