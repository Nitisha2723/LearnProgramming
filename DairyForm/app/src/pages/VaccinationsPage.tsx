import { useEffect, useState } from "react";
import { vaccinationsApi, animalsApi, type Vaccination, type VaccinationDue, type Animal } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const COMMON_VACCINES = [
  { name: "FMD Polyvalent", disease: "Foot and Mouth Disease", interval_days: 180 },
  { name: "HS", disease: "Haemorrhagic Septicaemia", interval_days: 365 },
  { name: "BQ", disease: "Black Quarter", interval_days: 365 },
  { name: "Brucella", disease: "Brucellosis", interval_days: null },
  { name: "Anthrax", disease: "Anthrax", interval_days: 365 },
  { name: "Theileriosis", disease: "Theileria (Tick Fever)", interval_days: null },
  { name: "LSD", disease: "Lumpy Skin Disease", interval_days: 365 },
];

const fmt = (d: string) =>
  new Date(d).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });

const today = new Date().toISOString().split("T")[0];

export default function VaccinationsPage() {
  const { user } = useAuth();
  const [vaccinations, setVaccinations] = useState<Vaccination[]>([]);
  const [dueList, setDueList] = useState<VaccinationDue[]>([]);
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [view, setView] = useState<"all" | "due">("all");
  const [editingId, setEditingId] = useState<number | null>(null);

  const canWrite = ["admin", "manager", "vet"].includes(user?.role || "");
  const canDelete = ["admin", "manager"].includes(user?.role || "");

  const emptyForm = {
    animal_id: "all",
    vaccine_name: "",
    disease: "",
    vaccination_date: today,
    dose_ml: "",
    route: "IM",
    batch_no: "",
    manufacturer: "",
    next_due_date: "",
    given_by: "",
    cost: "",
    is_govt_free: "false",
    notes: "",
  };
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    const [vaxRes, dueRes, anRes] = await Promise.all([
      vaccinationsApi.list({ limit: 200 }),
      vaccinationsApi.due(60),
      animalsApi.list(),
    ]);
    setVaccinations(vaxRes.data);
    setDueList(dueRes.data);
    setAnimals(anRes.data);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const animalMap = Object.fromEntries(animals.map((a) => [a.id, a]));

  const handleVaccineSelect = (name: string) => {
    const v = COMMON_VACCINES.find((x) => x.name === name);
    if (v) {
      const nextDue = v.interval_days
        ? new Date(new Date(form.vaccination_date).getTime() + v.interval_days * 86400000).toISOString().split("T")[0]
        : "";
      setForm({ ...form, vaccine_name: v.name, disease: v.disease, next_due_date: nextDue });
    } else {
      setForm({ ...form, vaccine_name: name });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const makePayload = (animalId: number) => ({
        animal_id: animalId,
        vaccine_name: form.vaccine_name,
        disease: form.disease || undefined,
        vaccination_date: form.vaccination_date,
        dose_ml: form.dose_ml ? Number(form.dose_ml) : undefined,
        route: form.route || undefined,
        batch_no: form.batch_no || undefined,
        manufacturer: form.manufacturer || undefined,
        next_due_date: form.next_due_date || undefined,
        given_by: form.given_by || undefined,
        cost: form.cost ? Number(form.cost) : undefined,
        is_govt_free: form.is_govt_free === "true",
        notes: form.notes || undefined,
      });

      if (editingId) {
        await vaccinationsApi.update(editingId, makePayload(Number(form.animal_id)));
      } else if (form.animal_id === "all") {
        // Batch vaccinate all active animals
        await Promise.all(animals.map((a) => vaccinationsApi.create(makePayload(a.id))));
      } else {
        await vaccinationsApi.create(makePayload(Number(form.animal_id)));
      }
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
      await load();
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (v: Vaccination) => {
    setForm({
      animal_id: String(v.animal_id),
      vaccine_name: v.vaccine_name,
      disease: v.disease || "",
      vaccination_date: v.vaccination_date,
      dose_ml: v.dose_ml ? String(v.dose_ml) : "",
      route: v.route || "IM",
      batch_no: v.batch_no || "",
      manufacturer: v.manufacturer || "",
      next_due_date: v.next_due_date || "",
      given_by: v.given_by || "",
      cost: v.cost ? String(v.cost) : "",
      is_govt_free: v.is_govt_free ? "true" : "false",
      notes: v.notes || "",
    });
    setEditingId(v.id);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this vaccination record?")) return;
    await vaccinationsApi.delete(id);
    await load();
  };

  const overdueCount = dueList.filter((d) => d.days_overdue > 0).length;

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Vaccination Tracker</h1>
          <p className="mt-1 text-sm text-[#7d8983]">
            {vaccinations.length} records · {overdueCount > 0 && <span className="text-[#b25248] font-semibold">{overdueCount} overdue</span>}
          </p>
        </div>
        {canWrite && (
          <button
            onClick={() => { setForm(emptyForm); setEditingId(null); setShowForm(true); }}
            className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]"
          >
            <Icon name="plus" size={15} /> Record Vaccination
          </button>
        )}
      </div>

      {/* Due alerts */}
      {dueList.length > 0 && (
        <div className="mb-4 rounded-2xl border border-[#fde9c8] bg-[#fffbf3] p-4">
          <div className="flex items-center gap-2 mb-3">
            <Icon name="alert" size={15} className="text-[#c07000]" />
            <span className="text-sm font-semibold text-[#c07000]">{dueList.length} vaccination(s) due in next 60 days</span>
          </div>
          <div className="flex flex-wrap gap-2">
            {dueList.slice(0, 8).map((d, i) => (
              <div key={i} className={`rounded-xl px-3 py-1.5 text-[11px] ${d.days_overdue > 0 ? "bg-[#fce9e7] text-[#b25248]" : "bg-[#fff3e0] text-[#c07000]"}`}>
                <span className="font-semibold">{d.tag_number}</span> · {d.vaccine_name}
                {d.days_overdue > 0 ? ` (${d.days_overdue}d overdue)` : ` (due ${fmt(d.next_due_date)})`}
              </div>
            ))}
            {dueList.length > 8 && <span className="text-[11px] text-[#7d8983] self-center">+{dueList.length - 8} more</span>}
          </div>
        </div>
      )}

      {/* View toggle */}
      <div className="mb-4 flex gap-2">
        {(["all", "due"] as const).map((v) => (
          <button
            key={v}
            onClick={() => setView(v)}
            className={`rounded-full px-4 py-1.5 text-xs font-semibold transition-colors ${view === v ? "bg-[#1d7352] text-white" : "bg-[#f0f3f1] text-[#4d5e58] hover:bg-[#e4e9e6]"}`}
          >
            {v === "all" ? "All Records" : `Due / Overdue (${dueList.length})`}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#f8faf9] border-b border-[#e4e9e6]">
              <tr>
                {view === "all" ? (
                  <>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Date</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Animal</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Vaccine</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Disease</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Given By</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Next Due</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Cost</th>
                    {(canWrite || canDelete) && <th className="px-4 py-3" />}
                  </>
                ) : (
                  <>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Animal</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Vaccine</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Due Date</th>
                    <th className="px-4 py-3 text-left text-[11px] font-semibold text-[#7d8983] uppercase">Status</th>
                  </>
                )}
              </tr>
            </thead>
            <tbody>
              {view === "all" ? (
                vaccinations.length === 0 ? (
                  <tr><td colSpan={8} className="px-4 py-8 text-center text-sm text-[#9aa7a1]">No vaccination records yet</td></tr>
                ) : vaccinations.map((vax) => {
                  const animal = animalMap[vax.animal_id];
                  return (
                    <tr key={vax.id} className="border-b border-[#f0f3f1] hover:bg-[#fafbfa]">
                      <td className="px-4 py-3 text-[13px] text-[#4d5e58] whitespace-nowrap">{fmt(vax.vaccination_date)}</td>
                      <td className="px-4 py-3">
                        <div className="font-semibold text-[13px]">{animal?.tag_number || vax.animal_id}</div>
                        <div className="text-[11px] text-[#9aa7a1]">{animal?.name || ""}</div>
                      </td>
                      <td className="px-4 py-3">
                        <div className="text-[13px] font-medium">{vax.vaccine_name}</div>
                        {vax.is_govt_free && <span className="text-[10px] text-[#2d8b65] font-semibold">Govt free</span>}
                      </td>
                      <td className="px-4 py-3 text-[12px] text-[#5a6b64]">{vax.disease || "—"}</td>
                      <td className="px-4 py-3 text-[12px] text-[#5a6b64]">{vax.given_by || "—"}</td>
                      <td className="px-4 py-3 text-[12px] whitespace-nowrap">
                        {vax.next_due_date ? (
                          <span className={new Date(vax.next_due_date) < new Date() ? "text-[#b25248] font-semibold" : "text-[#4d5e58]"}>
                            {fmt(vax.next_due_date)}
                          </span>
                        ) : "—"}
                      </td>
                      <td className="px-4 py-3 text-[12px] text-[#5a6b64]">
                        {vax.cost ? `₹${vax.cost}` : vax.is_govt_free ? "Free" : "—"}
                      </td>
                      {(canWrite || canDelete) && (
                        <td className="px-4 py-3">
                          <div className="flex gap-2 justify-end">
                            {canWrite && (
                              <button onClick={() => handleEdit(vax)} className="rounded-lg p-1.5 hover:bg-[#f0f3f1]">
                                <Icon name="edit" size={14} className="text-[#5a6b64]" />
                              </button>
                            )}
                            {canDelete && (
                              <button onClick={() => handleDelete(vax.id)} className="rounded-lg p-1.5 hover:bg-[#fce9e7]">
                                <Icon name="trash" size={14} className="text-[#b25248]" />
                              </button>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })
              ) : (
                dueList.length === 0 ? (
                  <tr><td colSpan={4} className="px-4 py-8 text-center text-sm text-[#9aa7a1]">No vaccinations due</td></tr>
                ) : dueList.map((d, i) => (
                  <tr key={i} className="border-b border-[#f0f3f1] hover:bg-[#fafbfa]">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-[13px]">{d.tag_number}</div>
                      <div className="text-[11px] text-[#9aa7a1]">{d.animal_name || ""}</div>
                    </td>
                    <td className="px-4 py-3 text-[13px] font-medium">{d.vaccine_name}</td>
                    <td className="px-4 py-3 text-[13px] whitespace-nowrap">{fmt(d.next_due_date)}</td>
                    <td className="px-4 py-3">
                      {d.days_overdue > 0 ? (
                        <span className="rounded-full bg-[#fce9e7] px-2 py-0.5 text-[10px] font-semibold text-[#b25248]">
                          {d.days_overdue}d overdue
                        </span>
                      ) : (
                        <span className="rounded-full bg-[#fff3e0] px-2 py-0.5 text-[10px] font-semibold text-[#c07000]">
                          Due soon
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add/Edit Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/30 p-4 overflow-y-auto">
          <div className="my-6 w-full max-w-[540px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">{editingId ? "Edit Record" : "Record Vaccination"}</h3>
              <button onClick={() => { setShowForm(false); setEditingId(null); }}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Animal *</label>
                <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.animal_id} onChange={(e) => setForm({ ...form, animal_id: e.target.value })} disabled={!!editingId}>
                  {!editingId && <option value="all">All Animals (batch vaccinate)</option>}
                  {animals.map((a) => <option key={a.id} value={a.id}>{a.tag_number} — {a.name}</option>)}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Vaccine *</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.vaccine_name} onChange={(e) => handleVaccineSelect(e.target.value)}>
                    <option value="">Select or type below</option>
                    {COMMON_VACCINES.map((v) => <option key={v.name} value={v.name}>{v.name}</option>)}
                  </select>
                  <input className="mt-1 w-full rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" placeholder="Or type vaccine name" value={form.vaccine_name} onChange={(e) => setForm({ ...form, vaccine_name: e.target.value })} required />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Disease</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.disease} onChange={(e) => setForm({ ...form, disease: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date *</label>
                  <input type="date" required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.vaccination_date} onChange={(e) => setForm({ ...form, vaccination_date: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Next Due Date</label>
                  <input type="date" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.next_due_date} onChange={(e) => setForm({ ...form, next_due_date: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Dose (ml)</label>
                  <input type="number" step="0.1" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.dose_ml} onChange={(e) => setForm({ ...form, dose_ml: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Route</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.route} onChange={(e) => setForm({ ...form, route: e.target.value })}>
                    <option value="IM">IM (Intramuscular)</option>
                    <option value="SC">SC (Subcutaneous)</option>
                    <option value="Oral">Oral</option>
                    <option value="IV">IV (Intravenous)</option>
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Given By</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.given_by} onChange={(e) => setForm({ ...form, given_by: e.target.value })} placeholder="Vet / Technician" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Cost (₹)</label>
                  <input type="number" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.cost} onChange={(e) => setForm({ ...form, cost: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Govt. Free</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.is_govt_free} onChange={(e) => setForm({ ...form, is_govt_free: e.target.value })}>
                    <option value="false">No (paid)</option>
                    <option value="true">Yes (free)</option>
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Batch No.</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.batch_no} onChange={(e) => setForm({ ...form, batch_no: e.target.value })} />
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <textarea rows={2} className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65] resize-none" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
              </div>

              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => { setShowForm(false); setEditingId(null); }} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : editingId ? "Update" : form.animal_id === "all" ? `Batch Record (${animals.length} animals)` : "Record"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
