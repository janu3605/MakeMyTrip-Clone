import React, { useMemo, useState } from "react";
import { Home, Star, Lock } from "lucide-react";

interface RoomSelection {
  roomId: string;
  floor: number;
  roomType: string;
  multiplier: number;
  price: number;
}

interface RoomGridProps {
  basePrice: number;
  availableRooms: number;
  quantity: number;
  onRoomSelect: (rooms: RoomSelection[]) => void;
}

const ROOM_TYPES = [
  { type: "Standard", multiplier: 1.0, color: "bg-blue-500", label: "STD" },
  { type: "Standard", multiplier: 1.0, color: "bg-blue-500", label: "STD" },
  { type: "Deluxe", multiplier: 1.5, color: "bg-purple-500", label: "DLX" },
  { type: "Deluxe", multiplier: 1.5, color: "bg-purple-500", label: "DLX" },
  { type: "Suite", multiplier: 2.5, color: "bg-yellow-500", label: "STE" },
  { type: "Suite", multiplier: 2.5, color: "bg-yellow-500", label: "STE" },
];

export const RoomGrid: React.FC<RoomGridProps> = ({ basePrice, availableRooms, quantity, onRoomSelect }) => {
  const totalFloors = Math.max(2, Math.ceil(availableRooms / 6));

  // Generate occupancy map
  const occupancyMap = useMemo(() => {
    const map: Record<string, boolean> = {};
    for (let floor = 1; floor <= totalFloors; floor++) {
      for (let roomIdx = 0; roomIdx < 6; roomIdx++) {
        const roomId = `F${floor}R${roomIdx + 1}`;
        const hash = (floor * 13 + roomIdx * 7 + 3) % 100;
        map[roomId] = hash < 30; // ~30% occupied
      }
    }
    return map;
  }, [totalFloors]);

  const [selectedRooms, setSelectedRooms] = useState<RoomSelection[]>([]);

  const handleRoomClick = (roomId: string, floor: number, roomType: typeof ROOM_TYPES[0]) => {
    if (occupancyMap[roomId]) return;

    const isSelected = selectedRooms.some((r) => r.roomId === roomId);
    let updated: RoomSelection[];

    if (isSelected) {
      updated = selectedRooms.filter((r) => r.roomId !== roomId);
    } else {
      const newRoom: RoomSelection = {
        roomId,
        floor,
        roomType: roomType.type,
        multiplier: roomType.multiplier,
        price: basePrice * roomType.multiplier,
      };
      if (selectedRooms.length >= quantity) {
        updated = [...selectedRooms.slice(1), newRoom];
      } else {
        updated = [...selectedRooms, newRoom];
      }
    }

    setSelectedRooms(updated);
    onRoomSelect(updated);
  };

  const getRoomColor = (roomId: string, roomType: typeof ROOM_TYPES[0]) => {
    if (selectedRooms.some((r) => r.roomId === roomId)) return "bg-green-500 text-white shadow-lg ring-2 ring-green-300";
    if (occupancyMap[roomId]) return "bg-orange-400 text-white cursor-not-allowed opacity-70";
    return `${roomType.color} text-white hover:brightness-110 cursor-pointer`;
  };

  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-lg font-bold mb-4 flex items-center">
        <Home className="w-5 h-5 mr-2" />
        Select Your Room
      </h2>

      {/* Legend */}
      <div className="flex flex-wrap gap-4 mb-6 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-blue-500"></div>
          <span>Standard (1×)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-purple-500"></div>
          <span>Deluxe (1.5×)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-yellow-500"></div>
          <span>Suite (2.5×)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-orange-400"></div>
          <span>Booked</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded bg-green-500"></div>
          <span>Selected</span>
        </div>
      </div>

      {/* Hotel Building */}
      <div className="border border-gray-200 rounded-xl overflow-hidden">
        {/* Roof */}
        <div className="bg-gray-700 text-white text-center py-2 text-sm font-bold tracking-wider">
          🏨 HOTEL — {totalFloors} Floors
        </div>

        {/* Floors — top floor first */}
        {Array.from({ length: totalFloors }, (_, i) => totalFloors - i).map((floor) => (
          <div key={floor} className={`border-b border-gray-200 ${floor % 2 === 0 ? "bg-gray-50" : "bg-white"}`}>
            <div className="flex items-center px-3 py-3">
              {/* Floor label */}
              <div className="w-16 text-xs font-bold text-gray-500 flex-shrink-0">
                Floor {floor}
              </div>

              {/* Rooms on this floor */}
              <div className="flex-1 grid grid-cols-6 gap-2">
                {ROOM_TYPES.map((roomType, roomIdx) => {
                  const roomId = `F${floor}R${roomIdx + 1}`;
                  const isBooked = occupancyMap[roomId];

                  return (
                    <button
                      key={roomId}
                      onClick={() => handleRoomClick(roomId, floor, roomType)}
                      disabled={isBooked}
                      className={`relative rounded-lg p-2 text-center transition-all ${getRoomColor(roomId, roomType)}`}
                      title={`Floor ${floor} — ${roomType.type} — ₹${(basePrice * roomType.multiplier).toLocaleString("en-IN")}/night`}
                    >
                      {isBooked && (
                        <Lock className="w-3 h-3 absolute top-1 right-1 opacity-70" />
                      )}
                      <div className="text-[10px] font-bold">{roomType.label}</div>
                      <div className="text-[9px] opacity-80">{floor}{String.fromCharCode(64 + roomIdx + 1)}</div>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        ))}

        {/* Lobby */}
        <div className="bg-gray-100 text-center py-2 text-xs font-medium text-gray-500">
          LOBBY & RECEPTION
        </div>
      </div>

      {/* Pricing Tiers */}
      <div className="mt-6 grid grid-cols-3 gap-3 text-center text-sm">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <p className="font-bold text-blue-700">Standard</p>
          <p className="text-lg font-bold">₹{basePrice.toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">per night</p>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-3">
          <p className="font-bold text-purple-700">Deluxe</p>
          <p className="text-lg font-bold">₹{(basePrice * 1.5).toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">per night</p>
        </div>
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
          <p className="font-bold text-yellow-700">Suite</p>
          <p className="text-lg font-bold">₹{(basePrice * 2.5).toLocaleString("en-IN")}</p>
          <p className="text-xs text-gray-500">per night</p>
        </div>
      </div>

      {/* Selection Summary */}
      {selectedRooms.length > 0 && (
        <div className="mt-4 bg-green-50 border border-green-200 rounded-lg p-3">
          <p className="text-sm font-semibold text-green-800 mb-1">Selected Rooms:</p>
          <div className="flex flex-wrap gap-2">
            {selectedRooms.map((r) => (
              <span key={r.roomId} className="inline-flex items-center bg-green-600 text-white text-xs px-2.5 py-1 rounded-full font-medium">
                Floor {r.floor} — {r.roomType} — ₹{r.price.toLocaleString("en-IN")}
              </span>
            ))}
          </div>
          <p className="text-sm font-bold mt-2 text-green-900">
            Total Room Cost: ₹{selectedRooms.reduce((sum, r) => sum + r.price, 0).toLocaleString("en-IN")}
          </p>
        </div>
      )}
    </div>
  );
};

export default RoomGrid;
