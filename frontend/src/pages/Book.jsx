import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import api from "../api";

function Book() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [car, setCar] = useState(null);
  const [booked, setBooked] = useState([]);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const today = new Date().toLocaleDateString("en-CA");

  useEffect(() => {
    api
      .get(`/cars/${id}`)
      .then((res) => setCar(res.data))
      .catch(() => setError("Car not found"));
  }, [id]);

  useEffect(() => {
    api
      .get(`/bookings/car/${id}/booked`)
      .then((res) => setBooked(res.data))
      .catch(() => {});
  }, [id]);

  let days = 0;
  if (startDate && endDate && endDate >= startDate) {
    days = (new Date(endDate) - new Date(startDate)) / 86400000 + 1;
  }
  const total = car ? days * car.pricePerDay : 0;

  const handleBook = async () => {
    setError("");
    setLoading(true);
    try {
      await api.post("/bookings", {
        carId: Number(id),
        startDate,
        endDate,
      });
      navigate("/my-bookings");
    } catch (err) {
      const status = err.response?.status;
      if (status === 409) setError("Car is already booked for these dates");
      else if (status === 400) setError("Invalid dates");
      else if (status === 401 || status === 403) setError("Please login again");
      else setError("Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  if (!car) {
    return (
      <p className="max-w-3xl mx-auto px-4 py-8 text-gray-500">
        {error || "Loading..."}
      </p>
    );
  }

  const inputClass =
    "w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500";

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/" className="text-sm text-indigo-600 hover:underline">
        Back to cars
      </Link>

      <div className="grid gap-6 md:grid-cols-2 mt-4">
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          <div className="h-48 bg-gradient-to-br from-indigo-100 to-indigo-200 flex items-center justify-center text-7xl">
            🚗
          </div>
          <div className="p-6">
            <h2 className="text-2xl font-bold text-gray-800">
              {car.brand} {car.model}
            </h2>
            <p className="text-gray-500 mb-4">Model year {car.year}</p>
            <p className="text-indigo-600 text-xl font-bold">
              Rs {car.pricePerDay}
              <span className="text-sm text-gray-400 font-normal"> / day</span>
            </p>

            {booked.length > 0 && (
              <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 mt-5">
                <p className="text-sm font-semibold text-amber-800 mb-1">
                  Already booked
                </p>
                {booked.map((r, i) => (
                  <p key={i} className="text-sm text-amber-700">
                    {r.startDate} to {r.endDate}
                  </p>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">
            Choose your dates
          </h3>

          <label className="block text-sm font-medium text-gray-700 mb-1">
            Start date
          </label>
          <input
            type="date"
            min={today}
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className={`${inputClass} mb-4`}
          />

          <label className="block text-sm font-medium text-gray-700 mb-1">
            End date
          </label>
          <input
            type="date"
            min={startDate || today}
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className={`${inputClass} mb-4`}
          />

          {days > 0 && (
            <div className="bg-indigo-50 rounded-lg p-4 mb-4">
              <div className="flex justify-between text-sm text-gray-600">
                <span>
                  {days} day(s) x Rs {car.pricePerDay}
                </span>
              </div>
              <div className="flex justify-between mt-1">
                <span className="font-semibold text-gray-800">Total</span>
                <span className="font-bold text-indigo-600 text-lg">
                  Rs {total}
                </span>
              </div>
            </div>
          )}

          {error && (
            <p className="bg-red-50 text-red-600 text-sm rounded-lg px-3 py-2 mb-4">
              {error}
            </p>
          )}

          <button
            disabled={days === 0 || loading}
            onClick={handleBook}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2.5 rounded-lg text-sm disabled:opacity-50"
          >
            {loading ? "Booking..." : "Confirm booking"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Book;