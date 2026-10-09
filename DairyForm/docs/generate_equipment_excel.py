from openpyxl import Workbook
from openpyxl.styles import (
    PatternFill, Font, Alignment, Border, Side, GradientFill
)
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

wb = Workbook()

# ── colour palette ──────────────────────────────────────────────────────────
DARK_GREEN   = "1B5E20"
MID_GREEN    = "2E7D32"
LIGHT_GREEN  = "C8E6C9"
PALE_GREEN   = "E8F5E9"
ORANGE       = "E65100"
LIGHT_ORANGE = "FFE0B2"
PALE_ORANGE  = "FFF3E0"
YELLOW       = "F9A825"
LIGHT_YELLOW = "FFF9C4"
BLUE         = "1565C0"
LIGHT_BLUE   = "BBDEFB"
PALE_BLUE    = "E3F2FD"
GREY_HEAD    = "37474F"
LIGHT_GREY   = "ECEFF1"
WHITE        = "FFFFFF"
RED          = "B71C1C"
LIGHT_RED    = "FFCDD2"

def fill(hex_color):
    return PatternFill("solid", fgColor=hex_color)

def font(bold=False, color="000000", size=10):
    return Font(bold=bold, color=color, size=size, name="Calibri")

def border_thin():
    s = Side(style="thin", color="BDBDBD")
    return Border(left=s, right=s, top=s, bottom=s)

def border_medium():
    s = Side(style="medium", color="78909C")
    return Border(left=s, right=s, top=s, bottom=s)

def center(wrap=False):
    return Alignment(horizontal="center", vertical="center", wrap_text=wrap)

def left(wrap=False):
    return Alignment(horizontal="left", vertical="center", wrap_text=wrap)

def apply(ws, row, col, value, bg=None, bold=False, fcolor="000000",
          size=10, align=None, wrap=False, border=True):
    cell = ws.cell(row=row, column=col, value=value)
    if bg:
        cell.fill = fill(bg)
    cell.font = font(bold=bold, color=fcolor, size=size)
    cell.alignment = align if align else left(wrap)
    if border:
        cell.border = border_thin()
    return cell

def merge_title(ws, row, start_col, end_col, value, bg, fcolor="FFFFFF", size=12):
    ws.merge_cells(start_row=row, start_column=start_col,
                   end_row=row, end_column=end_col)
    cell = ws.cell(row=row, column=start_col, value=value)
    cell.fill = fill(bg)
    cell.font = Font(bold=True, color=fcolor, size=size, name="Calibri")
    cell.alignment = center(wrap=False)
    cell.border = border_medium()

# ════════════════════════════════════════════════════════════════════════════
# SHEET 1 — MASTER EQUIPMENT LIST
# ════════════════════════════════════════════════════════════════════════════
ws1 = wb.active
ws1.title = "Equipment Master List"
ws1.sheet_view.showGridLines = False
ws1.freeze_panes = "A5"

# Title rows
ws1.row_dimensions[1].height = 30
ws1.row_dimensions[2].height = 20
ws1.row_dimensions[3].height = 16
ws1.row_dimensions[4].height = 30

ws1.merge_cells("A1:P1")
c = ws1["A1"]
c.value = "DAIRY FARM EQUIPMENT PROCUREMENT MASTER LIST"
c.fill = fill(DARK_GREEN)
c.font = Font(bold=True, color="FFFFFF", size=16, name="Calibri")
c.alignment = center()

ws1.merge_cells("A2:P2")
c = ws1["A2"]
c.value = "Applicant: Smt. Danthuluri Bhavani | Farm: Devunipalavalasa, Ranasthalam, Srikakulam, AP | 40 Murrah Buffaloes"
c.fill = fill(MID_GREEN)
c.font = Font(bold=False, color="FFFFFF", size=10, name="Calibri")
c.alignment = center()

ws1.merge_cells("A3:P3")
c = ws1["A3"]
c.value = "All prices are approximate market rates (2026). Obtain actual quotations from at least 2 suppliers before finalising."
c.fill = fill(LIGHT_YELLOW)
c.font = Font(bold=False, color=ORANGE, size=9, name="Calibri")
c.alignment = center()

# Header row 4
headers = [
    "S.No", "Category", "Equipment Name", "Qty", "Unit",
    "Min Spec (Must Have)", "Preferred Spec (Best Quality)",
    "Price Low (₹)", "Price High (₹)", "Avg Price (₹)",
    "Total Avg (₹)", "Supplier Type / Where to Buy",
    "Key Things to Check Before Buying",
    "Spare Parts to Buy Along",
    "Warranty Expected", "Priority"
]
col_widths = [5, 18, 22, 5, 6, 35, 35, 12, 12, 12, 12, 28, 40, 35, 14, 10]

for i, (h, w) in enumerate(zip(headers, col_widths), 1):
    ws1.column_dimensions[get_column_letter(i)].width = w
    apply(ws1, 4, i, h, bg=GREY_HEAD, bold=True, fcolor="FFFFFF",
          size=10, align=center(wrap=True))

ws1.row_dimensions[4].height = 40

# ── DATA ────────────────────────────────────────────────────────────────────
# Columns: s, cat, name, qty, unit, min_spec, pref_spec,
#          price_low, price_high, avg_price, total_avg,
#          supplier, check_before, spares, warranty, priority

