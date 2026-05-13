import React, { useState, useEffect } from "react";
import SignupDialog from "./SignupDialog";
import { LogOut, Plane, User } from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import Link from "next/link";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
    DropdownMenuGroup,
} from "@/components/ui/dropdown-menu";
import { Button } from "./ui/button";
import { Avatar, AvatarFallback } from "./ui/avatar";
import { clearUser } from "@/store";
import { useRouter } from "next/navigation";

const Navbar = () => {
    const dispatch = useDispatch();
    const user = useSelector((state: any) => state.user.user);
    const router = useRouter();

    // 1. Add state to track if the component has mounted in the browser
    const [isMounted, setIsMounted] = useState(false);

    // 2. Set it to true as soon as the client loads
    useEffect(() => {
        setIsMounted(true);
    }, []);

    const logout = () => {
        dispatch(clearUser());
    };

    return (
        <header className="backdrop-blur-md py-4 sticky top-0 z-50">
            <div className="container mx-auto px-4 flex items-center justify-between">

                <Link href="/" className="flex items-center space-x-2 hover:opacity-80 transition-opacity cursor-pointer">
                    <Plane className="w-8 h-8 text-red-500" />
                    <span className="text-2xl font-bold text-black">MakeMyTour</span>
                </Link>

                <div className="flex items-center space-x-4">

                    {/* 3. Only render the Auth buttons IF the component is mounted */}
                    {isMounted && (
                        user ? (
                            <>
                                {user.role === "ADMIN" && (
                                    <Button variant="default" onClick={() => router.push("/admin")}>
                                        ADMIN
                                    </Button>
                                )}

                                <DropdownMenu>
                                    <DropdownMenuTrigger>
                                        <Button
                                            variant="ghost"
                                            className="relative h-8 w-8 rounded-full"
                                        >
                                            <Avatar className="h-8 w-8">
                                                <AvatarFallback>
                                                    {user?.firstName?.charAt(0)}
                                                </AvatarFallback>
                                            </Avatar>
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent className="w-56" align="end">
                                        <DropdownMenuGroup>
                                            <DropdownMenuLabel className="font-normal">
                                                <div className="flex flex-col space-y-1">
                                                    <p className="text-sm font-medium leading-none">
                                                        {user?.firstName}
                                                    </p>
                                                    <p className="text-xs leading-none text-muted-foreground">
                                                        {user?.email}
                                                    </p>
                                                </div>
                                            </DropdownMenuLabel>
                                        </DropdownMenuGroup>
                                        <DropdownMenuSeparator />
                                        <DropdownMenuItem onClick={() => router.push("/profile")}>
                                            <User className="mr-2 h-4 w-4" />
                                            <span>Profile</span>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem onClick={() => logout()}>
                                            <LogOut className="mr-2 h-4 w-4" />
                                            <span>Log out</span>
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </>
                        ) : (
                            <SignupDialog />
                        )
                    )}

                </div>
            </div>
        </header>
    );
};

export default Navbar;