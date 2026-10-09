import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const navItems = [
  { label: "Dashboard", path: "/", icon: "grid" as const, roles: null },
  { label: "Herd", path: "/animals", icon: "cow" as const, roles: null },
  { label: "Milk Records", path: "/milk", icon: "milk" as const, roles: null },
  { label: "Health", path: "/health", icon: "heart" as const, roles: null },
  { label: "Quality", path: "/quality", icon: "shield" as const, roles: null },
  { label: "Feed", path: "/feed", icon: "box" as const, roles: null },
  { label: "Tasks", path: "/tasks", icon: "calendar" as const, roles: null },
  { label: "Expenses", path: "/expenses", icon: "activity" as const, roles: null },
  { label: "Repro", path: "/reproductive", icon: "heart" as const, roles: null },
  { label: "Calves", path: "/calves", icon: "cow" as const, roles: null },
  { label: "Vaccines", path: "/vaccinations", icon: "shield" as const, roles: null },
  { label: "Users", path: "/users", icon: "users" as const, roles: ["admin"] },
  { label: "Devices", path: "/devices", icon: "cpu" as const, roles: ["admin"] },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const visibleItems = navItems.filter(
    (item) => !item.roles || item.roles.includes(user?.role || "")
  );

  const today = new Date().toLocaleDateString("en-IN", {
    weekday: "long", day: "numeric", month: "long",
  }).toUpperCase();

  return (
    <div className="flex min-h-screen bg-[#f5f7f6] text-[#17221d]">
      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-[248px] flex-col border-r border-[#e6ebe8] bg-white transition-transform duration-200 lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Logo */}
        <div className="flex h-[72px] items-center gap-3 px-6">
          <div className="flex size-9 items-center justify-center rounded-xl bg-[#1f7a57] text-white">
            <Icon name="drop" size={18} />
          </div>
          <div>
            <div className="text-[16px] font-bold tracking-[-0.03em]">DairyFlow</div>
            <div className="text-[9px] font-semibold uppercase tracking-[0.18em] text-[#9aa7a1]">Smart farm</div>
          </div>
          <button className="ml-auto lg:hidden" onClick={() => setSidebarOpen(false)}>
            <Icon name="close" size={18} />
          </button>
        </div>

        {/* Farm card */}
        <div className="mx-4 rounded-xl border border-[#e8ecea] bg-[#fafcfb] p-3">
          <div className="flex items-center gap-2.5">
            <div className="flex size-8 items-center justify-center rounded-lg bg-[#dff0e8] text-[11px] font-bold text-[#277858]">BD</div>
            <div className="min-w-0 flex-1">
              <div className="truncate text-[12px] font-semibold">Bhavani Dairy Farm</div>
              <div className="text-[10px] text-[#87938d]">Srikakulam, AP</div>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="mt-5 flex-1 space-y-0.5 px-3">
          <div className="mb-2 px-3 text-[9px] font-bold uppercase tracking-[0.16em] text-[#a0aaa5]">Navigation</div>
          {visibleItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === "/"}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-[13px] font-medium transition ${
                  isActive
                    ? "bg-[#eaf4ef] text-[#176b4b]"
                    : "text-[#67736d] hover:bg-[#f5f7f6] hover:text-[#26332d]"
                }`
              }
            >
              <Icon name={item.icon} size={17} />
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Status card */}
        <div className="mx-4 mb-3 rounded-2xl bg-[#183e31] p-4 text-white">
          <div className="mb-2 flex size-7 items-center justify-center rounded-lg bg-white/10">
            <Icon name="activity" size={15} />
          </div>
          <div className="text-[12px] font-semibold">NABARD DEDS Active</div>
          <div className="mt-0.5 text-[10px] text-white/55">₹44.7L subsidy approved</div>
        </div>

        {/* User */}
        <div className="flex items-center gap-3 border-t border-[#edf0ee] px-5 py-4">
          <div className="flex size-8 items-center justify-center rounded-full bg-[#dff0e8] text-[11px] font-bold text-[#1f7a57]">
            {user?.avatar_initials}
          </div>
          <div className="flex-1 min-w-0">
            <div className="truncate text-[12px] font-semibold">{user?.name}</div>
            <div className="text-[10px] text-[#97a19c] capitalize">{user?.role?.replace("_", " ")}</div>
          </div>
          <button onClick={handleLogout} title="Logout" className="text-[#97a19c] hover:text-[#df675a]">
            <Icon name="logout" size={16} />
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 lg:ml-[248px]">
        {/* Header */}
        <header className="sticky top-0 z-30 flex h-[64px] items-center border-b border-[#e3e8e5] bg-white/90 px-4 backdrop-blur-xl sm:px-6">
          <button className="mr-3 lg:hidden" onClick={() => setSidebarOpen(true)}>
            <Icon name="menu" size={22} />
          </button>
          <div className="hidden text-[11px] font-semibold text-[#28815f] sm:block">{today}</div>
          <div className="ml-auto flex items-center gap-2">
            <div className="hidden items-center gap-2 rounded-full border border-[#e5e9e7] px-3 py-1.5 text-[10px] font-medium text-[#647069] sm:flex">
              <span className="size-1.5 rounded-full bg-[#30a46c]"></span> Live
            </div>
          </div>
        </header>

        {/* Page content */}
        <Outlet />
      </main>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <button
          className="fixed inset-0 z-30 bg-black/20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
          aria-label="Close menu"
        />
      )}
    </div>
  );
}
