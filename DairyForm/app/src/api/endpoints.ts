import api from "./client";

export interface LoginResponse {
  access_token: string;
  token_type: string;
  user: User;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  avatar_initials: string;
  is_active: boolean;
  created_at: string;
}

export interface Animal {
  id: number;
  tag_number: string;
  name: string;
  breed: string;
  status: string;
  parity: number;
  weight_kg: number;
  last_calving_date: string | null;
  is_active: boolean;
}

export interface MilkRecord {
  id: number;
  animal_id: number;
  record_date: string;
  session: string;
  quantity_litres: number;
  source: string;
  created_at: string;
}

export interface HealthRecord {
  id: number;
  animal_id: number;
  record_date: string;
  condition: string;
  severity: string;
  symptoms?: string;
  treatment?: string;
  vet_name?: string;
  resolved: boolean;
}

export interface QualityCheck {
  id: number;
  check_date: string;
  fat_percent: number;
  snf_percent: number;
  clr?: number;
  temperature_c?: number;
  grade: string | null;
  adulteration: boolean;
  notes?: string;
  recorded_by?: number;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  category: string;
  due_date: string;
  due_time: string | null;
  priority: string;
  status: string;
  assigned_to: number | null;
  created_by: number;
}

export interface DashboardStats {
  total_animals: number;
  lactating: number;
  dry: number;
  pregnant: number;
  sick: number;
  milk_today_litres: number;
  milk_yesterday_litres: number;
  milk_trend_pct: number;
  tasks_today: number;
  tasks_overdue: number;
  quality_grade_today: string | null;
  avg_fat_today: number | null;
  avg_snf_today: number | null;
  alerts: Array<{ type: string; message: string; severity: string }>;
}

export const authApi = {
  login: (email: string, password: string) =>
    api.post<LoginResponse>("/api/auth/login", new URLSearchParams({ username: email, password }), {
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
    }),
  me: () => api.get<User>("/api/auth/me"),
  changePassword: (current: string, newPwd: string) =>
    api.post("/api/auth/change-password", { current_password: current, new_password: newPwd }),
};

export const usersApi = {
  list: () => api.get<User[]>("/api/users/"),
  create: (data: { name: string; email: string; password: string; role: string; phone?: string }) =>
    api.post<User>("/api/users/", data),
  update: (id: number, data: Partial<{ name: string; role: string; is_active: boolean }>) =>
    api.put<User>(`/api/users/${id}`, data),
  deactivate: (id: number) => api.delete(`/api/users/${id}`),
};

export const animalsApi = {
  list: (params?: { status?: string }) => api.get<Animal[]>("/api/animals/", { params }),
  get: (id: number) => api.get<Animal>(`/api/animals/${id}`),
  create: (data: Partial<Animal>) => api.post<Animal>("/api/animals/", data),
  update: (id: number, data: Partial<Animal>) => api.put<Animal>(`/api/animals/${id}`, data),
  milkHistory: (id: number, days = 30) => api.get<MilkRecord[]>(`/api/animals/${id}/milk-history`, { params: { days } }),
  healthHistory: (id: number) => api.get<HealthRecord[]>(`/api/animals/${id}/health-history`),
};

export const milkApi = {
  list: (params?: object) => api.get<MilkRecord[]>("/api/milk/", { params }),
  create: (data: Partial<MilkRecord>) => api.post<MilkRecord>("/api/milk/", data),
  today: () => api.get("/api/milk/today"),
  daily: (params?: { days?: number }) => api.get("/api/milk/summary/daily", { params }),
};

export const healthApi = {
  list: (params?: object) => api.get<HealthRecord[]>("/api/health/", { params }),
  create: (data: Partial<HealthRecord>) => api.post<HealthRecord>("/api/health/", data),
  update: (id: number, data: Partial<HealthRecord>) => api.put<HealthRecord>(`/api/health/${id}`, data),
  alerts: () => api.get<HealthRecord[]>("/api/health/alerts"),
};

export const qualityApi = {
  list: (params?: object) => api.get<QualityCheck[]>("/api/quality/", { params }),
  create: (data: Partial<QualityCheck>) => api.post<QualityCheck>("/api/quality/", data),
  latest: () => api.get<QualityCheck>("/api/quality/latest"),
  averages: () => api.get("/api/quality/averages"),
};

export const tasksApi = {
  list: (params?: object) => api.get<Task[]>("/api/tasks/", { params }),
  create: (data: Partial<Task>) => api.post<Task>("/api/tasks/", data),
  update: (id: number, data: Partial<Task>) => api.put<Task>(`/api/tasks/${id}`, data),
  delete: (id: number) => api.delete(`/api/tasks/${id}`),
  calendar: (params?: { year?: number; month?: number }) =>
    api.get<Record<string, Task[]>>("/api/tasks/calendar", { params }),
  myToday: () => api.get<Task[]>("/api/tasks/my/today"),
};

export const devicesApi = {
  list: () => api.get("/api/devices/"),
  create: (data: { device_id: string; name: string; device_type: string; location?: string }) =>
    api.post("/api/devices/", data),
  deactivate: (id: number) => api.delete(`/api/devices/${id}`),
  readings: (params?: object) => api.get("/api/devices/readings", { params }),
};

