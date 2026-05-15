"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useEffect, useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

import { Textarea } from "@/components/ui/textarea";
import FlightList from "@/components/Flights/Flightlist";
import {
    addflight,
    addhotel,
    editflight,
    edithotel,
    getuserbyemail,
    getSeasonalRules,
    addSeasonalRule,
} from "@/api";
import HotelList from "@/components/Hotel/Hotel";
const mockFlights = [
    {
        _id: "1",
        flightName: "AirOne 101",
        from: "New York",
        to: "London",
        departureTime: "2023-07-01T08:00",
        arrivalTime: "2023-07-01T20:00",
        price: 500,
        availableSeats: 150,
    },
    {
        _id: "2",
        flightName: "SkyHigh 202",
        from: "Paris",
        to: "Tokyo",
        departureTime: "2023-07-02T10:00",
        arrivalTime: "2023-07-03T06:00",
        price: 800,
        availableSeats: 200,
    },
    {
        _id: "3",
        flightName: "EagleWings 303",
        from: "Los Angeles",
        to: "Sydney",
        departureTime: "2023-07-03T22:00",
        arrivalTime: "2023-07-05T06:00",
        price: 1200,
        availableSeats: 180,
    },
];

const mockHotels = [
    {
        _id: "1",
        hotelName: "Luxury Palace",
        location: "Paris, France",
        pricePerNight: 300,
        availableRooms: 50,
        amenities: "Wi-Fi, Pool, Spa, Restaurant",
    },
    {
        _id: "2",
        hotelName: "Seaside Resort",
        location: "Bali, Indonesia",
        pricePerNight: 200,
        availableRooms: 100,
        amenities: "Beach Access, Wi-Fi, Restaurant, Water Sports",
    },
    {
        _id: "3",
        hotelName: "Mountain Lodge",
        location: "Aspen, Colorado",
        pricePerNight: 250,
        availableRooms: 30,
        amenities: "Ski-in/Ski-out, Fireplace, Hot Tub, Restaurant",
    },
];
interface User {
    _id: string;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    phoneNumber: string;
}

function UserSearch() {
    const [email, setEmail] = useState("");
    const [user, setUser] = useState<User | null>(null);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();
        const data = await getuserbyemail(email);
        const mockUser: User = data;
        setUser(mockUser);
    };

    return (
        <div className="space-y-4">
            <form onSubmit={handleSearch} className="flex gap-2">
                <div className="flex-1">
                    <Label htmlFor="email" className="sr-only">
                        Email
                    </Label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="Search user by email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <Button type="submit">Search</Button>
            </form>
            {user && (
                <div className="border p-4 rounded-md">
                    <h3 className="font-bold mb-2">User Details</h3>
                    <p>
                        <strong>Name:</strong> {user.firstName} {user.lastName}
                    </p>
                    <p>
                        <strong>Email:</strong> {user.email}
                    </p>
                    <p>
                        <strong>Role:</strong> {user.role}
                    </p>
                    <p>
                        <strong>Phone:</strong> {user.phoneNumber}
                    </p>
                </div>
            )}
        </div>
    );
}

interface Hotel {
    id?: string;
    hotelName: string;
    location: string;
    pricePerNight: number;
    availableRooms: number;
    amenities: string;
}

function AddEditHotel({ hotel }: { hotel: Hotel | null }) {
    const [formData, setFormData] = useState<Hotel>({
        hotelName: "",
        location: "",
        pricePerNight: 0,
        availableRooms: 0,
        amenities: "",
    });

    useEffect(() => {
        if (hotel) {
            setFormData(hotel);
        } else {
            setFormData({
                hotelName: "",
                location: "",
                pricePerNight: 0,
                availableRooms: 0,
                amenities: "",
            });
        }
    }, [hotel]);

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (hotel) {
            await edithotel(
                hotel.id,
                formData.hotelName,
                formData.location,
                formData.pricePerNight,
                formData.availableRooms,
                formData.amenities
            );
            return;
        }
        await addhotel(
            formData.hotelName,
            formData.location,
            formData.pricePerNight,
            formData.availableRooms,
            formData.amenities
        );
        if (!hotel) {
            setFormData({
                hotelName: "",
                location: "",
                pricePerNight: 0,
                availableRooms: 0,
                amenities: "",
            });
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <h3 className="text-lg font-semibold mb-2">
                {hotel ? "Edit Hotel" : "Add New Hotel"}
            </h3>
            <div>
                <Label htmlFor="hotelName">Hotel Name</Label>
                <Input
                    id="hotelName"
                    name="hotelName"
                    value={formData.hotelName}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="location">Location</Label>
                <Input
                    id="location"
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="pricePerNight">Price Per Night</Label>
                <Input
                    id="pricePerNight"
                    name="pricePerNight"
                    type="number"
                    value={formData.pricePerNight}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="availableRooms">Available Rooms</Label>
                <Input
                    id="availableRooms"
                    name="availableRooms"
                    type="number"
                    value={formData.availableRooms}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="amenities">Amenities</Label>
                <Textarea
                    id="amenities"
                    name="amenities"
                    value={formData.amenities}
                    onChange={handleChange}
                    required
                />
            </div>
            <Button type="submit">{hotel ? "Update Hotel" : "Add Hotel"}</Button>
        </form>
    );
}

