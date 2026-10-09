import { useEffect, useState } from "react";
import { usersApi, type User } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const ROLE_COLORS: Record<string, string> = {
  admin: "bg-[#fce9e7] text-[#b25248]",
  manager: "bg-[#e8f0fb] text-[#3a74c9]",
  vet: "bg-[#f0ebf7] text-[#7a5ba8]",
  data_entry: "bg-[#fff8e7] text-[#9a6a1f]",
  quality: "bg-[#e6f4ec] text-[#2d8b65]",
  monitor: "bg-[#f0f3f1] text-[#6b7c75]",
};

export default function UsersPage() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    email: "",
    name: "",
    role: "data_entry",
    password: "",
    phone: "",
  });
  const [error, setError] = useState("");

  const load = async () => {
    const { data } = await usersApi.list();
    setUsers(data);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      await usersApi.create({
        email: form.email,
        name: form.name,
        role: form.role,
        password: form.password,
        phone: form.phone || undefined,
      });
      setShowForm(false);
      setForm({ email: "", name: "", role: "data_entry", password: "", phone: "" });
      load();
    } catch (err: any) {
      setError(err?.response?.data?.detail || "Failed to create user");
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (userId: number) => {
    if (!confirm("Deactivate this user? They won't be able to log in.")) return;
    await usersApi.deactivate(userId);
    setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, is_active: false } : u)));
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">User Management</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{users.filter((u) => u.is_active).length} active staff accounts</p>
        </div>
        <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
          <Icon name="plus" size={15} /> Add user
        </button>
      </div>

      <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[#f0f3f1] bg-[#fafcfb]">
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">USER</th>
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983]">ROLE</th>
                <th className="px-5 py-3 text-left text-[10px] font-semibold text-[#7e8983] hidden sm:table-cell">EMAIL</th>
                <th className="px-5 py-3 text-center text-[10px] font-semibold text-[#7e8983]">STATUS</th>
                <th className="px-5 py-3 text-right text-[10px] font-semibold text-[#7e8983]">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u, i) => (
                <tr key={u.id} className={`border-b border-[#f5f7f6] ${i % 2 === 0 ? "" : "bg-[#fafcfb]"}`}>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-3">
                      <div className="flex size-8 items-center justify-center rounded-full bg-[#dff0e8] text-[11px] font-bold text-[#1f7a57]">
                        {u.avatar_initials || u.name?.slice(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <div className="text-[12px] font-semibold">{u.name}</div>
                        <div className="text-[10px] text-[#8c9691] sm:hidden">{u.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold capitalize ${ROLE_COLORS[u.role] || "bg-[#f0f3f1] text-[#6b7c75]"}`}>
                      {u.role.replace("_", " ")}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-[12px] text-[#6b7c75] hidden sm:table-cell">{u.email}</td>
                  <td className="px-5 py-3 text-center">
                    <span className={`rounded-full px-2 py-0.5 text-[9px] font-semibold ${u.is_active ? "bg-[#e6f4ec] text-[#2d8b65]" : "bg-[#fce9e7] text-[#b25248]"}`}>
                      {u.is_active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-right">
                    {u.id !== me?.id && u.is_active && (
                      <button onClick={() => handleDeactivate(u.id)} className="text-[11px] font-semibold text-[#b25248] hover:underline">Deactivate</button>
                    )}
                    {u.id === me?.id && (
                      <span className="text-[10px] text-[#9aa7a1]">You</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create user modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Add Staff Account</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Full name *</label>
                  <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Raj Kumar" />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Role</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                    <option value="manager">Manager</option>
                    <option value="vet">Vet</option>
                    <option value="data_entry">Data Entry</option>
                    <option value="quality">Quality</option>
                    <option value="monitor">Monitor</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Email *</label>
                <input required type="email" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="raj@bhavani.farm" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Password *</label>
                <input required type="password" minLength={6} className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Min. 6 characters" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Phone</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="+91 98765 43210" />
              </div>
              {error && (
                <div className="flex items-center gap-2 rounded-xl bg-[#fbe9e6] px-4 py-3 text-xs text-[#b25248]">
                  <Icon name="alert" size={14} />{error}
                </div>
              )}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Creating..." : "Create User"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
