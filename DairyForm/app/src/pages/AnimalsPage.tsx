import { useEffect, useState } from "react";
import { animalsApi, type Animal } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const STATUS_COLORS: Record<string, string> = {
  lactating: "bg-[#e6f4ec] text-[#2d8b65]",
  pregnant: "bg-[#e8f0fb] text-[#3a74c9]",
  dry: "bg-[#fff8e7] text-[#9a6a1f]",
  sick: "bg-[#fce9e7] text-[#c0392b]",
  heifer: "bg-[#f0ebf7] text-[#7a5ba8]",
  calf: "bg-[#f0f3f1] text-[#5a6b64]",
};

export default function AnimalsPage() {
  const { user } = useAuth();
  const [animals, setAnimals] = useState<Animal[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ tag_number: "", name: "", breed: "Murrah Buffalo", status: "lactating", parity: 1, weight_kg: "" });
  const [saving, setSaving] = useState(false);

  const canWrite = ["admin", "manager"].includes(user?.role || "");

  useEffect(() => {
    loadAnimals();
  }, [statusFilter]);

  const loadAnimals = async () => {
    setLoading(true);
    try {
      const { data } = await animalsApi.list(statusFilter !== "all" ? { status: statusFilter } : {});
      setAnimals(data);
    } finally {
      setLoading(false);
    }
  };

  const filtered = animals.filter(
    (a) =>
      a.tag_number.toLowerCase().includes(search.toLowerCase()) ||
      (a.name || "").toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await animalsApi.create({ ...form, weight_kg: Number(form.weight_kg) });
      setShowForm(false);
      setForm({ tag_number: "", name: "", breed: "Murrah Buffalo", status: "lactating", parity: 1, weight_kg: "" });
      loadAnimals();
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Herd Registry</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{animals.length} animals registered</p>
        </div>
        {canWrite && (
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]"
          >
            <Icon name="plus" size={15} /> Add animal
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="mb-5 flex flex-wrap gap-3">
        <div className="flex items-center gap-2 rounded-xl border border-[#e4e9e6] bg-white px-3 py-2.5">
          <Icon name="search" size={15} className="text-[#8c9691]" />
          <input
            className="w-40 bg-transparent text-sm outline-none placeholder:text-[#a0aaa5]"
            placeholder="Search tag / name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select
          className="rounded-xl border border-[#e4e9e6] bg-white px-3 py-2.5 text-sm outline-none"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="all">All status</option>
          <option value="lactating">Lactating</option>
          <option value="pregnant">Pregnant</option>
          <option value="dry">Dry</option>
          <option value="sick">Sick</option>
          <option value="heifer">Heifer</option>
        </select>
      </div>

      {/* Animals grid */}
      {loading ? (
        <div className="flex h-40 items-center justify-center">
          <div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex h-40 items-center justify-center rounded-2xl border border-[#e4e9e6] bg-white text-[#8c9691]">
          No animals found
        </div>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filtered.map((animal) => (
            <div key={animal.id} className="rounded-2xl border border-[#e4e9e6] bg-white p-4 hover:border-[#bfcfc9] transition">
              <div className="flex items-start justify-between">
                <div className="flex size-9 items-center justify-center rounded-xl bg-[#e6f4ec]">
                  <Icon name="tag" size={16} className="text-[#2d8b65]" />
                </div>
                <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold capitalize ${STATUS_COLORS[animal.status] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>
                  {animal.status}
                </span>
              </div>
              <div className="mt-3">
                <div className="text-[15px] font-bold">{animal.tag_number}</div>
                <div className="text-sm text-[#5a6b64]">{animal.name || "—"}</div>
              </div>
              <div className="mt-3 grid grid-cols-2 gap-2 border-t border-[#f0f3f1] pt-3">
                <div>
                  <div className="text-[9px] text-[#9aa7a1]">BREED</div>
                  <div className="mt-0.5 text-[11px] font-medium truncate">{animal.breed}</div>
                </div>
                <div>
                  <div className="text-[9px] text-[#9aa7a1]">PARITY</div>
                  <div className="mt-0.5 text-[11px] font-medium">{animal.parity}</div>
                </div>
                {animal.weight_kg && (
                  <div>
                    <div className="text-[9px] text-[#9aa7a1]">WEIGHT</div>
                    <div className="mt-0.5 text-[11px] font-medium">{Number(animal.weight_kg).toFixed(0)} kg</div>
                  </div>
                )}
                {animal.last_calving_date && (
                  <div>
                    <div className="text-[9px] text-[#9aa7a1]">LAST CALVING</div>
                    <div className="mt-0.5 text-[11px] font-medium">
                      {new Date(animal.last_calving_date).toLocaleDateString("en-IN", { day: "numeric", month: "short" })}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add animal modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Add New Animal</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Tag Number *</label>
                  <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.tag_number} onChange={(e) => setForm({ ...form, tag_number: e.target.value })} placeholder="B-41" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Name</label>
                  <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Kamala" />
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Status</label>
                <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value="lactating">Lactating</option>
                  <option value="pregnant">Pregnant</option>
                  <option value="dry">Dry</option>
                  <option value="heifer">Heifer</option>
                </select>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Parity</label>
                  <input type="number" min={0} className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.parity} onChange={(e) => setForm({ ...form, parity: Number(e.target.value) })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Weight (kg)</label>
                  <input type="number" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.weight_kg} onChange={(e) => setForm({ ...form, weight_kg: e.target.value })} placeholder="480" />
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold text-[#5a6b64]">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Add Animal"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
