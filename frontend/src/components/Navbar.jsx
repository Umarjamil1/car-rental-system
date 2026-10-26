import { Link, useLocation, useNavigate } from "react-router-dom";

function Navbar() {
  useLocation();
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role");

  const logout = () => {
    localStorage.clear();
    navigate("/login");
  };

  const linkClass = "text-gray-700 hover:text-indigo-600 font-medium";

  return (
    <nav className="bg-white shadow-sm">
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-6">
        <Link to="/" className="text-xl font-bold text-indigo-600">
          CarRental
        </Link>

        <Link to="/" className={linkClass}>Home</Link>
        {token && <Link to="/my-bookings" className={linkClass}>My Bookings</Link>}
        {role === "ADMIN" && <Link to="/admin" className={linkClass}>Admin</Link>}

        <div className="ml-auto flex items-center gap-4">
          {token ? (
            <>
              <span className="text-sm text-gray-500">{email}</span>
              <button
                onClick={logout}
                className="bg-red-500 hover:bg-red-600 text-white px-4 py-1.5 rounded-lg text-sm"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={linkClass}>Login</Link>
              <Link
                to="/register"
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-1.5 rounded-lg text-sm"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;