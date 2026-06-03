import { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { Sparkles, TrendingUp, ChevronLeft, ChevronRight } from "lucide-react";
import { getRecommendations, getTrendingRecommendations } from "@/api";
import RecommendationCard from "./RecommendationCard";

type FilterType = "ALL" | "HOTEL" | "FLIGHT";

export default function RecommendationCarousel() {
  const user = useSelector((state: any) => state.user.user);
  const [recommendations, setRecommendations] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState<FilterType>("ALL");
  const scrollRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const isPersonalized = !!user;

  useEffect(() => {
    const fetchRecs = async () => {
      setLoading(true);
      try {
        let data: any[];
        if (user && user.id) {
          data = await getRecommendations(user.id, undefined);
        } else {
          data = await getTrendingRecommendations();
        }
        setRecommendations(data || []);
      } catch (err) {
        console.error("Failed to load recommendations:", err);
        setRecommendations([]);
      } finally {
        setLoading(false);
      }
    };
    fetchRecs();
  }, [user]);

  const filteredRecs =
    activeFilter === "ALL"
      ? recommendations
      : recommendations.filter((r) => r.entityType === activeFilter);

  const updateScrollButtons = () => {
    const el = scrollRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 10);
    setCanScrollRight(el.scrollLeft < el.scrollWidth - el.clientWidth - 10);
  };

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    updateScrollButtons();
    el.addEventListener("scroll", updateScrollButtons);
    return () => el.removeEventListener("scroll", updateScrollButtons);
  }, [filteredRecs]);

  const scroll = (direction: "left" | "right") => {
    const el = scrollRef.current;
    if (!el) return;
    const scrollAmount = 340;
    el.scrollBy({
      left: direction === "left" ? -scrollAmount : scrollAmount,
      behavior: "smooth",
    });
  };

  if (loading) {
    return (
      <section className="rec-carousel-section">
        <div className="rec-carousel-header">
          <div className="rec-carousel-title-group">
            <Sparkles className="rec-carousel-title-icon" size={24} />
            <h2 className="rec-carousel-title">Loading Recommendations...</h2>
          </div>
        </div>
        <div className="rec-carousel-skeleton-row">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="rec-card-skeleton">
              <div className="rec-skeleton-image" />
              <div className="rec-skeleton-content">
                <div className="rec-skeleton-line rec-skeleton-line-title" />
                <div className="rec-skeleton-line rec-skeleton-line-sub" />
                <div className="rec-skeleton-line rec-skeleton-line-price" />
              </div>
            </div>
          ))}
        </div>
      </section>
    );
  }

  if (filteredRecs.length === 0 && !loading) {
    return null;
  }

  return (
    <section className="rec-carousel-section">
      {/* Header */}
      <div className="rec-carousel-header">
        <div className="rec-carousel-title-group">
          {isPersonalized ? (
            <Sparkles className="rec-carousel-title-icon rec-icon-sparkle" size={24} />
          ) : (
            <TrendingUp className="rec-carousel-title-icon rec-icon-trending" size={24} />
          )}
          <h2 className="rec-carousel-title">
            {isPersonalized ? "Picked Just for You" : "Trending Now"}
          </h2>
          {isPersonalized && <span className="rec-carousel-sparkle-emoji">✨</span>}
          {!isPersonalized && <span className="rec-carousel-sparkle-emoji">🔥</span>}
        </div>

        {/* Filter Pills */}
        <div className="rec-carousel-filters">
          {(["ALL", "HOTEL", "FLIGHT"] as FilterType[]).map((filter) => (
            <button
              key={filter}
              className={`rec-filter-pill ${
                activeFilter === filter ? "rec-filter-pill-active" : ""
              }`}
              onClick={() => setActiveFilter(filter)}
            >
              {filter === "ALL"
                ? "All"
                : filter === "HOTEL"
                ? "🏨 Hotels"
                : "✈️ Flights"}
            </button>
          ))}
        </div>
      </div>

      {/* Carousel */}
      <div className="rec-carousel-container">
        {/* Left Arrow */}
        {canScrollLeft && (
          <button
            className="rec-carousel-arrow rec-carousel-arrow-left"
            onClick={() => scroll("left")}
            aria-label="Scroll left"
          >
            <ChevronLeft size={20} />
          </button>
        )}

        {/* Scrollable Row */}
        <div className="rec-carousel-scroll" ref={scrollRef}>
          {filteredRecs.map((rec: any) => (
            <div key={rec.id || rec.entityId} className="rec-carousel-item">
              <RecommendationCard
                id={rec.id}
                entityId={rec.entityId}
                entityType={rec.entityType}
                entityName={rec.entityName}
                entityLocation={rec.entityLocation}
                entityPrice={rec.entityPrice}
                entityImageUrl={rec.entityImageUrl}
                entityCategory={rec.entityCategory}
                score={rec.score}
                reason={rec.reason}
                reasonCode={rec.reasonCode}
                feedback={rec.feedback}
                userId={user?.id || ""}
              />
            </div>
          ))}
        </div>

        {/* Right Arrow */}
        {canScrollRight && (
          <button
            className="rec-carousel-arrow rec-carousel-arrow-right"
            onClick={() => scroll("right")}
            aria-label="Scroll right"
          >
            <ChevronRight size={20} />
          </button>
        )}

        {/* Gradient Fades */}
        {canScrollLeft && <div className="rec-carousel-fade rec-carousel-fade-left" />}
        {canScrollRight && <div className="rec-carousel-fade rec-carousel-fade-right" />}
      </div>
    </section>
  );
}
