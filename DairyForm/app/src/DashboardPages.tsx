import { useMemo, useState } from "react";

type PageName = "Herd management" | "Machinery" | "Inventory" | "Finance" | "Analytics" | string;

function MiniIcon({ name, size = 18 }: { name: string; size?: number }) {
  const path: Record<string, React.ReactNode> = {
    cow: <><path d="M7 10c-2-1-3-3-2-5 2 0 3 1 4 3h6c1-2 2-3 4-3 1 2 0 4-2 5v5c0 3-2 5-5 5s-5-2-5-5v-5Z"/><path d="M9 14h.01M15 14h.01M10 17h4"/></>,
    plus: <path d="M12 5v14M5 12h14"/>,
    filter: <><path d="M4 6h16M7 12h10M10 18h4"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    arrow: <path d="m9 18 6-6-6-6"/>,
    dots: <><circle cx="5" cy="12" r="1" fill="currentColor"/><circle cx="12" cy="12" r="1" fill="currentColor"/><circle cx="19" cy="12" r="1" fill="currentColor"/></>,
    gear: <><circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4.9 4.9 7 7M17 17l2.1 2.1M2 12h3M19 12h3M4.9 19.1 7 17M17 7l2.1-2.1"/></>,
    bolt: <path d="m13 2-9 12h8l-1 8 9-12h-8l1-8Z"/>,
    water: <path d="M12 2S6 9 6 14a6 6 0 0 0 12 0c0-5-6-12-6-12Z"/>,
    box: <><path d="m4 7 8-4 8 4-8 4-8-4Z"/><path d="m4 7 8 4 8-4v10l-8 4-8-4V7Z"/></>,
    money: <><rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="12" cy="12" r="3"/><path d="M7 9H6M18 15h-1"/></>,
    chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></>,
    download: <><path d="M12 3v12M7 10l5 5 5-5M4 21h16"/></>,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/></>,
    alert: <><path d="M12 3 2.5 20h19L12 3Z"/><path d="M12 9v5M12 17h.01"/></>,
    check: <path d="m5 12 4 4L19 6"/>,
    clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
    milk: <><path d="M8 3h8l1 5v12H7V8l1-5Z"/><path d="M7 9h10M10 3v6"/></>,
    wifi: <><path d="M5 13a10 10 0 0 1 14 0M1.5 9.5a15 15 0 0 1 21 0M8.5 16.5a5 5 0 0 1 7 0"/><circle cx="12" cy="20" r="1" fill="currentColor" stroke="none"/></>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{path[name]}</svg>;
}

function PageHeader({ eyebrow, title, description, action, onAction }: { eyebrow: string; title: string; description: string; action: string; onAction?: () => void }) {
  return <div className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
    <div><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-[#28815f]">{eyebrow}</div><h1 className="mt-1.5 text-[27px] font-bold tracking-[-0.04em]">{title}</h1><p className="mt-1 text-xs text-[#7d8983]">{description}</p></div>
    <button onClick={onAction} className="flex items-center justify-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-[#185f45]"><MiniIcon name="plus" size={16}/>{action}</button>
  </div>;
}

function Stat({ label, value, detail, icon, tone = "green" }: { label: string; value: string; detail: string; icon: string; tone?: string }) {
  return <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
    <div className="flex items-center justify-between"><span className="text-[11px] font-semibold text-[#7d8983]">{label}</span><span className={`page-icon ${tone}`}><MiniIcon name={icon} size={16}/></span></div>
    <div className="mt-3 text-[25px] font-bold tracking-[-0.04em]">{value}</div><div className="mt-1 text-[9px] text-[#77847d]">{detail}</div>
  </div>;
}

function SearchBar({ placeholder }: { placeholder: string }) {
  return <div className="flex items-center gap-2 rounded-xl border border-[#e4e9e6] bg-white px-3 py-2.5 text-[#8e9993]"><MiniIcon name="search" size={16}/><input className="w-full bg-transparent text-[11px] outline-none" placeholder={placeholder}/></div>;
}

// 40 Murrah buffalo data
const herd = Array.from({ length: 40 }, (_, i) => {
  const n = i + 1;
  const id = `B-${String(n).padStart(2, "0")}`;
  const names = ["Kamala","Meena","Durga","Saraswati","Lakshmi","Bhavani","Parvati","Radha","Sita","Ganga","Yamuna","Kaveri","Krishna","Tulasi","Nandini","Gauri","Renuka","Ambika","Shanthi","Vijaya","Padma","Sudha","Uma","Priya","Anitha","Kalyani","Sowmya","Revathi","Mythili","Janaki","Hamsa","Vasantha","Manjula","Asha","Chandra","Tara","Vasumathi","Indira","Komala","Hema"];
  const breeds = ["Murrah", "Murrah", "Murrah", "Murrah", "Murrah"];
  const stages = ["Lactating","Lactating","Lactating","Pregnant","Dry"];
  const health = ["Healthy","Healthy","Healthy","Healthy","Healthy","Healthy","Attention","Treatment"];
  const stage = stages[n % 5 === 0 ? 4 : n % 4 === 0 ? 3 : 1];
  const h = health[n % 8];
  const milk = stage === "Lactating" ? `${(10 + (n % 6)).toFixed(1)} L` : "—";
  return { id, name: names[i], breed: breeds[0], age: `${2 + (n % 6)}y ${n % 12}m`, stage, milk, health: h, avatar: names[i].slice(0,2).toUpperCase() };
});

function HerdPage({ notify }: { notify: (v: string) => void }) {
  const [filter, setFilter] = useState("All");
  const [page, setPage] = useState(0);
  const PER_PAGE = 10;
  const filtered = filter === "All" ? herd : filter === "Attention" ? herd.filter(a => a.health !== "Healthy") : herd.filter(a => a.stage === filter);
  const visible = filtered.slice(page * PER_PAGE, (page + 1) * PER_PAGE);
  const totalPages = Math.ceil(filtered.length / PER_PAGE);

  return <>
    <PageHeader eyebrow="Livestock" title="Herd management" description="40 Murrah buffaloes — health, milk, breeding, and daily care records." action="Add buffalo" onAction={() => notify("New buffalo form ready")}/>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Stat label="Total buffaloes" value="40" detail="All Murrah breed" icon="cow"/>
      <Stat label="Lactating" value="24" detail="~12.3 L/day average" icon="milk" tone="purple"/>
      <Stat label="Pregnant" value="10" detail="4 due within 30 days" icon="calendar" tone="amber"/>
      <Stat label="Health alerts" value="6" detail="2 under treatment" icon="alert" tone="red"/>
    </div>
    <div className="mt-4 grid gap-4 xl:grid-cols-[1fr_280px]">
      <div className="overflow-hidden rounded-2xl border border-[#e4e9e6] bg-white">
        <div className="flex flex-col gap-3 border-b border-[#ebeeec] p-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex gap-1 overflow-x-auto">
            {["All","Lactating","Pregnant","Attention"].map((v) => <button key={v} onClick={() => { setFilter(v); setPage(0); }} className={`whitespace-nowrap rounded-lg px-3 py-2 text-[10px] font-semibold ${filter === v ? "bg-[#e9f4ef] text-[#1f7453]" : "text-[#7d8983]"}`}>{v}</button>)}
          </div>
          <div className="flex gap-2"><SearchBar placeholder="Search buffalo..."/><button className="rounded-xl border border-[#e4e9e6] p-2.5 text-[#6f7c75]"><MiniIcon name="filter" size={16}/></button></div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[700px] text-left">
            <thead><tr className="bg-[#fafbfa] text-[9px] uppercase tracking-wider text-[#919b96]"><th className="px-5 py-3 font-semibold">Buffalo</th><th className="px-4 py-3 font-semibold">Breed</th><th className="px-4 py-3 font-semibold">Age</th><th className="px-4 py-3 font-semibold">Stage</th><th className="px-4 py-3 font-semibold">Today's milk</th><th className="px-4 py-3 font-semibold">Health</th><th></th></tr></thead>
            <tbody>
              {visible.map((animal) => (
                <tr key={animal.id} className="border-t border-[#eef1ef] text-[11px] hover:bg-[#fbfcfb]">
                  <td className="px-5 py-3.5"><div className="flex items-center gap-3"><span className="flex size-9 items-center justify-center rounded-xl bg-[#ecf3ef] text-[9px] font-bold text-[#31785c]">{animal.avatar}</span><div><b>{animal.name}</b><div className="mt-0.5 text-[9px] text-[#97a09b]">#{animal.id}</div></div></div></td>
                  <td className="px-4 text-[#65716b]">{animal.breed}</td>
                  <td className="px-4 text-[#65716b]">{animal.age}</td>
                  <td className="px-4"><span className="rounded-full bg-[#f1f4f2] px-2.5 py-1 text-[9px]">{animal.stage}</span></td>
                  <td className="px-4 font-semibold">{animal.milk}</td>
                  <td className="px-4"><Status value={animal.health}/></td>
                  <td className="px-4"><MiniIcon name="dots" size={16}/></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between border-t border-[#eef1ef] px-5 py-3">
          <span className="text-[9px] text-[#8d9792]">Showing {page * PER_PAGE + 1}–{Math.min((page + 1) * PER_PAGE, filtered.length)} of {filtered.length}</span>
          <div className="flex gap-2">
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="rounded-lg border border-[#e4e9e6] px-3 py-1.5 text-[9px] font-semibold disabled:opacity-40">← Prev</button>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="rounded-lg border border-[#e4e9e6] px-3 py-1.5 text-[9px] font-semibold disabled:opacity-40">Next →</button>
          </div>
        </div>
      </div>
      <div className="space-y-4">
        <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
          <h3 className="text-xs font-bold">Upcoming care</h3><p className="mt-1 text-[9px] text-[#8d9792]">Next 7 days</p>
          <div className="mt-5 space-y-4">
            {[["Vaccination (FMD)", "5 buffaloes", "Tomorrow"], ["Pregnancy check", "4 buffaloes", "3 Oct"], ["Deworming", "40 buffaloes", "5 Oct"]].map((v) => (
              <div className="flex items-center gap-3" key={v[0]}>
                <span className="page-icon green"><MiniIcon name="calendar" size={14}/></span>
                <div className="flex-1"><div className="text-[10px] font-semibold">{v[0]}</div><div className="text-[9px] text-[#929c97]">{v[1]}</div></div>
                <span className="text-[9px] text-[#7b8781]">{v[2]}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="rounded-2xl bg-[#1c4637] p-5 text-white">
          <div className="text-xs font-semibold">Average milk yield</div>
          <div className="mt-3 text-3xl font-bold">12.3 L</div>
          <div className="mt-1 text-[9px] text-white/55">per lactating buffalo / day</div>
          <div className="mt-4 h-1.5 rounded-full bg-white/10"><div className="h-full w-[82%] rounded-full bg-[#7fc2a5]"></div></div>
          <div className="mt-2 text-[9px] text-white/40">Target: 15 L · 82% achieved</div>
        </div>
      </div>
    </div>
  </>;
}

function Status({ value }: { value: string }) {
  const style = value === "Healthy" || value === "Online" || value === "In stock" || value === "Completed" ? "bg-[#e8f4ee] text-[#257653]" : value === "Attention" || value === "Low stock" || value === "Scheduled" ? "bg-[#fff2dc] text-[#9b6a22]" : value === "Treatment" || value === "Offline" || value === "Critical" ? "bg-[#fce9e6] text-[#b44f44]" : "bg-[#eef1ef] text-[#6c7771]";
  return <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[9px] font-semibold ${style}`}><span className="size-1.5 rounded-full bg-current"/>{value}</span>;
}

const initialMachines = [
  { name: "TMR feed wagon", location: "Shed · all sections", status: "Online", mode: "Auto · 05:45 & 16:00", metric: "40 / 40 fed", icon: "box" },
  { name: "High-pressure pump", location: "Shed cleaning", status: "Online", mode: "Manual on demand", metric: "62 L/min", icon: "water" },
  { name: "Dung separator", location: "Collection pit", status: "Idle", mode: "Scheduled 14:30", metric: "Cycle 2 / 4", icon: "gear" },
  { name: "Milking machine", location: "Milking parlour", status: "Online", mode: "AM / PM cycle", metric: "32 / 40 stalls", icon: "milk" },
  { name: "Biogas plant", location: "Rear compound", status: "Online", mode: "Continuous", metric: "30 m³ capacity", icon: "bolt" },
  { name: "Backup generator", location: "Utility shed", status: "Offline", mode: "Standby", metric: "Diesel 87%", icon: "bolt" },
];

function MachineryPage({ notify }: { notify: (v: string) => void }) {
  const [items, setItems] = useState(initialMachines);
  const toggle = (name: string) => setItems((all) => all.map((x) => x.name === name ? { ...x, status: x.status === "Online" ? "Offline" : "Online" } : x));
  return <>
    <PageHeader eyebrow="Automation center" title="Machinery & controls" description="Monitor and control all farm equipment — milking, feeding, cleaning, and biogas." action="Add equipment" onAction={() => notify("Equipment form ready")}/>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Stat label="Connected devices" value="8" detail="7 currently online" icon="gear"/>
      <Stat label="Power usage" value="62 kWh" detail="8% below average" icon="bolt" tone="blue"/>
      <Stat label="Water pumped" value="6.8 kL" detail="Shed + drinking today" icon="water" tone="purple"/>
      <Stat label="Biogas generated" value="18 m³" detail="Equiv. 7.2 kg LPG" icon="check" tone="amber"/>
    </div>
    <div className="mt-4 rounded-2xl border border-[#e4e9e6] bg-white p-5">
      <div className="flex items-center justify-between"><div><h3 className="text-sm font-bold">Equipment status</h3><p className="mt-1 text-[9px] text-[#8c9691]">Live controls and operating details</p></div><button className="flex items-center gap-2 rounded-lg border border-[#e3e8e5] px-3 py-2 text-[10px] font-semibold"><MiniIcon name="filter" size={14}/>Filter</button></div>
      <div className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {items.map((item) => (
          <div className="rounded-xl border border-[#e9edea] p-4" key={item.name}>
            <div className="flex items-start gap-3">
              <span className="page-icon green"><MiniIcon name={item.icon}/></span>
              <div className="flex-1"><div className="text-[11px] font-bold">{item.name}</div><div className="mt-0.5 text-[9px] text-[#939d98]">{item.location}</div></div>
              <Status value={item.status}/>
            </div>
            <div className="my-4 h-px bg-[#edf0ee]"/>
            <div className="flex items-end justify-between">
              <div><div className="text-[9px] text-[#929b97]">{item.mode}</div><div className="mt-1 text-[11px] font-semibold">{item.metric}</div></div>
              <button onClick={() => toggle(item.name)} className={`relative h-6 w-11 rounded-full transition ${item.status === "Online" ? "bg-[#2f8a65]" : "bg-[#cbd2ce]"}`}><span className={`absolute top-1 size-4 rounded-full bg-white shadow transition ${item.status === "Online" ? "left-6" : "left-1"}`}/></button>
            </div>
          </div>
        ))}
      </div>
    </div>
    <div className="mt-4 grid gap-4 lg:grid-cols-2">
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <h3 className="text-sm font-bold">Daily automation schedule</h3>
        <div className="mt-4 space-y-3">
          {[["Morning TMR feed", "Daily · 05:45", "Completed"], ["Shed pressure cleaning", "Daily · 07:00", "Completed"], ["Afternoon TMR feed", "Daily · 16:00", "Scheduled"], ["Dung separator cycle", "Daily · 14:30", "Scheduled"], ["Evening milking", "Daily · 17:30", "Scheduled"]].map((x) => (
            <div className="flex items-center gap-3 rounded-xl bg-[#f8faf9] p-3" key={x[0]}>
              <span className="page-icon blue"><MiniIcon name="clock" size={14}/></span>
              <div className="flex-1"><div className="text-[10px] font-semibold">{x[0]}</div><div className="text-[9px] text-[#909a95]">{x[1]}</div></div>
              <Status value={x[2]}/>
            </div>
          ))}
        </div>
      </div>
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <h3 className="text-sm font-bold">Energy consumption</h3><p className="mt-1 text-[9px] text-[#8d9792]">Today by equipment group</p>
        <Bars data={[["Milking system", 38], ["Cooling fans", 28], ["Water pumps", 20], ["TMR wagon", 14]]}/>
      </div>
    </div>
  </>;
}

const inventory = [
  ["Maize silage", "TMR Feed", "8,400 kg", "12,000 kg", "In stock"],
  ["Dry fodder (bajra straw)", "TMR Feed", "2,840 kg", "6,000 kg", "Low stock"],
  ["Cattle concentrate", "TMR Feed", "680 kg", "2,400 kg", "Low stock"],
  ["Mineral mixture", "Supplements", "48 kg", "300 kg", "Low stock"],
  ["FMD vaccine", "Medicine", "55 vials", "60 vials", "In stock"],
  ["Mastitis treatment", "Medicine", "8 units", "30 units", "Critical"],
  ["Cleaning concentrate", "Supplies", "6 cans", "30 cans", "Low stock"],
  ["AI straws (Murrah)", "Breeding", "28 straws", "50 straws", "In stock"],
];

function InventoryPage({ notify }: { notify: (v: string) => void }) {
  const [category, setCategory] = useState("All items");
  const rows = category === "All items" ? inventory : inventory.filter((x) => x[1] === category);
  return <>
    <PageHeader eyebrow="Stock & purchasing" title="Inventory" description="TMR feed, medicines, supplements, and supplies for 40 Murrah buffaloes." action="Add inventory" onAction={() => notify("Inventory item form ready")}/>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Stat label="Inventory value" value="₹3.24L" detail="+₹12,400 this month" icon="money"/>
      <Stat label="Total items" value="42" detail="Across 5 categories" icon="box" tone="blue"/>
      <Stat label="Low stock" value="5" detail="3 require ordering" icon="alert" tone="amber"/>
      <Stat label="TMR daily need" value="1,200 kg" detail="30 kg per buffalo" icon="check" tone="purple"/>
    </div>
    <div className="mt-4 overflow-hidden rounded-2xl border border-[#e4e9e6] bg-white">
      <div className="flex flex-col gap-3 border-b border-[#e9edea] p-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex gap-1 overflow-x-auto">
          {["All items", "TMR Feed", "Medicine", "Breeding"].map((x) => <button key={x} onClick={() => setCategory(x)} className={`rounded-lg px-3 py-2 text-[10px] font-semibold ${category === x ? "bg-[#e9f4ef] text-[#1f7453]" : "text-[#7d8983]"}`}>{x}</button>)}
        </div>
        <div className="w-full sm:w-60"><SearchBar placeholder="Search inventory..."/></div>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[680px] text-left">
          <thead><tr className="bg-[#fafbfa] text-[9px] uppercase tracking-wider text-[#919b96]"><th className="px-5 py-3">Item</th><th className="px-4 py-3">Category</th><th className="px-4 py-3">Available</th><th className="px-4 py-3">Capacity</th><th className="px-4 py-3">Stock level</th><th></th></tr></thead>
          <tbody>
            {rows.map((x, i) => (
              <tr className="border-t border-[#edf0ee] text-[11px]" key={x[0]}>
                <td className="px-5 py-4"><div className="flex items-center gap-3"><span className={`page-icon ${i % 2 ? "amber" : "green"}`}><MiniIcon name="box" size={14}/></span><b>{x[0]}</b></div></td>
                <td className="px-4 text-[#6e7a74]">{x[1]}</td>
                <td className="px-4 font-semibold">{x[2]}</td>
                <td className="px-4 text-[#8a948f]">{x[3]}</td>
                <td className="px-4"><Status value={x[4]}/></td>
                <td className="px-4"><MiniIcon name="dots" size={16}/></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
    <div className="mt-4 grid gap-4 lg:grid-cols-[1fr_1.2fr]">
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5"><h3 className="text-sm font-bold">TMR feed composition</h3><Bars data={[["Maize silage (15 kg)", 83], ["Dry fodder (10 kg)", 56], ["Concentrate (5 kg)", 28], ["Mineral mix (100g)", 16]]}/></div>
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <div className="flex justify-between"><h3 className="text-sm font-bold">Recent purchase orders</h3><button className="text-[10px] font-semibold text-[#277b59]">View all</button></div>
        <div className="mt-4 space-y-3">
          {[["PO-1012", "Krishna Feeds, Srikakulam", "₹62,000", "Arriving 2 Oct"], ["PO-1011", "AP Vet Pharma", "₹8,400", "Arriving tomorrow"], ["PO-1010", "Maize silage contractor", "₹28,000", "Processing"]].map((x) => (
            <div className="grid grid-cols-[1fr_1.5fr_1fr] items-center rounded-xl bg-[#f8faf9] p-3 text-[10px]" key={x[0]}>
              <b>{x[0]}</b><span className="text-[#6f7b75]">{x[1]}</span><div className="text-right"><b>{x[2]}</b><div className="text-[8px] text-[#8e9893]">{x[3]}</div></div>
            </div>
          ))}
        </div>
      </div>
    </div>
  </>;
}

function Bars({ data }: { data: [string, number][] }) {
  return <div className="mt-5 space-y-4">{data.map(([label, value]) => <div key={label}><div className="mb-1.5 flex justify-between text-[9px]"><span className="text-[#6f7b75]">{label}</span><b>{value}%</b></div><div className="h-1.5 rounded-full bg-[#edf1ef]"><div className="h-full rounded-full bg-[#3a8e6c]" style={{ width: `${value}%` }}/></div></div>)}</div>;
}

// Financial data for Bhavani's farm (from NABARD DPR)
const transactions = [
  ["Sep 2026", "Milk sales · APDDCF cooperative", "Sales", "+₹41,600", "Income"],
  ["Sep 2026", "Maize silage & fodder", "TMR Feed", "−₹62,000", "Expense"],
  ["Sep 2026", "Veterinary services", "Healthcare", "−₹4,200", "Expense"],
  ["Sep 2026", "Organic manure sales", "Biogas/Manure", "+₹12,000", "Income"],
  ["Sep 2026", "Labour wages (3 workers)", "Labour", "−₹24,000", "Expense"],
  ["Sep 2026", "Electricity bill", "Utilities", "−₹4,800", "Expense"],
];

function FinancePage({ notify }: { notify: (v: string) => void }) {
  const [view, setView] = useState("Transactions");
  return <>
    <PageHeader eyebrow="Business finance" title="Finance" description="Income, expenses, NABARD loan status, and farm profitability — Bhavani Dairy Farm." action="Add transaction" onAction={() => notify("Transaction form ready")}/>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Stat label="Monthly income" value="₹4.16L" detail="↑ 6.2% vs last month" icon="money"/>
      <Stat label="Monthly expenses" value="₹2.18L" detail="↑ 1.4% vs last month" icon="chart" tone="red"/>
      <Stat label="Net profit" value="₹1.98L" detail="47.6% profit margin" icon="chart" tone="blue"/>
      <Stat label="NABARD loan" value="₹89.3L" detail="EMI ₹74,450/mo" icon="calendar" tone="amber"/>
    </div>
    <div className="mt-4 grid gap-4 xl:grid-cols-[1.6fr_1fr]">
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <div className="flex items-start justify-between">
          <div><h3 className="text-sm font-bold">Cash flow</h3><p className="mt-1 text-[9px] text-[#8c9691]">Income vs expenses — first 6 months</p></div>
          <select className="rounded-lg border border-[#e3e8e5] p-2 text-[9px]"><option>Last 6 months</option></select>
        </div>
        <div className="mt-6 flex h-52 items-end justify-around gap-3 border-b border-[#e7ebe8] px-2">
          {[["Apr", 34, 28], ["May", 38, 26], ["Jun", 40, 27], ["Jul", 42, 25], ["Aug", 44, 23], ["Sep", 42, 22]].map(([m, a, b]) => (
            <div className="flex h-full flex-1 flex-col items-center justify-end" key={m}>
              <div className="flex h-[85%] items-end gap-1">
                <div className="w-3 rounded-t bg-[#398b69]" style={{height: `${a}%`}}/>
                <div className="w-3 rounded-t bg-[#e5b466]" style={{height: `${b}%`}}/>
              </div>
              <span className="mt-2 text-[8px] text-[#909a95]">{m}</span>
            </div>
          ))}
        </div>
        <div className="mt-4 flex justify-center gap-5 text-[9px]">
          <span className="flex items-center gap-1.5"><i className="size-2 rounded bg-[#398b69]"/>Income</span>
          <span className="flex items-center gap-1.5"><i className="size-2 rounded bg-[#e5b466]"/>Expenses</span>
        </div>
      </div>
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <h3 className="text-sm font-bold">Expense breakdown</h3>
        <div className="mx-auto mt-6 flex size-36 items-center justify-center rounded-full" style={{background: "conic-gradient(#388a68 0 42%, #dfaa55 42% 62%, #668eae 62% 74%, #a1769b 74% 84%, #d46f61 84%)"}}>
          <div className="flex size-20 flex-col items-center justify-center rounded-full bg-white"><b className="text-lg">₹2.18L</b><span className="text-[8px] text-[#8e9893]">TOTAL</span></div>
        </div>
        <div className="mt-5 grid grid-cols-2 gap-3 text-[9px]">
          {[["Feed", "42%", "#388a68"], ["Labour", "20%", "#dfaa55"], ["Loan EMI", "12%", "#668eae"], ["Vet", "10%", "#a1769b"], ["Other", "16%", "#d46f61"]].map((x) => (
            <div className="flex items-center gap-2" key={x[0]}><span className="size-2 rounded-full" style={{background:x[2]}}/><span className="flex-1 text-[#758079]">{x[0]}</span><b>{x[1]}</b></div>
          ))}
        </div>
      </div>
    </div>

    {/* NABARD subsidy highlight */}
    <div className="mt-4 rounded-2xl bg-[#1c4637] p-5 text-white">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-[10px] font-bold uppercase tracking-[0.12em] text-[#82c6aa]">NABARD DEDS · Women Entrepreneur Subsidy</div>
          <div className="mt-2 text-2xl font-bold">₹44,66,600 approved</div>
          <div className="mt-1 text-[11px] text-white/60">33.33% of ₹1,34,00,000 total project cost · Bank loan: ₹89,33,400</div>
        </div>
        <div className="grid grid-cols-3 gap-4 text-center">
          {[["Total cost", "₹1.34Cr"], ["Subsidy", "₹44.7L"], ["Bank loan", "₹89.3L"]].map(([l, v]) => (
            <div key={l}><div className="text-[9px] text-white/50">{l}</div><div className="mt-1 text-sm font-bold">{v}</div></div>
          ))}
        </div>
      </div>
    </div>

    <div className="mt-4 overflow-hidden rounded-2xl border border-[#e4e9e6] bg-white">
      <div className="flex items-center justify-between border-b border-[#e9edea] p-4">
        <div className="flex gap-1">
          {["Transactions", "Loan details"].map((x) => <button onClick={() => setView(x)} className={`rounded-lg px-3 py-2 text-[10px] font-semibold ${view === x ? "bg-[#e9f4ef] text-[#1f7453]" : "text-[#7d8983]"}`} key={x}>{x}</button>)}
        </div>
        <button className="flex items-center gap-2 text-[10px] font-semibold text-[#277b59]"><MiniIcon name="download" size={14}/>Export</button>
      </div>
      {view === "Transactions" ? (
        <div className="divide-y divide-[#edf0ee]">
          {transactions.map((x) => (
            <div className="grid grid-cols-[80px_1fr_90px] items-center gap-3 px-5 py-3.5 text-[10px] sm:grid-cols-[90px_1fr_110px_100px]" key={x[1]}>
              <span className="text-[#8c9691]">{x[0]}</span>
              <div><b>{x[1]}</b><div className="text-[8px] text-[#949d98]">{x[2]}</div></div>
              <span className={`hidden sm:block ${x[4] === "Income" ? "text-[#27805c]" : "text-[#bb5c50]"}`}>{x[4]}</span>
              <b className={`text-right ${x[4] === "Income" ? "text-[#27805c]" : ""}`}>{x[3]}</b>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid gap-3 p-5 md:grid-cols-3">
          {[["NABARD dairy loan", "₹89,33,400 outstanding", "EMI ₹74,450 · due 5th monthly"], ["APDDCF advance", "₹0 outstanding", "Cleared · milk credit account"], ["Electricity bill", "₹4,800", "Due in 8 days"]].map((x) => (
            <div className="rounded-xl bg-[#f8faf9] p-4" key={x[0]}><b className="text-[11px]">{x[0]}</b><div className="mt-2 text-sm font-bold">{x[1]}</div><div className="mt-1 text-[9px] text-[#9a6a25]">{x[2]}</div></div>
          ))}
        </div>
      )}
    </div>
  </>;
}

function AnalyticsPage({ notify }: { notify: (v: string) => void }) {
  const [period, setPeriod] = useState("This month");
  const chart = useMemo(() => period === "This month" ? "M0 115 C60 105 75 90 125 97 S210 108 250 75 S335 52 380 65 S470 42 520 50 S600 25 700 30" : "M0 128 C90 115 120 98 190 108 S320 62 390 78 S540 38 700 25", [period]);
  return <>
    <PageHeader eyebrow="Farm intelligence" title="Analytics" description="Milk production, herd efficiency, resource usage, and financial performance." action="Create report" onAction={() => notify("Report builder opened")}/>
    <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#e4e9e6] bg-white p-3">
      <div className="flex gap-1">
        {["This week", "This month", "This quarter"].map((x) => <button onClick={() => setPeriod(x)} className={`rounded-lg px-3 py-2 text-[10px] font-semibold ${period === x ? "bg-[#e9f4ef] text-[#1f7453]" : "text-[#7d8983]"}`} key={x}>{x}</button>)}
      </div>
      <button className="flex items-center gap-2 rounded-lg border border-[#e3e8e5] px-3 py-2 text-[10px] font-semibold"><MiniIcon name="download" size={14}/>Export dashboard</button>
    </div>
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Stat label="Milk per buffalo" value="12.3 L" detail="↑ 3.4% in period" icon="water"/>
      <Stat label="Feed efficiency" value="0.41" detail="L milk / kg TMR" icon="chart" tone="blue"/>
      <Stat label="Cost per litre" value="₹14.20" detail="↓ ₹1.60 in period" icon="money" tone="amber"/>
      <Stat label="Herd productivity" value="91.8%" detail="↑ 2.2% in period" icon="cow" tone="purple"/>
    </div>
    <div className="mt-4 grid gap-4 xl:grid-cols-[1.5fr_1fr]">
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <div className="flex justify-between">
          <div><h3 className="text-sm font-bold">Production performance</h3><p className="mt-1 text-[9px] text-[#8c9691]">Daily milk yield vs 15 L target</p></div>
          <div className="text-right"><b className="text-lg">14,280 L</b><div className="text-[9px] text-[#27805c]">98.1% of target</div></div>
        </div>
        <div className="relative mt-7 h-52 overflow-hidden">
          <div className="absolute inset-0 flex flex-col justify-between">{[1,2,3,4].map(x=><span className="h-px bg-[#edf0ee]" key={x}/>)}</div>
          <svg viewBox="0 0 700 150" className="absolute inset-x-0 bottom-4 h-[170px] w-full" preserveAspectRatio="none">
            <defs><linearGradient id="analyticsArea" x1="0" y1="0" x2="0" y2="1"><stop stopColor="#318561" stopOpacity=".2"/><stop offset="1" stopColor="#318561" stopOpacity="0"/></linearGradient></defs>
            <path d={`${chart} V150 H0Z`} fill="url(#analyticsArea)"/>
            <path d={chart} fill="none" stroke="#318561" strokeWidth="3" vectorEffect="non-scaling-stroke"/>
            <path d="M0 80 C150 80 350 80 700 80" fill="none" stroke="#d7a34f" strokeDasharray="6 5" strokeWidth="2" vectorEffect="non-scaling-stroke"/>
          </svg>
          <div className="absolute bottom-0 flex w-full justify-between text-[8px] text-[#949d98]"><span>Week 1</span><span>Week 2</span><span>Week 3</span><span>Week 4</span></div>
        </div>
      </div>
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5">
        <h3 className="text-sm font-bold">Key insights</h3>
        <p className="mt-1 text-[9px] text-[#8c9691]">AI-generated farm observations</p>
        <div className="mt-5 space-y-3">
          {[["green", "Milk yield up 3.4% this month", "B-07 estrus detected — timely AI may increase calving by month 3."], ["amber", "Dry fodder stock low", "Order 4,000 kg bajra straw before 5 Oct to avoid TMR gap."], ["blue", "Biogas saving ₹4,200/mo", "Equivalent to 7.2 kg LPG daily from slurry digestion."]].map((x) => (
            <div className="rounded-xl bg-[#f8faf9] p-3.5" key={x[1]}>
              <div className="flex gap-3">
                <span className={`mt-1 size-2 shrink-0 rounded-full bg-${x[0]}-500`}/>
                <div><b className="text-[10px]">{x[1]}</b><p className="mt-1 text-[9px] leading-relaxed text-[#828d87]">{x[2]}</p></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
    <div className="mt-4 grid gap-4 lg:grid-cols-3">
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5"><h3 className="text-xs font-bold">Production by season</h3><Bars data={[["Monsoon (Jun–Sep)", 88], ["Post-monsoon (Oct–Jan)", 94], ["Summer (Feb–May)", 72], ["Current month", 91]]}/></div>
      <div className="rounded-2xl border border-[#e4e9e6] bg-white p-5"><h3 className="text-xs font-bold">Resource efficiency</h3><Bars data={[["Water utilization", 84], ["Energy efficiency", 78], ["Feed conversion", 81], ["Waste-to-biogas", 72]]}/></div>
      <div className="rounded-2xl bg-[#1c4637] p-5 text-white">
        <h3 className="text-xs font-bold">Projected monthly profit</h3>
        <div className="mt-5 text-3xl font-bold">₹2.14L</div>
        <div className="mt-1 text-[9px] text-[#91cbb4]">↑ 8.2% vs last month</div>
        <p className="mt-6 text-[9px] leading-relaxed text-white/55">Based on current 490 L/day yield, ₹42/L APDDCF rate, feed costs, and 3-worker wages.</p>
        <button className="mt-4 flex items-center gap-2 text-[10px] font-semibold text-[#8fd0b5]">View full forecast <MiniIcon name="arrow" size={12}/></button>
      </div>
    </div>
  </>;
}

export default function DashboardPage({ page }: { page: PageName }) {
  const [notice, setNotice] = useState("");
  const notify = (message: string) => { setNotice(message); window.setTimeout(() => setNotice(""), 2200); };
  return <div className="mx-auto max-w-[1500px] px-4 py-6 sm:px-7 lg:px-9 lg:py-8">
    {notice && <div className="fixed left-1/2 top-5 z-50 -translate-x-1/2 rounded-full bg-[#173f31] px-5 py-3 text-xs font-semibold text-white shadow-xl">{notice}</div>}
    {page === "Herd management" && <HerdPage notify={notify}/>}
    {page === "Machinery" && <MachineryPage notify={notify}/>}
    {page === "Inventory" && <InventoryPage notify={notify}/>}
    {page === "Finance" && <FinancePage notify={notify}/>}
    {page === "Analytics" && <AnalyticsPage notify={notify}/>}
  </div>;
}