rows = [
    # ── MILKING ──
    ("MILKING EQUIPMENT", None),
    (1, "Milking", "Automatic Bucket Milking Machine", 2, "Unit",
     "Electric, single-phase 220V, SS304 bucket 20L, 4-teat cluster, buffalo-size teat cups, vacuum pump 250 LPM, pulsator 60 PPM",
     "Stainless steel body, oil-lubricated vacuum pump, trolley-mounted, auto pulsation control, digital vacuum gauge, silicon liners",
     90000, 150000, 120000, 240000,
     "Dairy equipment dealers in Hyderabad/Vijayawada; IndiaMart: BouMatic, GEA, Raj Process, Milkrite",
     "1) Buffalo-size teat cups (bigger than cow cups) — confirm before ordering\n2) Vacuum level should reach 42-50 kPa\n3) Check pulsation ratio 60:40\n4) Stainless steel grade SS304 (not MS painted)\n5) Ask for demo before payment\n6) Confirm spare liners availability in AP",
     "2 sets teat cup liners, 2 pulsator membranes, 1 vacuum pump oil (1L), 2 milk tubes, 1 extra cluster assembly",
     "1 Year", "HIGH"),

    (2, "Milking", "Bulk Milk Cooler / Milk Chiller", 1, "Unit",
     "500 litre capacity, SS304 inner tank, compressor cooling, 4°C in 3 hours, single-phase or 3-phase",
     "Direct expansion cooling, agitator/stirrer, digital temperature display, CIP (clean-in-place) inlet, insulated outer body, auto cut-off thermostat",
     220000, 280000, 250000, 250000,
     "Hyderabad: Milky Way, Condux, Alfa Laval dealers; IndiaMart: 'bulk milk cooler 500L'",
     "1) Cooling time: must reach 4°C within 3 hours of milking\n2) Insulation thickness min 50mm polyurethane\n3) CIP nozzle for easy cleaning\n4) Compressor brand (Danfoss/Embraco preferred)\n5) Single vs 3-phase — confirm your electricity supply\n6) Check power consumption (should be <3kW)\n7) Auto temperature alarm feature",
     "1 compressor capacitor, 1 thermostat sensor, 1 agitator motor, 1 kg refrigerant gas (R-404A), 1 set door gaskets",
     "2 Years", "HIGH"),

    (3, "Milking", "Milk Fat/SNF Analyzer (Milko Tester)", 1, "Unit",
     "Ultrasonic type, measures Fat%, SNF%, Water%, Density, Temperature; result in 60 seconds",
     "Automatic calibration, data storage, Bluetooth/USB export, 10+ parameter reading, FSSAI accepted model",
     60000, 100000, 80000, 80000,
     "Hyderabad dealers; IndiaMart: 'milk analyzer ultrasonic'; Brands: Lactoscan, Milkotronic, Kumbhat",
     "1) Must measure buffalo milk (separate calibration for buffalo vs cow)\n2) Check if calibration certificate is provided\n3) USB/data export — useful for records\n4) Ask if FSSAI/NABL accepted model\n5) Calibration fluid availability in India",
     "2 calibration fluid sets, 1 spare temperature probe, cleaning tablets (1 pack)",
     "1 Year", "MEDIUM"),

    # ── FEEDING ──
    ("FEEDING EQUIPMENT", None),
    (4, "Feeding", "TMR Mixer / Feed Mixer Wagon", 1, "Unit",
     "Capacity 500-800 kg batch, electric motor 5-7.5 HP, stainless steel mixing drum, discharge door",
     "Horizontal auger type, SS316 drum, PTO or electric, load cell weighing, discharge chute on both sides, rubber wheels",
     280000, 400000, 340000, 340000,
     "Hyderabad/Pune dealers; IndiaMart: 'TMR mixer wagon dairy'; Brands: Trioliet, Kuhn (imported), Hay King (Indian)",
     "1) Capacity: min 500 kg per batch for 40 animals\n2) Mixing uniformity — ask for demo video\n3) Motor HP: minimum 5HP for buffalo TMR\n4) Discharge height — should match your feeding manger height\n5) Stainless steel contact parts only\n6) Check local service availability\n7) Wheel type — rubber preferred for concrete floors",
     "1 set auger blades, 2 mixer belts, 1 motor capacitor, 2 litres gear oil, 1 set discharge door hinges",
     "1 Year", "HIGH"),

    (5, "Feeding", "Electric Chaff Cutter (Heavy Duty)", 1, "Unit",
     "Capacity min 500 kg/hr, 3-phase or single phase, 5 HP motor, 3-roller feed mechanism, adjustable cut size 10-50mm",
     "5 HP or 7.5 HP motor, 3-knife drum, adjustable cut length, flywheel safety guard, blower for output, auto-feed rollers",
     60000, 100000, 80000, 80000,
     "Srikakulam/Vizianagaram agriculture dealers; IndiaMart: 'chaff cutter electric 5HP'; Brands: Shaktiman, Landforce, KMG",
     "1) Blade sharpness and replacement availability locally\n2) Safety guard over flywheel — mandatory\n3) Single vs 3-phase — match your power supply\n4) Output blower height — should reach storage bin\n5) Capacity: 500 kg/hr minimum for 40 animals\n6) Check if local mechanic can repair — avoid imported brands",
     "2 sets cutting blades (6 blades/set), 2 belts (V-belt), 1 sharpening stone, 2 shear bolts (safety fuse bolts), 1 litre gear oil",
     "1 Year", "HIGH"),

    (6, "Feeding", "Automatic Feed Conveyor Belt (Shed)", 1, "Unit",
     "Length 30-40 metres, belt width 300mm, electric motor 1 HP, food-grade belt material",
     "Stainless steel frame, speed controller, rubber belt with side guides, auto stop at end, easy cleaning",
     80000, 120000, 100000, 100000,
     "Hyderabad conveyor dealers; IndiaMart: 'farm feed conveyor belt'",
     "1) Belt material — food grade rubber\n2) Frame material — galvanised or SS (not plain iron — rusts)\n3) Speed adjustable\n4) Easy to dismantle for cleaning\n5) Length must match your shed feeding lane",
     "2 spare belts (cut to size), 4 bearing sets, 1 motor capacitor, 1 litre belt dressing spray",
     "1 Year", "MEDIUM"),

    # ── WATER & HYGIENE ──
    ("WATER, HYGIENE & DISINFECTION", None),
    (7, "Water", "Automatic Water Drinker / Drinking Bowl (Buffalo)", 10, "Unit",
     "Cast iron or SS bowl, float valve type, capacity 15-20L, 1/2 inch inlet connection",
     "Stainless steel SS304, heavy duty float valve, anti-rust coating, recessed design, easy clean drain plug",
     6000, 10000, 8000, 80000,
     "Srikakulam agri shops or Hyderabad livestock equipment; IndiaMart: 'automatic cattle drinker buffalo'",
     "1) Buffalo drinkers are bigger than cow drinkers — confirm size\n2) Float valve quality — brass valve preferred over plastic\n3) Water flow rate: min 10 litres/minute\n4) Check if drain plug present for cleaning\n5) Installation: bolt to concrete wall — check fixing holes",
     "10 spare float valves (brass), 10 inlet washers/gaskets, 2 spare bowls",
     "6 Months", "HIGH"),

    (8, "Water", "Overhead Water Tank (5000 litre)", 2, "Unit",
     "LLDPE food grade plastic, 5000L, ISI marked, UV stabilised",
     "Double-layer insulated tank, inlet/outlet/overflow fittings included, 3-layer rotomoulded",
     15000, 22000, 18000, 36000,
     "Any hardware/plumbing shop in Srikakulam; Brands: Sintex, Penguin, Vectus",
     "1) ISI mark mandatory\n2) Food grade LLDPE — not general plastic\n3) UV stabilised (outdoor use)\n4) Check all fittings included — inlet, outlet, overflow, drain\n5) Capacity: 5000L each = 10,000L total (4 days buffer)",
     "2 inlet ball valves, 2 outlet ball valves, 1 float valve for each tank",
     "10 Years (tank body)", "HIGH"),

    (9, "Hygiene", "High Pressure Washer / Pressure Jet Machine", 1, "Unit",
     "Electric, min 120 bar pressure, 1500 PSI, 5-8 LPM flow, 1.5-2 HP motor, single phase",
     "2000 PSI, variable pressure nozzle, 10m hose, detergent injection, stainless spray lance",
     18000, 35000, 25000, 25000,
     "Hardware shops / IndiaMart: 'high pressure washer 150 bar'; Brands: Bosch, Karcher, ResQTech, Zucchetti",
     "1) Pressure min 120 bar for shed cleaning\n2) Flow rate min 5 LPM\n3) Hose length min 10 metres\n4) Detergent tank attachment for disinfectant use\n5) Single phase (easier to use anywhere in shed)\n6) Check availability of nozzles and hose locally",
     "2 spare nozzles (0° and 25°), 1 spare high-pressure hose (10m), 1 pump seal kit, 1 O-ring set",
     "1 Year", "HIGH"),

    (10, "Hygiene", "Foot Bath / Disinfection Trough at Entry", 2, "Unit",
     "Concrete or HDPE trough, 2m x 0.6m x 0.15m depth, non-slip, drain plug",
     "FRP (fibreglass) prefab trough with non-slip rubber mat, drain valve, stainless steel frame surround",
     8000, 15000, 10000, 20000,
     "FRP suppliers in Hyderabad; or fabricate locally in concrete (cheaper)",
     "1) Depth min 15cm for hoof immersion\n2) Non-slip surface mandatory — buffaloes slip on smooth surfaces\n3) Drain plug for easy changing of disinfectant\n4) Copper sulphate or Virkon S solution used inside\n5) One at farm entry, one at milking parlour entry",
     "10 kg copper sulphate (annual stock), 5 kg Virkon S disinfectant, 2 rubber mats",
     "Not applicable", "MEDIUM"),

    (11, "Hygiene", "Sprinkler / Mist Disinfection System at Entry Gate", 1, "Unit",
     "Automatic misting nozzles at gate, timer-controlled, 12V solenoid valve, covers person + vehicle entry",
     "SS316 nozzles, 10-15 nozzle arch, 50L chemical tank, auto timer, UV-resistant pipe",
     25000, 45000, 35000, 35000,
     "IndiaMart: 'vehicle disinfection arch system'; Pune/Hyderabad agri automation suppliers",
     "1) Nozzle coverage: full body spray (top, sides, feet)\n2) Chemical tank min 50L\n3) Auto timer: set to spray for 30 seconds at each entry\n4) SS nozzles — plastic clogs with disinfectant chemicals\n5) Low-pressure type (1-2 bar) — not high pressure\n6) Ask if compatible with Virkon S / glutaraldehyde",
     "20 spare nozzle tips, 1 solenoid valve, 1 timer controller, 1 diaphragm pump",
     "1 Year", "MEDIUM"),

    # ── SHED CLIMATE ──
    ("SHED TEMPERATURE & CLIMATE CONTROL", None),
    (12, "Climate", "Automatic Temperature-Controlled Exhaust Fans", 6, "Unit",
     "Diameter 24 inch, single phase, 1/4 HP motor, 120W, automatic thermostat switch at 30°C, corrosion-resistant blade",
     "FRP (fibreglass) blades (rust-free), SS guard, IP55 weatherproof motor, 3-speed, built-in thermostat, remote monitoring",
     8000, 14000, 11000, 66000,
     "Electrical/hardware shops in Srikakulam or Vizianagaram; IndiaMart: 'exhaust fan agricultural 24 inch'",
     "1) FRP blades only — metal blades rust within 1 year in farm environment\n2) IP55 weatherproof motor — farm has dust, moisture, ammonia\n3) Built-in thermostat or connect to central controller\n4) Airflow: min 3000 CFM per fan\n5) Placement: one fan every 6-7 metres of shed length\n6) Must have automatic on/off at 30°C and 35°C setpoints",
     "6 spare FRP blades, 6 motor capacitors, 6 thermostat switches",
     "1 Year", "HIGH"),

    (13, "Climate", "Automatic Thermostat Controller (Central)", 1, "Unit",
     "Digital temperature controller, 4-channel output (for 4-6 fans), 230V relay output, set point 28-35°C",
     "8-channel, temperature + humidity sensing, LCD display, GSM alert on phone when temp exceeds limit, data logging",
     5000, 15000, 10000, 10000,
     "Electronics shops in Srikakulam; IndiaMart: 'automatic temperature controller fan agriculture'",
     "1) Number of output channels must match number of fans\n2) Relay output rating: min 10A per channel\n3) Set ON at 30°C, OFF at 27°C — ask if adjustable\n4) GSM/WiFi alert feature — very useful for remote monitoring\n5) IP54 enclosure (dust and moisture proof)",
     "2 spare temperature sensors (NTC type), 2 spare relays (10A), 1 spare power supply unit",
     "1 Year", "MEDIUM"),

    (14, "Climate", "Mist Cooling System (Fogger) for Shed", 1, "Set",
     "High pressure fogging, 50 nozzles minimum for 4000 sqft shed, stainless nozzles 0.3mm, pump 70 bar",
     "Fully stainless system, 70-100 bar pump, 50-60 micron droplet size, auto timer + thermostat trigger, separate zone control",
     40000, 80000, 60000, 60000,
     "Hyderabad/Pune misting system suppliers; IndiaMart: 'high pressure misting system dairy farm'",
     "1) Droplet size must be under 50 microns (evaporates before wetting animals)\n2) Stainless nozzles only — plastic clogs in AP water\n3) Pump pressure min 70 bar for true fogging\n4) Auto trigger on thermostat at 32°C\n5) Nozzle spacing max 1.5 metres\n6) Water filter (50 micron) mandatory before pump — AP water has minerals that block nozzles",
     "100 spare nozzle tips (0.3mm), 1 pump seal kit, 2 water filter cartridges (50 micron), 5m spare SS pipe",
     "1 Year", "HIGH"),

    (15, "Climate", "Ceiling/Paddle Fans for Milking Parlour", 2, "Unit",
     "48-inch sweep, 70W, single phase, 3-speed, corrosion-resistant",
     "BLDC motor (energy efficient), rust-proof aluminium/ABS blades, 5-speed, remote control",
     3000, 6000, 4500, 9000,
     "Any electrical shop; Brands: Orient, Havells, Crompton",
     "1) Blade material: ABS plastic or aluminium — not iron (rust)\n2) BLDC motor saves 50% electricity vs regular\n3) Must work at low speed to avoid stress to animals during milking",
     "2 capacitors, 2 speed regulators",
     "2 Years", "LOW"),

    # ── MANURE MANAGEMENT ──
    ("MANURE & WASTE MANAGEMENT", None),
    (16, "Manure", "Automatic Manure Floor Scraper", 1, "Unit",
     "Electric cable-driven or hydraulic, scraper blade width min 2m, covers full shed length, auto timer 2x/day",
     "Heavy duty steel blade with rubber edge, cable + winch system, 0.5 HP motor, auto reverse, daily timer, remote override",
     120000, 180000, 150000, 150000,
     "Hyderabad dairy equipment dealers; IndiaMart: 'automatic manure scraper cattle shed'",
     "1) Scraper width must match your shed passageway width exactly\n2) Rubber edge on blade — prevents concrete floor damage\n3) Cable vs hydraulic: cable type is simpler to maintain\n4) Auto timer: set at 5 AM and 6 PM\n5) Check if motor is weatherproof (IP55)\n6) Get installation included in price",
     "1 spare scraper blade edge (rubber), 20m spare cable, 2 cable end clamps, 1 motor capacitor, 1 set pulleys",
     "1 Year", "HIGH"),

    (17, "Manure", "Biogas Plant (Fixed Dome, 30 m³)", 1, "Unit",
     "KVIC/Deenbandhu design, 30 cubic metre capacity, brick and cement construction, inlet pipe, outlet slurry tank",
     "GLS (Glass-Lined Steel) prefab type, inlet mixer, slurry agitator, gas storage dome 6 m³, outlet connected to compost yard",
     250000, 350000, 300000, 300000,
     "MNRE-registered biogas contractors; AP KVIC office Hyderabad; IndiaMart: 'biogas plant 30 cubic metre'",
     "1) Use only MNRE-registered contractor for government subsidy eligibility\n2) Inlet pipe must have mixer to break scum\n3) Gas pipe: ISI-approved HDPE pipe, not regular PVC\n4) Check gas pressure: min 8-10 cm water column\n5) Slurry outlet must connect directly to compost yard\n6) Demand MNRE registration certificate from contractor",
     "10m HDPE gas pipe (extra), 2 gas valves, 1 pressure gauge, 1 gas stove (for kitchen use), inlet mixing tool",
     "5 Years (civil structure)", "HIGH"),

    (18, "Manure", "Compost Turner / Windrow Turner (Manual)", 1, "Unit",
     "Manual push type, 5-tine fork on wheels, or basic power tiller attachment",
     "Tractor-mounted windrow turner if tractor available, or motorised compost aerator",
     15000, 40000, 25000, 25000,
     "Agriculture equipment dealers; if tractor available, get tractor attachment",
     "1) Match to your tractor PTO if you have one\n2) Manual type sufficient for 40 animals\n3) Stainless or powder-coated — plain iron rusts fast in compost",
     "2 spare tines/forks, 1 replacement handle",
     "1 Year", "LOW"),

    # ── POWER & SOLAR ──
    ("POWER — SOLAR & BIOGAS", None),
    (19, "Power", "Solar Power System (On-Grid + Battery Backup)", 1, "Set",
     "5 kW system, monocrystalline panels, MNRE approved, grid-tied inverter, net metering compatible",
     "6 kW, Tier-1 panels (Longi/Jinko), 48V lithium battery 100Ah x4, MPPT charge controller, APP monitoring, 25 year panel warranty",
     200000, 300000, 250000, 250000,
     "APEPDCL empanelled solar dealer near Srikakulam; PM Kusum Yojana dealers; IndiaMart: 'solar system 5kw agriculture'",
     "1) Only MNRE/APEPDCL empanelled installers qualify for PM Kusum subsidy\n2) Monocrystalline panels only (higher efficiency)\n3) Panel brand: Tier-1 only (Longi, Jinko, Waaree, Adani)\n4) Inverter: Luminous/Fronius/SMA — not unknown brands\n5) Battery: Lithium preferred over lead acid (longer life)\n6) Ask for net metering connection to APSPDCL\n7) Get 25-year performance warranty on panels in writing",
     "2 spare MC4 connectors, 1 spare fuse set, 1 spare inverter display unit, 10m spare DC cable",
     "5 Years (inverter), 25 Years (panels)", "HIGH"),

    (20, "Power", "Diesel Generator Set (Backup)", 1, "Unit",
     "10 kVA, single/3-phase, self-start, fuel tank 15L, CPCB II emission norms",
     "15 kVA, silent canopy type, Kirloskar/Mahindra engine, auto mains failure (AMF) panel, acoustic enclosure",
     80000, 130000, 105000, 105000,
     "Kirloskar/Mahindra genset dealers in Srikakulam; IndiaMart: 'diesel generator 10 kva silent'",
     "1) KVA rating: 10 kVA minimum (milking machine + cooler + fans)\n2) Silent/canopy type — less noise stress to animals\n3) AMF panel: auto starts when APSPDCL power fails\n4) Fuel tank: 15L minimum (4 hrs backup)\n5) Engine brand: Kirloskar, Mahindra, Greaves — avoid unknown Chinese engines\n6) Check service centre in Srikakulam district",
     "10L diesel fuel can, 5L engine oil (10W-30), 2 oil filters, 1 air filter, 1 fuel filter, 1 V-belt",
     "2 Years", "HIGH"),

    # ── MONITORING & AUTOMATION ──
    ("MONITORING, CCTV & FARM AUTOMATION", None),
    (21, "Monitoring", "CCTV System (8 cameras, NVR, mobile app)", 1, "Set",
     "8 cameras, 2MP resolution, IP66 weatherproof, 30m IR night vision, 4-channel NVR, 1TB HDD, mobile app view",
     "4MP resolution, 8-channel NVR, 2TB HDD, 60m IR, PoE cameras, motion alert on phone, cloud backup option",
     40000, 75000, 55000, 55000,
     "Electronics shops in Srikakulam; IndiaMart: 'CCTV system 8 camera farm'; Brands: Hikvision, CP Plus, Dahua",
     "1) IP66 weatherproof rating mandatory (dust + water)\n2) IR night vision min 30m — animals active at night\n3) Mobile app: must work on 4G/WiFi from anywhere\n4) NVR storage: 1TB minimum (30 days recording)\n5) PoE cameras preferred (single cable for power + data)\n6) Motion alert on phone feature\n7) Cover: entry gate, shed, milking parlour, feed store",
     "1 spare 1TB HDD, 4 spare camera brackets, 10m spare CAT6 cable, 1 spare power supply",
     "2 Years", "MEDIUM"),

    (22, "Monitoring", "IoT Animal Health & Estrus Sensors (Ear Tags)", 40, "Unit",
     "RFID or Bluetooth ear tag, step counter / activity monitor, temperature sensor, battery life min 6 months",
     "GPS + accelerometer, estrus detection, fever alert, cloud dashboard, mobile app per animal history",
     1500, 3000, 2200, 88000,
     "IndiaMart: 'cattle ear tag IoT sensor'; Hyderabad agritech startups; Brands: Moocall, Cowconnect, SmaXtec",
     "1) Battery life: min 6 months — less = too much maintenance\n2) App must work on basic Android phone (not iPhone only)\n3) Estrus detection accuracy: ask for >90% claim\n4) Waterproof IP68 — buffaloes bathe\n5) Indian supplier preferred for after-sales support\n6) Ask for pilot with 5 animals before buying all 40",
     "10 spare ear tag clips, 2 spare charging cables, 1 spare tag reader",
     "1 Year", "MEDIUM"),

    (23, "Monitoring", "Farm Management Software (FMS)", 1, "License",
     "Animal record keeping, milk production log, health records, expense tracker, Hindi/Telugu language",
     "Cloud-based, mobile + desktop, automatic report generation, integration with milking machine data, multi-user",
     20000, 60000, 40000, 40000,
     "IndiaMart: 'dairy farm management software India'; Brands: Herdsman, Stellapps, MooFarm, Afimilk (budget)",
     "1) Telugu/Hindi language support — important for daily workers to use\n2) Works offline (farm may have poor internet)\n3) Mobile app for Android — easy for workers\n4) Automatic milk production report for bank/NABARD\n5) Ask for free trial before purchase\n6) Data export to Excel — needed for bank reporting",
     "N/A (software — annual subscription model)",
     "1 Year subscription", "MEDIUM"),

    # ── ANIMAL CARE ──
    ("ANIMAL CARE & VETERINARY EQUIPMENT", None),
    (24, "Animal Care", "Veterinary First Aid Kit (Farm Level)", 1, "Set",
     "Thermometer, stethoscope, drenching gun, dosing syringe, bandages, antiseptic, gloves",
     "Complete kit: digital thermometer, adult stethoscope, 60ml drenching gun, 20ml syringe set, antiseptic spray, wound powder, ORS sachets, calcium borogluconate bottles (10)",
     5000, 12000, 8000, 8000,
     "Veterinary medicine shops in Ranasthalam or Srikakulam",
     "1) Digital thermometer — glass ones break easily\n2) Stock calcium borogluconate for milk fever (common in Murrah)\n3) Keep ORS sachets for dehydration cases\n4) Antiseptic spray (Povidone iodine) for wound care\n5) Gloves: both examination and heavy-duty rubber",
     "Extra thermometer, 20 gloves, 2 drenching guns, antiseptic spray x3, ORS x10 packets, calcium borogluconate x6 bottles",
     "Not applicable", "HIGH"),

    (25, "Animal Care", "Weighing Scale (Animal / Platform Scale)", 1, "Unit",
     "Platform scale, capacity 1000 kg, digital display, accuracy ±0.5 kg",
     "1500 kg capacity, stainless load cell, remote display, data storage, animal ID entry",
     25000, 50000, 35000, 35000,
     "IndiaMart: 'cattle weighing scale platform 1000kg'",
     "1) Capacity: 1000 kg minimum (adult Murrah buffalo up to 700 kg)\n2) Load cell type: stainless steel (not aluminium — corrodes)\n3) Platform size: min 1.5m x 1.5m for easy animal entry\n4) Waterproof display (IP65)\n5) Battery backup for display",
     "1 spare load cell, 2 spare display batteries, 1 spare platform rubber mat",
     "2 Years", "MEDIUM"),

    # ── MISCELLANEOUS ──
    ("MISCELLANEOUS & SAFETY", None),
    (26, "Safety", "Fire Extinguisher (ABC type, 5kg)", 4, "Unit",
     "ABC dry powder, 5 kg, ISI marked, with hanger bracket",
     "6 kg, ABC type, pressure gauge, safety pin, hose, ISI certified",
     1500, 2500, 2000, 8000,
     "Hardware shops in Srikakulam; any fire safety equipment dealer",
     "1) ISI mark mandatory\n2) Place at: entry, milking parlour, feed store, generator room\n3) Check pressure gauge (needle in green zone)\n4) Annual refilling required",
     "Annual refilling (budget ₹400/unit/year)",
     "5 Years (cylinder)", "HIGH"),

    (27, "Safety", "First Aid Kit (Human)", 1, "Set",
     "Standard workplace first aid kit, bandages, antiseptic, ORS, pain relief",
     "OSHA standard kit, eyewash bottle, burn gel, crepe bandage, wound dressing",
     1000, 2500, 1500, 1500,
     "Any pharmacy",
     "1) Keep near milking parlour\n2) Restock after use",
     "Restock annually",
     "Not applicable", "MEDIUM"),

    (28, "Miscellaneous", "Milking Aprons, Gloves & PPE for Workers", 2, "Set",
     "PVC apron, rubber gloves, rubber boots (gumboots)",
     "Heavy duty PVC apron, nitrile gloves, knee-high gumboots, head cap",
     2000, 4000, 3000, 6000,
     "Srikakulam safety equipment shops or online",
     "1) Gumboot size: confirm worker sizes before ordering\n2) Replace gloves every 3 months",
     "6 pairs extra gloves, 2 extra aprons annually",
     "6 Months (PPE)", "MEDIUM"),
]