interface Flight {
    id?: string;
    flightName: string;
    from: string;
    to: string;
    departureTime: string;
    arrivalTime: string;
    price: number;
    availableSeats: number;
}

function AddEditFlight({ flight }: { flight: Flight | null }) {
    const [formData, setFormData] = useState<Flight>({
        flightName: "",
        from: "",
        to: "",
        departureTime: "",
        arrivalTime: "",
        price: 0,
        availableSeats: 0,
    });

    useEffect(() => {
        if (flight) {
            setFormData(flight);
        } else {
            setFormData({
                flightName: "",
                from: "",
                to: "",
                departureTime: "",
                arrivalTime: "",
                price: 0,
                availableSeats: 0,
            });
        }
    }, [flight]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        // Here you would typically send this data to your backend
        console.log("Submitting flight data:", formData);
        if (flight) {
            await editflight(
                flight?.id,
                formData.flightName,
                formData.from,
                formData.to,
                formData.departureTime,
                formData.arrivalTime,
                formData.price,
                formData.availableSeats
            );
            return;
        }
        await addflight(
            formData.flightName,
            formData.from,
            formData.to,
            formData.departureTime,
            formData.arrivalTime,
            formData.price,
            formData.availableSeats
        );
        if (!flight) {
            setFormData({
                flightName: "",
                from: "",
                to: "",
                departureTime: "",
                arrivalTime: "",
                price: 0,
                availableSeats: 0,
            });
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <h3 className="text-lg font-semibold mb-2">
                {flight ? "Edit Flight" : "Add New Flight"}
            </h3>
            <div>
                <Label htmlFor="flightName">Flight Name</Label>
                <Input
                    id="flightName"
                    name="flightName"
                    value={formData.flightName}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="from">From</Label>
                <Input
                    id="from"
                    name="from"
                    value={formData.from}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="to">To</Label>
                <Input
                    id="to"
                    name="to"
                    value={formData.to}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="departureTime">Departure Time</Label>
                <Input
                    id="departureTime"
                    name="departureTime"
                    type="datetime-local"
                    value={formData.departureTime}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="arrivalTime">Arrival Time</Label>
                <Input
                    id="arrivalTime"
                    name="arrivalTime"
                    type="datetime-local"
                    value={formData.arrivalTime}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="price">Price</Label>
                <Input
                    id="price"
                    name="price"
                    type="number"
                    value={formData.price}
                    onChange={handleChange}
                    required
                />
            </div>
            <div>
                <Label htmlFor="availableSeats">Available Seats</Label>
                <Input
                    id="availableSeats"
                    name="availableSeats"
                    type="number"
                    value={formData.availableSeats}
                    onChange={handleChange}
                    required
                />
            </div>
            <Button type="submit">{flight ? "Update Flight" : "Add Flight"}</Button>
        </form>
    );
}

function SeasonalRulesManager() {
    const [rules, setRules] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [formData, setFormData] = useState({
        name: "",
        startDate: "",
        endDate: "",
        multiplier: 1.2,
    });

    useEffect(() => {
        const fetch = async () => {
            const data = await getSeasonalRules();
            setRules(data);
            setLoading(false);
        };
        fetch();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const newRule = await addSeasonalRule(
                formData.name,
                formData.startDate,
                formData.endDate,
                formData.multiplier
            );
            setRules([...rules, newRule]);
            setFormData({ name: "", startDate: "", endDate: "", multiplier: 1.2 });
        } catch (error) {
            console.error("Failed to add rule", error);
        }
    };

    const isActive = (startDate: string, endDate: string) => {
        const today = new Date().toISOString().split("T")[0];
        return today >= startDate && today <= endDate;
    };

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Rules Table */}
            <div>
                <h3 className="text-lg font-semibold mb-4">Active Seasonal Rules</h3>
                {loading ? (
                    <p className="text-gray-900">Loading rules...</p>
                ) : rules.length === 0 ? (
                    <p className="text-gray-900">No seasonal rules configured yet.</p>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead>Dates</TableHead>
                                <TableHead>Multiplier</TableHead>
                                <TableHead>Status</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {rules.map((rule: any) => (
                                <TableRow key={rule.id || rule._id}>
                                    <TableCell className="font-medium">{rule.name}</TableCell>
                                    <TableCell className="text-sm text-black">
                                        {rule.startDate} → {rule.endDate}
                                    </TableCell>
                                    <TableCell>
                                        <span className="font-semibold text-orange-600">
                                            {rule.multiplier}x
                                        </span>
                                        <span className="text-xs text-gray-900 ml-1">
                                            (+{Math.round((rule.multiplier - 1) * 100)}%)
                                        </span>
                                    </TableCell>
                                    <TableCell>
                                        {isActive(rule.startDate, rule.endDate) ? (
                                            <span className="px-2 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">
                                                Active Now
                                            </span>
                                        ) : (
                                            <span className="px-2 py-1 rounded-full text-xs font-semibold bg-gray-100 text-black">
                                                Scheduled
                                            </span>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </div>

            {/* Add Rule Form */}
            <div>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <h3 className="text-lg font-semibold mb-2">Add New Seasonal Rule</h3>
                    <div>
                        <Label htmlFor="ruleName">Rule Name</Label>
                        <Input
                            id="ruleName"
                            placeholder="e.g. Diwali Peak Season"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <Label htmlFor="ruleStart">Start Date</Label>
                        <Input
                            id="ruleStart"
                            type="date"
                            value={formData.startDate}
                            onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <Label htmlFor="ruleEnd">End Date</Label>
                        <Input
                            id="ruleEnd"
                            type="date"
                            value={formData.endDate}
                            onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <Label htmlFor="ruleMultiplier">Price Multiplier</Label>
                        <Input
                            id="ruleMultiplier"
                            type="number"
                            step="0.05"
                            min="1.0"
                            max="3.0"
                            value={formData.multiplier}
                            onChange={(e) =>
                                setFormData({ ...formData, multiplier: parseFloat(e.target.value) || 1.0 })
                            }
                            required
                        />
                        <p className="text-xs text-gray-900 mt-1">
                            1.20 = 20% price increase during this period
                        </p>
                    </div>
                    <Button type="submit">Add Seasonal Rule</Button>
                </form>
            </div>
        </div>
    );
}

export default function AdminDashboard() {
    const [activeTab, setActiveTab] = useState("flights");
    const [selectedFlight, setSelectedFlight] = useState(null);
    const [selectedHotel, setSelectedHotel] = useState(null);

    return (
        <div className="container mx-auto p-4 bg-white max-w-full">
            <h1 className="text-3xl font-bold mb-6 ">Admin Dashboard</h1>
            <Tabs value={activeTab} onValueChange={setActiveTab}>
                <TabsList className="grid w-full grid-cols-4  text-black">
                    <TabsTrigger value="flights">Flights</TabsTrigger>
                    <TabsTrigger value="hotels">Hotels</TabsTrigger>
                    <TabsTrigger value="users">Users</TabsTrigger>
                    <TabsTrigger value="pricing">Pricing</TabsTrigger>
                </TabsList>
                <TabsContent value="flights">
                    <Card>
                        <CardHeader>
                            <CardTitle>Manage Flights</CardTitle>
                            <CardDescription>
                                Add, edit, or remove flights from the system.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-2 gap-4">
                                <FlightList onSelect={setSelectedFlight} />
                                <AddEditFlight flight={selectedFlight} />
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>
                <TabsContent value="hotels">
                    <Card>
                        <CardHeader>
                            <CardTitle>Manage Hotels</CardTitle>
                            <CardDescription>
                                Add, edit, or remove hotels from the system.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-2 gap-4">
                                <HotelList onSelect={setSelectedHotel} />
                                <AddEditHotel hotel={selectedHotel} />
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>
                <TabsContent value="users">
                    <Card>
                        <CardHeader>
                            <CardTitle>User Management</CardTitle>
                            <CardDescription>Search for users by email.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <UserSearch />
                        </CardContent>
                    </Card>
                </TabsContent>
                <TabsContent value="pricing">
                    <Card>
                        <CardHeader>
                            <CardTitle>Dynamic Pricing</CardTitle>
                            <CardDescription>
                                Configure seasonal pricing rules. The pricing engine applies these
                                multipliers automatically to all flights and hotels during the specified
                                date ranges.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <SeasonalRulesManager />
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}