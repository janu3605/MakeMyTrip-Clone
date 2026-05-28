import { useState } from "react";
import { Star } from "lucide-react";

interface StarRatingProps {
  rating: number;
  maxRating?: number;
  size?: "sm" | "md" | "lg";
  readOnly?: boolean;
  onChange?: (rating: number) => void;
}

const sizeMap = {
  sm: "w-4 h-4",
  md: "w-6 h-6",
  lg: "w-8 h-8",
};

export const StarRating = ({
  rating,
  maxRating = 5,
  size = "md",
  readOnly = false,
  onChange,
}: StarRatingProps) => {
  const [hoverRating, setHoverRating] = useState(0);

  const handleClick = (index: number) => {
    if (!readOnly && onChange) {
      onChange(index);
    }
  };

  const handleMouseEnter = (index: number) => {
    if (!readOnly) {
      setHoverRating(index);
    }
  };

  const handleMouseLeave = () => {
    if (!readOnly) {
      setHoverRating(0);
    }
  };

  const getStarClass = (index: number) => {
    const isActive = hoverRating ? index <= hoverRating : index <= rating;
    const isHovered = hoverRating && index <= hoverRating;

    if (isHovered) {
      return `${sizeMap[size]} fill-current text-amber-300 transition-all duration-200`;
    }
    if (isActive) {
      return `${sizeMap[size]} fill-current text-amber-400 transition-all duration-200`;
    }
    return `${sizeMap[size]} text-gray-300 transition-all duration-200`;
  };

  return (
    <div className="flex items-center gap-0.5" onMouseLeave={handleMouseLeave}>
      {[...Array(maxRating)].map((_, i) => {
        const index = i + 1;
        return (
          <button
            key={index}
            type="button"
            className={`${
              readOnly ? "cursor-default" : "cursor-pointer hover:scale-110"
            } transition-all duration-200 focus:outline-none`}
            onClick={() => handleClick(index)}
            onMouseEnter={() => handleMouseEnter(index)}
            disabled={readOnly}
            aria-label={`Rate ${index} out of ${maxRating}`}
          >
            <Star className={getStarClass(index)} />
          </button>
        );
      })}
    </div>
  );
};

export default StarRating;