# ── write rows ───────────────────────────────────────────────────────────────
row = 5
for item in rows:
    if len(item) == 2 and item[1] is None:
        # Section header
        ws1.merge_cells(start_row=row, start_column=1, end_row=row, end_column=16)
        c = ws1.cell(row=row, column=1, value=f"  ▶  {item[0]}")
        c.fill = fill(DARK_GREEN)
        c.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
        c.alignment = left()
        c.border = border_medium()
        ws1.row_dimensions[row].height = 22
        row += 1
        continue

    (sno, cat, name, qty, unit, min_spec, pref_spec,
     price_low, price_high, avg_price, total_avg,
     supplier, check_before, spares, warranty, priority) = item

    bg = PALE_GREEN if row % 2 == 0 else WHITE
    ws1.row_dimensions[row].height = 80

    priority_colors = {"HIGH": (LIGHT_RED, RED), "MEDIUM": (LIGHT_YELLOW, ORANGE), "LOW": (LIGHT_BLUE, BLUE)}
    p_bg, p_fg = priority_colors.get(priority, (WHITE, "000000"))

    values = [sno, cat, name, qty, unit, min_spec, pref_spec,
              price_low, price_high, avg_price, total_avg,
              supplier, check_before, spares, warranty, priority]
    bgs    = [bg, bg, bg, bg, bg, PALE_GREEN, PALE_BLUE,
              bg, bg, LIGHT_YELLOW, LIGHT_GREEN,
              PALE_BLUE, PALE_ORANGE, LIGHT_YELLOW, bg, p_bg]
    fgs    = ["000000"]*15 + [p_fg]
    bolds  = [True, False, True, True, False, False, False,
              False, False, True, True,
              False, False, False, False, True]

    for col, (val, b, f, bold) in enumerate(zip(values, bgs, fgs, bolds), 1):
        cell = ws1.cell(row=row, column=col, value=val)
        cell.fill = fill(b)
        cell.font = Font(bold=bold, color=f, size=9, name="Calibri")
        cell.alignment = left(wrap=True)
        cell.border = border_thin()
        # currency formatting
        if col in (8, 9, 10, 11):
            cell.number_format = '₹#,##0'

    row += 1

