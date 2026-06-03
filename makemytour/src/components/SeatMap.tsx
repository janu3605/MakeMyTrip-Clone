import React, { useMemo, useState } from "react";

interface SeatSelection {
  seatId: string;
  row: number;
  col: string;
  tier: string;
  multiplier: number;
  price: number;
}

interface SeatMapProps {
  basePrice: number;
  availableSeats: number;
  quantity: number;
  onSeatSelect: (seats: SeatSelection[]) => void;
}

const COLS = ["A", "D", "F"];
const TOTAL_ROWS = 12;

const getTier = (row: number) => {
  if (row <= 3) return { name: "Business", multiplier: 2.0, color: "bg-yellow-500" };
  if (row <= 6) return { name: "Premium Economy", multiplier: 1.5, color: "bg-purple-500" };
  return { name: "Economy", multiplier: 1.0, color: "bg-blue-500" };
};

export const SeatMap: React.FC<SeatMapProps> = ({ basePrice, availableSeats, quantity, onSeatSelect }) => {

  const occupancyMap = useMemo(() => {
    const map: Record<string, boolean> = {};
    const totalSeats = TOTAL_ROWS * COLS.length;
    const bookedCount = Math.min(Math.floor(totalSeats * 0.3), totalSeats - availableSeats);
    const allSeats: string[] = [];
    for (let r = 1; r <= TOTAL_ROWS; r++) {
      for (const c of COLS) {
        allSeats.push(`${r}${c}`);
      }
    }
    for (let i = 0; i < allSeats.length; i++) {
      const hash = (i * 7 + 13) % 100;
      map[allSeats[i]] = hash < (bookedCount / totalSeats) * 100;
    }
    return map;
  }, [availableSeats]);

  const [selectedSeats, setSelectedSeats] = useState<SeatSelection[]>([]);

  const handleSeatClick = (seatId: string, row: number, col: string) => {
    if (occupancyMap[seatId]) return;

    const isSelected = selectedSeats.some((s) => s.seatId === seatId);

    let updated: SeatSelection[];
    if (isSelected) {
      updated = selectedSeats.filter((s) => s.seatId !== seatId);
    } else {
      const tier = getTier(row);
      const newSeat: SeatSelection = {
        seatId,
        row,
        col,
        tier: tier.name,
        multiplier: tier.multiplier,
        price: basePrice * tier.multiplier,
      };
      if (selectedSeats.length >= quantity) {
  
        updated = [...selectedSeats.slice(1), newSeat];
      } else {
        updated = [...selectedSeats, newSeat];
      }
    }

    setSelectedSeats(updated);
    onSeatSelect(updated);
  };

  const getSeatColor = (seatId: string, row: number) => {
    if (selectedSeats.some((s) => s.seatId === seatId)) return "bg-green-500 text-white shadow-lg scale-110";
    if (occupancyMap[seatId]) return "bg-orange-400 text-white cursor-not-allowed";
    const tier = getTier(row);
    return `${tier.color} text-white hover:brightness-110 cursor-pointer`;
  };

  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-lg font-bold mb-4">Select Your Seat</h2>

      {/* Legend */}
      <div className="flex flex-wrap gap-4 mb-6 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-blue-500"></div>
          <span>Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-orange-400"></div>
          <span>Booked</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-green-500"></div>
          <span>Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-yellow-500"></div>
          <span>Business (2×)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-purple-500"></div>
          <span>Premium (1.5×)</span>
        </div>
      </div>

      {/* Airplane Body */}
      <div className="flex justify-center">
        <div className="relative w-64">
          {/* Nose */}
          <div className="mx-auto w-40 h-16 bg-gray-100 rounded-t-[80px] border-x border-t border-gray-300 flex items-end justify-center pb-1">
            <span className="text-[10px] text-gray-400 font-medium">COCKPIT</span>
          </div>

          {/* Fuselage */}
          <div className="bg-gray-50 border-x border-gray-300 px-4 py-3">
            {/* Column headers */}
            <div className="grid grid-cols-5 gap-1 mb-2">
              <div className="text-center text-xs font-bold text-gray-500">A</div>
              <div></div>
              <div className="text-center text-xs font-bold text-gray-500">D</div>
              <div></div>
              <div className="text-center text-xs font-bold text-gray-500">F</div>
            </div>

            {/* Seat Rows */}
            {Array.from({ length: TOTAL_ROWS }, (_, i) => i + 1).map((row) => {
              const tier = getTier(row);
              return (
                <div key={row} className="grid grid-cols-5 gap-1 mb-1.5">
                  {COLS.map((col, colIdx) => {
                    const seatId = `${row}${col}`;
                    return (
                      <React.Fragment key={col}>
                        {/* Aisle gap after seat A */}
                        {colIdx === 1 && (
                          <div className="flex items-center justify-center text-[10px] text-gray-400 font-mono">
                            {row}
                          </div>
                        )}
                        <button
                          onClick={() => handleSeatClick(seatId, row, col)}
                          disabled={occupancyMap[seatId]}
                          className={`w-full aspect-square rounded-md text-[11px] font-bold flex items-center justify-center transition-all ${getSeatColor(seatId, row)}`}
                          title={`Seat ${seatId} — ${tier.name} — ₹${(basePrice * tier.multiplier).toLocaleString("en-IN")}`}
                        >
                          {seatId}
                        </button>
                        {/* Aisle gap after seat D */}
                        {colIdx === 1 && <></>}
                      </React.Fragment>
                    );
                  })}
                </div>
              );
            })}
          </div>

          {/* Tail */}
          <div className="mx-auto w-40 h-10 bg-gray-100 rounded-b-[40px] border-x border-b border-gray-300 flex items-center justify-center">
            <span className="text-[10px] text-gray-400">🚻</span>
          </div>
        </div>
      </div>

      {/* Pricing Tiers */}
      <div className="mt-6 grid grid-cols-3 gap-3 text-center text-sm">
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
          <p className="font-bold text-yellow-700">Business</p>
          <p className="text-lg font-bold">₹{(basePrice * 2).toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">Rows 1-3</p>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-3">
          <p className="font-bold text-purple-700">Premium</p>
          <p className="text-lg font-bold">₹{(basePrice * 1.5).toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">Rows 4-6</p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <p className="font-bold text-blue-700">Economy</p>
          <p className="text-lg font-bold">₹{basePrice.toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">Rows 7-12</p>
        </div>
      </div>

      {/* Selection Summary */}
      {selectedSeats.length > 0 && (
        <div className="mt-4 bg-green-50 border border-green-200 rounded-lg p-3">
          <p className="text-sm font-semibold text-green-800 mb-1">Selected Seats:</p>
          <div className="flex flex-wrap gap-2">
            {selectedSeats.map((s) => (
              <span key={s.seatId} className="inline-flex items-center bg-green-600 text-white text-xs px-2.5 py-1 rounded-full font-medium">
                {s.seatId} — {s.tier} — ₹{s.price.toLocaleString("en-IN")}
              </span>
            ))}
          </div>
          <p className="text-sm font-bold mt-2 text-green-900">
            Total Seat Cost: ₹{selectedSeats.reduce((sum, s) => sum + s.price, 0).toLocaleString("en-IN")}
          </p>
        </div>
      )}
    </div>
  );
};

export default SeatMap;
