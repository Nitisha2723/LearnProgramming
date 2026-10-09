import { useEffect, useState } from "react";
import { expensesApi, type Expense } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const CATEGORIES = [
  { value: "feed", label: "Feed & Fodder" },
  { value: "veterinary", label: "Veterinary" },
  { value: "labour", label: "Labour" },
  { value: "medicine", label: "Medicine" },
  { value: "equipment", label: "Equipment" },
  { value: "utilities", label: "Utilities" },
  { value: "maintenance", label: "Maintenance" },
  { value: "insurance", label: "Insurance" },
  { value: "other", label: "Other" },
];

const PAYMENT_MODES = ["cash", "upi", "bank_transfer", "cheque"];

const CAT_COLORS: Record<string, string> = {
  feed: "bg-[#e6f4ec] text-[#2d8b65]",
  veterinary: "bg-[#fce9e7] text-[#b25248]",
  labour: "bg-[#eef3ff] text-[#4a67c8]",
  medicine: "bg-[#fff3e0] text-[#c07000]",
  equipment: "bg-[#f3e8ff] text-[#7c3aed]",
  utilities: "bg-[#e0f4ff] text-[#0d7ab8]",
  maintenance: "bg-[#fdf2e0] text-[#a0580c]",
  insurance: "bg-[#f0f4e8] text-[#5a7a2e]",
  other: "bg-[#f0f3f1] text-[#6b7c75]",
};

const fmt = (n: number) =>
  new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 }).format(n);

