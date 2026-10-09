import { useEffect, useState } from "react";
import { calvesApi, animalsApi, type Calf, type Animal, type CalfWeight } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const STATUS_COLORS: Record<string, string> = {
  alive: "bg-[#e6f4ec] text-[#2d8b65]",
  weaned: "bg-[#eef3ff] text-[#4a67c8]",
  sold: "bg-[#f0f3f1] text-[#6b7c75]",
  dead: "bg-[#fce9e7] text-[#b25248]",
};

const fmt = (d: string) =>
  new Date(d).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });

const ageInDays = (dob: string) => {
  const ms = new Date().getTime() - new Date(dob).getTime();
  return Math.floor(ms / (1000 * 60 * 60 * 24));
};

export default function CalvesPage() {
  const { user } = useAuth();
  const [calves, setCalves] = useState<Calf[]>([]);
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showWeightForm, setShowWeightForm] = useState<number | null>(null);
  const [weights, setWeights] = useState<CalfWeight[]>([]);
  const [saving, setSaving] = useState(false);
  const [filterStatus, setFilterStatus] = useState("alive");
  const [editingId, setEditingId] = useState<number | null>(null);

  const canWrite = ["admin", "manager", "vet", "data_entry"].includes(user?.role || "");
  const canDelete = ["admin", "manager"].includes(user?.role || "");

  const emptyForm = {
    tag_number: "",
    name: "",
    dam_id: "",
    sire_name: "",
    date_of_birth: new Date().toISOString().split("T")[0],
    sex: "female",
    birth_weight_kg: "",
    colostrum_given: "false",
    colostrum_time_hrs: "",
    notes: "",
  };
  const [form, setForm] = useState(emptyForm);
  const [weightForm, setWeightForm] = useState({ record_date: new Date().toISOString().split("T")[0], weight_kg: "" });

  const load = async () => {
    const [calvRes, anRes] = await Promise.all([
      calvesApi.list(filterStatus ? { status: filterStatus, is_active: true } : { is_active: true }),
      animalsApi.list(),
    ]);
    setCalves(calvRes.data);
    setAnimals(anRes.data);
    setLoading(false);
  };

  useEffect(() => { load(); }, [filterStatus]);

  const animalMap = Object.fromEntries(animals.map((a) => [a.id, a]));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: any = {
        tag_number: form.tag_number,
        name: form.name || undefined,
        dam_id: Number(form.dam_id),
        sire_name: form.sire_name || undefined,
        date_of_birth: form.date_of_birth,
        sex: form.sex,
        birth_weight_kg: form.birth_weight_kg ? Number(form.birth_weight_kg) : undefined,
        colostrum_given: form.colostrum_given === "true",
        colostrum_time_hrs: form.colostrum_time_hrs ? Number(form.colostrum_time_hrs) : undefined,
        notes: form.notes || undefined,
      };
      if (editingId) {
        await calvesApi.update(editingId, payload);
      } else {
        await calvesApi.create(payload);
      }
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
      await load();
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (c: Calf) => {
    setForm({
      tag_number: c.tag_number,
      name: c.name || "",
      dam_id: String(c.dam_id),
      sire_name: c.sire_name || "",
      date_of_birth: c.date_of_birth,
      sex: c.sex,
      birth_weight_kg: c.birth_weight_kg ? String(c.birth_weight_kg) : "",
      colostrum_given: c.colostrum_given ? "true" : "false",
      colostrum_time_hrs: c.colostrum_time_hrs ? String(c.colostrum_time_hrs) : "",
      notes: c.notes || "",
    });
    setEditingId(c.id);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this calf record?")) return;
    await calvesApi.delete(id);
    await load();
  };

  const openWeights = async (calfId: number) => {
    const res = await calvesApi.weights(calfId);
    setWeights(res.data);
    setShowWeightForm(calfId);
  };

  const handleAddWeight = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showWeightForm) return;
    setSaving(true);
    try {
      await calvesApi.addWeight(showWeightForm, {
        record_date: weightForm.record_date,
        weight_kg: Number(weightForm.weight_kg),
      });
      const res = await calvesApi.weights(showWeightForm);
      setWeights(res.data);
      setWeightForm({ record_date: new Date().toISOString().split("T")[0], weight_kg: "" });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Calf Management</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{calves.length} calves</p>
        </div>
        {canWrite && (
          <button
            onClick={() => { setForm(emptyForm); setEditingId(null); setShowForm(true); }}
            className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]"
          >
            <Icon name="plus" size={15} /> Register Calf
          </button>
        )}
      </div>

      {/* Filter chips */}
      <div className="mb-4 flex flex-wrap gap-2">
        {["alive", "weaned", "sold", "dead", ""].map((s) => (
          <button
            key={s}
            onClick={() => setFilterStatus(s)}
            className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${filterStatus === s ? "bg-[#1d7352] text-white" : "bg-[#f0f3f1] text-[#4d5e58] hover:bg-[#e4e9e6]"}`}
          >
            {s === "" ? "All" : s.charAt(0).toUpperCase() + s.slice(1)}
          </button>
        ))}
      </div>

      {/* Cards grid */}
      {calves.length === 0 ? (
        <div className="rounded-2xl border border-[#e4e9e6] bg-white p-8 text-center text-sm text-[#9aa7a1]">No calves found</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {calves.map((calf) => {
            const dam = animalMap[calf.dam_id];
            const age = ageInDays(calf.date_of_birth);
            return (
              <div key={calf.id} className="rounded-2xl border border-[#e4e9e6] bg-white p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <div className="text-sm font-bold">{calf.tag_number}</div>
                    <div className="text-xs text-[#7d8983]">{calf.name || "—"}</div>
                  </div>
                  <span className={`rounded-full px-2 py-0.5 text-[9px] font-semibold ${STATUS_COLORS[calf.status] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>
                    {calf.status}
                  </span>
                </div>

                <div className="mt-3 grid grid-cols-2 gap-2 text-[11px]">
                  <div>
                    <span className="text-[#9aa7a1]">Sex</span>
                    <div className="font-medium capitalize">{calf.sex}</div>
                  </div>
                  <div>
                    <span className="text-[#9aa7a1]">Age</span>
                    <div className="font-medium">{age < 30 ? `${age}d` : `${Math.floor(age / 30)}mo`}</div>
                  </div>
                  <div>
                    <span className="text-[#9aa7a1]">Birth wt</span>
                    <div className="font-medium">{calf.birth_weight_kg ? `${calf.birth_weight_kg} kg` : "—"}</div>
                  </div>
                  <div>
                    <span className="text-[#9aa7a1]">Dam</span>
                    <div className="font-medium">{dam ? `${dam.tag_number}` : "—"}</div>
                  </div>
                  <div>
                    <span className="text-[#9aa7a1]">Colostrum</span>
                    <div className={`font-medium ${calf.colostrum_given ? "text-[#2d8b65]" : "text-[#b25248]"}`}>
                      {calf.colostrum_given ? `Yes${calf.colostrum_time_hrs ? ` (${calf.colostrum_time_hrs}h)` : ""}` : "No"}
                    </div>
                  </div>
                  <div>
                    <span className="text-[#9aa7a1]">DOB</span>
                    <div className="font-medium">{fmt(calf.date_of_birth)}</div>
                  </div>
                </div>

                <div className="mt-3 border-t border-[#f0f3f1] pt-3 flex gap-2">
                  <button onClick={() => openWeights(calf.id)} className="flex-1 rounded-lg bg-[#e6f4ec] py-1.5 text-[11px] font-semibold text-[#1d7352] hover:bg-[#d0eada]">
                    Weights
                  </button>
                  {canWrite && (
                    <button onClick={() => handleEdit(calf)} className="rounded-lg p-1.5 hover:bg-[#f0f3f1]">
                      <Icon name="edit" size={13} className="text-[#5a6b64]" />
                    </button>
                  )}
                  {canDelete && (
                    <button onClick={() => handleDelete(calf.id)} className="rounded-lg p-1.5 hover:bg-[#fce9e7]">
                      <Icon name="trash" size={13} className="text-[#b25248]" />
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Add/Edit Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/30 p-4 overflow-y-auto">
          <div className="my-6 w-full max-w-[520px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">{editingId ? "Edit Calf" : "Register Calf"}</h3>
              <button onClick={() => { setShowForm(false); setEditingId(null); }}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Tag Number *</label>
                  <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.tag_number} onChange={(e) => setForm({ ...form, tag_number: e.target.value })} placeholder="C-001" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Name</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Mother (Dam) *</label>
                  <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.dam_id} onChange={(e) => setForm({ ...form, dam_id: e.target.value })}>
                    <option value="">Select...</option>
                    {animals.map((a) => <option key={a.id} value={a.id}>{a.tag_number} — {a.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Bull (Sire)</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.sire_name} onChange={(e) => setForm({ ...form, sire_name: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date of Birth *</label>
                  <input type="date" required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.date_of_birth} onChange={(e) => setForm({ ...form, date_of_birth: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Sex *</label>
                  <select required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.sex} onChange={(e) => setForm({ ...form, sex: e.target.value })}>
                    <option value="female">Female</option>
                    <option value="male">Male</option>
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Birth Weight (kg)</label>
                  <input type="number" step="0.1" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.birth_weight_kg} onChange={(e) => setForm({ ...form, birth_weight_kg: e.target.value })} placeholder="25.0" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Colostrum Given</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.colostrum_given} onChange={(e) => setForm({ ...form, colostrum_given: e.target.value })}>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>
                {form.colostrum_given === "true" && (
                  <div className="col-span-2">
                    <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Colostrum within (hours of birth)</label>
                    <input type="number" step="0.5" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.colostrum_time_hrs} onChange={(e) => setForm({ ...form, colostrum_time_hrs: e.target.value })} placeholder="2" />
                  </div>
                )}
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <textarea rows={2} className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65] resize-none" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => { setShowForm(false); setEditingId(null); }} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : editingId ? "Update" : "Register"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Weight Records Modal */}
      {showWeightForm !== null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl max-h-[80vh] flex flex-col">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Weight Records</h3>
              <button onClick={() => setShowWeightForm(null)}><Icon name="close" size={18} /></button>
            </div>

            {/* Weight history */}
            <div className="flex-1 overflow-y-auto mb-4">
              {weights.length === 0 ? (
                <p className="text-sm text-[#9aa7a1] text-center py-4">No weight records yet</p>
              ) : (
                <table className="w-full text-sm">
                  <thead><tr className="text-[10px] text-[#9aa7a1] uppercase border-b border-[#f0f3f1]">
                    <th className="pb-2 text-left">Date</th>
                    <th className="pb-2 text-right">Weight (kg)</th>
                  </tr></thead>
                  <tbody>
                    {weights.map((w) => (
                      <tr key={w.id} className="border-b border-[#f8faf9]">
                        <td className="py-2">{fmt(w.record_date)}</td>
                        <td className="py-2 text-right font-semibold">{w.weight_kg}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {/* Add weight form */}
            {canWrite && (
              <form onSubmit={handleAddWeight} className="flex gap-2 border-t border-[#f0f3f1] pt-4">
                <input type="date" required className="flex-1 rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={weightForm.record_date} onChange={(e) => setWeightForm({ ...weightForm, record_date: e.target.value })} />
                <input type="number" required step="0.1" placeholder="kg" className="w-24 rounded-xl border border-[#dce1de] px-3 py-2 text-sm outline-none focus:border-[#2d8b65]" value={weightForm.weight_kg} onChange={(e) => setWeightForm({ ...weightForm, weight_kg: e.target.value })} />
                <button type="submit" disabled={saving} className="rounded-xl bg-[#1d7352] px-3 py-2 text-sm font-semibold text-white disabled:opacity-60">Add</button>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
