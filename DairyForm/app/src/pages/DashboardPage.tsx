import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { dashboardApi, milkApi, tasksApi, type DashboardStats } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Icon from "../components/Icon";

interface DailySummary {
  date: string;
  morning: number;
  evening: number;
  total: number;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [dailyMilk, setDailyMilk] = useState<DailySummary[]>([]);
  const [myTasks, setMyTasks] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 17) return "Good afternoon";
    return "Good evening";
  };

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, milkRes, tasksRes] = await Promise.all([
          dashboardApi.stats(),
          milkApi.daily({ days: 7 }),
          tasksApi.myToday(),
        ]);
        setStats(statsRes.data);
        setDailyMilk(milkRes.data);
        setMyTasks(tasksRes.data);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" />
      </div>
    );
  }

  const maxMilk = Math.max(...dailyMilk.map((d) => d.total), 1);
  const chartH = 120;

  const today = new Date().toLocaleDateString("en-IN", {
    weekday: "long", day: "numeric", month: "long",
  }).toUpperCase();

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      {/* Greeting */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="text-xs font-semibold text-[#28815f]">{today}</div>
          <h1 className="mt-1 text-[26px] font-bold tracking-[-0.04em]">
            {greeting()}, {user?.name?.split(" ")[0]}
          </h1>
          <p className="mt-1 text-sm text-[#7d8983]">
            40 Murrah buffaloes · Srikakulam, AP
          </p>
        </div>
        <Link
          to="/milk"
          className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-[#185f45]"
        >
          <Icon name="plus" size={15} /> Log milk
        </Link>
      </div>

      {/* Alert bar */}
      {stats?.alerts && stats.alerts.length > 0 && (
        <div className="mb-5 space-y-2">
          {stats.alerts.slice(0, 6).map((alert, i) => {
            const isHigh = alert.severity === "high";
            const typeIcon = alert.type === "vaccination" ? "shield"
              : alert.type === "calving" ? "cow"
              : alert.type === "repro" ? "heart"
              : "alert";
            return (
              <div
                key={i}
                className={`flex items-center gap-3 rounded-xl px-4 py-3 text-[12px] font-medium ${
                  isHigh ? "bg-[#fbe9e6] text-[#b25248]" : "bg-[#fff8e7] text-[#9a6a1f]"
                }`}
              >
                <Icon name={typeIcon as any} size={15} />
                {alert.message}
              </div>
            );
          })}
          {stats.alerts.length > 6 && (
            <div className="px-4 text-[11px] text-[#9aa7a1]">+{stats.alerts.length - 6} more alerts</div>
          )}
        </div>
      )}

      {/* KPI cards */}
      <section className="grid grid-cols-2 gap-3 xl:grid-cols-4">
        {[
          {
            label: "Total herd",
            value: String(stats?.total_animals ?? 0),
            unit: "buffaloes",
            sub: `${stats?.lactating ?? 0} lactating`,
            icon: "cow" as const,
            color: "green",
          },
          {
            label: "Milk today",
            value: stats?.milk_today_litres ? stats.milk_today_litres.toFixed(0) : "—",
            unit: "litres",
            sub: stats?.milk_trend_pct
              ? `${stats.milk_trend_pct > 0 ? "↑" : "↓"} ${Math.abs(stats.milk_trend_pct)}% vs yesterday`
              : "No data yet",
            icon: "milk" as const,
            color: "purple",
          },
          {
            label: "Tasks today",
            value: String(stats?.tasks_today ?? 0),
            unit: "pending",
            sub: stats?.tasks_overdue ? `${stats.tasks_overdue} overdue` : "All on track",
            icon: "calendar" as const,
            color: stats?.tasks_overdue ? "amber" : "blue",
          },
          {
            label: "Milk quality",
            value: stats?.quality_grade_today ?? "—",
            unit: "grade",
            sub: stats?.avg_fat_today
              ? `Fat ${stats.avg_fat_today.toFixed(1)}% · SNF ${stats.avg_snf_today?.toFixed(1)}%`
              : "No test today",
            icon: "shield" as const,
            color: stats?.quality_grade_today === "A" ? "green" : stats?.quality_grade_today === "B" ? "amber" : "green",
          },
        ].map((card) => (
          <div key={card.label} className="rounded-2xl border border-[#e4e9e6] bg-white p-4 sm:p-5">
            <div className="flex items-start justify-between">
              <div className="text-[11px] font-semibold text-[#7e8983]">{card.label}</div>
              <div className={`icon-tile ${card.color}`}>
                <Icon name={card.icon} size={16} />
              </div>
            </div>
            <div className="mt-3 flex items-baseline gap-1.5">
              <span className="text-[26px] font-bold tracking-[-0.04em]">{card.value}</span>
              <span className="text-[10px] text-[#8d9792]">{card.unit}</span>
            </div>
            <div className="mt-1.5 text-[10px] text-[#7d8983]">{card.sub}</div>
          </div>
        ))}
      </section>

      {/* Milk chart + Herd health */}
      <section className="mt-4 grid gap-4 xl:grid-cols-[1.6fr_1fr]">
        {/* Milk chart */}
        <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5 sm:p-6">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="section-title">Milk production</h2>
              <p className="section-subtitle">Daily yield — last 7 days</p>
            </div>
            <Link to="/milk" className="text-[10px] font-semibold text-[#287b5a]">View all →</Link>
          </div>
          {dailyMilk.length > 0 ? (
            <>
              <div className="mt-4 flex items-end gap-2">
                <span className="text-2xl font-bold tracking-[-0.04em]">
                  {dailyMilk.reduce((s, d) => s + d.total, 0).toFixed(0)} L
                </span>
                <span className="mb-1 rounded-full bg-[#e6f4ec] px-2 py-0.5 text-[9px] font-semibold text-[#247353]">
                  7-day total
                </span>
              </div>
              <div className="mt-5 flex items-end gap-2" style={{ height: `${chartH}px` }}>
                {dailyMilk.slice(-7).map((d) => {
                  const h = Math.max((d.total / maxMilk) * chartH, 8);
                  const label = new Date(d.date).toLocaleDateString("en-IN", { weekday: "short" });
                  return (
                    <div key={d.date} className="flex flex-1 flex-col items-center gap-1">
                      <div className="w-full rounded-t-lg bg-[#dff0e8] transition-all" style={{ height: `${h}px` }}>
                        <div
                          className="w-full rounded-t-lg bg-[#2d8b65] transition-all"
                          style={{ height: `${d.total > 0 ? (d.morning / d.total) * h : 0}px` }}
                        />
                      </div>
                      <span className="text-[9px] text-[#9ca59f]">{label}</span>
                    </div>
                  );
                })}
              </div>
              <div className="mt-3 flex gap-4 text-[9px]">
                <span className="flex items-center gap-1"><span className="size-2 rounded-full bg-[#2d8b65]"></span> Morning</span>
                <span className="flex items-center gap-1"><span className="size-2 rounded-full bg-[#dff0e8]"></span> Evening</span>
              </div>
            </>
          ) : (
            <div className="mt-8 flex h-32 items-center justify-center text-sm text-[#9aa7a1]">
              No milk records found. <Link to="/milk" className="ml-1 text-[#287b5a] font-semibold">Log today's milk →</Link>
            </div>
          )}
        </div>

        {/* Herd health */}
        <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5 sm:p-6">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="section-title">Herd status</h2>
              <p className="section-subtitle">Current distribution</p>
            </div>
            <Link to="/animals" className="text-[10px] font-semibold text-[#287b5a]">View all</Link>
          </div>
          <div className="mt-6 space-y-3">
            {[
              { label: "Lactating", count: stats?.lactating ?? 0, color: "#2d8b65", bg: "#e8f4ee" },
              { label: "Pregnant", count: stats?.pregnant ?? 0, color: "#4a90d9", bg: "#e8f0fb" },
              { label: "Dry", count: stats?.dry ?? 0, color: "#e4ae58", bg: "#fef6e4" },
              { label: "Sick / Treatment", count: stats?.sick ?? 0, color: "#df675a", bg: "#fce9e7" },
            ].map((row) => (
              <div key={row.label} className="flex items-center gap-3">
                <div className="flex size-7 items-center justify-center rounded-lg text-[10px] font-bold" style={{ background: row.bg, color: row.color }}>
                  {row.count}
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between text-[11px]">
                    <span className="font-medium text-[#3d4d47]">{row.label}</span>
                    <span className="text-[#9aa7a1]">{Math.round((row.count / (stats?.total_animals || 40)) * 100)}%</span>
                  </div>
                  <div className="mt-1 h-1.5 rounded-full bg-[#f0f3f1]">
                    <div
                      className="h-full rounded-full transition-all"
                      style={{
                        width: `${Math.round((row.count / (stats?.total_animals || 40)) * 100)}%`,
                        background: row.color,
                      }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
          {(stats?.sick ?? 0) > 0 && (
            <Link
              to="/health"
              className="mt-5 flex items-center gap-2 rounded-xl bg-[#fce9e7] px-4 py-3 text-[11px] font-semibold text-[#b25248]"
            >
              <Icon name="alert" size={13} />
              {stats?.sick} animal(s) need attention
              <Icon name="chevron" size={13} className="ml-auto" />
            </Link>
          )}
        </div>
      </section>

      {/* My Tasks today */}
      <section className="mt-4 rounded-2xl border border-[#e4e9e6] bg-white p-5 sm:p-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="section-title">Today&apos;s tasks</h2>
            <p className="section-subtitle">Assigned to you</p>
          </div>
          <Link to="/tasks" className="text-[10px] font-semibold text-[#287b5a]">View all →</Link>
        </div>
        {myTasks.length === 0 ? (
          <div className="flex items-center gap-3 rounded-xl bg-[#f7f9f8] px-4 py-4 text-sm text-[#8c9691]">
            <Icon name="check" size={18} className="text-[#2d8b65]" />
            All tasks done for today!
          </div>
        ) : (
          <div className="space-y-2">
            {myTasks.slice(0, 5).map((task) => (
              <div key={task.id} className="flex items-center gap-3 rounded-xl border border-[#edf0ee] px-4 py-3">
                <div
                  className={`size-2 rounded-full ${
                    task.priority === "high" ? "bg-[#df675a]" : task.priority === "medium" ? "bg-[#e4ae58]" : "bg-[#9aa7a1]"
                  }`}
                />
                <div className="flex-1 min-w-0">
                  <div className="truncate text-[12px] font-semibold">{task.title}</div>
                  <div className="text-[10px] text-[#8c9691] capitalize">{task.category} {task.due_time ? `· ${task.due_time}` : ""}</div>
                </div>
                <span className="rounded-full bg-[#f0f3f1] px-2 py-0.5 text-[9px] font-medium capitalize text-[#6b7c75]">
                  {task.status}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