export default function ExpensesPage() {
  const { user } = useAuth();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [summary, setSummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [filterCat, setFilterCat] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);

  const canWrite = ["admin", "manager", "data_entry"].includes(user?.role || "");
  const canDelete = ["admin", "manager"].includes(user?.role || "");

  const emptyForm = {
    expense_date: new Date().toISOString().split("T")[0],
    category: "feed",
    description: "",
    amount: "",
    vendor: "",
    reference_no: "",
    payment_mode: "cash",
    notes: "",
  };
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    const params = filterCat ? { category: filterCat } : undefined;
    const [listRes, sumRes] = await Promise.all([
      expensesApi.list(params),
      expensesApi.summary(1),
    ]);
    setExpenses(listRes.data);
    setSummary(sumRes.data);
    setLoading(false);
  };

  useEffect(() => { load(); }, [filterCat]);

  const openCreate = () => {
    setForm(emptyForm);
    setEditingId(null);
    setShowForm(true);
  };

  const openEdit = (e: Expense) => {
    setForm({
      expense_date: e.expense_date,
      category: e.category,
      description: e.description,
      amount: String(e.amount),
      vendor: e.vendor || "",
      reference_no: e.reference_no || "",
      payment_mode: e.payment_mode,
      notes: e.notes || "",
    });
    setEditingId(e.id);
    setShowForm(true);
  };

  const handleSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        amount: Number(form.amount),
        vendor: form.vendor || undefined,
        reference_no: form.reference_no || undefined,
        notes: form.notes || undefined,
      };
      if (editingId) {
        await expensesApi.update(editingId, payload);
      } else {
        await expensesApi.create(payload);
      }
      setShowForm(false);
      await load();
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this expense?")) return;
    await expensesApi.delete(id);
    await load();
  };

  if (loading) {
    return (
      <div className="flex h-40 items-center justify-center">
        <div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" />
      </div>
    );
  }

  const grandTotal = summary?.grand_total ?? 0;

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Expenses</h1>
          <p className="mt-1 text-sm text-[#7d8983]">Cost tracking — this month total ₹{fmt(grandTotal)}</p>
        </div>
        {canWrite && (
          <button
            onClick={openCreate}
            className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]"
          >
            <Icon name="plus" size={15} /> Add expense
          </button>
        )}
      </div>

      {/* Category summary tiles */}
      {summary?.by_category?.length > 0 && (
        <div className="mb-5 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          <div className="rounded-2xl border border-[#e4e9e6] bg-white p-4 col-span-2 sm:col-span-1">
            <div className="text-[10px] font-semibold text-[#7e8983]">TOTAL (30 DAYS)</div>
            <div className="mt-2 text-xl font-bold tracking-tight">₹{fmt(grandTotal)}</div>
            <div className="mt-1 text-[10px] text-[#9aa7a1]">{expenses.length} entries</div>
          </div>
          {summary.by_category.slice(0, 4).map((c: any) => (
            <div key={c.category} className="rounded-2xl border border-[#e4e9e6] bg-white p-4">
              <div className={`mb-1 inline-block rounded-full px-2 py-0.5 text-[9px] font-semibold ${CAT_COLORS[c.category] || CAT_COLORS.other}`}>
                {CATEGORIES.find((x) => x.value === c.category)?.label ?? c.category}
              </div>
              <div className="mt-1 text-lg font-bold tracking-tight">₹{fmt(c.total)}</div>
              <div className="text-[10px] text-[#9aa7a1]">{c.count} entries</div>
            </div>
          ))}
        </div>
      )}

      {/* Filter bar */}
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <button
          onClick={() => setFilterCat("")}
          className={`rounded-full px-3 py-1.5 text-[11px] font-semibold ${filterCat === "" ? "bg-[#1d7352] text-white" : "border border-[#dce1de] text-[#5a6b64] hover:bg-[#f0f3f1]"}`}
        >
          All
        </button>
        {CATEGORIES.map((c) => (
          <button
            key={c.value}
            onClick={() => setFilterCat(filterCat === c.value ? "" : c.value)}
            className={`rounded-full px-3 py-1.5 text-[11px] font-semibold ${filterCat === c.value ? "bg-[#1d7352] text-white" : "border border-[#dce1de] text-[#5a6b64] hover:bg-[#f0f3f1]"}`}
          >
            {c.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="px-5 py-4 border-b border-[#f0f3f1]">
          <h2 className="text-[13px] font-bold text-[#3d4d47] uppercase tracking-wide">Expense log</h2>
        </div>
        {expenses.length === 0 ? (
          <div className="px-5 py-10 text-center text-sm text-[#9aa7a1]">No expenses recorded yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[#f0f3f1] bg-[#fafcfb]">
                  <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">DATE</th>
                  <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">CATEGORY</th>
                  <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">DESCRIPTION</th>
                  <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">VENDOR</th>
                  <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">PAYMENT</th>
                  <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">AMOUNT (₹)</th>
                  {(canWrite || canDelete) && (
                    <th className="px-5 py-3 text-center text-[10px] font-semibold text-[#7e8983]">ACTION</th>
                  )}
                </tr>
              </thead>
              <tbody>
                {expenses.map((e, i) => (
                  <tr key={e.id} className={`border-b border-[#f5f7f6] ${i % 2 === 0 ? "" : "bg-[#fafcfb]"}`}>
                    <td className="px-5 py-3 text-[12px] font-medium whitespace-nowrap">
                      {new Date(e.expense_date).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}
                    </td>
                    <td className="px-5 py-3">
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${CAT_COLORS[e.category] || CAT_COLORS.other}`}>
                        {CATEGORIES.find((c) => c.value === e.category)?.label ?? e.category}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-[12px] max-w-[200px] truncate">{e.description}</td>
                    <td className="px-5 py-3 text-[12px] text-[#6b7c75]">{e.vendor || "—"}</td>
                    <td className="px-5 py-3 text-[11px] capitalize text-[#6b7c75]">{e.payment_mode.replace("_", " ")}</td>
                    <td className="px-5 py-3 text-right text-[13px] font-bold">₹{fmt(e.amount)}</td>
                    {(canWrite || canDelete) && (
                      <td className="px-5 py-3 text-center">
                        <div className="flex items-center justify-center gap-2">
                          {canWrite && (
                            <button
                              onClick={() => openEdit(e)}
                              className="rounded-lg px-2 py-1 text-[11px] font-medium text-[#2d8b65] hover:bg-[#e6f4ec]"
                            >
                              Edit
                            </button>
                          )}
                          {canDelete && (
                            <button
                              onClick={() => handleDelete(e.id)}
                              className="rounded-lg px-2 py-1 text-[11px] font-medium text-[#b25248] hover:bg-[#fce9e7]"
                            >
                              Del
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add / Edit modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[480px] rounded-2xl bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">{editingId ? "Edit Expense" : "Add Expense"}</h3>
              <button onClick={() => setShowForm(false)}>
                <Icon name="close" size={18} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Date *</label>
                  <input
                    type="date"
                    required
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    value={form.expense_date}
                    onChange={(e) => setForm({ ...form, expense_date: e.target.value })}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Category *</label>
                  <select
                    required
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    value={form.category}
                    onChange={(e) => setForm({ ...form, category: e.target.value })}
                  >
                    {CATEGORIES.map((c) => (
                      <option key={c.value} value={c.value}>{c.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Description *</label>
                <input
                  required
                  className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                  placeholder="e.g. Concentrate feed purchase"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Amount (₹) *</label>
                  <input
                    required
                    type="number"
                    step="0.01"
                    min="0"
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    placeholder="5000"
                    value={form.amount}
                    onChange={(e) => setForm({ ...form, amount: e.target.value })}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Payment mode</label>
                  <select
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    value={form.payment_mode}
                    onChange={(e) => setForm({ ...form, payment_mode: e.target.value })}
                  >
                    {PAYMENT_MODES.map((m) => (
                      <option key={m} value={m}>{m.replace("_", " ")}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Vendor</label>
                  <input
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    placeholder="Vendor name"
                    value={form.vendor}
                    onChange={(e) => setForm({ ...form, vendor: e.target.value })}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Reference no.</label>
                  <input
                    className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                    placeholder="Bill / invoice no."
                    value={form.reference_no}
                    onChange={(e) => setForm({ ...form, reference_no: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Notes</label>
                <input
                  className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]"
                  placeholder="Optional notes..."
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60"
                >
                  {saving ? "Saving..." : editingId ? "Update" : "Add Expense"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
