import React, { useEffect, useState, useRef } from 'react';
import { Badge } from "@/components/ui/badge";
import { Plane, AlertCircle, CheckCircle2, Clock, AlertTriangle, Info } from "lucide-react";
import { toast } from "sonner";

interface FlightStatus {
    flightId: string;
    status: string;
    message: string;
    reason: string;
    progress: number;
    lastUpdated: string;
}

const STATUS_CONFIG: Record<string, { color: string; bg: string; icon: React.ReactNode; label: string }> = {
    SCHEDULED: { color: "text-blue-600", bg: "bg-blue-50", icon: <Clock className="w-4 h-4 text-blue-500" />, label: "Scheduled" },
    BOARDING: { color: "text-amber-600", bg: "bg-amber-50", icon: <Info className="w-4 h-4 text-amber-500" />, label: "Boarding" },
    IN_AIR: { color: "text-indigo-600", bg: "bg-indigo-50", icon: <Plane className="w-4 h-4 text-indigo-500" />, label: "In Air" },
    DELAYED: { color: "text-red-600", bg: "bg-red-50", icon: <AlertCircle className="w-4 h-4 text-red-500" />, label: "Delayed" },
    CANCELLED: { color: "text-red-800", bg: "bg-red-100", icon: <AlertTriangle className="w-4 h-4 text-red-700" />, label: "Cancelled" },
    LANDED: { color: "text-green-600", bg: "bg-green-50", icon: <CheckCircle2 className="w-4 h-4 text-green-500" />, label: "Landed" },
};

export const LiveTracker = ({ flightId }: { flightId: string }) => {
    const [status, setStatus] = useState<FlightStatus | null>(null);
    const [isMounted, setIsMounted] = useState(false);
    const prevStatusRef = useRef<string | null>(null);

    useEffect(() => {
        setIsMounted(true);
        const eventSource = new EventSource(`https://makemytrip-clone-7iaw.onrender.com/api/tracking/stream`);

        eventSource.addEventListener("flight-status", (event) => {
            const data = JSON.parse(event.data);
            const currentFlight = data[flightId];

            if (currentFlight) {
                const prevStatus = prevStatusRef.current;

                // Fire notification popup for EVERY status change
                if (prevStatus && prevStatus !== currentFlight.status) {
                    const shortId = flightId.substring(0, 8);

                    if (currentFlight.status === "DELAYED") {
                        toast.error(`✈️ Flight ${shortId}… Delayed`, {
                            description: `Reason: ${currentFlight.reason || "Operational delay"}. ${currentFlight.message}`,
                            duration: 8000,
                        });
                    } else if (currentFlight.status === "CANCELLED") {
                        toast.error(`❌ Flight ${shortId}… Cancelled`, {
                            description: `Reason: ${currentFlight.reason || "Schedule cancellation"}`,
                            duration: 10000,
                        });
                    } else if (currentFlight.status === "LANDED") {
                        toast.success(`🛬 Flight ${shortId}… has landed!`, {
                            description: currentFlight.message,
                            duration: 6000,
                        });
                    } else if (currentFlight.status === "BOARDING") {
                        toast.info(`🚶 Flight ${shortId}… now boarding`, {
                            description: currentFlight.message,
                            duration: 5000,
                        });
                    } else if (currentFlight.status === "IN_AIR") {
                        toast.info(`✈️ Flight ${shortId}… is now airborne`, {
                            description: currentFlight.message,
                            duration: 5000,
                        });
                    } else {
                        toast(`Flight ${shortId}… status: ${currentFlight.status}`, {
                            description: currentFlight.message,
                            duration: 4000,
                        });
                    }
                }

                prevStatusRef.current = currentFlight.status;
                setStatus(currentFlight);
            }
        });

        eventSource.onerror = () => {
            console.error("SSE Connection Failed");
            eventSource.close();
        };

        return () => eventSource.close();
    }, [flightId]);

    if (!isMounted || !status) return <div className="text-xs text-gray-400 animate-pulse mt-2">Connecting to radar...</div>;

    const config = STATUS_CONFIG[status.status] || STATUS_CONFIG.SCHEDULED;

    return (
        <div className="mt-4 pt-4 border-t border-dashed border-gray-200">
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                    {config.icon}
                    <span className="text-sm font-medium text-gray-700">Live Status Tracking</span>
                </div>
                <Badge variant={status.status === "DELAYED" || status.status === "CANCELLED" ? "destructive" : "secondary"} className="text-[10px] uppercase">
                    {config.label}
                </Badge>
            </div>

            <div className="space-y-2">
                <div className="flex justify-between text-xs text-gray-600">
                    <span>{status.message}</span>
                    <span>{status.progress}%</span>
                </div>
                <div className="relative w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                        className={`absolute top-0 left-0 h-full transition-all duration-1000 ease-linear ${
                            status.status === 'DELAYED' || status.status === 'CANCELLED' ? 'bg-red-500' : 
                            status.status === 'LANDED' ? 'bg-green-500' : 'bg-blue-500'
                        }`}
                        style={{ width: `${status.progress}%` }}
                    />
                </div>
            </div>

            {/* Delay / Cancellation reason block */}
            {(status.status === "DELAYED" || status.status === "CANCELLED") && status.reason && status.reason !== "N/A" && (
                <div className={`mt-3 text-[11px] p-2.5 rounded-lg ${
                    status.status === "CANCELLED" ? "text-red-800 bg-red-50 border border-red-200" : "text-red-600 bg-red-50 border border-red-100"
                }`}>
                    <div className="flex items-start gap-1.5">
                        <AlertCircle className="w-3.5 h-3.5 flex-shrink-0 mt-0.5" />
                        <div>
                            <strong>{status.status === "CANCELLED" ? "Cancellation Reason:" : "Delay Reason:"}</strong>{" "}
                            {status.reason}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};