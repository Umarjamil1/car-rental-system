import { useEffect, useState } from "react";
import api from "../api";

function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState("");

  const load = () => {
    api
      .get("/bookings/my")
      .then((res) => setBookings(res.data))
      .catch(() => setError("Could not load bookings. Please login."));
  };

  useEffect(load, []);

  const cancel = async (id) => {
    try {
      await api.patch(`/bookings/${id}/cancel`);
      load();
    } catch {
      setError("Could not cancel booking");
    }
  };

  const badge = (status) =>
    status === "CONFIRMED"
      ? "bg-green-100 text-green-700"
      : "bg-red-100 text-red-700";

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">My Bookings</h1>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      {bookings.length === 0 && !error && (
        <div className="bg-white rounded-2xl shadow-sm p-10 text-center text-gray-500">
          You have no bookings yet.
        </div>
      )}

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {bookings.map((b) => (
          <div key={b.id} className="bg-white rounded-2xl shadow-sm p-5">
            <div className="flex items-start justify-between mb-3">
              <h3 className="text-lg font-semibold text-gray-800">{b.car}</h3>
              <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${badge(b.status)}`}>
                {b.status}
              </span>
            </div>

            <p className="text-sm text-gray-500 mb-1">
              {b.startDate} to {b.endDate}
            </p>
            <p className="text-indigo-600 font-bold mb-4">Rs {b.totalPrice}</p>

            {b.status === "CONFIRMED" && (
              <button
                onClick={() => cancel(b.id)}
                className="w-full border border-red-300 text-red-600 hover:bg-red-50 py-1.5 rounded-lg text-sm"
              >
                Cancel booking
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default MyBookings;