import axios from "axios";

const BACKEND_URL = "https://makemytrip-clone-7iaw.onrender.com";

export const login = async (email, password) => {
  try {
    const url = `${BACKEND_URL}/user/login?email=${email}&password=${password}`;
    const res = await axios.post(url);
    const data = res.data;
    return data;
  } catch (error) {
    throw error;
  }
};

export const signup = async (
  firstName,
  lastName,
  email,
  phoneNumber,
  password
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/user/signup`, {
      firstName,
      lastName,
      email,
      phoneNumber,
      password,
    });
    const data = res.data;
    // console.log(data);
    return data;
  } catch (error) {
    throw error;
  }
};

export const getuserbyemail = async (email) => {
  try {
    const res = await axios.get(`${BACKEND_URL}/user/email?email=${email}`);
    const data = res.data;
    return data;
  } catch (error) {
    throw error;
  }
};

export const editprofile = async (
  id,
  firstName,
  lastName,
  email,
  phoneNumber
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/user/edit?id=${id}`, {
      firstName,
      lastName,
      email,
      phoneNumber,
    });
    const data = res.data;
    return data;
  } catch (error) { }
};
export const getflight = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/flight`);
    const data = res.data;
    return data;
  } catch (error) {
    console.error("Error fetching flights:", error.message);
    throw error;
  }
};

export const addflight = async (
  flightName,
  from,
  to,
  departureTime,
  arrivalTime,
  price,
  availableSeats
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/admin/flight`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const editflight = async (
  id,
  flightName,
  from,
  to,
  departureTime,
  arrivalTime,
  price,
  availableSeats
) => {
  try {
    const res = await axios.put(`${BACKEND_URL}/admin/flight/${id}`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const gethotel = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/hotel`);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(data);
  }
};

export const addhotel = async (
  hotelName,
  location,
  pricePerNight,
  availableRooms,
  amenities
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/admin/hotel`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const edithotel = async (
  id,
  hotelName,
  location,
  pricePerNight,
  availableRooms,
  amenities
) => {
  try {
    const res = await axios.put(`${BACKEND_URL}/admin/hotel/${id}`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const handleflightbooking = async (userId, flightId, seats, price, selectedSeat = "", seatPremium = 0) => {
  try {
    const url = `${BACKEND_URL}/booking/flight?userId=${userId}&flightId=${flightId}&seats=${seats}&price=${price}&selectedSeat=${encodeURIComponent(selectedSeat)}&seatPremium=${seatPremium}`;
    const res = await axios.post(url);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const handlehotelbooking = async (userId, hotelId, rooms, price, selectedRoom = "", roomPremium = 0) => {
  try {
    const url = `${BACKEND_URL}/booking/hotel?userId=${userId}&hotelId=${hotelId}&rooms=${rooms}&price=${price}&selectedRoom=${encodeURIComponent(selectedRoom)}&roomPremium=${roomPremium}`;
    const res = await axios.post(url);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

// ========== DYNAMIC PRICING APIs ==========

export const getPriceHistory = async (entityId, entityType) => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/pricing/history?entityId=${entityId}&entityType=${entityType}`
    );
    return res.data;
  } catch (error) {
    console.error("Error fetching price history:", error.message);
    return [];
  }
};

export const createPriceFreeze = async (userId, entityId, entityType, quantity) => {
  try {
    const url = `${BACKEND_URL}/api/pricing/freeze?userId=${userId}&entityId=${entityId}&entityType=${entityType}&quantity=${quantity}`;
    const res = await axios.post(url);
    return res.data;
  } catch (error) {
    console.error("Error creating price freeze:", error.message);
    throw error;
  }
};

export const checkPriceFreeze = async (userId, entityId) => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/pricing/freeze/check?userId=${userId}&entityId=${entityId}`
    );
    return res.data;
  } catch (error) {
    // 404 means no active freeze — not an error
    return null;
  }
};

export const getFreezeFee = async (quantity) => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/pricing/freeze/fee?quantity=${quantity}`
    );
    return res.data;
  } catch (error) {
    return 99; // fallback
  }
};

export const getSeasonalRules = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/api/pricing/rules`);
    return res.data;
  } catch (error) {
    console.error("Error fetching seasonal rules:", error.message);
    return [];
  }
};

export const addSeasonalRule = async (name, startDate, endDate, multiplier) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/api/pricing/rules`, {
      name,
      startDate,
      endDate,
      multiplier,
      active: true,
    });
    return res.data;
  } catch (error) {
    console.error("Error adding seasonal rule:", error.message);
    throw error;
  }
};

export const cancelBooking = async (userId, bookingId, reason) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/booking/cancel?userId=${userId}&bookingId=${bookingId}&reason=${reason}`);
    return res.data;
  } catch (error) {
    console.error("Error cancelling booking:", error.message);
    throw error;
  }
};

// ========== REVIEW & RATING APIs ==========

export const createReview = async (reviewData) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/api/reviews`, reviewData);
    return res.data;
  } catch (error) {
    console.error("Error creating review:", error.message);
    throw error;
  }
};

export const getReviews = async (entityId, entityType, sortBy = "newest") => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/reviews?entityId=${entityId}&entityType=${entityType}&sortBy=${sortBy}`
    );
    return res.data;
  } catch (error) {
    console.error("Error fetching reviews:", error.message);
    return [];
  }
};

export const getRatingSummary = async (entityId, entityType) => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/reviews/rating?entityId=${entityId}&entityType=${entityType}`
    );
    return res.data;
  } catch (error) {
    console.error("Error fetching rating summary:", error.message);
    return { averageRating: 0, totalReviews: 0, ratingDistribution: {} };
  }
};

export const addReviewReply = async (reviewId, replyData) => {
  try {
    const res = await axios.post(
      `${BACKEND_URL}/api/reviews/${reviewId}/reply`,
      replyData
    );
    return res.data;
  } catch (error) {
    console.error("Error adding reply:", error.message);
    throw error;
  }
};

export const voteHelpful = async (reviewId, userId) => {
  try {
    const res = await axios.post(
      `${BACKEND_URL}/api/reviews/${reviewId}/helpful?userId=${userId}`
    );
    return res.data;
  } catch (error) {
    console.error("Error voting helpful:", error.message);
    throw error;
  }
};

export const flagReview = async (reviewId, flagData) => {
  try {
    const res = await axios.post(
      `${BACKEND_URL}/api/reviews/${reviewId}/flag`,
      flagData
    );
    return res.data;
  } catch (error) {
    console.error("Error flagging review:", error.message);
    throw error;
  }
};

export const getFlaggedReviews = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/api/reviews/flagged`);
    return res.data;
  } catch (error) {
    console.error("Error fetching flagged reviews:", error.message);
    return [];
  }
};

export const moderateReview = async (reviewId, action) => {
  try {
    const res = await axios.post(
      `${BACKEND_URL}/api/reviews/${reviewId}/moderate?action=${action}`
    );
    return res.data;
  } catch (error) {
    console.error("Error moderating review:", error.message);
    throw error;
  }
};