# ── TOTAL ROW ────────────────────────────────────────────────────────────────
ws1.merge_cells(start_row=row, start_column=1, end_row=row, end_column=10)
c = ws1.cell(row=row, column=1, value="GRAND TOTAL — ALL EQUIPMENT")
c.fill = fill(DARK_GREEN)
c.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
c.alignment = center()
c.border = border_medium()

total_val = sum(item[10] for item in rows if len(item) > 2)
c2 = ws1.cell(row=row, column=11, value=total_val)
c2.fill = fill(MID_GREEN)
c2.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
c2.number_format = '₹#,##0'
c2.alignment = center()
c2.border = border_medium()
for col in range(12, 17):
    ws1.cell(row=row, column=col).fill = fill(DARK_GREEN)
    ws1.cell(row=row, column=col).border = border_medium()

ws1.row_dimensions[row].height = 22


# ════════════════════════════════════════════════════════════════════════════
# SHEET 2 — SUPPLIER COMPARISON (4 suppliers per item)
# ════════════════════════════════════════════════════════════════════════════
ws2 = wb.create_sheet("Supplier Comparison")
ws2.sheet_view.showGridLines = False
ws2.freeze_panes = "A5"

ws2.merge_cells("A1:S1")
c = ws2["A1"]
c.value = "SUPPLIER QUOTATION COMPARISON SHEET — Fill in prices after getting quotations"
c.fill = fill(BLUE)
c.font = Font(bold=True, color="FFFFFF", size=14, name="Calibri")
c.alignment = center()
ws2.row_dimensions[1].height = 28

