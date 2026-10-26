import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

const emptyForm = { brand: "", model: "", year: "", pricePerDay: "" };

function Admin() {
  const navigate = useNavigate();
  const [cars, setCars] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  const isAdmin = localStorage.getItem("role") === "ADMIN";
  const today = new Date().toLocaleDateString("en-CA");

  const load = () => {
    api.get("/cars").then((res) => setCars(res.data));
    api
      .get("/bookings")
      .then((res) => setBookings(res.data))
      .catch(() => {});
  };

  useEffect(() => {
    if (!isAdmin) {
      navigate("/");
      return;
    }
    load();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const addCar = async () => {
    setError("");
    try {
      await api.post("/cars", {
        brand: form.brand,
        model: form.model,
        year: Number(form.year),
        pricePerDay: Number(form.pricePerDay),
        available: true,
      });
      setForm(emptyForm);
      load();
    } catch {
      setError("Could not add car. Login again as admin.");
    }
  };

  const toggleAvailable = async (car) => {
    try {
      await api.patch(`/cars/${car.id}`, { available: !car.available });
      load();
    } catch {
      setError("Could not update car");
    }
  };

  const deleteCar = async (id) => {
    try {
      await api.delete(`/cars/${id}`);
      load();
    } catch {
      setError("Could not delete. This car may have bookings.");
    }
  };

  const isBookedNow = (carId) =>
    bookings.some(
      (b) =>
        b.carId === carId &&
        b.status === "CONFIRMED" &&
        b.startDate <= today &&
        b.endDate >= today
    );

  const cancelBooking = async (id) => {
    try {
      await api.patch(`/bookings/${id}/cancel`);
      load();
    } catch {
      setError("Could not cancel booking");
    }
  };

  const inputClass =
    "border border-gray-300 rounded-lg px-3 py-2 text-sm w-full";
  const th = "text-left text-xs font-semibold text-gray-500 uppercase px-4 py-3";
  const td = "px-4 py-3 text-sm text-gray-700";

  const pill = (good, yes, no) => (
    <span
      className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
        good ? "bg-green-100 text-green-700" : "bg-gray-200 text-gray-600"
      }`}
    >
      {good ? yes : no}
    </span>
  );

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Admin Dashboard</h1>

      <div className="grid grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <p className="text-sm text-gray-500">Cars</p>
          <p className="text-2xl font-bold text-indigo-600">{cars.length}</p>
        </div>
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <p className="text-sm text-gray-500">Bookings</p>
          <p className="text-2xl font-bold text-indigo-600">{bookings.length}</p>
        </div>
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <p className="text-sm text-gray-500">Active bookings</p>
          <p className="text-2xl font-bold text-indigo-600">
            {bookings.filter((b) => b.status === "CONFIRMED").length}
          </p>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm p-5 mb-8">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Add a car</h2>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <input name="brand" placeholder="Brand" value={form.brand} onChange={handleChange} className={inputClass} />
          <input name="model" placeholder="Model" value={form.model} onChange={handleChange} className={inputClass} />
          <input name="year" type="number" placeholder="Year" value={form.year} onChange={handleChange} className={inputClass} />
          <input name="pricePerDay" type="number" placeholder="Price per day" value={form.pricePerDay} onChange={handleChange} className={inputClass} />
          <button
            onClick={addCar}
            className="bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium py-2"
          >
            Add car
          </button>
        </div>
      </div>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      <div className="bg-white rounded-2xl shadow-sm mb-8 overflow-x-auto">
        <h2 className="text-lg font-semibold text-gray-800 px-4 pt-5">Cars</h2>
        <table className="w-full mt-3">
          <thead className="bg-gray-50">
            <tr>
              <th className={th}>ID</th>
              <th className={th}>Car</th>
              <th className={th}>Year</th>
              <th className={th}>Price</th>
              <th className={th}>Listed</th>
              <th className={th}>Today</th>
              <th className={th}>Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {cars.map((car) => (
              <tr key={car.id}>
                <td className={td}>{car.id}</td>
                <td className={`${td} font-medium`}>{car.brand} {car.model}</td>
                <td className={td}>{car.year}</td>
                <td className={td}>Rs {car.pricePerDay}</td>
                <td className={td}>{pill(car.available, "Yes", "No")}</td>
                <td className={td}>
                  <span
                    className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
                      isBookedNow(car.id)
                        ? "bg-amber-100 text-amber-700"
                        : "bg-green-100 text-green-700"
                    }`}
                  >
                    {isBookedNow(car.id) ? "Booked" : "Free"}
                  </span>
                </td>
                <td className={td}>
                  <div className="flex gap-2">
                    <button
                      onClick={() => toggleAvailable(car)}
                      className="border border-gray-300 hover:bg-gray-50 px-3 py-1 rounded-lg text-xs"
                    >
                      {car.available ? "Unlist" : "List"}
                    </button>
                    <button
                      onClick={() => deleteCar(car.id)}
                      className="border border-red-300 text-red-600 hover:bg-red-50 px-3 py-1 rounded-lg text-xs"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="bg-white rounded-2xl shadow-sm overflow-x-auto">
        <h2 className="text-lg font-semibold text-gray-800 px-4 pt-5">All bookings</h2>
        <table className="w-full mt-3">
          <thead className="bg-gray-50">
            <tr>
              <th className={th}>ID</th>
              <th className={th}>Car</th>
              <th className={th}>Customer</th>
              <th className={th}>From</th>
              <th className={th}>To</th>
              <th className={th}>Total</th>
              <th className={th}>Status</th>
              <th className={th}>Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {bookings.map((b) => (
              <tr key={b.id}>
                <td className={td}>{b.id}</td>
                <td className={`${td} font-medium`}>{b.car}</td>
                <td className={td}>{b.userEmail}</td>
                <td className={td}>{b.startDate}</td>
                <td className={td}>{b.endDate}</td>
                <td className={td}>Rs {b.totalPrice}</td>
                <td className={td}>
                  <span
                    className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
                      b.status === "CONFIRMED"
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {b.status}
                  </span>
                </td>
                <td className={td}>
                  {b.status === "CONFIRMED" && (
                    <button
                      onClick={() => cancelBooking(b.id)}
                      className="border border-red-300 text-red-600 hover:bg-red-50 px-3 py-1 rounded-lg text-xs"
                    >
                      Cancel
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Admin;