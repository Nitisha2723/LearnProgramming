import { useEffect, useState } from "react";
import { qualityApi, type QualityCheck } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const GRADE_COLORS: Record<string, string> = {
  A: "bg-[#e6f4ec] text-[#2d8b65]",
  B: "bg-[#fff8e7] text-[#9a6a1f]",
  C: "bg-[#fce9e7] text-[#b25248]",
};

export default function QualityPage() {
  const { user } = useAuth();
  const [checks, setChecks] = useState<QualityCheck[]>([]);
  const [latest, setLatest] = useState<QualityCheck | null>(null);
  const [averages, setAverages] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    check_date: new Date().toISOString().split("T")[0],
    fat_percent: "",
    snf_percent: "",
    temperature_c: "",
    adulteration: false,
    notes: "",
  });

  const canCreate = ["admin", "manager", "quality"].includes(user?.role || "");

  useEffect(() => {
    const load = async () => {
      const [listRes, latestRes, avgRes] = await Promise.all([
        qualityApi.list(),
        qualityApi.latest(),
        qualityApi.averages(),
      ]);
      setChecks(listRes.data);
      setLatest(latestRes.data);
      setAverages(avgRes.data);
      setLoading(false);
    };
    load();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await qualityApi.create({
        check_date: form.check_date,
        fat_percent: Number(form.fat_percent),
        snf_percent: Number(form.snf_percent),
        temperature_c: form.temperature_c ? Number(form.temperature_c) : undefined,
        adulteration: form.adulteration,
        notes: form.notes || undefined,
      });
      setShowForm(false);
      const [listRes, latestRes, avgRes] = await Promise.all([qualityApi.list(), qualityApi.latest(), qualityApi.averages()]);
      setChecks(listRes.data);
      setLatest(latestRes.data);
      setAverages(avgRes.data);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Quality Checks</h1>
          <p className="mt-1 text-sm text-[#7d8983]">Milk composition & adulteration testing</p>
        </div>
        {canCreate && (
          <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
            <Icon name="plus" size={15} /> New test
          </button>
        )}
      </div>

      {/* Latest + averages */}
      <div className="mb-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { label: "Latest grade", value: latest?.grade ?? "—", note: latest ? new Date(latest.check_date).toLocaleDateString("en-IN", { day: "numeric", month: "short" }) : "No data", badge: true },
          { label: "Fat %", value: latest?.fat_percent?.toFixed(2) ?? "—", note: `Avg 30d: ${averages?.avg_fat?.toFixed(2) ?? "—"}%` },
          { label: "SNF %", value: latest?.snf_percent?.toFixed(2) ?? "—", note: `Avg 30d: ${averages?.avg_snf?.toFixed(2) ?? "—"}%` },
          { label: "Temperature", value: latest?.temperature_c ? `${latest.temperature_c.toFixed(1)}°C` : "—", note: "At time of testing" },
        ].map((c) => (
          <div key={c.label} className="rounded-2xl border border-[#e4e9e6] bg-white p-4">
            <div className="text-[10px] font-semibold text-[#7e8983]">{c.label}</div>
            <div className="mt-2 flex items-baseline gap-2">
              {c.badge && c.value !== "—" ? (
                <span className={`rounded-full px-3 py-1 text-xl font-bold ${GRADE_COLORS[c.value as string] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>{c.value}</span>
              ) : (
                <span className="text-2xl font-bold tracking-tight">{c.value}</span>
              )}
            </div>
            <div className="mt-1 text-[10px] text-[#9aa7a1]">{c.note}</div>
          </div>
        ))}
      </div>

      {/* Checks table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="px-5 py-4 border-b border-[#f0f3f1]">
          <h2 className="section-title">Test history</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[#f0f3f1] bg-[#fafcfb]">
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">DATE</th>
                <th className="px-5 py-3 text-center text-[10px] font-semibold text-[#7e8983]">GRADE</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">FAT %</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">SNF %</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">TEMP °C</th>
                <th className="px-5 py-3 text-center text-[10px] font-semibold text-[#7e8983]">ADULTERATION</th>
              </tr>
            </thead>
            <tbody>
              {checks.map((c, i) => (
                <tr key={c.id} className={`border-b border-[#f5f7f6] ${i % 2 === 0 ? "" : "bg-[#fafcfb]"}`}>
                  <td className="px-5 py-3 text-[12px]">
                    {new Date(c.check_date).toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" })}
                  </td>
                  <td className="px-5 py-3 text-center">
                    <span className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${GRADE_COLORS[c.grade || ""] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>{c.grade || "—"}</span>
                  </td>
                  <td className="px-5 py-3 text-right text-[12px]">{c.fat_percent?.toFixed(2)}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{c.snf_percent?.toFixed(2)}</td>
                  <td className="px-5 py-3 text-right text-[12px]">{c.temperature_c?.toFixed(1) ?? "—"}</td>
                  <td className="px-5 py-3 text-center">
                    <span className={`rounded-full px-2 py-0.5 text-[9px] font-semibold ${c.adulteration ? "bg-[#fce9e7] text-[#b25248]" : "bg-[#e6f4ec] text-[#2d8b65]"}`}>
                      {c.adulteration ? "Detected" : "Clean"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* New quality test modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">New Quality Test</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Test Date</label>
                <input type="date" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.check_date} onChange={(e) => setForm({ ...form, check_date: e.target.value })} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Fat % *</label>
                  <input required type="number" step="0.01" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.fat_percent} onChange={(e) => setForm({ ...form, fat_percent: e.target.value })} placeholder="6.5" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">SNF % *</label>
                  <input required type="number" step="0.01" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.snf_percent} onChange={(e) => setForm({ ...form, snf_percent: e.target.value })} placeholder="9.2" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Temp (°C)</label>
                  <input type="number" step="0.1" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.temperature_c} onChange={(e) => setForm({ ...form, temperature_c: e.target.value })} placeholder="4.0" />
                </div>
              </div>
              <label className="flex cursor-pointer items-center gap-3 rounded-xl border border-[#dce1de] px-4 py-3">
                <input type="checkbox" checked={form.adulteration} onChange={(e) => setForm({ ...form, adulteration: e.target.checked })} className="size-4 accent-[#1d7352]" />
                <span className="text-sm font-medium">Adulteration detected</span>
              </label>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Optional notes..." />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Save Test"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