ws2.merge_cells("A2:S2")
c = ws2["A2"]
c.value = "Get quotations from at least 2 suppliers for each item. Enter prices below. Green = lowest price."
c.fill = fill(LIGHT_BLUE)
c.font = Font(bold=False, color=BLUE, size=10, name="Calibri")
c.alignment = center()
ws2.row_dimensions[2].height = 18

ws2.merge_cells("A3:S3")
c = ws2["A3"]
c.value = "INSTRUCTIONS: Call/WhatsApp suppliers. Ask for 'written quotation on letterhead with GST number'. Keep PDF/photo for bank file."
c.fill = fill(LIGHT_YELLOW)
c.font = Font(bold=False, color=ORANGE, size=9, name="Calibri")
c.alignment = center()
ws2.row_dimensions[3].height = 16

# headers
sup_headers = [
    "S.No", "Equipment Name", "Qty",
    "Supplier 1\nName", "Supplier 1\nPhone", "Supplier 1\nPrice (₹)", "Supplier 1\nGST%", "Supplier 1\nDelivery Days",
    "Supplier 2\nName", "Supplier 2\nPhone", "Supplier 2\nPrice (₹)", "Supplier 2\nGST%", "Supplier 2\nDelivery Days",
    "Supplier 3\nName", "Supplier 3\nPhone", "Supplier 3\nPrice (₹)", "Supplier 3\nGST%", "Supplier 3\nDelivery Days",
    "Lowest Price\nSelected (₹)"
]
sup_widths = [5, 25, 5,
              18, 14, 12, 8, 12,
              18, 14, 12, 8, 12,
              18, 14, 12, 8, 12,
              14]

for i, (h, w) in enumerate(zip(sup_headers, sup_widths), 1):
    ws2.column_dimensions[get_column_letter(i)].width = w
    apply(ws2, 4, i, h, bg=GREY_HEAD, bold=True, fcolor="FFFFFF",
          size=9, align=center(wrap=True))
ws2.row_dimensions[4].height = 35

# Equipment rows (subset — main equipment only)
equipment_items = [
    (1, "Automatic Bucket Milking Machine", 2),
    (2, "Bulk Milk Cooler / Milk Chiller 500L", 1),
    (3, "Milk Fat/SNF Analyzer (Milko Tester)", 1),
    (4, "TMR Mixer / Feed Mixer Wagon", 1),
    (5, "Electric Chaff Cutter (Heavy Duty)", 1),
    (6, "Automatic Feed Conveyor Belt", 1),
    (7, "Automatic Water Drinker / Bowl (Buffalo)", 10),
    (8, "Overhead Water Tank 5000L", 2),
    (9, "High Pressure Washer", 1),
    (10, "Foot Bath Trough at Entry", 2),
    (11, "Sprinkler Disinfection System at Gate", 1),
    (12, "Automatic Temperature-Controlled Exhaust Fan 24\"", 6),
    (13, "Automatic Thermostat Controller", 1),
    (14, "Mist Cooling System (Shed Fogger)", 1),
    (15, "Ceiling Fans — Milking Parlour", 2),
    (16, "Automatic Manure Floor Scraper", 1),
    (17, "Biogas Plant (30 m³)", 1),
    (18, "Solar Power System (5-6 kW)", 1),
    (19, "Diesel Generator Set (10 kVA)", 1),
    (20, "CCTV System (8 cameras + NVR)", 1),
    (21, "IoT Animal Health Sensors (Ear Tags)", 40),
    (22, "Farm Management Software", 1),
    (23, "Veterinary First Aid Kit", 1),
    (24, "Platform Weighing Scale (1000 kg)", 1),
    (25, "Fire Extinguishers (5 kg ABC)", 4),
]

sup_row = 5
for sno, name, qty in equipment_items:
    bg = PALE_GREEN if sup_row % 2 == 0 else WHITE
    ws2.row_dimensions[sup_row].height = 22
    for col in range(1, 20):
        cell = ws2.cell(row=sup_row, column=col)
        cell.fill = fill(bg)
        cell.border = border_thin()
        cell.alignment = center(wrap=False)
        cell.font = Font(size=9, name="Calibri")

    ws2.cell(row=sup_row, column=1, value=sno).font = Font(bold=True, size=9, name="Calibri")
    ws2.cell(row=sup_row, column=2, value=name).alignment = left()
    ws2.cell(row=sup_row, column=2).font = Font(bold=True, size=9, name="Calibri")
    ws2.cell(row=sup_row, column=3, value=qty)

    # placeholder text for supplier cells
    for sup_start in [4, 9, 14]:
        ws2.cell(row=sup_row, column=sup_start, value="[Enter name]").font = Font(color="AAAAAA", size=8, name="Calibri", italic=True)
        ws2.cell(row=sup_row, column=sup_start+1, value="[Phone]").font = Font(color="AAAAAA", size=8, name="Calibri", italic=True)
        ws2.cell(row=sup_row, column=sup_start+2).number_format = '₹#,##0'
        ws2.cell(row=sup_row, column=sup_start+3, value="18%").font = Font(size=8, name="Calibri")
        ws2.cell(row=sup_row, column=sup_start+4, value="[Days]").font = Font(color="AAAAAA", size=8, name="Calibri", italic=True)

    ws2.cell(row=sup_row, column=19).fill = fill(LIGHT_GREEN)
    ws2.cell(row=sup_row, column=19).number_format = '₹#,##0'
    sup_row += 1


# ════════════════════════════════════════════════════════════════════════════
# SHEET 3 — SPARE PARTS MASTER STOCK LIST
# ════════════════════════════════════════════════════════════════════════════
ws3 = wb.create_sheet("Spare Parts Stock List")
ws3.sheet_view.showGridLines = False
ws3.freeze_panes = "A5"

ws3.merge_cells("A1:J1")
c = ws3["A1"]
c.value = "SPARE PARTS MASTER STOCK LIST — Buy These Along With Each Equipment"
c.fill = fill(ORANGE)
c.font = Font(bold=True, color="FFFFFF", size=14, name="Calibri")
c.alignment = center()
ws3.row_dimensions[1].height = 28

ws3.merge_cells("A2:J2")
c = ws3["A2"]
c.value = "Keep these spares at the farm always. Ordering from Hyderabad takes 3-5 days — downtime costs more than spare parts."
c.fill = fill(PALE_ORANGE)
c.font = Font(bold=False, color=ORANGE, size=10, name="Calibri")
c.alignment = center()
ws3.row_dimensions[2].height = 18

ws3.merge_cells("A3:J3")
ws3.row_dimensions[3].height = 10

spare_headers = ["S.No", "Equipment", "Spare Part Name", "Qty to Stock", "Unit",
                 "Approx Cost (₹)", "Where to Buy", "Reorder Level", "Current Stock", "Notes"]
spare_widths   = [5, 22, 30, 12, 8, 14, 25, 14, 14, 30]

for i, (h, w) in enumerate(zip(spare_headers, spare_widths), 1):
    ws3.column_dimensions[get_column_letter(i)].width = w
    apply(ws3, 4, i, h, bg=ORANGE, bold=True, fcolor="FFFFFF", size=10, align=center(wrap=True))
ws3.row_dimensions[4].height = 28

