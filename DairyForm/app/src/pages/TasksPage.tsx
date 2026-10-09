import { useEffect, useState } from "react";
import { tasksApi, usersApi, type Task } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

const PRIORITY_DOT: Record<string, string> = {
  high: "bg-[#df675a]",
  medium: "bg-[#e4ae58]",
  low: "bg-[#9aa7a1]",
};

const STATUS_BADGE: Record<string, string> = {
  pending: "bg-[#f0f3f1] text-[#6b7c75]",
  in_progress: "bg-[#e8f0fb] text-[#3a74c9]",
  done: "bg-[#e6f4ec] text-[#2d8b65]",
  overdue: "bg-[#fce9e7] text-[#b25248]",
};

export default function TasksPage() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [calendarData, setCalendarData] = useState<Record<string, Task[]>>({});
  const [users, setUsers] = useState<any[]>([]);
  const [view, setView] = useState<"list" | "calendar">("list");
  const [calMonth, setCalMonth] = useState(() => {
    const now = new Date();
    return { year: now.getFullYear(), month: now.getMonth() + 1 };
  });
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "feeding",
    priority: "medium",
    assigned_to_id: "",
    due_date: new Date().toISOString().split("T")[0],
    due_time: "",
  });

  const canCreate = ["admin", "manager"].includes(user?.role || "");

  const load = async (year = calMonth.year, month = calMonth.month) => {
    const promises: Promise<any>[] = [tasksApi.list(), tasksApi.calendar({ year, month })];
    if (canCreate) promises.push(usersApi.list());
    const results = await Promise.all(promises);
    setTasks(results[0].data);
    setCalendarData(results[1].data);
    if (canCreate && results[2]) setUsers(results[2].data);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const changeMonth = (delta: number) => {
    const d = new Date(calMonth.year, calMonth.month - 1 + delta, 1);
    const next = { year: d.getFullYear(), month: d.getMonth() + 1 };
    setCalMonth(next);
    load(next.year, next.month);
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await tasksApi.create({
        title: form.title,
        description: form.description || undefined,
        category: form.category,
        priority: form.priority,
        assigned_to_id: form.assigned_to_id ? Number(form.assigned_to_id) : undefined,
        due_date: form.due_date,
        due_time: form.due_time || undefined,
      });
      setShowForm(false);
      setForm({ title: "", description: "", category: "feeding", priority: "medium", assigned_to_id: "", due_date: new Date().toISOString().split("T")[0], due_time: "" });
      load();
    } finally {
      setSaving(false);
    }
  };

  const updateStatus = async (taskId: number, status: string) => {
    await tasksApi.update(taskId, { status });
    setTasks((prev) => prev.map((t) => (t.id === taskId ? { ...t, status } : t)));
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  const calendarDates = Object.keys(calendarData).sort();

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Task Calendar</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{tasks.filter((t) => t.status === "pending").length} pending tasks</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex rounded-xl border border-[#e4e9e6] bg-white p-0.5">
            {(["list", "calendar"] as const).map((v) => (
              <button key={v} onClick={() => setView(v)} className={`rounded-lg px-3 py-1.5 text-xs font-semibold capitalize transition ${view === v ? "bg-[#1d7352] text-white shadow-sm" : "text-[#6b7c75] hover:text-[#17221d]"}`}>{v}</button>
            ))}
          </div>
          {canCreate && (
            <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
              <Icon name="plus" size={15} /> New task
            </button>
          )}
        </div>
      </div>

      {view === "list" ? (
        <div className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
          <div className="divide-y divide-[#f5f7f6]">
            {tasks.length === 0 ? (
              <div className="flex h-32 items-center justify-center text-sm text-[#9aa7a1]">No tasks found</div>
            ) : tasks.map((task) => (
              <div key={task.id} className="flex items-start gap-4 px-5 py-4">
                <div className={`mt-1.5 size-2 flex-none rounded-full ${PRIORITY_DOT[task.priority] || "bg-[#9aa7a1]"}`} />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-[13px] font-semibold truncate">{task.title}</span>
                    <span className={`hidden shrink-0 rounded-full px-2 py-0.5 text-[9px] font-semibold capitalize sm:inline ${STATUS_BADGE[task.status] || ""}`}>{task.status.replace("_", " ")}</span>
                  </div>
                  <div className="mt-0.5 text-[11px] text-[#6b7c75] capitalize">
                    {task.category} · {task.due_date}{task.due_time ? ` ${task.due_time}` : ""}
                  </div>
                </div>
                {task.status === "pending" && (
                  <button onClick={() => updateStatus(task.id, "in_progress")} className="shrink-0 rounded-lg border border-[#dce1de] px-2 py-1 text-[10px] font-semibold text-[#3a74c9] hover:bg-[#e8f0fb]">Start</button>
                )}
                {task.status === "in_progress" && (
                  <button onClick={() => updateStatus(task.id, "done")} className="shrink-0 rounded-lg border border-[#d0ead9] px-2 py-1 text-[10px] font-semibold text-[#2d8b65] hover:bg-[#e6f4ec]">Done</button>
                )}
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between rounded-2xl border border-[#e4e9e6] bg-white px-5 py-3">
            <button onClick={() => changeMonth(-1)} className="rounded-lg border border-[#e4e9e6] px-3 py-1.5 text-xs font-semibold hover:bg-[#f5f7f6]">← Prev</button>
            <span className="text-sm font-bold">
              {new Date(calMonth.year, calMonth.month - 1).toLocaleString("en-IN", { month: "long", year: "numeric" })}
            </span>
            <button onClick={() => changeMonth(1)} className="rounded-lg border border-[#e4e9e6] px-3 py-1.5 text-xs font-semibold hover:bg-[#f5f7f6]">Next →</button>
          </div>
          {calendarDates.length === 0 ? (
            <div className="flex h-32 items-center justify-center rounded-2xl border border-[#e4e9e6] bg-white text-sm text-[#9aa7a1]">No tasks scheduled</div>
          ) : calendarDates.map((date) => (
            <div key={date} className="rounded-2xl border border-[#e4e9e6] bg-white overflow-hidden">
              <div className="border-b border-[#f0f3f1] px-5 py-3">
                <span className="text-[12px] font-bold">
                  {new Date(date).toLocaleDateString("en-IN", { weekday: "long", day: "numeric", month: "long" })}
                </span>
                {date === new Date().toISOString().split("T")[0] && (
                  <span className="ml-2 rounded-full bg-[#e6f4ec] px-2 py-0.5 text-[9px] font-semibold text-[#2d8b65]">Today</span>
                )}
              </div>
              <div className="divide-y divide-[#f5f7f6]">
                {(calendarData[date] || []).map((task) => (
                  <div key={task.id} className="flex items-center gap-3 px-5 py-3">
                    <div className={`size-1.5 rounded-full ${PRIORITY_DOT[task.priority]}`} />
                    <div className="flex-1 min-w-0">
                      <div className="text-[12px] font-medium truncate">{task.title}</div>
                      <div className="text-[10px] text-[#8c9691] capitalize">{task.category}{task.due_time ? ` · ${task.due_time}` : ""}</div>
                    </div>
                    <span className={`rounded-full px-2 py-0.5 text-[9px] font-semibold capitalize ${STATUS_BADGE[task.status] || ""}`}>{task.status.replace("_", " ")}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create task modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">New Task</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Title *</label>
                <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Morning milking — all animals" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Description</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Optional details..." />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Category</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                    {["feeding", "milking", "health", "cleaning", "veterinary", "quality_check", "general"].map((c) => (
                      <option key={c} value={c}>{c.replace("_", " ")}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Priority</label>
                  <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
                    <option value="low">Low</option>
                    <option value="medium">Medium</option>
                    <option value="high">High</option>
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Due date</label>
                  <input type="date" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.due_date} onChange={(e) => setForm({ ...form, due_date: e.target.value })} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Time</label>
                  <input type="time" className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.due_time} onChange={(e) => setForm({ ...form, due_time: e.target.value })} />
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Assign to</label>
                <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.assigned_to_id} onChange={(e) => setForm({ ...form, assigned_to_id: e.target.value })}>
                  <option value="">— Unassigned —</option>
                  {users.map((u) => <option key={u.id} value={u.id}>{u.name} ({u.role.replace("_", " ")})</option>)}
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Saving..." : "Create Task"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
