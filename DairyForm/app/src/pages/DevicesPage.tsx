import { useEffect, useState } from "react";
import { devicesApi } from "../api/endpoints";
import Icon from "../components/Icon";

interface Device {
  id: number;
  device_id: string;
  name: string;
  device_type: string;
  location?: string;
  is_active: boolean;
  last_seen?: string;
  api_key?: string;
}

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [createdDevice, setCreatedDevice] = useState<any>(null);
  const [form, setForm] = useState({
    device_id: "",
    name: "",
    device_type: "milk_meter",
    location: "",
  });

  const load = async () => {
    const { data } = await devicesApi.list();
    setDevices(data);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const { data } = await devicesApi.create({
        device_id: form.device_id,
        name: form.name,
        device_type: form.device_type,
        location: form.location || undefined,
      });
      setCreatedDevice(data);
      setShowForm(false);
      setForm({ device_id: "", name: "", device_type: "milk_meter", location: "" });
      load();
    } finally {
      setSaving(false);
    }
  };

  const TYPE_ICON: Record<string, string> = {
    milk_meter: "milk",
    quality_analyzer: "shield",
    ear_tag: "tag",
    weight_scale: "activity",
    temperature: "thermo",
    temperature_sensor: "thermo",
  };

  if (loading) {
    return <div className="flex h-40 items-center justify-center"><div className="size-8 animate-spin rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" /></div>;
  }

  return (
    <div className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">IoT Devices</h1>
          <p className="mt-1 text-sm text-[#7d8983]">{devices.filter((d) => d.is_active).length} active devices</p>
        </div>
        <button onClick={() => setShowForm(true)} className="flex items-center gap-2 rounded-xl bg-[#1d7352] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#185f45]">
          <Icon name="plus" size={15} /> Register device
        </button>
      </div>

      {/* API endpoint hint */}
      <div className="mb-5 rounded-2xl border border-[#e4e9e6] bg-white p-4">
        <div className="flex items-start gap-3">
          <div className="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-lg bg-[#e6f4ec]">
            <Icon name="wifi" size={14} className="text-[#2d8b65]" />
          </div>
          <div>
            <div className="text-[12px] font-semibold">Device Ingestion Endpoint</div>
            <code className="mt-1 block text-[11px] text-[#5a6b64]">POST /api/devices/ingest</code>
            <div className="mt-1 text-[11px] text-[#8c9691]">Devices send readings using their <code className="text-[#3d4d47]">device_id</code> + <code className="text-[#3d4d47]">api_key</code>. No JWT required. Auto-creates milk records and quality checks.</div>
          </div>
        </div>
      </div>

      {/* Devices grid */}
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {devices.map((device) => (
          <div key={device.id} className="rounded-2xl border border-[#e4e9e6] bg-white p-4">
            <div className="flex items-start justify-between">
              <div className="flex size-9 items-center justify-center rounded-xl bg-[#e6f4ec]">
                <Icon name={(TYPE_ICON[device.device_type] as any) || "cpu"} size={17} className="text-[#2d8b65]" />
              </div>
              <span className={`rounded-full px-2 py-0.5 text-[9px] font-semibold ${device.is_active ? "bg-[#e6f4ec] text-[#2d8b65]" : "bg-[#f0f3f1] text-[#6b7c75]"}`}>
                {device.is_active ? "Active" : "Offline"}
              </span>
            </div>
            <div className="mt-3">
              <div className="text-[14px] font-bold">{device.name}</div>
              <code className="text-[11px] text-[#6b7c75]">{device.device_id}</code>
            </div>
            <div className="mt-3 border-t border-[#f0f3f1] pt-3 grid grid-cols-2 gap-2">
              <div>
                <div className="text-[9px] text-[#9aa7a1]">TYPE</div>
                <div className="mt-0.5 text-[11px] font-medium capitalize">{device.device_type.replace(/_/g, " ")}</div>
              </div>
              {device.location && (
                <div>
                  <div className="text-[9px] text-[#9aa7a1]">LOCATION</div>
                  <div className="mt-0.5 text-[11px] font-medium">{device.location}</div>
                </div>
              )}
              {device.last_seen && (
                <div className="col-span-2">
                  <div className="text-[9px] text-[#9aa7a1]">LAST SEEN</div>
                  <div className="mt-0.5 text-[11px] font-medium">
                    {new Date(device.last_seen).toLocaleDateString("en-IN", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" })}
                  </div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Register device modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-bold">Register IoT Device</h3>
              <button onClick={() => setShowForm(false)}><Icon name="close" size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Device ID *</label>
                <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.device_id} onChange={(e) => setForm({ ...form, device_id: e.target.value })} placeholder="MM-002" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Device name *</label>
                <input required className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Milk Meter #2" />
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Type</label>
                <select className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.device_type} onChange={(e) => setForm({ ...form, device_type: e.target.value })}>
                  <option value="milk_meter">Milk meter</option>
                  <option value="quality_analyzer">Quality analyzer</option>
                  <option value="ear_tag">IoT ear tag</option>
                  <option value="weight_scale">Weight scale</option>
                  <option value="temperature">Temperature sensor</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#3d4d47]">Location</label>
                <input className="w-full rounded-xl border border-[#dce1de] px-3 py-2.5 text-sm outline-none focus:border-[#2d8b65]" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} placeholder="Shed A, Stall 1" />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="flex-1 rounded-xl border border-[#e4e9e6] py-2.5 text-sm font-semibold">Cancel</button>
                <button type="submit" disabled={saving} className="flex-1 rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white disabled:opacity-60">
                  {saving ? "Registering..." : "Register"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Show generated API key after registration */}
      {createdDevice && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-[440px] rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center gap-3">
              <div className="flex size-9 items-center justify-center rounded-xl bg-[#e6f4ec]">
                <Icon name="check" size={18} className="text-[#2d8b65]" />
              </div>
              <h3 className="text-[15px] font-bold">Device Registered!</h3>
            </div>
            <p className="text-[12px] text-[#6b7c75] mb-3">Save the API key below — it will not be shown again.</p>
            <div className="rounded-xl bg-[#f0f3f1] p-4">
              <div className="mb-1 text-[10px] font-semibold text-[#7e8983]">API KEY</div>
              <code className="break-all text-[12px] text-[#17221d]">{createdDevice.api_key}</code>
            </div>
            <button onClick={() => setCreatedDevice(null)} className="mt-4 w-full rounded-xl bg-[#1d7352] py-2.5 text-sm font-semibold text-white">
              I've saved the key
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
