import { useEffect, useState } from "react";
import { milkApi, animalsApi, type MilkRecord, type Animal } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

interface DailySummary { date: string; morning: number; evening: number; total: number; }

export default function MilkPage() {
  const { user } = useAuth();
  const [daily, setDaily] = useState<DailySummary[]>([]);
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [todaySummary, setTodaySummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    animal_id: "",
    record_date: new Date().toISOString().split("T")[0],
    session: "morning",
    quantity_litres: "",
  });

  const canWrite = ["admin", "manager", "data_entry"].includes(user?.role || "");

  useEffect(() => {
    const load = async () => {
      const [dailyRes, animalsRes, todayRes] = await Promise.all([
        milkApi.daily({ days: 14 }),
        animalsApi.list({ status: "lactating" }),
        milkApi.today(),
      ]);
      setDaily(dailyRes.data);
      setAnimals(animalsRes.data);
      setTodaySummary(todayRes.data);
      setLoading(false);
    };
    load();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await milkApi.create({
        animal_id: Number(form.animal_id),
        record_date: form.record_date,
        session: form.session,
        quantity_litres: Number(form.quantity_litres),
      });
      setShowForm(false);
      // Reload
      const [dailyRes, todayRes] = await Promise.all([milkApi.daily({ days: 14 }), milkApi.today()]);
      setDaily(dailyRes.data);
      setTodaySummary(todayRes.data);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  const todayRow = daily.find((d) => d.date === new Date().toISOString().split("T")[0]);

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Milk Records</h1>
          <p className="mt-1 text-sm text-[#7d8983]">Daily production tracking</p>
        </div>
        {canWrite && (
          <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
            <Icon name="plus" size={15} /> Log entry
          </button>
        )}
      </div>

      {/* Today summary */}
      <div className="mb-5 grid grid-cols-3 gap-3">
        {[
          { label: "Morning", value: todayRow?.morning?.toFixed(1) ?? "—", unit: "L", color: "text-[#2d8b65]" },
          { label: "Evening", value: todayRow?.evening?.toFixed(1) ?? "—", unit: "L", color: "text-[#4a90d9]" },
          { label: "Total today", value: todayRow?.total?.toFixed(1) ?? "—", unit: "L", color: "text-[#17221d]" },
        ].map((s) => (
          <div key={s.label} className="rounded-2xl border border-[#e4e9e6] bg-white p-4 text-center">
            <div className="text-[10px] font-semibold text-[#7e8983]">{s.label}</div>
            <div className={`mt-1 text-2xl font-bold tracking-tight ${s.color}`}>{s.value}</div>
            <div className="text-[10px] text-[#9aa7a1]">{s.unit}</div>
          </div>
        ))}
      </div>

      {/* 14-day table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="px-5 py-4 border-b border-[#f0f3f1]">
          <h2 className="section-title">14-Day Summary</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[#f0f3f1] bg-[#fafcfb]">
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">DATE</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">MORNING (L)</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">EVENING (L)</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">TOTAL (L)</th>
              </tr>
            </thead>
            <tbody>
              {daily.slice().reverse().map((row, i) => (
                <tr key={row.date} className={`border-b border-[#f5f7f6] ${i % 2 === 0 ? "" : "bg-[#fafcfb]"}`}>
                  <td className="px-5 py-3 text-[12px] font-medium">
                    {new Date(row.date).toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" })}
                    {row.date === new Date().toISOString().split("T")[0] && (
                      <span className="ml-2 rounded-full bg-[#e6f4ec] px-2 py-0.5 text-[9px] font-semibold text-[#2d8b65]">Today</span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-right text-[12px]">{row.morning.toFixed(1)}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{row.evening.toFixed(1)}</td>
                  <td className="px-5 py-3 text-right text-[13px] font-bold">{row.total.toFixed(1)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add milk modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[400px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Log Milk Entry</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Animal *</label>
                <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.animal_id} onChange={(e) => setForm({ ...form, animal_id: e.target.value })}>
                  <option value="">Select animal</option>
                  {animals.map((a) => <option key={a.id} value={a.id}>{a.tag_number} — {a.name || "unnamed"}</option>)}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date</label>
                  <input type="date" required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.record_date} onChange={(e) => setForm({ ...form, record_date: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Session</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.session} onChange={(e) => setForm({ ...form, session: e.target.value })}>
                    <option value="morning">Morning</option>
                    <option value="evening">Evening</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Quantity (litres) *</label>
                <input type="number" step="0.1" required min="0" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.quantity_litres} onChange={(e) => setForm({ ...form, quantity_litres: e.target.value })} placeholder="7.5" />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Log Entry"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