spares_data = [
    # MILKING
    (1,  "Milking Machine",     "Teat Cup Liner (silicon)",             4,  "Set",   2000,  "Dairy equipment dealer / IndiaMart",   2,  "", "Replace every 2500 milkings or 6 months"),
    (2,  "Milking Machine",     "Pulsator Membrane / Diaphragm",        4,  "Pcs",   800,   "Same supplier as machine",             2,  "", "Replace if pulsation rate drops"),
    (3,  "Milking Machine",     "Vacuum Pump Oil (1 litre)",            2,  "Bottle",500,   "Auto parts shop / pump dealer",        1,  "", "Change every 3 months"),
    (4,  "Milking Machine",     "Milk Tube (silicon, 1m)",              4,  "Pcs",   400,   "Dairy equipment dealer",               2,  "", "Replace if cracked or discoloured"),
    (5,  "Milking Machine",     "Teat Cup Shell (SS304)",               2,  "Pcs",   1500,  "Dairy equipment dealer",               1,  "", "Replace if dented/damaged"),
    (6,  "Milk Cooler",         "Compressor Capacitor",                 1,  "Pcs",   800,   "AC/refrigeration shop",                1,  "", "Keep as emergency spare"),
    (7,  "Milk Cooler",         "Thermostat Sensor (NTC)",              2,  "Pcs",   600,   "Electronics shop / IndiaMart",         1,  "", "Faulty thermostat = milk spoilage"),
    (8,  "Milk Cooler",         "Agitator Motor (0.25 HP)",             1,  "Unit",  3500,  "Motor dealer / AC shop",               1,  "", "Critical — milk must be agitated"),
    (9,  "Milk Cooler",         "Door Gasket Set",                      1,  "Set",   1200,  "Same supplier as cooler",              1,  "", "Replace if not sealing — temp rises"),
    (10, "Milk Cooler",         "Refrigerant Gas R-404A (bottle)",      1,  "Can",   3500,  "Refrigeration service technician",     1,  "", "Keep for service calls — do not DIY"),
    # FEEDING
    (11, "TMR Mixer",           "Auger / Mixing Blade",                 2,  "Pcs",   4000,  "Supplier / fabricate locally",         1,  "", "Inspect monthly — wear is slow"),
    (12, "TMR Mixer",           "Mixer Drive Belt",                     2,  "Pcs",   1500,  "Belt dealer / IndiaMart",              1,  "", "Check tension weekly"),
    (13, "TMR Mixer",           "Gear Oil (90W, 2 litre)",              2,  "Bottle",600,   "Auto parts shop",                      1,  "", "Change every 6 months"),
    (14, "TMR Mixer",           "Motor Capacitor",                      1,  "Pcs",   400,   "Electrical shop",                      1,  "", "Motor hums but won't start = dead cap"),
    (15, "Chaff Cutter",        "Cutting Blades (set of 3)",            2,  "Set",   2500,  "Agri equipment dealer Srikakulam",     1,  "", "Sharpen every month; replace when worn"),
    (16, "Chaff Cutter",        "V-Belt (A/B section)",                 2,  "Pcs",   600,   "Belt dealer / agri shop",              1,  "", "Snap without warning — always have spare"),
    (17, "Chaff Cutter",        "Shear Bolt (safety fuse bolt)",        10, "Pcs",   50,    "Agri shop",                            5,  "", "These break on stone/metal contact — normal"),
    # WATER & HYGIENE
    (18, "Water Drinker",       "Float Valve (brass, 1/2 inch)",        10, "Pcs",   250,   "Plumbing shop Srikakulam",             5,  "", "Replace if bowl overflows or stays empty"),
    (19, "Water Drinker",       "Inlet Washer/Gasket",                  20, "Pcs",   20,    "Plumbing shop",                        10, "", "Replace with float valve"),
    (20, "Pressure Washer",     "High Pressure Hose (10m)",             1,  "Pcs",   2500,  "Same supplier / hardware shop",        1,  "", "Replace if burst or leaking"),
    (21, "Pressure Washer",     "Spray Nozzle Set (0°, 25°, 40°)",      2,  "Set",   800,   "Hardware shop",                        1,  "", "Nozzles block with hard water — keep spare"),
    (22, "Pressure Washer",     "Pump Seal Kit",                        1,  "Set",   1500,  "Same supplier",                        1,  "", "Replace if water leaks from pump head"),
    # CLIMATE
    (23, "Exhaust Fan",         "FRP Fan Blade (24 inch)",              6,  "Pcs",   1200,  "Fan dealer / IndiaMart",               3,  "", "Replace if cracked — imbalance damages motor"),
    (24, "Exhaust Fan",         "Motor Capacitor (4-6 µF)",             6,  "Pcs",   350,   "Electrical shop",                      3,  "", "Fan hums but won't spin = dead capacitor"),
    (25, "Exhaust Fan",         "Thermostat Switch",                    3,  "Pcs",   600,   "Electrical/electronics shop",          2,  "", "Keep 3 spares for 6 fans"),
    (26, "Mist Cooling",        "Nozzle Tip (0.3mm SS)",                100,"Pcs",   80,    "Misting supplier / IndiaMart",         50, "", "Block frequently with hard water — clean weekly"),
    (27, "Mist Cooling",        "Water Filter Cartridge (50 micron)",   4,  "Pcs",   400,   "Hardware/plumbing shop",               2,  "", "Change monthly in AP (hard water)"),
    (28, "Mist Cooling",        "Pump Seal Kit",                        1,  "Set",   2000,  "Misting system supplier",              1,  "", "Replace if pressure drops suddenly"),
    # MANURE
    (29, "Floor Scraper",       "Scraper Rubber Edge Blade",            1,  "Set",   4000,  "Supplier or fabricate locally",        1,  "", "Replace when worn — scratches floor if missing"),
    (30, "Floor Scraper",       "Cable (galvanised, 20m)",              1,  "Roll",  3000,  "Hardware/cable dealer",                1,  "", "Check for fraying monthly"),
    (31, "Floor Scraper",       "Cable End Clamps",                     4,  "Pcs",   200,   "Hardware shop",                        2,  "", "Keep spare — lose grip suddenly"),
    (32, "Floor Scraper",       "Motor Capacitor",                      1,  "Pcs",   400,   "Electrical shop",                      1,  "", ""),
    # POWER
    (33, "Generator",           "Engine Oil 10W-30 (5 litre)",          2,  "Can",   1800,  "Auto parts shop / Kirloskar dealer",   1,  "", "Change every 250 hrs or 6 months"),
    (34, "Generator",           "Oil Filter",                           2,  "Pcs",   500,   "Kirloskar/Mahindra service centre",    1,  "", "Change with oil"),
    (35, "Generator",           "Air Filter",                           2,  "Pcs",   400,   "Service centre",                       1,  "", "Clean monthly; replace every 6 months"),
    (36, "Generator",           "Fuel Filter",                          2,  "Pcs",   350,   "Service centre",                       1,  "", "Replace every 6 months"),
    (37, "Generator",           "V-Belt",                               2,  "Pcs",   600,   "Belt dealer",                          1,  "", "Inspect monthly"),
    (38, "Solar System",        "MC4 Connector Pair",                   5,  "Pair",  120,   "Solar dealer / electronics shop",      2,  "", "Replace if panel output drops"),
    (39, "Solar System",        "Fuse Set (DC, 10A-30A)",               1,  "Set",   500,   "Electrical shop",                      1,  "", "Keep full fuse assortment"),
    # MONITORING
    (40, "CCTV",                "Hard Disk Drive 1TB (Surveillance)",   1,  "Pcs",   3500,  "Electronics shop",                     1,  "", "Replace if recording stops; lifespan ~3 years"),
    (41, "CCTV",                "Power Supply Unit (12V, 5A)",          1,  "Pcs",   800,   "Electronics shop",                     1,  "", "Replace if cameras go offline suddenly"),
    # BIOGAS
    (42, "Biogas Plant",        "Gas Valve (brass, 1/2 inch)",          3,  "Pcs",   600,   "Hardware/plumbing shop",               1,  "", "Replace if gas smell detected at valve"),
    (43, "Biogas Plant",        "Pressure Gauge (0-30 cm WC)",          1,  "Pcs",   800,   "Industrial supplier",                  1,  "", "Check daily — indicates plant health"),
    (44, "Biogas Plant",        "HDPE Gas Pipe (10m, 1/2 inch)",        1,  "Roll",  1500,  "Hardware shop",                        1,  "", "Keep for extension or repair"),
    # GENERAL
    (45, "General",             "Electrical MCB/Fuse Box (spare MCBs)", 1,  "Set",   1500,  "Electrical shop",                      1,  "", "Keep 6A, 16A, 32A sizes"),
    (46, "General",             "Cable Ties (100 pcs)",                 2,  "Pack",  200,   "Hardware shop",                        1,  "", "Always useful"),
    (47, "General",             "Teflon Tape (plumbing, 10 rolls)",     1,  "Set",   200,   "Hardware shop",                        5,  "", "For all water pipe joints"),
    (48, "General",             "WD-40 Spray (multipurpose lubricant)", 3,  "Can",   400,   "Hardware shop",                        1,  "", "For all moving parts — prevents rust"),
    (49, "General",             "Disinfectant — Virkon S (1 kg pack)",  5,  "Pack",  1200,  "Vet medicine shop",                    2,  "", "For foot bath, gate spray, shed cleaning"),
    (50, "General",             "Copper Sulphate (5 kg)",               2,  "Bag",   1000,  "Agri shop / vet shop",                 1,  "", "For foot bath — prevents hoof rot"),
]

spare_row = 5
for item in spares_data:
    sno, equip, spare, qty_stock, unit_s, cost, where, reorder, curr, notes = item
    bg = PALE_ORANGE if spare_row % 2 == 0 else WHITE
    ws3.row_dimensions[spare_row].height = 22

    vals = [sno, equip, spare, qty_stock, unit_s, cost, where, reorder, curr, notes]
    for col, val in enumerate(vals, 1):
        cell = ws3.cell(row=spare_row, column=col, value=val)
        cell.fill = fill(bg)
        cell.border = border_thin()
        cell.alignment = left(wrap=True)
        cell.font = Font(size=9, name="Calibri",
                        bold=(col in [1,2,3]))
        if col == 6:
            cell.number_format = '₹#,##0'
    spare_row += 1

