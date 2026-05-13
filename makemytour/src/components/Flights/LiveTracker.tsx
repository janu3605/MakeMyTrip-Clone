import React, { useEffect, useState } from 'react';
import { Badge } from "@/components/ui/badge";
import { Plane, AlertCircle, CheckCircle2, Clock } from "lucide-react";
import { toast } from "sonner"; // Updated to Sonner

interface FlightStatus {
    flightId: string;
    status: string;
    message: string;
    reason: string;
    progress: number;
    lastUpdated: string;
}

export const LiveTracker = ({ flightId }: { flightId: string }) => {
    const [status, setStatus] = useState<FlightStatus | null>(null);
    const [isMounted, setIsMounted] = useState(false);

    useEffect(() => {
        setIsMounted(true);
        // Connect to your live Render backend
        const eventSource = new EventSource(`https://makemytrip-clone-7iaw.onrender.com/api/tracking/stream?flightId=${flightId}`);

        eventSource.addEventListener("flight-status", (event) => {
            const data = JSON.parse(event.data);
            const currentFlight = data[flightId];

            if (currentFlight) {
                // Sonner Push Notification: Trigger if status flips to DELAYED
                if (status && status.status !== currentFlight.status && currentFlight.status === "DELAYED") {
                    toast.error(`Flight ${flightId} Delayed`, {
                        description: currentFlight.reason,
                        duration: 5000,
                    });
                }

                // Sonner Push Notification: Trigger if flight lands
                if (status && status.status !== currentFlight.status && currentFlight.status === "LANDED") {
                    toast.success(`Flight ${flightId} has landed!`);
                }

                setStatus(currentFlight);
            }
        });

        eventSource.onerror = () => {
            console.error("SSE Connection Failed");
            eventSource.close();
        };

        return () => eventSource.close();
    }, [flightId, status]);

    if (!isMounted || !status) return <div className="text-xs text-gray-400 animate-pulse mt-2">Connecting to radar...</div>;

    const getStatusIcon = () => {
        if (status.status === "DELAYED") return <AlertCircle className="w-4 h-4 text-red-500" />;
        if (status.status === "LANDED") return <CheckCircle2 className="w-4 h-4 text-green-500" />;
        return <Clock className="w-4 h-4 text-blue-500" />;
    };

    return (
        <div className="mt-4 pt-4 border-t border-dashed border-gray-200">
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                    {getStatusIcon()}
                    <span className="text-sm font-medium text-gray-700">Live Status Tracking</span>
                </div>
                <Badge variant={status.status === "DELAYED" ? "destructive" : "secondary"} className="text-[10px] uppercase">
                    {status.status.replace("_", " ")}
                </Badge>
            </div>

            <div className="space-y-2">
                <div className="flex justify-between text-xs text-gray-500">
                    <span>{status.message}</span>
                    <span>{status.progress}%</span>
                </div>
                <div className="relative w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                        className={`absolute top-0 left-0 h-full transition-all duration-1000 ease-linear ${status.status === 'DELAYED' ? 'bg-red-500' : 'bg-blue-500'
                            }`}
                        style={{ width: `${status.progress}%` }}
                    />
                </div>
            </div>

            {status.status === "DELAYED" && (
                <p className="mt-2 text-[11px] text-red-600 bg-red-50 p-2 rounded">
                    <strong>Notice:</strong> {status.reason}
                </p>
            )}
        </div>
    );
};