export const dashboardApi = {
  stats: () => api.get<DashboardStats>("/api/dashboard/stats"),
  herdHealth: () => api.get("/api/dashboard/herd-health"),
};

export interface Expense {
  id: number;
  expense_date: string;
  category: string;
  description: string;
  amount: number;
  vendor?: string;
  reference_no?: string;
  payment_mode: string;
  animal_id?: number | null;
  recorded_by?: number | null;
  created_at: string;
  notes?: string;
}

export const expensesApi = {
  list: (params?: object) => api.get<Expense[]>("/api/expenses/", { params }),
  create: (data: Partial<Expense>) => api.post<Expense>("/api/expenses/", data),
  update: (id: number, data: Partial<Expense>) => api.put<Expense>(`/api/expenses/${id}`, data),
  delete: (id: number) => api.delete(`/api/expenses/${id}`),
  summary: (months?: number) => api.get("/api/expenses/summary", { params: { months } }),
  monthly: (months?: number) => api.get("/api/expenses/monthly", { params: { months } }),
};

export const feedApi = {
  list: (params?: object) => api.get("/api/feed/", { params }),
  create: (data: object) => api.post("/api/feed/", data),
  today: () => api.get("/api/feed/today"),
};

// ─── Reproductive Events ──────────────────────────────────────────────────────

export interface ReproEvent {
  id: number;
  animal_id: number;
  event_date: string;
  event_type: string;
  bull_name?: string | null;
  semen_batch?: string | null;
  ai_technician?: string | null;
  ai_number?: number | null;
  diagnosed_by?: string | null;
  calf_sex?: string | null;
  calf_weight_kg?: number | null;
  calving_ease?: string | null;
  next_heat_expected?: string | null;
  notes?: string | null;
  recorded_by?: number | null;
  created_at: string;
}

export const reproductiveApi = {
  list: (params?: object) => api.get<ReproEvent[]>("/api/reproductive/", { params }),
  byAnimal: (animalId: number) => api.get<ReproEvent[]>(`/api/reproductive/animal/${animalId}`),
  create: (data: Partial<ReproEvent>) => api.post<ReproEvent>("/api/reproductive/", data),
  update: (id: number, data: Partial<ReproEvent>) => api.put<ReproEvent>(`/api/reproductive/${id}`, data),
  delete: (id: number) => api.delete(`/api/reproductive/${id}`),
};

// ─── Calves ───────────────────────────────────────────────────────────────────

export interface Calf {
  id: number;
  tag_number: string;
  name?: string | null;
  dam_id: number;
  sire_name?: string | null;
  date_of_birth: string;
  sex: string;
  birth_weight_kg?: number | null;
  colostrum_given: boolean;
  colostrum_time_hrs?: number | null;
  status: string;
  weaning_date?: string | null;
  weaning_weight_kg?: number | null;
  sold_date?: string | null;
  sold_price?: number | null;
  notes?: string | null;
  is_active: boolean;
  created_at: string;
}

export interface CalfWeight {
  id: number;
  calf_id: number;
  record_date: string;
  weight_kg: number;
}

export const calvesApi = {
  list: (params?: object) => api.get<Calf[]>("/api/calves/", { params }),
  get: (id: number) => api.get<Calf>(`/api/calves/${id}`),
  create: (data: Partial<Calf>) => api.post<Calf>("/api/calves/", data),
  update: (id: number, data: Partial<Calf>) => api.put<Calf>(`/api/calves/${id}`, data),
  delete: (id: number) => api.delete(`/api/calves/${id}`),
  weights: (id: number) => api.get<CalfWeight[]>(`/api/calves/${id}/weights`),
  addWeight: (id: number, data: { record_date: string; weight_kg: number }) =>
    api.post<CalfWeight>(`/api/calves/${id}/weights`, { calf_id: id, ...data }),
};

// ─── Vaccinations ─────────────────────────────────────────────────────────────

export interface Vaccination {
  id: number;
  animal_id: number;
  vaccine_name: string;
  disease?: string | null;
  vaccination_date: string;
  dose_ml?: number | null;
  route?: string | null;
  batch_no?: string | null;
  manufacturer?: string | null;
  next_due_date?: string | null;
  given_by?: string | null;
  cost?: number | null;
  is_govt_free: boolean;
  recorded_by?: number | null;
  created_at: string;
  notes?: string | null;
}

export interface VaccinationDue {
  animal_id: number;
  tag_number: string;
  animal_name?: string | null;
  vaccine_name: string;
  next_due_date: string;
  days_overdue: number;
}

export const vaccinationsApi = {
  list: (params?: object) => api.get<Vaccination[]>("/api/vaccinations/", { params }),
  due: (daysAhead?: number) => api.get<VaccinationDue[]>("/api/vaccinations/due", { params: { days_ahead: daysAhead } }),
  byAnimal: (animalId: number) => api.get<Vaccination[]>(`/api/vaccinations/animal/${animalId}`),
  create: (data: Partial<Vaccination>) => api.post<Vaccination>("/api/vaccinations/", data),
  update: (id: number, data: Partial<Vaccination>) => api.put<Vaccination>(`/api/vaccinations/${id}`, data),
  delete: (id: number) => api.delete(`/api/vaccinations/${id}`),
};

