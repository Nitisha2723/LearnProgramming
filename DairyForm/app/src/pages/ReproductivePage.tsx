import { useEffect, useState } from "react";
import { reproductiveApi, animalsApi, type ReproEvent, type Animal } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const EVENT_TYPES = [
  { value: "heat_observed", label: "Heat Observed", color: "bg-[#fff3e0] text-[#c07000]" },
  { value: "ai_done", label: "AI Done", color: "bg-[#eef3ff] text-[#4a67c8]" },
  { value: "pregnancy_confirmed", label: "Pregnancy Confirmed", color: "bg-[#e6f4ec] text-[#2d8b65]" },
  { value: "pregnancy_negative", label: "Pregnancy Negative", color: "bg-[#fce9e7] text-[#b25248]" },
  { value: "dry_off", label: "Dry Off", color: "bg-[#f0f3f1] text-[#6b7c75]" },
  { value: "calving", label: "Calving", color: "bg-[#f3e8ff] text-[#7c3aed]" },
  { value: "abortion", label: "Abortion", color: "bg-[#fce9e7] text-[#b25248]" },
];

const TYPE_MAP = Object.fromEntries(EVENT_TYPES.map((t) => [t.value, t]));

const fmt = (d: string) =>
  new Date(d).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });

export default function ReproductivePage() {
  const { user } = useAuth();
  const [events, setEvents] = useState<ReproEvent[]>([]);
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [filterType, setFilterType] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);

  const canWrite = ["admin", "manager", "vet", "data_entry"].includes(user?.role || "");
  const canDelete = ["admin", "manager"].includes(user?.role || "");

  const emptyForm = {
    animal_id: "",
    event_date: new Date().toISOString().split("T")[0],
    event_type: "heat_observed",
    bull_name: "",
    semen_batch: "",
    ai_technician: "",
    ai_number: "",
    diagnosed_by: "",
    calf_sex: "",
    calf_weight_kg: "",
    calving_ease: "normal",
    next_heat_expected: "",
    notes: "",
  };
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    const [evRes, anRes] = await Promise.all([
      reproductiveApi.list(filterType ? { event_type: filterType } : {}),
      animalsApi.list(),
    ]);
    setEvents(evRes.data);
    setAnimals(anRes.data);
    setLoading(false);
  };

  useEffect(() => { load(); }, [filterType]);

  const animalMap = Object.fromEntries(animals.map((a) => [a.id, a]));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: any = {
        animal_id: Number(form.animal_id),
        event_date: form.event_date,
        event_type: form.event_type,
        notes: form.notes || undefined,
        bull_name: form.bull_name || undefined,
        semen_batch: form.semen_batch || undefined,
        ai_technician: form.ai_technician || undefined,
        ai_number: form.ai_number ? Number(form.ai_number) : undefined,
        diagnosed_by: form.diagnosed_by || undefined,
        calf_sex: form.calf_sex || undefined,
        calf_weight_kg: form.calf_weight_kg ? Number(form.calf_weight_kg) : undefined,
        calving_ease: form.calving_ease || undefined,
        next_heat_expected: form.next_heat_expected || undefined,
      };
      if (editingId) {
        await reproductiveApi.update(editingId, payload);
      } else {
        await reproductiveApi.create(payload);
      }
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
      await load();
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (ev: ReproEvent) => {
    setForm({
      animal_id: String(ev.animal_id),
      event_date: ev.event_date,
      event_type: ev.event_type,
      bull_name: ev.bull_name || "",
      semen_batch: ev.semen_batch || "",
      ai_technician: ev.ai_technician || "",
      ai_number: ev.ai_number ? String(ev.ai_number) : "",
      diagnosed_by: ev.diagnosed_by || "",
      calf_sex: ev.calf_sex || "",
      calf_weight_kg: ev.calf_weight_kg ? String(ev.calf_weight_kg) : "",
      calving_ease: ev.calving_ease || "normal",
      next_heat_expected: ev.next_heat_expected || "",
      notes: ev.notes || "",
    });
    setEditingId(ev.id);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this reproductive event?")) return;
    await reproductiveApi.delete(id);
    await load();
  };

  const isAI = form.event_type === "ai_done";
  const isPregnancy = form.event_type.startsWith("pregnancy_");
  const isCalving = form.event_type === "calving";

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Reproductive Records</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{events.length} events recorded</p>
        </div>
        {canWrite && (
          <button
            onClick={() => { setForm(emptyForm); setEditingId(null); setShowForm(true); }}
            className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]"
          >
            <Icon name="plus" size={15} /> Add Event
          </button>
        )}
      </div>

      {/* Filter chips */}
      <div className="mb-4 flex flex-wrap gap-2">
        <button
          onClick={() => setFilterType("")}
          className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${filterType === "" ? "bg-[#1d7352] text-white" : "bg-[#f0f3f1] text-[#4d5e58] hover:bg-[#e4e9e6]"}`}
        >
          All
        </button>
        {EVENT_TYPES.map((t) => (
          <button
            key={t.value}
            onClick={() => setFilterType(t.value === filterType ? "" : t.value)}
            className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${filterType === t.value ? "bg-[#1d7352] text-white" : "bg-[#f0f3f1] text-[#4d5e58] hover:bg-[#e4e9e6]"}`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#f8faf9] border-b border-[#e4e9e6]">
              <tr>
                <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Date</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Animal</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Event</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Details</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Notes</th>
                {(canWrite || canDelete) && <th className="px-4 py-3" />}
              </tr>
            </thead>
            <tbody>
              {events.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-sm text-[#9aa7a1]">No events recorded yet</td></tr>
              ) : events.map((ev) => {
                const animal = animalMap[ev.animal_id];
                const evType = TYPE_MAP[ev.event_type];
                let detail = "";
                if (ev.event_type === "ai_done") detail = `Bull: ${ev.bull_name || "—"} | AI#${ev.ai_number || 1}`;
                if (ev.event_type === "calving") detail = `Sex: ${ev.calf_sex || "—"} | ${ev.calf_weight_kg ? ev.calf_weight_kg + " kg" : ""} | ${ev.calving_ease || "—"}`;
                if (ev.event_type === "pregnancy_confirmed") detail = `Diagnosed by: ${ev.diagnosed_by || "—"}`;
                if (ev.next_heat_expected) detail += ` | Next heat: ${fmt(ev.next_heat_expected)}`;
                return (
                  <tr key={ev.id} className="border-b border-[#f0f3f1] hover:bg-[#fafbfa]">
                    <td className="px-4 py-3 text-[13px] text-[#4d5e58] whitespace-nowrap">{fmt(ev.event_date)}</td>
                    <td className="px-4 py-3">
                      <div className="font-semibold text-[13px]">{animal?.tag_number || ev.animal_id}</div>
                      <div className="text-[11px] text-[#9aa7a1]">{animal?.name || ""}</div>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${evType?.color || "bg-[#f0f3f1] text-[#6b7c75]"}`}>
                        {evType?.label || ev.event_type}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-[12px] text-[#5a6b64]">{detail}</td>
                    <td className="px-4 py-3 max-w-[180px] text-[12px] text-[#7d8983] truncate">{ev.notes}</td>
                    {(canWrite || canDelete) && (
                      <td className="px-4 py-3">
                        <div className="flex gap-2 justify-end">
                          {canWrite && (
                            <button onClick={() => handleEdit(ev)} className="rounded-lg p-1.5 hover:bg-[#f0f3f1]">
                              <Icon name="edit" size={14} className="text-[#5a6b64]" />
                            </button>
                          )}
                          {canDelete && (
                            <button onClick={() => handleDelete(ev.id)} className="rounded-lg p-1.5 hover:bg-[#fce9e7]">
                              <Icon name="trash" size={14} className="text-[#b25248]" />
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add/Edit Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/30 p-4 overflow-y-auto">
          <div className="my-6 w-full max-w-[520px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">{editingId ? "Edit Event" : "Add Reproductive Event"}</h3>
              <button onClick={() => { setShowForm(false); setEditingId(null); }}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Animal *</label>
                  <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.animal_id} onChange={(e) => setForm({ ...form, animal_id: e.target.value })}>
                    <option value="">Select...</option>
                    {animals.map((a) => <option key={a.id} value={a.id}>{a.tag_number} — {a.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date *</label>
                  <input type="date" required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.event_date} onChange={(e) => setForm({ ...form, event_date: e.target.value })} />
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Event Type *</label>
                <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.event_type} onChange={(e) => setForm({ ...form, event_type: e.target.value })}>
                  {EVENT_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
                </select>
              </div>

              {/* AI-specific fields */}
              {isAI && (
                <div className="rounded-xl bg-[#f8faf9] p-3 space-y-3">
                  <div className="text-xs font-semibold text-[#1d7352]">AI Details</div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Bull Name</label>
                      <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.bull_name} onChange={(e) => setForm({ ...form, bull_name: e.target.value })} placeholder="e.g. Shiva Elite" />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">AI Number</label>
                      <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.ai_number} onChange={(e) => setForm({ ...form, ai_number: e.target.value })}>
                        <option value="1">1st AI</option>
                        <option value="2">2nd AI</option>
                        <option value="3">3rd AI</option>
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Semen Batch</label>
                      <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.semen_batch} onChange={(e) => setForm({ ...form, semen_batch: e.target.value })} />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">AI Technician</label>
                      <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.ai_technician} onChange={(e) => setForm({ ...form, ai_technician: e.target.value })} />
                    </div>
                  </div>
                </div>
              )}

              {/* Pregnancy fields */}
              {isPregnancy && (
                <div className="rounded-xl bg-[#f8faf9] p-3 space-y-2">
                  <div className="text-xs font-semibold text-[#1d7352]">Pregnancy Diagnosis</div>
                  <div>
                    <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Diagnosed By</label>
                    <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.diagnosed_by} onChange={(e) => setForm({ ...form, diagnosed_by: e.target.value })} placeholder="Vet name" />
                  </div>
                  {form.event_type === "pregnancy_negative" && (
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Next Heat Expected</label>
                      <input type="date" className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.next_heat_expected} onChange={(e) => setForm({ ...form, next_heat_expected: e.target.value })} />
                    </div>
                  )}
                </div>
              )}

              {/* Calving fields */}
              {isCalving && (
                <div className="rounded-xl bg-[#f8faf9] p-3 space-y-3">
                  <div className="text-xs font-semibold text-[#1d7352]">Calving Details</div>
                  <div className="grid grid-cols-3 gap-3">
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Calf Sex</label>
                      <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.calf_sex} onChange={(e) => setForm({ ...form, calf_sex: e.target.value })}>
                        <option value="">Select</option>
                        <option value="female">Female</option>
                        <option value="male">Male</option>
                      </select>
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Birth Wt (kg)</label>
                      <input type="number" step="0.1" className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.calf_weight_kg} onChange={(e) => setForm({ ...form, calf_weight_kg: e.target.value })} placeholder="25.0" />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Calving Ease</label>
                      <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={form.calving_ease} onChange={(e) => setForm({ ...form, calving_ease: e.target.value })}>
                        <option value="normal">Normal</option>
                        <option value="assisted">Assisted</option>
                        <option value="difficult">Difficult</option>
                      </select>
                    </div>
                  </div>
                </div>
              )}

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <textarea rows={2} className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65] resize-none" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
              </div>

              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => { setShowForm(false); setEditingId(null); }} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : editingId ? "Update" : "Add Event"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
