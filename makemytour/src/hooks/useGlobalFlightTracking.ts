import { useEffect } from 'react';
import { toast } from "sonner";
import { useSelector } from "react-redux";

export const useGlobalFlightTracking = () => {
    const user = useSelector((state: any) => state.user.user);

    useEffect(() => {
        if (!user) return;

        const eventSource = new EventSource(`https://makemytrip-clone-7iaw.onrender.com/api/tracking/stream`);

        eventSource.addEventListener("flight-status", (event) => {
            const data = JSON.parse(event.data);
            const now = new Date();

            user.bookings.forEach((booking: any) => {
                if (booking.type !== "Flight") return;

                const liveData = data[booking.bookingId];
                if (!liveData) return;

                // TRIGGER 1: Delay Notification
                if (liveData.status === "DELAYED") {
                    toast.error(`Flight ${booking.bookingId} Delayed`, {
                        description: liveData.reason,
                    });
                }

                // TRIGGER 2: 5 Minutes Before Boarding
                // Assuming departureTime is available in your booking object
                const departure = new Date(booking.date);
                const diff = (departure.getTime() - now.getTime()) / (1000 * 60);

                if (diff > 4.5 && diff < 5.5) {
                    toast.warning(`Final Call: ${booking.bookingId}`, {
                        description: "Boarding closes in 5 minutes!",
                    });
                }
            });
        });

        return () => eventSource.close();
    }, [user]);
};