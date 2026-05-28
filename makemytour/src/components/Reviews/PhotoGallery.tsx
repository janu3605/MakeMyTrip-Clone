import { useState, useEffect, useCallback } from "react";
import { ChevronLeft, ChevronRight, X } from "lucide-react";

interface PhotoGalleryProps {
  photos: string[];
}

export const PhotoGallery = ({ photos }: PhotoGalleryProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);

  const openLightbox = (index: number) => {
    setCurrentIndex(index);
    setIsOpen(true);
  };

  const closeLightbox = () => {
    setIsOpen(false);
  };

  const goToPrevious = useCallback(() => {
    setCurrentIndex((prev) => (prev === 0 ? photos.length - 1 : prev - 1));
  }, [photos.length]);

  const goToNext = useCallback(() => {
    setCurrentIndex((prev) => (prev === photos.length - 1 ? 0 : prev + 1));
  }, [photos.length]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!isOpen) return;
      if (e.key === "Escape") closeLightbox();
      if (e.key === "ArrowLeft") goToPrevious();
      if (e.key === "ArrowRight") goToNext();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, goToPrevious, goToNext]);

  if (!photos || photos.length === 0) return null;

  const getImageSrc = (photo: string) => {
    if (photo.startsWith("data:")) return photo;
    return `data:image/jpeg;base64,${photo}`;
  };

  return (
    <>
      {/* Thumbnails */}
      <div className="flex flex-row gap-2 mt-3">
        {photos.map((photo, index) => (
          <button
            key={index}
            type="button"
            onClick={() => openLightbox(index)}
            className="flex-shrink-0 rounded-lg overflow-hidden border border-gray-200 hover:border-blue-400 transition-all duration-200 hover:shadow-md"
          >
            <img
              src={getImageSrc(photo)}
              alt={`Review photo ${index + 1}`}
              className="w-16 h-16 object-cover"
            />
          </button>
        ))}
      </div>

      {/* Lightbox Modal */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/80 z-50 flex items-center justify-center animate-in fade-in duration-300"
          onClick={closeLightbox}
        >
          {/* Close Button */}
          <button
            onClick={closeLightbox}
            className="absolute top-4 right-4 text-white hover:text-gray-300 transition-colors z-10"
            aria-label="Close lightbox"
          >
            <X className="w-8 h-8" />
          </button>

          {/* Previous Arrow */}
          {photos.length > 1 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                goToPrevious();
              }}
              className="absolute left-4 text-white hover:text-gray-300 transition-colors z-10 bg-black/40 rounded-full p-2 hover:bg-black/60"
              aria-label="Previous photo"
            >
              <ChevronLeft className="w-8 h-8" />
            </button>
          )}

          {/* Image */}
          <div
            className="max-w-3xl max-h-[80vh] px-4"
            onClick={(e) => e.stopPropagation()}
          >
            <img
              src={getImageSrc(photos[currentIndex])}
              alt={`Review photo ${currentIndex + 1}`}
              className="max-w-full max-h-[80vh] object-contain rounded-lg"
            />
            <div className="text-center text-white/70 text-sm mt-2">
              {currentIndex + 1} / {photos.length}
            </div>
          </div>

          {/* Next Arrow */}
          {photos.length > 1 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                goToNext();
              }}
              className="absolute right-4 text-white hover:text-gray-300 transition-colors z-10 bg-black/40 rounded-full p-2 hover:bg-black/60"
              aria-label="Next photo"
            >
              <ChevronRight className="w-8 h-8" />
            </button>
          )}
        </div>
      )}
    </>
  );
};

export default PhotoGallery;