# Total spares cost
ws3.merge_cells(start_row=spare_row, start_column=1, end_row=spare_row, end_column=5)
c = ws3.cell(row=spare_row, column=1, value="TOTAL ESTIMATED SPARE PARTS COST (One-time with equipment purchase)")
c.fill = fill(ORANGE)
c.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
c.alignment = center()
c.border = border_medium()
total_spares = sum(item[3]*item[5] for item in spares_data)
ct = ws3.cell(row=spare_row, column=6, value=total_spares)
ct.fill = fill(ORANGE)
ct.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
ct.number_format = '₹#,##0'
ct.alignment = center()
ct.border = border_medium()
for col in range(7, 11):
    ws3.cell(row=spare_row, column=col).fill = fill(ORANGE)
    ws3.cell(row=spare_row, column=col).border = border_medium()


# ════════════════════════════════════════════════════════════════════════════
# SHEET 4 — SUPPLIER CONTACT DIRECTORY
# ════════════════════════════════════════════════════════════════════════════
ws4 = wb.create_sheet("Supplier Directory")
ws4.sheet_view.showGridLines = False

ws4.merge_cells("A1:H1")
c = ws4["A1"]
c.value = "SUPPLIER CONTACT DIRECTORY — Andhra Pradesh & Telangana"
c.fill = fill(MID_GREEN)
c.font = Font(bold=True, color="FFFFFF", size=14, name="Calibri")
c.alignment = center()
ws4.row_dimensions[1].height = 28

ws4.merge_cells("A2:H2")
c = ws4["A2"]
c.value = "Call these suppliers, mention: 40-animal Murrah buffalo dairy farm in Srikakulam AP. Ask for written quotation with GST on letterhead."
c.fill = fill(LIGHT_GREEN)
c.font = Font(bold=False, color=DARK_GREEN, size=10, name="Calibri")
c.alignment = center()

dir_headers = ["Category", "Supplier Type / Brand", "Location", "What They Supply",
               "How to Contact", "IndiaMart Search Term", "Expected Price Range", "Notes"]
dir_widths   = [18, 22, 18, 30, 25, 30, 20, 35]

for i, (h, w) in enumerate(zip(dir_headers, dir_widths), 1):
    ws4.column_dimensions[get_column_letter(i)].width = w
    apply(ws4, 3, i, h, bg=DARK_GREEN, bold=True, fcolor="FFFFFF", size=10, align=center(wrap=True))
ws4.row_dimensions[3].height = 28

directory = [
    ("Milking & Dairy Equip.", "BouMatic / GEA India", "Hyderabad", "Milking machines, milk coolers, milking parlour equipment",
     "Search Google: 'BouMatic dairy equipment Hyderabad' or call local dealer",
     "BouMatic milking machine dealer Hyderabad", "₹1.5L–5L per unit", "Premium brand; good after-sales in AP"),
    ("Milking & Dairy Equip.", "Raj Process Equipments", "Pune (ships to AP)", "Milking machines, bulk coolers, dairy tanks",
     "IndiaMart enquiry; WhatsApp quotation", "Raj Process dairy equipment milking machine", "₹90K–2L per unit", "Indian brand; good value for money"),
    ("Milking & Dairy Equip.", "Milkrite / Interpuls", "Pan India dealers", "Teat cup liners, cluster assemblies, milking spares",
     "IndiaMart: Milkrite India", "Milkrite teat cup liner buffalo", "₹800–2500 per set", "Best quality liners for buffalo"),
    ("Milking & Dairy Equip.", "Condux / Milky Way Dairy", "Hyderabad", "Bulk milk coolers, milk storage tanks, pasteurisers",
     "Google: 'Milky Way dairy equipment Hyderabad'", "bulk milk cooler 500 litre Hyderabad", "₹2L–3.5L", "Ask for CIP-compatible cooler"),
    ("Milk Analyzer",          "Lactoscan / Milkotronic", "Pan India (import)", "Ultrasonic milk analyzers (fat, SNF, water, density)",
     "IndiaMart enquiry", "Lactoscan milk analyzer India", "₹60K–1.2L", "Bulgarian brand; most dairies use this"),
    ("Milk Analyzer",          "Kumbhat & Co.",           "Hyderabad/Mumbai",  "Milk testing equipment, FSSAI lab equipment",
     "IndiaMart: 'Kumbhat dairy equipment'", "milk fat SNF analyzer India", "₹50K–90K", "Indian distributor; good support"),
    ("Feed Equipment",         "Shaktiman / Landforce",   "Dealers in AP",     "Chaff cutters, TMR mixers, fodder equipment",
     "Local agri equipment dealer or IndiaMart", "Shaktiman chaff cutter 5HP dealer AP", "₹60K–1.2L", "Very common in AP; spare parts easily available"),
    ("Feed Equipment",         "Hay King / Forage Plus",  "Hyderabad",         "TMR mixers, feed wagons, forage equipment",
     "IndiaMart: 'TMR mixer dairy farm India'", "TMR mixer feed wagon 500kg dairy", "₹2.5L–5L", "Get demo before buying — mixers vary a lot"),
    ("Climate Control",        "Vostermans Ventilation",  "Hyderabad dealer",  "Agricultural exhaust fans, tunnel ventilation systems",
     "Google: 'Vostermans fan dealer Hyderabad'", "agricultural exhaust fan 24 inch FRP", "₹10K–20K/fan", "Dutch brand; best for farm use; FRP blades"),
    ("Climate Control",        "Munters / EWS Systems",   "Hyderabad/Chennai", "Misting/fogging systems, evaporative cooling, dairy climate",
     "IndiaMart: 'high pressure misting system dairy'", "dairy shed misting cooling system 70 bar", "₹40K–1L", "Ask specifically for 70 bar pump — lower won't mist"),
    ("Solar Power",            "APEPDCL Empanelled Dealer","Srikakulam district","Solar panels, inverters, batteries, PM Kusum installation",
     "Call APEPDCL helpline 1912 for nearest empanelled dealer", "solar system 5kw agriculture AP subsidy", "₹2L–3.5L", "MUST use empanelled dealer for PM Kusum subsidy"),
    ("Solar Power",            "Waaree / Adani Solar",    "Pan India dealers", "Solar panels (Tier-1), inverters",
     "IndiaMart: 'Waaree solar panel dealer Srikakulam'", "Waaree solar panel dealer Andhra Pradesh", "₹25–35/watt", "Tier-1 panel brands for 25-year warranty"),
    ("Generator",              "Kirloskar Green",         "Srikakulam / Viza.", "Diesel generator sets, AMF panels, service",
     "Kirloskar dealer in Srikakulam — Google 'Kirloskar genset dealer Srikakulam'", "Kirloskar genset 10 kva silent dealer Srikakulam", "₹85K–1.5L", "Best service network in AP"),
    ("Manure / Scraper",       "Dairy Equipment India",   "Hyderabad/Pune",    "Automatic manure scrapers, barn equipment",
     "IndiaMart: 'automatic manure scraper cattle barn'", "automatic manure floor scraper cattle shed", "₹1.2L–2L", "Get installation included in price"),
    ("Biogas Plant",           "MNRE Registered Contractor","Srikakulam/Viza.", "Biogas plant construction, KVIC design",
     "Contact KVIC office: Hyderabad or district KVIC officer", "biogas plant 30 cubic metre KVIC contractor AP", "₹2.5L–3.5L", "Only MNRE registered = eligible for subsidy"),
    ("CCTV & Security",        "CP Plus / Hikvision",     "Srikakulam town",   "CCTV cameras, NVR, mobile app monitoring",
     "Any CCTV dealer in Srikakulam — very common", "CP Plus CCTV 8 camera NVR kit", "₹40K–80K for full system", "CP Plus has good Indian support; Hikvision also fine"),
    ("Water Equipment",        "Sintex / Vectus",         "Hardware shops AP", "LLDPE water tanks, overhead tanks",
     "Any hardware/plumbing shop in Srikakulam", "Sintex water tank 5000 litre Srikakulam", "₹15K–22K each", "ISI mark mandatory — check before buying"),
    ("IoT / Farm Tech",        "Stellapps / MooFarm",     "Bangalore (ships)",  "IoT ear tags, milk yield sensors, farm management app",
     "IndiaMart or direct website: stellapps.com", "cattle IoT ear tag activity sensor India", "₹1.5K–3K per tag", "Stellapps is used by Vijaya Dairy — ask for compatibility"),
    ("Disinfectants",          "Local Vet Medicine Shop", "Ranasthalam / Skm", "Virkon S, Copper Sulphate, Iodine, vaccines",
     "Visit: veterinary medicine shop near JDAH office Srikakulam", "Virkon S disinfectant 1kg", "₹1K–1.5K/kg (Virkon)", "Buy 6-month stock when you open account"),
    ("Pressure Washer",        "Karcher / Bosch",         "Srikakulam / online","High pressure washers, industrial cleaners",
     "Amazon India / local hardware shop", "Karcher pressure washer 120 bar farm", "₹18K–35K", "Karcher has good service; Bosch also fine"),
]

