import { useEffect, useState } from "react";
import { feedApi } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

interface FeedLog {
  id: number;
  log_date: string;
  session: string;
  green_fodder_kg: number;
  dry_fodder_kg: number;
  concentrate_kg: number;
  mineral_mix_kg: number;
  total_kg: number;
  notes?: string;
}

export default function FeedPage() {
  const { user } = useAuth();
  const [logs, setLogs] = useState<FeedLog[]>([]);
  const [today, setToday] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    log_date: new Date().toISOString().split("T")[0],
    session: "morning",
    green_fodder_kg: "",
    dry_fodder_kg: "",
    concentrate_kg: "",
    mineral_mix_kg: "",
    notes: "",
  });

  const canWrite = ["admin", "manager", "data_entry"].includes(user?.role || "");

  useEffect(() => {
    const load = async () => {
      const [listRes, todayRes] = await Promise.all([feedApi.list(), feedApi.today()]);
      setLogs(listRes.data);
      setToday(todayRes.data);
      setLoading(false);
    };
    load();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await feedApi.create({
        log_date: form.log_date,
        session: form.session,
        green_fodder_kg: Number(form.green_fodder_kg) || 0,
        dry_fodder_kg: Number(form.dry_fodder_kg) || 0,
        concentrate_kg: Number(form.concentrate_kg) || 0,
        mineral_mix_kg: Number(form.mineral_mix_kg) || 0,
        notes: form.notes || undefined,
      });
      setShowForm(false);
      setForm({ log_date: new Date().toISOString().split("T")[0], session: "morning", green_fodder_kg: "", dry_fodder_kg: "", concentrate_kg: "", mineral_mix_kg: "", notes: "" });
      const [listRes, todayRes] = await Promise.all([feedApi.list(), feedApi.today()]);
      setLogs(listRes.data);
      setToday(todayRes.data);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  const todayTotal = today?.total_kg ?? 0;

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Feed Management</h1>
          <p className="mt-1 text-sm text-[#7d8983]">Daily feeding records for 40 buffaloes</p>
        </div>
        {canWrite && (
          <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
            <Icon name="plus" size={15} /> Log feed
          </button>
        )}
      </div>

      {/* Today summary */}
      {today && (
        <div className="mb-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {[
            { label: "Green fodder", value: `${today.green_fodder_kg?.toFixed(0) ?? 0} kg` },
            { label: "Dry fodder", value: `${today.dry_fodder_kg?.toFixed(0) ?? 0} kg` },
            { label: "Concentrate", value: `${today.concentrate_kg?.toFixed(0) ?? 0} kg` },
            { label: "Total today", value: `${todayTotal.toFixed(0)} kg`, bold: true },
          ].map((c) => (
            <div key={c.label} className="rounded-2xl border border-[#e4e9e6] bg-white p-4">
              <div className="text-[10px] font-semibold text-[#7e8983]">{c.label}</div>
              <div className={`mt-2 text-xl tracking-tight ${c.bold ? "font-bold" : "font-semibold"}`}>{c.value}</div>
            </div>
          ))}
        </div>
      )}

      {/* History table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="px-5 py-4 border-b border-[#f0f3f1]">
          <h2 className="section-title">Feed log history</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[#f0f3f1] bg-[#fafcfb]">
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">DATE</th>
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">SESSION</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">GREEN (kg)</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">DRY (kg)</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">CONCENTRATE</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">TOTAL</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l, i) => (
                <tr key={l.id} className={`border-b border-[#f5f7f6] ${i % 2 === 0 ? "" : "bg-[#fafcfb]"}`}>
                  <td className="px-5 py-3 text-[12px] font-medium">
                    {new Date(l.log_date).toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" })}
                  </td>
                  <td className="px-5 py-3 text-[12px] capitalize">{l.session}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{l.green_fodder_kg?.toFixed(0)}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{l.dry_fodder_kg?.toFixed(0)}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{l.concentrate_kg?.toFixed(0)}</td>
                  <td className="px-5 py-3 text-right text-[13px] font-bold">{l.total_kg?.toFixed(0)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Log feed modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Log Feed</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date</label>
                  <input type="date" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.log_date} onChange={(e) => setForm({ ...form, log_date: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Session</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.session} onChange={(e) => setForm({ ...form, session: e.target.value })}>
                    <option value="morning">Morning</option>
                    <option value="evening">Evening</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Green fodder (kg)</label>
                  <input type="number" step="0.1" min="0" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.green_fodder_kg} onChange={(e) => setForm({ ...form, green_fodder_kg: e.target.value })} placeholder="200" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Dry fodder (kg)</label>
                  <input type="number" step="0.1" min="0" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.dry_fodder_kg} onChange={(e) => setForm({ ...form, dry_fodder_kg: e.target.value })} placeholder="40" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Concentrate (kg)</label>
                  <input type="number" step="0.1" min="0" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.concentrate_kg} onChange={(e) => setForm({ ...form, concentrate_kg: e.target.value })} placeholder="60" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Mineral mix (kg)</label>
                  <input type="number" step="0.01" min="0" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.mineral_mix_kg} onChange={(e) => setForm({ ...form, mineral_mix_kg: e.target.value })} placeholder="2" />
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Optional notes..." />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Log Feed"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
