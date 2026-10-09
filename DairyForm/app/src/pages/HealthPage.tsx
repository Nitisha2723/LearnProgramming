import { useEffect, useState } from "react";
import { healthApi, animalsApi, type HealthRecord, type Animal } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const SEVERITY_COLORS: Record<string, string> = {
  normal: "bg-[#e6f4ec] text-[#2d8b65]",
  watch: "bg-[#fff8e7] text-[#9a6a1f]",
  treatment: "bg-[#fce9e7] text-[#b25248]",
  critical: "bg-[#fce9e7] text-[#991b1b]",
};

export default function HealthPage() {
  const { user } = useAuth();
  const [records, setRecords] = useState<HealthRecord[]>([]);
  const [alerts, setAlerts] = useState<any[]>([]);
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [resolvingId, setResolvingId] = useState<number | null>(null);
  const [form, setForm] = useState({
    animal_id: "",
    condition: "",
    symptoms: "",
    severity: "watch",
    treatment: "",
    vet_name: "",
  });

  const canCreate = ["admin", "manager", "vet"].includes(user?.role || "");

  const animalMap = Object.fromEntries(animals.map((a) => [a.id, a]));

  const loadData = async () => {
    try {
      const [recRes, alertRes, animRes] = await Promise.all([
        healthApi.list(),
        healthApi.alerts(),
        animalsApi.list(),
      ]);
      setRecords(recRes.data);
      setAlerts(alertRes.data);
      setAnimals(animRes.data);
    } catch (err) {
      console.error("Failed to load health data", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await healthApi.create({
        animal_id: Number(form.animal_id),
        condition: form.condition,
        symptoms: form.symptoms || undefined,
        severity: form.severity,
        treatment: form.treatment || undefined,
        vet_name: form.vet_name || undefined,
      });
      setShowForm(false);
      setForm({ animal_id: "", condition: "", symptoms: "", severity: "watch", treatment: "", vet_name: "" });
      await loadData();
    } catch (err) {
      console.error("Failed to save health record", err);
    } finally {
      setSaving(false);
    }
  };

  const handleResolve = async (id: number) => {
    setResolvingId(id);
    try {
      await healthApi.update(id, { resolved: true });
      await loadData();
    } catch (err) {
      console.error("Failed to resolve record", err);
    } finally {
      setResolvingId(null);
    }
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  const open = records.filter((r) => !r.resolved);
  const resolved = records.filter((r) => r.resolved);

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Health Records</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{open.length} open · {resolved.length} resolved</p>
        </div>
        {canCreate && (
          <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
            <Icon name="plus" size={15} /> New record
          </button>
        )}
      </div>

      {/* Alerts */}
      {alerts.length > 0 && (
        <div className="mb-5 space-y-2">
          {alerts.map((a: any, i: number) => (
            <div key={i} className={`flex items-center gap-3 rounded-xl px-4 py-3 text-[12px] font-medium ${a.severity === "critical" || a.severity === "treatment" ? "bg-[#fbe9e6] text-[#b25248]" : "bg-[#fff8e7] text-[#9a6a1f]"}`}>
              <Icon name="alert" size={15} />
              <span>{a.animal_tag} — {a.condition}: {a.message}</span>
            </div>
          ))}
        </div>
      )}

      {/* Open records */}
      {open.length > 0 && (
        <div className="mb-5 rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
          <div className="border-b border-[#f0f3f1] px-5 py-4">
            <h2 className="section-title">Active cases</h2>
          </div>
          <div className="divide-y divide-[#f5f7f6]">
            {open.map((r) => {
              const animal = animalMap[r.animal_id];
              return (
                <div key={r.id} className="flex items-start gap-4 px-5 py-4">
                  <span className={`mt-0.5 rounded-full px-2 py-0.5 text-[10px] font-semibold capitalize ${SEVERITY_COLORS[r.severity] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>{r.severity}</span>
                  <div className="flex-1 min-w-0">
                    <div className="text-[13px] font-semibold">{r.condition}</div>
                    <div className="mt-0.5 text-[11px] text-[#6b7c75]">
                      {animal ? `${animal.tag_number}${animal.name ? ` — ${animal.name}` : ""}` : `Animal #${r.animal_id}`}
                      {r.symptoms ? ` · ${r.symptoms}` : ""}
                    </div>
                    {r.treatment && <div className="mt-1 text-[11px] text-[#3d4d47]">Tx: {r.treatment}</div>}
                    {r.vet_name && <div className="mt-0.5 text-[11px] text-[#3d4d47]">Vet: {r.vet_name}</div>}
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <div className="text-[10px] text-[#9aa7a1]">
                      {new Date(r.record_date).toLocaleDateString("en-IN", { day: "numeric", month: "short" })}
                    </div>
                    {canCreate && (
                      <button
                        onClick={() => handleResolve(r.id)}
                        disabled={resolvingId === r.id}
                        className="rounded-lg bg-[#e6f4ec] px-2.5 py-1 text-[10px] font-semibold text-[#1d7352] hover:bg-[#d0ecdc] disabled:opacity-60"
                      >
                        {resolvingId === r.id ? "..." : "Mark Resolved"}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Resolved records */}
      {resolved.length > 0 && (
        <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
          <div className="border-b border-[#f0f3f1] px-5 py-4">
            <h2 className="section-title text-[#8c9691]">Resolved cases</h2>
          </div>
          <div className="divide-y divide-[#f5f7f6]">
            {resolved.map((r) => {
              const animal = animalMap[r.animal_id];
              return (
                <div key={r.id} className="flex items-start gap-4 px-5 py-4 opacity-60">
                  <span className="mt-0.5 rounded-full bg-[#e6f4ec] px-2 py-0.5 text-[10px] font-semibold text-[#2d8b65]">Resolved</span>
                  <div className="flex-1 min-w-0">
                    <div className="text-[13px] font-semibold">{r.condition}</div>
                    <div className="mt-0.5 text-[11px] text-[#6b7c75]">
                      {animal ? `${animal.tag_number}${animal.name ? ` — ${animal.name}` : ""}` : `Animal #${r.animal_id}`}
                    </div>
                  </div>
                  <div className="text-[10px] text-[#9aa7a1]">
                    {new Date(r.record_date).toLocaleDateString("en-IN", { day: "numeric", month: "short" })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {open.length === 0 && resolved.length === 0 && (
        <div className="flex h-40 items-center justify-center rounded-2xl border border-[#e4e9e6] bg-white text-sm text-[#9aa7a1]">
          No health records yet
        </div>
      )}

      {/* New health record modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">New Health Record</h3>
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
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Condition *</label>
                  <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.condition} onChange={(e) => setForm({ ...form, condition: e.target.value })} placeholder="Mastitis" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Severity</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })}>
                    <option value="normal">Normal</option>
                    <option value="watch">Watch</option>
                    <option value="treatment">Treatment</option>
                    <option value="critical">Critical</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Symptoms</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.symptoms} onChange={(e) => setForm({ ...form, symptoms: e.target.value })} placeholder="Swollen udder, reduced milk..." />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Treatment</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.treatment} onChange={(e) => setForm({ ...form, treatment: e.target.value })} placeholder="Antibiotic 5 days..." />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Vet Name</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.vet_name} onChange={(e) => setForm({ ...form, vet_name: e.target.value })} placeholder="Dr. Priya" />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Save Record"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
