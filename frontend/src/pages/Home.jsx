import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

function Home() {
  const [cars, setCars] = useState([]);
  const [error, setError] = useState("");
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [filtered, setFiltered] = useState(false);
  const navigate = useNavigate();

  const today = new Date().toLocaleDateString("en-CA");

  const loadAll = () => {
    setFiltered(false);
    api
      .get("/cars")
      .then((res) => setCars(res.data))
      .catch(() => setError("Could not load cars"));
  };

  useEffect(loadAll, []);

  const checkAvailability = () => {
    setError("");
    api
      .get("/cars/available", { params: { start, end } })
      .then((res) => {
        setCars(res.data);
        setFiltered(true);
      })
      .catch(() => setError("Could not check availability"));
  };

  const handleBook = (id) => {
    if (!localStorage.getItem("token")) navigate("/login");
    else navigate(`/book/${id}`);
  };

  const inputClass = "border border-gray-300 rounded-lg px-3 py-2 text-sm";

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="bg-indigo-600 text-white rounded-2xl p-8 mb-8">
        <h1 className="text-3xl font-bold mb-2">Find your perfect ride</h1>
        <p className="mb-6 text-indigo-100">Pick your dates and see which cars are free.</p>

        <div className="flex flex-wrap items-end gap-3 text-gray-800">
          <div>
            <label className="block text-xs text-indigo-100 mb-1">From</label>
            <input type="date" min={today} value={start}
              onChange={(e) => setStart(e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className="block text-xs text-indigo-100 mb-1">To</label>
            <input type="date" min={start || today} value={end}
              onChange={(e) => setEnd(e.target.value)} className={inputClass} />
          </div>
          <button
            disabled={!start || !end || end < start}
            onClick={checkAvailability}
            className="bg-white text-indigo-600 font-semibold px-5 py-2 rounded-lg text-sm hover:bg-indigo-50 disabled:opacity-50"
          >
            Check availability
          </button>
          {filtered && (
            <button onClick={loadAll} className="text-white underline text-sm pb-2">
              Show all
            </button>
          )}
        </div>
      </div>

      {filtered && (
        <p className="text-gray-600 mb-4">
          Cars free from {start} to {end}: <b>{cars.length}</b>
        </p>
      )}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {cars.map((car) => (
          <div key={car.id} className="bg-white rounded-2xl shadow-sm hover:shadow-md transition overflow-hidden">
            <div className="h-36 bg-gradient-to-br from-indigo-100 to-indigo-200 flex items-center justify-center text-5xl">
              🚗
            </div>
            <div className="p-5">
              <h3 className="text-lg font-semibold text-gray-800">
                {car.brand} {car.model}
              </h3>
              <p className="text-sm text-gray-500 mb-3">Model year {car.year}</p>
              <div className="flex items-center justify-between">
                <p className="text-indigo-600 font-bold">
                  Rs {car.pricePerDay}
                  <span className="text-xs text-gray-400 font-normal"> / day</span>
                </p>
                <button
                  disabled={!car.available}
                  onClick={() => handleBook(car.id)}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-1.5 rounded-lg text-sm disabled:bg-gray-300"
                >
                  Book
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Home;