import React, { useState } from "react";
import {
    User,
    Phone,
    Mail,
    Edit2,
    MapPin,
    Calendar,
    CreditCard,
    X,
    Check,
    LogOut,
    Plane,
    Building2,
} from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import { useRouter } from "next/router";
import { clearUser, setUser } from "@/store";
import { editprofile, cancelBooking, getuserbyemail } from "@/api";
import { LiveTracker } from "@/components/Flights/LiveTracker";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
const index = () => {
    const dispatch = useDispatch();
    const user = useSelector((state: any) => state.user.user);
    const router = useRouter();

    const logout = () => {
        dispatch(clearUser());
        router.push("/");
    };
    const [isEditing, setIsEditing] = useState(false);
    const [userData, setUserData] = useState({
        firstName: user?.firstName ? user?.firstName : "",
        lastName: user?.lastName ? user?.lastName : "",
        email: user?.email ? user?.email : "",
        phoneNumber: user?.phoneNumber ? user?.phoneNumber : "",
        bookings: [
            {
                type: "Flight",
                bookingId: "F123456",
                date: "2024-03-25",
                quantity: 2,
                totalPrice: 12499,
                details: {
                    from: "Delhi",
                    to: "Mumbai",
                    airline: "IndiGo",
                },
            },
            {
                type: "Hotel",
                bookingId: "H789012",
                date: "2024-04-15",
                quantity: 1,
                totalPrice: 8999,
                details: {
                    name: "Taj Palace",
                    location: "Goa",
                    nights: 3,
                },
            },
        ],
    });

    const [editForm, setEditForm] = useState({ ...userData });
    const handleSave = async () => {
        try {
            const data = await editprofile(
                user?.id,
                userData.firstName,
                userData.lastName,
                userData.email,
                userData.phoneNumber
            );
            dispatch(setUser(data));
            setIsEditing(false);
        } catch (error) {
            setUserData(editForm);
            setIsEditing(false);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("en-IN", {
            day: "numeric",
            month: "short",
            year: "numeric",
        });
    };
    const handleEditFormChange = (field: any, value: any) => {
        setUserData((prevState) => ({
            ...prevState,
            [field]: value, // Update the specific field dynamically
        }));
    };

    const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
    const [bookingToCancel, setBookingToCancel] = useState<any>(null);
    const [cancelReason, setCancelReason] = useState("");
    const [isCancelling, setIsCancelling] = useState(false);

    const handleCancelClick = (booking: any) => {
        setBookingToCancel(booking);
        setCancelDialogOpen(true);
    };

    const confirmCancel = async () => {
        if (!cancelReason) {
            toast.error("Please select a reason");
            return;
        }
        setIsCancelling(true);
        try {
            await cancelBooking(user.id, bookingToCancel.id, cancelReason);
            toast.success("Booking cancelled successfully!");
            setCancelDialogOpen(false);
            // Refresh user data to see updated booking status
            const updatedUser = await getuserbyemail(user.email);
            if (updatedUser) dispatch(setUser(updatedUser));
        } catch (error) {
            toast.error("Failed to cancel booking");
        } finally {
            setIsCancelling(false);
        }
    };

    const getRefundPreview = (booking: any) => {
        if (!booking) return 0;
        const bookingDate = new Date(booking.date).getTime();
        const now = new Date().getTime();
        const diffHours = (now - bookingDate) / (1000 * 60 * 60);
        return diffHours <= 24 ? booking.totalPrice * 0.5 : booking.totalPrice * 0.2;
    };

    // Sort recent first, filter out COMPLETED refunds
    const displayBookings = [...(user?.bookings || [])]
        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
        .filter((b) => b.refundStatus !== "COMPLETED");

    return (
        <div className="min-h-screen bg-gray-50 pt-8 px-4">
            <div className="max-w-6xl mx-auto">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {/* Sidebar: Wallet & Profile */}
                    <div className="md:col-span-1 space-y-6">
                        {/* Wallet Balance Card */}
                        <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-xl shadow-lg p-6 text-white">
                            <h2 className="text-sm font-medium opacity-80 mb-1">My Wallet Balance</h2>
                            <p className="text-3xl font-bold">
                                ₹ {user?.mockBalance ? user.mockBalance.toLocaleString("en-IN") : "0"}
                            </p>
                        </div>

                        {/* Profile Section */}
                        <div className="bg-white rounded-xl shadow-lg p-6">
                            <div className="flex justify-between items-start mb-6">
                                <h2 className="text-2xl font-bold">Profile</h2>
                                {!isEditing && (
                                    <button
                                        onClick={() => setIsEditing(true)}
                                        className="text-red-600 flex items-center space-x-1 hover:text-red-700"
                                    >
                                        <Edit2 className="w-4 h-4" />
                                        <span>Edit</span>
                                    </button>
                                )}
                            </div>

                            {isEditing ? (
                                <div className="space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            First Name
                                        </label>
                                        <input
                                            type="text"
                                            value={userData.firstName}
                                            onChange={(e) => handleEditFormChange("firstName", e.target.value)}
                                            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Last Name
                                        </label>
                                        <input
                                            type="text"
                                            value={userData.lastName}
                                            onChange={(e) => handleEditFormChange("lastName", e.target.value)}
                                            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Email
                                        </label>
                                        <input
                                            type="email"
                                            value={userData.email}
                                            onChange={(e) => handleEditFormChange("email", e.target.value)}

                                            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Phone Number
                                        </label>
                                        <input
                                            type="tel"
                                            value={userData.phoneNumber}
                                            onChange={(e) => handleEditFormChange("phoneNumber", e.target.value)}
                                            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                        />
                                    </div>
                                    <div className="flex space-x-3">
                                        <button
                                            onClick={handleSave}
                                            className="flex-1 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition-colors flex items-center justify-center space-x-2"
                                        >
                                            <Check className="w-4 h-4" />
                                            <span>Save</span>
                                        </button>
                                        <button
                                            onClick={() => {
                                                setIsEditing(false);
                                                setEditForm({ ...user });
                                            }}
                                            className="flex-1 bg-gray-100 text-gray-700 py-2 rounded-lg hover:bg-gray-200 transition-colors flex items-center justify-center space-x-2"
                                        >
                                            <X className="w-4 h-4" />
                                            <span>Cancel</span>
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <div className="space-y-6">
                                    <div className="flex items-center space-x-3">
                                        <User className="w-5 h-5 text-gray-900" />
                                        <div>
                                            <p className="font-medium">
                                                {user?.firstName} {user?.lastName}
                                            </p>
                                            {/* <p className="text-sm text-gray-900">{userData.role}</p> */}
                                        </div>
                                    </div>
                                    <div className="flex items-center space-x-3">
                                        <Mail className="w-5 h-5 text-gray-900" />
                                        <p>{user?.email}</p>
                                    </div>
                                    <div className="flex items-center space-x-3">
                                        <Phone className="w-5 h-5 text-gray-900" />
                                        <p>{user?.phoneNumber}</p>
                                    </div>
                                    <button
                                        className="w-full mt-4 flex items-center justify-center space-x-2 text-red-600 hover:text-red-700"
                                        onClick={logout}
                                    >
                                        <LogOut className="w-4 h-4" />
                                        <span>Logout</span>
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Bookings Section */}
                    <div className="md:col-span-2">
                        <div className="bg-white rounded-xl shadow-lg p-6">
                            <h2 className="text-2xl font-bold mb-6">My Bookings</h2>
                            <div className="space-y-6">
                                {displayBookings.map((booking: any, index: any) => (
                                    <div
                                        key={index}
                                        className={`border rounded-lg p-4 transition-shadow ${
                                            booking?.status === "CANCELLED" ? "bg-gray-50 border-gray-200" : "hover:shadow-md bg-white"
                                        }`}
                                    >
                                        <div className="flex items-start justify-between mb-4">
                                            <div className="flex items-center space-x-3">
                                                {booking?.type === "Flight" ? (
                                                    <div className={`p-2 rounded-lg ${booking?.status === "CANCELLED" ? "bg-gray-200" : "bg-blue-100"}`}>
                                                        <Plane className={`w-6 h-6 ${booking?.status === "CANCELLED" ? "text-gray-400" : "text-blue-600"}`} />
                                                    </div>
                                                ) : (
                                                    <div className={`p-2 rounded-lg ${booking?.status === "CANCELLED" ? "bg-gray-200" : "bg-green-100"}`}>
                                                        <Building2 className={`w-6 h-6 ${booking?.status === "CANCELLED" ? "text-gray-400" : "text-green-600"}`} />
                                                    </div>
                                                )}
                                                <div>
                                                    <h3 className={`font-semibold ${booking?.status === "CANCELLED" && "text-gray-500"}`}>{booking?.type}</h3>
                                                    <p className="text-sm text-gray-900">
                                                        Booking ID: {booking?.bookingId}
                                                    </p>
                                                    {booking?.status === "CANCELLED" && (
                                                        <span className="inline-block mt-1 px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700">
                                                            CANCELLED
                                                        </span>
                                                    )}
                                                </div>
                                            </div>
                                            <div className="text-right">
                                                <p className={`font-semibold ${booking?.status === "CANCELLED" ? "text-gray-400 line-through" : ""}`}>
                                                    ₹ {booking?.totalPrice.toLocaleString("en-IN")}
                                                </p>
                                                {booking?.status !== "CANCELLED" && (
                                                    <Button variant="ghost" className="text-red-600 hover:text-red-700 hover:bg-red-50 text-xs h-7 mt-1 px-2" onClick={() => handleCancelClick(booking)}>
                                                        Cancel Booking
                                                    </Button>
                                                )}
                                            </div>
                                        </div>
                                        <div className={`flex flex-wrap gap-4 text-sm ${booking?.status === "CANCELLED" ? "text-gray-500" : "text-black"}`}>
                                            <div className="flex items-center space-x-1">
                                                <Calendar className="w-4 h-4" />
                                                <span>{formatDate(booking?.date)}</span>
                                            </div>
                                            <div className="flex items-center space-x-1">
                                                <MapPin className="w-4 h-4" />
                                                <span>{booking?.type}</span>
                                            </div>
                                        </div>

                                        {booking?.status === "CANCELLED" && (
                                            <div className="mt-4 pt-4 border-t border-gray-200">
                                                <div className="flex justify-between items-center mb-2">
                                                    <p className="text-sm font-semibold text-gray-700">Refund Tracker</p>
                                                    <p className="text-sm font-bold text-green-600">₹{booking.refundAmount?.toLocaleString("en-IN")}</p>
                                                </div>
                                                <div className="flex items-center justify-between text-xs font-medium">
                                                    <div className={`flex flex-col items-center ${booking.refundStatus === "PENDING" || booking.refundStatus === "PROCESSED" || booking.refundStatus === "COMPLETED" ? "text-blue-600" : "text-gray-400"}`}>
                                                        <div className="w-4 h-4 rounded-full bg-current mb-1"></div>
                                                        <span>Pending</span>
                                                    </div>
                                                    <div className={`flex-1 h-0.5 mx-2 ${booking.refundStatus === "PROCESSED" || booking.refundStatus === "COMPLETED" ? "bg-blue-600" : "bg-gray-200"}`}></div>
                                                    <div className={`flex flex-col items-center ${booking.refundStatus === "PROCESSED" || booking.refundStatus === "COMPLETED" ? "text-blue-600" : "text-gray-400"}`}>
                                                        <div className="w-4 h-4 rounded-full bg-current mb-1"></div>
                                                        <span>Processed</span>
                                                    </div>
                                                    <div className={`flex-1 h-0.5 mx-2 ${booking.refundStatus === "COMPLETED" ? "bg-blue-600" : "bg-gray-200"}`}></div>
                                                    <div className={`flex flex-col items-center ${booking.refundStatus === "COMPLETED" ? "text-blue-600" : "text-gray-400"}`}>
                                                        <div className="w-4 h-4 rounded-full bg-current mb-1"></div>
                                                        <span>Completed</span>
                                                    </div>
                                                </div>
                                            </div>
                                        )}

                                        {/* Integration: Live tracker for each purchased flight */}
                                        {booking?.type === "Flight" && booking?.status !== "CANCELLED" && (
                                            <LiveTracker flightId={booking?.bookingId} />
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
                <DialogContent className="sm:max-w-md bg-white">
                    <DialogHeader>
                        <DialogTitle>Cancel Booking</DialogTitle>
                    </DialogHeader>
                    <div className="py-4">
                        <p className="text-sm text-gray-600 mb-4">
                            Are you sure you want to cancel your {bookingToCancel?.type?.toLowerCase()} booking? 
                            Based on our policy, you are eligible for a refund of <span className="font-bold text-green-600">₹{getRefundPreview(bookingToCancel).toLocaleString("en-IN")}</span>.
                        </p>
                        <Select onValueChange={(val) => setCancelReason(val || "")} value={cancelReason}>
                            <SelectTrigger>
                                <SelectValue placeholder="Select a reason for cancellation" />
                            </SelectTrigger>
                            <SelectContent className="bg-white">
                                <SelectItem value="Change of plans">Change of plans</SelectItem>
                                <SelectItem value="Found a better deal">Found a better deal</SelectItem>
                                <SelectItem value="Medical emergency">Medical emergency</SelectItem>
                                <SelectItem value="Flight/Hotel delayed">Flight/Hotel delayed or changed</SelectItem>
                                <SelectItem value="Other">Other</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <DialogFooter className="flex sm:justify-between">
                        <Button variant="outline" onClick={() => setCancelDialogOpen(false)} disabled={isCancelling}>
                            Keep Booking
                        </Button>
                        <Button variant="destructive" className="bg-red-600 text-white" onClick={confirmCancel} disabled={isCancelling}>
                            {isCancelling ? "Cancelling..." : "Confirm Cancellation"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default index;