import { useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

export default function LoginPage() {
  const { login, user } = useAuth();
  const [email, setEmail] = useState("admin@bhavani.farm");
  const [password, setPassword] = useState("Admin@2026");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
    } catch {
      setError("Invalid email or password. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const demoAccounts = [
    { role: "Admin", email: "admin@bhavani.farm", pwd: "Admin@2026", color: "green" },
    { role: "Manager", email: "ravi@bhavani.farm", pwd: "Manager@123", color: "blue" },
    { role: "Vet", email: "priya@bhavani.farm", pwd: "Vet@2026", color: "purple" },
    { role: "Data Entry", email: "suresh@bhavani.farm", pwd: "Entry@2026", color: "amber" },
  ];

  return (
    <div className="flex min-h-screen bg-[#f5f7f6]">
      {/* Left — hero panel */}
      <div className="hidden flex-col justify-between bg-[#143d2e] px-12 py-14 lg:flex lg:w-[440px] xl:w-[520px]">
        <div className="flex items-center gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-white/10">
            <Icon name="drop" size={22} className="text-[#72d4a8]" />
          </div>
          <div>
            <div className="text-lg font-bold text-white">DairyFlow</div>
            <div className="text-[10px] font-semibold uppercase tracking-[0.15em] text-white/40">Smart Farm Management</div>
          </div>
        </div>

        <div>
          <div className="mb-6 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-2 text-xs font-medium text-white/70">
            <span className="size-1.5 rounded-full bg-[#72d4a8]"></span> NABARD DEDS Scheme — AP
          </div>
          <h1 className="text-4xl font-bold leading-tight tracking-tight text-white">
            Bhavani Dairy<br />Farm, Srikakulam
          </h1>
          <p className="mt-4 text-[13px] leading-relaxed text-white/55">
            Smart management for 40 Murrah buffaloes. Real-time milk tracking, health monitoring, task coordination and IoT device integration.
          </p>

          <div className="mt-10 grid grid-cols-2 gap-4">
            {[
              { label: "40", sub: "Murrah buffaloes" },
              { label: "~490 L", sub: "Daily milk yield" },
              { label: "₹1.34 Cr", sub: "Project value" },
              { label: "₹44.7L", sub: "NABARD subsidy" },
            ].map((s) => (
              <div key={s.label} className="rounded-2xl bg-white/5 p-4">
                <div className="text-2xl font-bold text-white">{s.label}</div>
                <div className="mt-1 text-[10px] text-white/45">{s.sub}</div>
              </div>
            ))}
          </div>
        </div>

        <p className="text-[10px] text-white/25">© 2026 Bhavani Dairy Farm · Srikakulam, AP</p>
      </div>

      {/* Right — login form */}
      <div className="flex flex-1 flex-col items-center justify-center px-6 py-12">
        <div className="w-full max-w-[400px]">
          {/* Mobile logo */}
          <div className="mb-8 flex items-center gap-2 lg:hidden">
            <div className="flex size-9 items-center justify-center rounded-xl bg-[#1d7352]">
              <Icon name="drop" size={18} className="text-white" />
            </div>
            <span className="text-lg font-bold">DairyFlow</span>
          </div>

          <h2 className="text-2xl font-bold tracking-tight text-[#17221d]">Welcome back</h2>
          <p className="mt-1.5 text-sm text-[#7d8983]">Sign in to your farm management account</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-4">
            <div>
              <label className="mb-1.5 block text-xs font-semibold text-[#3d4d47]">Email address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full rounded-xl border border-[#dce1de] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#2d8b65] focus:ring-2 focus:ring-[#2d8b65]/10"
                placeholder="you@bhavani.farm"
              />
            </div>
            <div>
              <label className="mb-1.5 block text-xs font-semibold text-[#3d4d47]">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full rounded-xl border border-[#dce1de] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#2d8b65] focus:ring-2 focus:ring-[#2d8b65]/10"
                placeholder="••••••••"
              />
            </div>

            {error && (
              <div className="flex items-center gap-2 rounded-xl bg-[#fbe9e6] px-4 py-3 text-xs text-[#b25248]">
                <Icon name="alert" size={14} />
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#1d7352] py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#185f45] disabled:opacity-60"
            >
              {loading ? (
                <><Icon name="loader" size={16} className="animate-spin" /> Signing in...</>
              ) : (
                "Sign in to DairyFlow"
              )}
            </button>
          </form>

          {/* Demo accounts */}
          <div className="mt-8">
            <div className="mb-3 flex items-center gap-2">
              <div className="h-px flex-1 bg-[#e8edea]"></div>
              <span className="text-[10px] font-semibold text-[#a0aaa5]">DEMO ACCOUNTS</span>
              <div className="h-px flex-1 bg-[#e8edea]"></div>
            </div>
            <div className="grid grid-cols-2 gap-2">
              {demoAccounts.map((a) => (
                <button
                  key={a.role}
                  type="button"
                  onClick={() => { setEmail(a.email); setPassword(a.pwd); }}
                  className="rounded-xl border border-[#e4e9e6] bg-white p-3 text-left transition hover:border-[#bfcfc9] hover:bg-[#f8faf9]"
                >
                  <div className="text-[11px] font-bold text-[#17221d]">{a.role}</div>
                  <div className="mt-0.5 truncate text-[9px] text-[#8c9691]">{a.email}</div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