dir_row = 4
for item in directory:
    bg = PALE_GREEN if dir_row % 2 == 0 else WHITE
    ws4.row_dimensions[dir_row].height = 45
    for col, val in enumerate(item, 1):
        cell = ws4.cell(row=dir_row, column=col, value=val)
        cell.fill = fill(bg)
        cell.border = border_thin()
        cell.alignment = left(wrap=True)
        cell.font = Font(size=9, name="Calibri", bold=(col == 1))
    dir_row += 1


# ════════════════════════════════════════════════════════════════════════════
# SHEET 5 — BUYING CHECKLIST
# ════════════════════════════════════════════════════════════════════════════
ws5 = wb.create_sheet("Buying Checklist")
ws5.sheet_view.showGridLines = False

ws5.merge_cells("A1:G1")
c = ws5["A1"]
c.value = "EQUIPMENT BUYING CHECKLIST — Follow These Steps for Every Purchase"
c.fill = fill(MID_GREEN)
c.font = Font(bold=True, color="FFFFFF", size=14, name="Calibri")
c.alignment = center()
ws5.row_dimensions[1].height = 28

checklist_widths = [5, 35, 45, 15, 15, 15, 25]
checklist_headers = ["S.No", "Step / Action", "What to Do / What to Ask", "For Bank File?",
                     "Priority", "Done? ✓", "Notes"]
for i, (h, w) in enumerate(zip(checklist_headers, checklist_widths), 1):
    ws5.column_dimensions[get_column_letter(i)].width = w
    apply(ws5, 2, i, h, bg=DARK_GREEN, bold=True, fcolor="FFFFFF", size=10, align=center(wrap=True))
ws5.row_dimensions[2].height = 28

checklist_data = [
    ("BEFORE YOU BUY", None),
    (1,  "Search on IndiaMart for the equipment",
     "Type the equipment name. Click 'Get Latest Price'. Fill your mobile number. Wait for calls.",
     "No", "HIGH", "", "You will get 5-10 calls within 24 hours"),
    (2,  "Contact at least 2-3 suppliers per item",
     "Compare prices, warranty, delivery time, and whether they will give written quotation on letterhead.",
     "No", "HIGH", "", "Never buy from only 1 supplier"),
    (3,  "Ask for written quotation on letterhead with GST",
     "Say: 'Mujhe letterhead pe GST ke saath quotation chahiye bank ke liye' (or in Telugu). Get PDF or printed copy.",
     "YES — Bank needs this", "HIGH", "", "This is mandatory for NABARD/bank file"),
    (4,  "Verify GST number of supplier",
     "Visit: gst.gov.in → Search taxpayer → Enter supplier GST number. Check if active.",
     "No", "MEDIUM", "", "Avoid suppliers with inactive/fake GST"),
    (5,  "Check if supplier can deliver to Srikakulam",
     "Ask: 'Kya aap Srikakulam, Andhra Pradesh mein deliver karte hain?' Confirm freight charges.",
     "No", "HIGH", "", "Get freight cost in writing — some add surprise charges"),
    (6,  "Ask for warranty in writing",
     "Warranty must be on the quotation or a separate warranty card. Verbal promise has no value.",
     "No", "HIGH", "", "Minimum 1 year for all electrical equipment"),
    (7,  "Ask for installation & training",
     "For milking machine, cooler, misting system — ask: 'Do you do installation at site?' Should be free.",
     "No", "HIGH", "", "Pay nothing extra for installation on major equipment"),
    (8,  "Ask for local service support",
     "Ask: 'Do you have a service engineer in Vizianagaram or Srikakulam?' If no, ask how quickly they respond.",
     "No", "HIGH", "", "A broken milking machine = loss every hour"),
    ("WHILE BUYING", None),
    (9,  "Never pay 100% advance",
     "Pay 30-50% advance. Pay balance only after delivery and installation. Never pay full amount upfront.",
     "No", "HIGH", "", "Especially important for large equipment like cooler, milking machine"),
    (10, "Ask for proper tax invoice (GST invoice)",
     "Not just a quotation — a proper GST invoice with HSN code, GSTIN of supplier, your name and address.",
     "YES — For subsidy claim", "HIGH", "", "GST invoice needed for NABARD subsidy documentation"),
    (11, "Check equipment on delivery before signing",
     "Open box, check for damage, check all accessories/spares are included as per quotation. Photograph everything.",
     "No", "HIGH", "", "Refuse damaged goods — supplier must replace"),
    (12, "Get demo/trial run before final payment",
     "Milking machine, TMR mixer, chaff cutter — run them for 15 minutes before paying balance amount.",
     "No", "HIGH", "", "Do not pay balance until satisfied with performance"),
    (13, "Collect all manuals, warranty cards, spares",
     "Operator manual, warranty card, list of spares, service contact number — collect all on delivery day.",
     "No", "MEDIUM", "", "Keep in a folder — will need for servicing"),
    ("FOR BANK / NABARD FILE", None),
    (14, "Collect quotation from 2 suppliers (per item)",
     "Both quotations go into the bank file. Bank needs to see you compared prices (competitive procurement).",
     "YES", "HIGH", "", "Keep both quotations — even the higher one"),
    (15, "Collect GST invoice after purchase",
     "Original GST invoice with GSTIN, HSN code, your address, item description, amount.",
     "YES", "HIGH", "", "NABARD subsidy audit will verify these invoices"),
    (16, "Photograph equipment after installation",
     "Take clear photos with the equipment clearly visible. Include a date reference (show newspaper/phone).",
     "YES", "HIGH", "", "NABARD inspector visits farm and checks physically"),
    ("CLIMATE CONTROL SPECIAL TIPS", None),
    (17, "Exhaust fans: measure your shed height first",
     "Fans must be installed at 2.5-3 metres height for best airflow. Measure before ordering mounting brackets.",
     "No", "MEDIUM", "", ""),
    (18, "Misting system: get water quality tested",
     "AP water is hard (high TDS). Get TDS test at any water lab or use TDS meter (₹500). If TDS >500, add softener.",
     "No", "HIGH", "", "Hard water blocks nozzles in 2 weeks"),
    (19, "Thermostat: set ON at 30°C, OFF at 27°C",
     "Buffaloes are comfortable at 25-30°C. Above 32°C milk production drops significantly.",
     "No", "HIGH", "", "Place temperature sensor at animal body level (1.5m height), not ceiling"),
    ("WATER & HYGIENE SPECIAL TIPS", None),
    (20, "Footbath: change disinfectant every 3 days",
     "Use 5% copper sulphate solution or Virkon S. Dirty/weak solution has no effect. Change schedule is critical.",
     "No", "MEDIUM", "", "Keep a maintenance register — NABARD may check"),
    (21, "Pressure washer: use clean water only",
     "AP bore water is hard and may damage seals. Use overhead tank water for pressure washer — not direct bore.",
     "No", "MEDIUM", "", ""),
]

ck_row = 3
for item in checklist_data:
    if len(item) == 2 and item[1] is None:
        ws5.merge_cells(start_row=ck_row, start_column=1, end_row=ck_row, end_column=7)
        c = ws5.cell(row=ck_row, column=1, value=f"  ▶  {item[0]}")
        c.fill = fill(DARK_GREEN)
        c.font = Font(bold=True, color="FFFFFF", size=11, name="Calibri")
        c.alignment = left()
        c.border = border_medium()
        ws5.row_dimensions[ck_row].height = 22
        ck_row += 1
        continue

    sno, step, what, bank, pri, done, notes = item
    bg = PALE_GREEN if ck_row % 2 == 0 else WHITE
    ws5.row_dimensions[ck_row].height = 35

    p_color = {"HIGH": LIGHT_RED, "MEDIUM": LIGHT_YELLOW}.get(pri, WHITE)

    vals = [sno, step, what, bank, pri, done, notes]
    bgs2  = [bg, bg, bg,
             LIGHT_GREEN if bank == "YES" else bg,
             p_color, LIGHT_BLUE, bg]
    for col, (val, b) in enumerate(zip(vals, bgs2), 1):
        cell = ws5.cell(row=ck_row, column=col, value=val)
        cell.fill = fill(b)
        cell.border = border_thin()
        cell.alignment = left(wrap=True)
        cell.font = Font(size=9, name="Calibri", bold=(col in [2]))
    ck_row += 1


# ── Save ─────────────────────────────────────────────────────────────────────
output = "/Users/D069583/myPrivateProjects/DairyForm/docs/Equipment_Procurement_Guide.xlsx"
wb.save(output)
print(f"Saved: {output}")
print(f"Sheets: {[s.title for s in wb.worksheets]}")
print(f"Total equipment cost (avg): ₹{total_val:,}")
print(f"Total spare parts cost: ₹{total_spares:,}")
