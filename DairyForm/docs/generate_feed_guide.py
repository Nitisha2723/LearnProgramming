"""
Bhavani Dairy Farm — Complete Buffalo Feed & Health Management Guide
40 Murrah Buffaloes · Pydi Bhimavaram, Srikakulam, Andhra Pradesh
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm, cm
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    HRFlowable, PageBreak, KeepTogether
)
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT, TA_JUSTIFY
from reportlab.platypus import Flowable
import os

OUTPUT = os.path.join(os.path.dirname(__file__), "Bhavani_Dairy_Farm_Feed_Health_Guide.pdf")

# ── Colours ────────────────────────────────────────────────────────────────
GREEN  = colors.HexColor("#1d7352")
LGREEN = colors.HexColor("#e6f4ec")
DGREEN = colors.HexColor("#155a3e")
GREY   = colors.HexColor("#f5f7f6")
DGREY  = colors.HexColor("#3d4d47")
GOLD   = colors.HexColor("#e4ae58")
LGOLD  = colors.HexColor("#fff8e7")
RED    = colors.HexColor("#b25248")
LRED   = colors.HexColor("#fce9e7")
BLUE   = colors.HexColor("#3a74c9")
LBLUE  = colors.HexColor("#e8f0fb")
WHITE  = colors.white
BLACK  = colors.HexColor("#17221d")

# ── Styles ─────────────────────────────────────────────────────────────────
def make_styles():
    base = getSampleStyleSheet()
    def S(name, **kw):
        return ParagraphStyle(name, **kw)

    return {
        "cover_title": S("cover_title", fontName="Helvetica-Bold", fontSize=28,
                         textColor=WHITE, alignment=TA_CENTER, spaceAfter=6),
        "cover_sub":   S("cover_sub",   fontName="Helvetica",      fontSize=13,
                         textColor=colors.HexColor("#c8e6d8"), alignment=TA_CENTER, spaceAfter=4),
        "cover_meta":  S("cover_meta",  fontName="Helvetica",      fontSize=10,
                         textColor=colors.HexColor("#a0c4b4"), alignment=TA_CENTER, spaceAfter=3),
        "h1":  S("h1",  fontName="Helvetica-Bold", fontSize=16, textColor=GREEN,
                 spaceBefore=14, spaceAfter=6, borderPad=4),
        "h2":  S("h2",  fontName="Helvetica-Bold", fontSize=12, textColor=DGREEN,
                 spaceBefore=10, spaceAfter=4),
        "h3":  S("h3",  fontName="Helvetica-Bold", fontSize=10, textColor=DGREY,
                 spaceBefore=7,  spaceAfter=3),
        "body": S("body", fontName="Helvetica", fontSize=9, textColor=BLACK,
                  spaceAfter=4, leading=14),
        "bodyb": S("bodyb", fontName="Helvetica-Bold", fontSize=9, textColor=BLACK,
                   spaceAfter=4, leading=14),
        "small": S("small", fontName="Helvetica", fontSize=8, textColor=colors.HexColor("#6b7c75"),
                   spaceAfter=3, leading=12),
        "note":  S("note",  fontName="Helvetica-Oblique", fontSize=8.5,
                   textColor=colors.HexColor("#5a6b64"), spaceAfter=4, leading=13),
        "tch":   S("tch",   fontName="Helvetica-Bold", fontSize=8,
                   textColor=WHITE, alignment=TA_CENTER),
        "tc":    S("tc",    fontName="Helvetica", fontSize=8.5,
                   textColor=BLACK, alignment=TA_CENTER),
        "tcl":   S("tcl",   fontName="Helvetica", fontSize=8.5,
                   textColor=BLACK, alignment=TA_LEFT),
        "tcb":   S("tcb",   fontName="Helvetica-Bold", fontSize=8.5,
                   textColor=BLACK, alignment=TA_LEFT),
        "tct":   S("tct",   fontName="Helvetica-Bold", fontSize=8.5,
                   textColor=GREEN, alignment=TA_LEFT),
        "right": S("right", fontName="Helvetica", fontSize=8.5,
                   textColor=BLACK, alignment=TA_RIGHT),
        "rightb":S("rightb",fontName="Helvetica-Bold", fontSize=8.5,
                   textColor=GREEN, alignment=TA_RIGHT),
        "footer":S("footer",fontName="Helvetica", fontSize=7.5,
                   textColor=colors.HexColor("#9aa7a1"), alignment=TA_CENTER),
    }

ST = make_styles()

def P(text, style="body"): return Paragraph(text, ST[style])
def SP(h=4): return Spacer(1, h*mm)
def HR(): return HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#dce1de"), spaceAfter=4*mm)

def table(data, colWidths, style_cmds, repeat_header=False):
    t = Table(data, colWidths=colWidths, repeatRows=1 if repeat_header else 0)
    base = [
        ("BACKGROUND", (0,0), (-1,0), GREEN),
        ("TEXTCOLOR",  (0,0), (-1,0), WHITE),
        ("FONTNAME",   (0,0), (-1,0), "Helvetica-Bold"),
        ("FONTSIZE",   (0,0), (-1,0), 8),
        ("ALIGN",      (0,0), (-1,-1), "CENTER"),
        ("VALIGN",     (0,0), (-1,-1), "MIDDLE"),
        ("ROWBACKGROUNDS", (0,1), (-1,-1), [WHITE, GREY]),
        ("GRID",       (0,0), (-1,-1), 0.4, colors.HexColor("#dce1de")),
        ("TOPPADDING",  (0,0), (-1,-1), 4),
        ("BOTTOMPADDING",(0,0),(-1,-1), 4),
        ("LEFTPADDING", (0,0), (-1,-1), 5),
        ("RIGHTPADDING",(0,0),(-1,-1), 5),
    ]
    t.setStyle(TableStyle(base + style_cmds))
    return t

def color_row(row, bg, fg=BLACK):
    return ("BACKGROUND", (0,row),(-1,row), bg), ("TEXTCOLOR",(0,row),(-1,row),fg)

# ── Page template ──────────────────────────────────────────────────────────
PAGE_W, PAGE_H = A4
L, R, T, B = 18*mm, 18*mm, 20*mm, 20*mm

def on_page(canvas, doc):
    canvas.saveState()
    # header bar
    canvas.setFillColor(GREEN)
    canvas.rect(0, PAGE_H-12*mm, PAGE_W, 12*mm, fill=1, stroke=0)
    canvas.setFillColor(WHITE)
    canvas.setFont("Helvetica-Bold", 7.5)
    canvas.drawString(L, PAGE_H-7.5*mm, "BHAVANI DAIRY FARM — FEED & HEALTH MANAGEMENT GUIDE 2026")
    canvas.setFont("Helvetica", 7.5)
    canvas.drawRightString(PAGE_W-R, PAGE_H-7.5*mm, "Pydi Bhimavaram · Srikakulam · Andhra Pradesh")
    # footer
    canvas.setFillColor(colors.HexColor("#9aa7a1"))
    canvas.setFont("Helvetica", 7)
    canvas.drawCentredString(PAGE_W/2, 10*mm, f"Page {doc.page}  ·  Prepared for Smt. Danthuluri Bhavani  ·  NABARD DEDS Scheme 2026")
    canvas.restoreState()

def on_first_page(canvas, doc):
    canvas.saveState()
    canvas.setFillColor(colors.HexColor("#9aa7a1"))
    canvas.setFont("Helvetica", 7)
    canvas.drawCentredString(PAGE_W/2, 10*mm, "Bhavani Dairy Farm · Srikakulam · Andhra Pradesh · 2026")
    canvas.restoreState()

# ══════════════════════════════════════════════════════════════════════════════
# BUILD CONTENT
# ══════════════════════════════════════════════════════════════════════════════

def build_cover():
    W = PAGE_W - L - R
    # full-page green background block via a Table
    cover_data = [[""]]
    cover_table = Table(cover_data, colWidths=[W], rowHeights=[PAGE_H - T - B - 30*mm])
    cover_table.setStyle(TableStyle([
        ("BACKGROUND",(0,0),(-1,-1), GREEN),
        ("GRID",(0,0),(-1,-1),0,WHITE),
    ]))

    elements = []
    # Green banner
    banner = Table([[
        Paragraph("BHAVANI DAIRY FARM", ParagraphStyle("ct", fontName="Helvetica-Bold", fontSize=26, textColor=WHITE, alignment=TA_CENTER, spaceAfter=2)),
    ]], colWidths=[W], rowHeights=[14*mm])
    banner.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1),GREEN),("VALIGN",(0,0),(-1,-1),"MIDDLE")]))
    elements.append(banner)

    sub_banner = Table([[
        Paragraph("Complete Feed Preparation, Health & Cost Management Guide", ParagraphStyle("cs", fontName="Helvetica", fontSize=13, textColor=colors.HexColor("#c8e6d8"), alignment=TA_CENTER)),
    ]], colWidths=[W], rowHeights=[10*mm])
    sub_banner.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1),DGREEN),("VALIGN",(0,0),(-1,-1),"MIDDLE")]))
    elements.append(sub_banner)
    elements.append(SP(6))

    # Info boxes
    info = [
        ("40 Murrah Buffaloes", "Herd size"),
        ("Pydi Bhimavaram,\nSrikakulam, AP", "Location"),
        ("NABARD DEDS\n₹44.7L Subsidy", "Scheme"),
        ("2026 Prices\n& Protocols", "Reference year"),
    ]
    info_cells = []
    info_styles = []
    for i, (val, lbl) in enumerate(info):
        cell = Table([
            [Paragraph(val, ParagraphStyle("iv", fontName="Helvetica-Bold", fontSize=11, textColor=GREEN, alignment=TA_CENTER, leading=14))],
            [Paragraph(lbl, ParagraphStyle("il", fontName="Helvetica", fontSize=8, textColor=colors.HexColor("#7e8983"), alignment=TA_CENTER))],
        ], colWidths=[(W-30*mm)/4])
        cell.setStyle(TableStyle([
            ("BACKGROUND",(0,0),(-1,-1),WHITE),
            ("BOX",(0,0),(-1,-1),0.5,colors.HexColor("#dce1de")),
            ("TOPPADDING",(0,0),(-1,-1),6),
            ("BOTTOMPADDING",(0,0),(-1,-1),6),
        ]))
        info_cells.append(cell)

    info_row = Table([info_cells], colWidths=[(W)/4]*4)
    info_row.setStyle(TableStyle([("ALIGN",(0,0),(-1,-1),"CENTER"),("VALIGN",(0,0),(-1,-1),"MIDDLE"),("LEFTPADDING",(0,0),(-1,-1),2),("RIGHTPADDING",(0,0),(-1,-1),2)]))
    elements.append(info_row)
    elements.append(SP(8))

    # TOC
    toc_items = [
        ("1", "Feed Ingredients — Local Availability & Prices"),
        ("2", "Daily Ration Design — Murrah Buffalo Requirements"),
        ("3", "Three Feeding Models Compared"),
        ("4", "Monthly Feed Cost for 40 Buffaloes"),
        ("5", "Seasonal Medication & Vaccination Calendar"),
        ("6", "Minerals, Vitamins & Supplements Protocol"),
        ("7", "Animal Health Tracking System"),
        ("8", "Water Quality Testing"),
        ("9", "Soil Testing for Fodder Cultivation"),
        ("10", "Annual Cost Summary & Revenue Projection"),
        ("11", "Recommended Model & Action Plan"),
    ]
    toc_data = [[P("CONTENTS", "h2")]] + [
        [P(f"  {n}.  {title}", "body")] for n, title in toc_items
    ]
    toc_table = Table(toc_data, colWidths=[W])
    toc_table.setStyle(TableStyle([
        ("BACKGROUND",(0,0),(-1,0),LGREEN),
        ("ROWBACKGROUNDS",(0,1),(-1,-1),[WHITE, GREY]),
        ("BOX",(0,0),(-1,-1),0.5,colors.HexColor("#dce1de")),
        ("GRID",(0,0),(-1,-1),0.3,colors.HexColor("#e4e9e6")),
        ("LEFTPADDING",(0,0),(-1,-1),8),
        ("TOPPADDING",(0,0),(-1,-1),4),
        ("BOTTOMPADDING",(0,0),(-1,-1),4),
    ]))
    elements.append(toc_table)
    elements.append(SP(6))
    elements.append(P("Prepared for: <b>Smt. Danthuluri Bhavani</b>  ·  Pydi Bhimavaram, Srikakulam District, AP 532001", "small"))
    elements.append(P("Prepared by: DairyFlow Smart Management System  ·  September 2026", "small"))
    elements.append(PageBreak())
    return elements

# ─────────────────────────────────────────────────────────────────────────────
def sec1_ingredients():
    W = PAGE_W - L - R
    els = []
    els.append(P("SECTION 1 — FEED INGREDIENTS: LOCAL AVAILABILITY & 2026 PRICES", "h1"))
    els.append(HR())
    els.append(P("Srikakulam district is a major paddy and sugarcane belt with good availability of green fodder, paddy by-products and oilseed cakes. The following ingredients are all procurable within 30–50 km of Pydi Bhimavaram.", "body"))
    els.append(SP(3))

    els.append(P("1.1  Green Fodder", "h2"))
    gf = [
        [P("Ingredient","tch"), P("Season Available","tch"), P("Source","tch"), P("Price/kg (₹)","tch"), P("Dry Matter %","tch"), P("Notes","tch")],
        [P("Napier Grass (CO-3/CO-4)","tcl"), P("Year-round","tc"), P("Own cultivation / local market","tcl"), P("0.50–0.80","tc"), P("20%","tc"), P("Best year-round green; plant once, harvest 6–8x/year","tcl")],
        [P("Maize (green)","tcl"),            P("Kharif (Jul–Oct)\nRabi (Jan–Mar)","tc"), P("Local farmers","tcl"), P("1.20–1.80","tc"), P("25%","tc"), P("High energy; ideal for silage","tcl")],
        [P("Sorghum / Jowar","tcl"),           P("Kharif & Rabi","tc"), P("Local markets","tcl"), P("0.80–1.20","tc"), P("22%","tc"), P("Good palatability; drought tolerant","tcl")],
        [P("Subabul (Leucaena)","tcl"),         P("Year-round","tc"), P("Bund planting","tcl"), P("0","tc"), P("30%","tc"), P("Free protein bank; plant on farm bunds","tcl")],
        [P("Para Grass","tcl"),                P("Year-round","tc"), P("Canal banks","tcl"), P("0–0.50","tc"), P("18%","tc"), P("Available near water channels in Srikakulam","tcl")],
        [P("Sugarcane tops","tcl"),             P("Nov–Feb","tc"), P("Local sugarcane farmers","tcl"), P("0.20–0.40","tc"), P("24%","tc"), P("Seasonal; low cost, bulk filler","tcl")],
        [P("Sweet Sorghum bagasse","tcl"),      P("Nov–Jan","tc"), P("Sugar mills","tcl"), P("0.15–0.30","tc"), P("28%","tc"), P("Energy-rich by-product","tcl")],
    ]
    els.append(table(gf, [55,42,55,35,28,80*mm*(W/PAGE_W)], [], repeat_header=True))
    els.append(SP(4))

    els.append(P("1.2  Dry Fodder", "h2"))
    df = [
        [P("Ingredient","tch"), P("Availability","tch"), P("Price/kg (₹)","tch"), P("DM %","tch"), P("Crude Protein %","tch"), P("Notes","tch")],
        [P("Paddy Straw","tcl"),       P("Year-round (abundant)","tc"), P("0.50–1.00","tc"), P("88%","tc"), P("3–4%","tc"), P("Most abundant; low nutrition — treat with urea","tcl")],
        [P("Urea-treated Straw","tcl"),P("Year-round","tc"),            P("1.00–1.50","tc"), P("88%","tc"), P("8–10%","tc"), P("Treat paddy straw with 4% urea solution — triples protein","tcl")],
        [P("Maize Stover","tcl"),      P("Post-harvest","tc"),          P("0.80–1.20","tc"), P("86%","tc"), P("6%","tc"),  P("Better than paddy straw; good energy","tcl")],
        [P("Groundnut Haulms","tcl"),  P("Nov–Jan","tc"),               P("2.00–3.00","tc"), P("90%","tc"), P("12%","tc"), P("Excellent protein in dry fodder — stock up in season","tcl")],
        [P("Soybean Stover","tcl"),    P("Oct–Nov","tc"),               P("1.50–2.00","tc"), P("88%","tc"), P("8%","tc"),  P("Available from Vizianagaram belt","tcl")],
    ]
    els.append(table(df, [50,50,38,25,40,None], [], repeat_header=True))
    els.append(SP(4))

    els.append(P("1.3  Concentrate Ingredients", "h2"))
    ci = [
        [P("Ingredient","tch"), P("Price/kg (₹)","tch"), P("Crude Protein %","tch"), P("TDN %","tch"), P("Source in Srikakulam","tch")],
        [P("Cotton Seed Cake (expeller)","tcl"), P("22–26","tc"), P("38–41%","tc"), P("72%","tc"), P("Dealer: Srikakulam / Palasa road","tcl")],
        [P("Groundnut Cake (expeller)","tcl"),   P("28–34","tc"), P("45–48%","tc"), P("74%","tc"), P("Available in Amadalavalasa, Narasannapeta","tcl")],
        [P("Rice Bran (deoiled)","tcl"),          P("14–18","tc"), P("12–14%","tc"), P("68%","tc"), P("Rice mills in Narasannapeta, Srikakulam town","tcl")],
        [P("Maize Grain","tcl"),                  P("20–24","tc"), P("9%","tc"),     P("80%","tc"), P("APMC Srikakulam / Etcherla market","tcl")],
        [P("Wheat Bran","tcl"),                   P("16–20","tc"), P("14%","tc"),    P("65%","tc"), P("Available from Vizag / Srikakulam wholesalers","tcl")],
        [P("Broken Rice","tcl"),                  P("18–22","tc"), P("8%","tc"),     P("78%","tc"), P("Rice mills — abundant and cheap locally","tcl")],
        [P("Soybean Meal (solvent)","tcl"),        P("35–40","tc"), P("44–46%","tc"), P("72%","tc"), P("Vizag-based dealers supply to Srikakulam","tcl")],
        [P("Molasses","tcl"),                     P("8–12","tc"),  P("3%","tc"),     P("60%","tc"), P("Sugar factories in Chipurupalle area","tcl")],
    ]
    els.append(table(ci, [65,40,45,30,None], [], repeat_header=True))
    els.append(SP(4))

    els.append(P("1.4  Minerals & Supplements", "h2"))
    ms = [
        [P("Supplement","tch"), P("Price/kg (₹)","tch"), P("Use","tch"), P("Source","tch")],
        [P("Mineral Mixture (ISI-marked)","tcl"), P("55–75","tc"),  P("50 g/animal/day","tc"),  P("Virbac, Provimi, Godrej Agrovet — any feed dealer","tcl")],
        [P("Common Salt (Iodised)","tcl"),         P("8–12","tc"),   P("30 g/animal/day","tc"),  P("Any grocery / feed store","tcl")],
        [P("Limestone Powder","tcl"),              P("3–5","tc"),    P("50 g/animal/day","tc"),  P("Building material suppliers","tcl")],
        [P("Bypass Fat (Megalac)","tcl"),           P("120–140","tc"), P("100–150 g/day (peak)","tc"), P("Dairy dealers; Vizag","tcl")],
        [P("Sodium Bicarbonate","tcl"),            P("25–30","tc"),  P("100 g/day (summer)","tc"),P("Chemical suppliers, Vizag","tcl")],
    ]
    els.append(table(ms, [80,40,70,None], [], repeat_header=True))

    els.append(SP(3))
    els.append(P("💡  <b>Tip:</b> Join the Srikakulam District Milk Producers Co-operative Society (SDMPCS) to access subsidised concentrate feed, mineral mixture and veterinary services at reduced cost.", "note"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec2_ration():
    W = PAGE_W - L - R
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 2 — DAILY RATION DESIGN FOR MURRAH BUFFALO", "h1"))
    els.append(HR())
    els.append(P("Based on ICAR-NDRI and NDDB guidelines for high-yielding Murrah buffaloes under tropical conditions (Andhra Pradesh). Requirements vary by physiological stage.", "body"))
    els.append(SP(3))

    els.append(P("2.1  Nutrient Requirements by Stage", "h2"))
    nr = [
        [P("Stage","tch"), P("Body Wt (kg)","tch"), P("Milk (L/day)","tch"), P("Dry Matter (kg/day)","tch"), P("Crude Protein (g/day)","tch"), P("TDN (kg/day)","tch"), P("Calcium (g)","tch"), P("Phosphorus (g)","tch")],
        [P("Maintenance only","tcl"),          P("450","tc"), P("0","tc"),    P("6.0","tc"),  P("480","tc"),  P("3.2","tc"), P("18","tc"), P("14","tc")],
        [P("Early lactation (8–12 L)","tcl"),  P("450","tc"), P("10","tc"),   P("13.5","tc"), P("1,450","tc"), P("8.1","tc"), P("55","tc"), P("40","tc")],
        [P("Peak lactation (12–16 L)","tcl"),  P("450","tc"), P("14","tc"),   P("16.0","tc"), P("1,750","tc"), P("9.6","tc"), P("65","tc"), P("48","tc")],
        [P("Late lactation (6–8 L)","tcl"),    P("450","tc"), P("7","tc"),    P("11.0","tc"), P("1,100","tc"), P("6.5","tc"), P("42","tc"), P("30","tc")],
        [P("Dry period (2 months)","tcl"),     P("470","tc"), P("0","tc"),    P("8.5","tc"),  P("900","tc"),  P("5.2","tc"), P("40","tc"), P("28","tc")],
        [P("Last 30 days pregnancy","tcl"),    P("490","tc"), P("0","tc"),    P("9.5","tc"),  P("1,100","tc"), P("6.0","tc"), P("55","tc"), P("38","tc")],
    ]
    els.append(table(nr, [55,35,35,45,50,38,32,45], [], repeat_header=True))
    els.append(SP(4))

    els.append(P("2.2  Recommended Daily Balanced Ration (per animal, 14 L/day peak lactation)", "h2"))
    els.append(P("This is the standard ration recommended for your 31 lactating Murrah buffaloes producing ~14 litres/day:", "body"))

    dr = [
        [P("Feed Ingredient","tch"), P("Qty (kg/day)","tch"), P("DM (kg)","tch"), P("CP (g)","tch"), P("TDN (kg)","tch"), P("Cost/day (₹)","tch")],
        [P("Green fodder (Napier/Maize)","tcl"), P("25","tc"), P("5.0","tc"), P("425","tc"), P("3.0","tc"), P("15.00","tc")],
        [P("Urea-treated paddy straw","tcl"),    P("5","tc"),  P("4.4","tc"), P("440","tc"), P("2.5","tc"), P("6.25","tc")],
        [P("Maize grain","tcl"),                 P("2","tc"),  P("1.76","tc"),P("180","tc"), P("1.6","tc"), P("44.00","tc")],
        [P("Rice bran (deoiled)","tcl"),          P("1.5","tc"),P("1.35","tc"),P("195","tc"), P("1.0","tc"), P("24.00","tc")],
        [P("Cotton seed cake","tcl"),             P("1.5","tc"),P("1.38","tc"),P("585","tc"), P("1.1","tc"), P("36.00","tc")],
        [P("Groundnut cake","tcl"),               P("0.5","tc"),P("0.46","tc"),P("225","tc"), P("0.37","tc"),P("15.00","tc")],
        [P("Molasses","tcl"),                     P("0.3","tc"),P("0.24","tc"),P("9","tc"),   P("0.18","tc"),P("3.00","tc")],
        [P("Mineral mixture","tcl"),              P("0.05","tc"),P("0.05","tc"),P("—","tc"),  P("—","tc"),   P("3.25","tc")],
        [P("Common salt","tcl"),                  P("0.03","tc"),P("0.03","tc"),P("—","tc"),  P("—","tc"),   P("0.30","tc")],
        [P("Limestone powder","tcl"),             P("0.05","tc"),P("0.05","tc"),P("—","tc"),  P("—","tc"),   P("0.25","tc")],
        [P("<b>TOTAL</b>","tcb"),                 P("<b>35.93</b>","tc"),P("<b>14.72</b>","tc"),P("<b>2,059</b>","tc"),P("<b>9.75</b>","tc"),P("<b>₹147.05</b>","tc")],
    ]
    t = table(dr, [80,38,32,32,32,None], [
        ("BACKGROUND",(0,11),(-1,11),LGREEN),
        ("FONTNAME",(0,11),(-1,11),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,11),(-1,11),DGREEN),
    ], repeat_header=True)
    els.append(t)

    els.append(SP(3))
    els.append(P("✅  This ration meets ICAR requirements: DM=14.7 kg (target 14–16 kg), CP=2,059 g (target 1,750 g), TDN=9.75 kg (target 9.6 kg), Ca:P ratio = 1.4:1 (ideal). Daily cost per peak-lactating animal = <b>₹147</b>.", "note"))
    els.append(SP(2))
    els.append(P("For <b>dry animals</b> (9 animals): reduce concentrate to 2 kg/day total, maintain green + straw — cost drops to ~₹80/day.", "body"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec3_models():
    W = PAGE_W - L - R
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 3 — THREE FEEDING MODELS COMPARED", "h1"))
    els.append(HR())
    els.append(P("Three practical models suited to Srikakulam conditions. All are designed for 40 Murrah buffaloes (31 lactating, 9 dry/pregnant).", "body"))
    els.append(SP(3))

    # MODEL A
    els.append(P("MODEL A — Fully Purchased Feed (Zero Own Cultivation)", "h2"))
    els.append(P("All green fodder, dry fodder and concentrate purchased from market. Suitable if land is not available.", "body"))
    ma = [
        [P("Item","tch"), P("Qty/day (40 animals)","tch"), P("Unit Price (₹)","tch"), P("Daily Cost (₹)","tch"), P("Monthly Cost (₹)","tch")],
        [P("Green fodder (purchased Napier)","tcl"), P("600 kg","tc"), P("0.70/kg","tc"), P("420","tc"), P("12,600","tc")],
        [P("Paddy straw (purchased)","tcl"),         P("120 kg","tc"), P("1.00/kg","tc"), P("120","tc"), P("3,600","tc")],
        [P("Maize grain","tcl"),                     P("56 kg","tc"),  P("22/kg","tc"),   P("1,232","tc"),P("36,960","tc")],
        [P("Rice bran","tcl"),                        P("42 kg","tc"),  P("16/kg","tc"),   P("672","tc"),  P("20,160","tc")],
        [P("Cotton seed cake","tcl"),                 P("42 kg","tc"),  P("24/kg","tc"),   P("1,008","tc"),P("30,240","tc")],
        [P("Groundnut cake","tcl"),                   P("14 kg","tc"),  P("30/kg","tc"),   P("420","tc"),  P("12,600","tc")],
        [P("Molasses","tcl"),                         P("8 kg","tc"),   P("10/kg","tc"),   P("80","tc"),   P("2,400","tc")],
        [P("Minerals + salt","tcl"),                  P("3.2 kg","tc"), P("60/kg","tc"),   P("192","tc"),  P("5,760","tc")],
        [P("<b>MODEL A TOTAL</b>","tcb"), P("","tc"), P("","tc"), P("<b>₹4,144</b>","tc"), P("<b>₹1,24,320</b>","tc")],
    ]
    t = table(ma, [90,55,45,40,None],[
        ("BACKGROUND",(0,9),(-1,9),LRED),
        ("FONTNAME",(0,9),(-1,9),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,9),(-1,9),RED),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("⚠️  <b>Cost: ₹1,24,320/month.</b> Highest cost model. Risk: green fodder availability varies seasonally. Not recommended as primary model.", "note"))
    els.append(SP(5))

    # MODEL B
    els.append(P("MODEL B — Mixed Model (Own Napier + Purchased Concentrate)  ★ RECOMMENDED", "h2"))
    els.append(P("Cultivate 2.5 acres of Napier/Maize for green fodder. Purchase all concentrate. This is the optimal balance for a 40-buffalo farm in Srikakulam.", "body"))
    mb = [
        [P("Item","tch"), P("Qty/day","tch"), P("Cost basis","tch"), P("Daily Cost (₹)","tch"), P("Monthly Cost (₹)","tch")],
        [P("Own Napier grass (2.5 ac)","tcl"),     P("500 kg/day","tc"), P("Own cultivation","tc"), P("125","tc"),  P("3,750","tc")],
        [P("Supplemental green (purchased)","tcl"),P("100 kg/day","tc"), P("₹0.70/kg","tc"),         P("70","tc"),   P("2,100","tc")],
        [P("Urea-treated paddy straw (own)","tcl"),P("120 kg/day","tc"), P("₹0.40/kg processed","tc"),P("48","tc"),  P("1,440","tc")],
        [P("Maize grain","tcl"),                   P("56 kg/day","tc"),  P("₹22/kg","tc"),           P("1,232","tc"),P("36,960","tc")],
        [P("Rice bran","tcl"),                      P("42 kg/day","tc"),  P("₹16/kg","tc"),           P("672","tc"),  P("20,160","tc")],
        [P("Cotton seed cake","tcl"),               P("42 kg/day","tc"),  P("₹24/kg","tc"),           P("1,008","tc"),P("30,240","tc")],
        [P("Groundnut cake","tcl"),                 P("14 kg/day","tc"),  P("₹30/kg","tc"),           P("420","tc"),  P("12,600","tc")],
        [P("Minerals + salt","tcl"),                P("3.2 kg/day","tc"), P("₹60/kg","tc"),           P("192","tc"),  P("5,760","tc")],
        [P("<b>MODEL B TOTAL</b>","tcb"), P("","tc"),P("","tc"), P("<b>₹3,767</b>","tc"), P("<b>₹1,13,010</b>","tc")],
    ]
    t = table(mb, [90,50,55,40,None],[
        ("BACKGROUND",(0,9),(-1,9),LGREEN),
        ("FONTNAME",(0,9),(-1,9),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,9),(-1,9),DGREEN),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("✅  <b>Cost: ₹1,13,010/month.</b> Saves ₹11,310/month vs Model A. 2.5 acres of Napier requires one-time planting cost of ~₹35,000 but lasts 5+ years with minimal maintenance.", "note"))
    els.append(SP(5))

    # MODEL C
    els.append(P("MODEL C — Silage-Based Model (Maize Silage + Concentrate)", "h2"))
    els.append(P("Make maize or sorghum silage during Kharif season (Aug–Oct) for year-round assured green fodder supply. Requires 3 acres and silage pit infrastructure.", "body"))
    mc = [
        [P("Item","tch"), P("Details","tch"), P("Annual Cost (₹)","tch"), P("Monthly equiv. (₹)","tch")],
        [P("Maize silage making (3 ac)","tcl"), P("60 T silage/yr — 550 kg/day","tc"), P("75,000","tc"), P("6,250","tc")],
        [P("Silage pit (one-time, 5yr life)","tcl"), P("60T pit — concrete","tc"), P("80,000 ÷ 5 = 16,000/yr","tc"), P("1,333","tc")],
        [P("Concentrate (reduced 20%)","tcl"),  P("Lower qty vs B — silage covers energy","tc"), P("10,10,000","tc"), P("84,167","tc")],
        [P("Minerals + salt","tcl"),             P("Same as Model B","tc"), P("69,120","tc"), P("5,760","tc")],
        [P("<b>MODEL C TOTAL</b>","tcb"),         P("","tc"), P("<b>₹11,70,120</b>","tc"), P("<b>₹97,510</b>","tc")],
    ]
    t = table(mc, [80,100,55,None],[
        ("BACKGROUND",(0,4),(-1,4),LBLUE),
        ("FONTNAME",(0,4),(-1,4),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,4),(-1,4),BLUE),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("ℹ️  <b>Cost: ₹97,510/month.</b> Lowest operating cost but requires upfront silage pit investment (~₹80,000). Best for farms with 3+ acres land. Seasonal maize availability in Srikakulam makes this very practical.", "note"))

    els.append(SP(5))
    els.append(P("3.4  Model Comparison Summary", "h2"))
    comp = [
        [P("Model","tch"), P("Monthly Feed Cost","tch"), P("Land Needed","tch"), P("Upfront Investment","tch"), P("Seasonal Risk","tch"), P("Verdict","tch")],
        [P("A — Full Purchase","tcl"),      P("₹1,24,320","tc"), P("None","tc"),   P("Nil","tc"),      P("High","tc"),   P("Not recommended","tc")],
        [P("B — Own Napier + Purchase","tcl"),P("₹1,13,010","tc"),P("2.5 acres","tc"),P("₹35,000","tc"),P("Low","tc"),   P("★ Best for your farm","tc")],
        [P("C — Silage-based","tcl"),        P("₹97,510","tc"),  P("3 acres","tc"), P("₹80,000","tc"),  P("Very Low","tc"),P("Best if 3 ac available","tc")],
    ]
    t = table(comp, [65,50,38,48,38,None],[
        ("BACKGROUND",(0,1),(-1,1),LRED),
        ("BACKGROUND",(0,2),(-1,2),LGREEN),
        ("BACKGROUND",(0,3),(-1,3),LBLUE),
        ("FONTNAME",(0,2),(-1,2),"Helvetica-Bold"),
    ], repeat_header=True)
    els.append(t)
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec4_monthly_cost():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 4 — MONTHLY FEED COST BREAKDOWN (40 BUFFALOES, MODEL B)", "h1"))
    els.append(HR())
    els.append(P("Detailed monthly feed cost for Model B — the recommended model. Costs vary slightly by season due to green fodder availability.", "body"))
    els.append(SP(3))

    mc = [
        [P("Month","tch"), P("Season","tch"), P("Green Fodder Cost","tch"), P("Dry Fodder Cost","tch"), P("Concentrate Cost","tch"), P("Minerals","tch"), P("Total/Month (₹)","tch")],
        [P("January","tcl"),  P("Winter/Rabi","tc"),   P("4,500","tc"),  P("2,500","tc"), P("1,00,000","tc"), P("5,760","tc"), P("1,12,760","tc")],
        [P("February","tcl"), P("Winter/Rabi","tc"),   P("4,500","tc"),  P("2,500","tc"), P("1,00,000","tc"), P("5,760","tc"), P("1,12,760","tc")],
        [P("March","tcl"),    P("Summer","tc"),         P("5,500","tc"),  P("3,000","tc"), P("1,05,000","tc"), P("5,760","tc"), P("1,19,260","tc")],
        [P("April","tcl"),    P("Summer (hot)","tc"),   P("6,500","tc"),  P("3,500","tc"), P("1,05,000","tc"), P("6,500","tc"), P("1,21,500","tc")],
        [P("May","tcl"),      P("Summer (peak)","tc"),  P("7,000","tc"),  P("4,000","tc"), P("1,10,000","tc"), P("6,500","tc"), P("1,27,500","tc")],
        [P("June","tcl"),     P("Pre-monsoon","tc"),    P("5,500","tc"),  P("3,000","tc"), P("1,05,000","tc"), P("5,760","tc"), P("1,19,260","tc")],
        [P("July","tcl"),     P("Monsoon","tc"),         P("3,750","tc"),  P("1,440","tc"), P("1,00,960","tc"), P("5,760","tc"), P("1,11,910","tc")],
        [P("August","tcl"),   P("Monsoon","tc"),         P("3,750","tc"),  P("1,440","tc"), P("1,00,960","tc"), P("5,760","tc"), P("1,11,910","tc")],
        [P("September","tcl"),P("Monsoon/Kharif","tc"), P("3,750","tc"),  P("1,440","tc"), P("1,00,960","tc"), P("5,760","tc"), P("1,11,910","tc")],
        [P("October","tcl"),  P("Post-monsoon","tc"),   P("3,750","tc"),  P("1,440","tc"), P("1,00,960","tc"), P("5,760","tc"), P("1,11,910","tc")],
        [P("November","tcl"), P("Cool/Rabi","tc"),      P("4,000","tc"),  P("1,800","tc"), P("1,00,960","tc"), P("5,760","tc"), P("1,12,520","tc")],
        [P("December","tcl"), P("Winter","tc"),          P("4,500","tc"),  P("2,500","tc"), P("1,00,000","tc"), P("5,760","tc"), P("1,12,760","tc")],
        [P("<b>ANNUAL TOTAL</b>","tcb"), P("","tc"), P("<b>₹57,000</b>","tc"), P("<b>₹28,560</b>","tc"), P("<b>₹12,29,800</b>","tc"), P("<b>₹70,080</b>","tc"), P("<b>₹13,56,960</b>","tc")],
    ]
    t = table(mc, [35,45,45,40,50,35,None],[
        ("BACKGROUND",(0,13),(-1,13),LGREEN),
        ("FONTNAME",(0,13),(-1,13),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,13),(-1,13),DGREEN),
        ("BACKGROUND",(0,4),(-1,5),colors.HexColor("#fff3f3")),
        ("BACKGROUND",(0,3),(-1,3),colors.HexColor("#fff3f3")),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(3))
    els.append(P("📊  <b>Annual feed cost (Model B): ₹13,56,960 (~₹13.57 lakhs)</b>  ·  Average ₹1,13,080/month  ·  Cost per litre of milk (at 426 L/day): ≈ <b>₹8.74/litre</b>", "note"))
    els.append(SP(2))
    els.append(P("<b>Summer months (March–May)</b> cost is higher due to: purchased green fodder, extra sodium bicarbonate for heat stress, and electrolyte supplements. Plan extra budget of ₹5,000–8,000/month for May.", "body"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec5_vaccination():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 5 — SEASONAL MEDICATION & VACCINATION CALENDAR", "h1"))
    els.append(HR())
    els.append(P("Schedule based on Andhra Pradesh Animal Husbandry Department guidelines and ICAR recommendations for coastal AP conditions. Some vaccines are provided FREE by the AP government through the local VAS (Veterinary Assistant Surgeon).", "body"))
    els.append(SP(3))

    els.append(P("5.1  Annual Vaccination Calendar", "h2"))
    vc = [
        [P("Month","tch"), P("Disease","tch"), P("Vaccine / Drug","tch"), P("Dose","tch"), P("Route","tch"), P("Cost/animal (₹)","tch"), P("Provided By","tch")],
        [P("January","tcl"),   P("Brucellosis (calves 4–8 mo)","tcl"), P("Brucella abortus S19","tcl"),  P("2 mL","tc"), P("SC","tc"), P("Free","tc"),     P("Govt. VAS","tc")],
        [P("February","tcl"),  P("FMD","tcl"),                          P("FMD Polyvalent (O, A, Asia-1)","tcl"), P("2 mL","tc"), P("SC","tc"), P("Free","tc"), P("Govt. — biannual drive","tc")],
        [P("February","tcl"),  P("Deworming","tcl"),                    P("Albendazole 10% bolus","tcl"), P("1 bolus (750 mg)","tc"), P("Oral","tc"), P("15–20","tc"), P("Purchase","tc")],
        [P("April","tcl"),     P("Theileriosis (calves)","tcl"),        P("Theileria annulata schizonts","tcl"), P("1 mL","tc"), P("SC","tc"), P("80–100","tc"),P("SVBP / Pvt. vet","tc")],
        [P("May","tcl"),       P("Heat stress support","tcl"),          P("Electral / ORS + Vit E+Se","tcl"), P("As per wt","tc"), P("Oral","tc"), P("20–30","tc"), P("Purchase","tc")],
        [P("June","tcl"),      P("HS (Haemorrhagic Septicaemia)","tcl"),P("HS Alum precipitate vaccine","tcl"), P("2 mL","tc"), P("SC","tc"), P("Free","tc"),  P("Govt. — pre-monsoon","tc")],
        [P("June","tcl"),      P("BQ (Black Quarter)","tcl"),           P("BQ vaccine (spore suspension)","tcl"),P("5 mL","tc"), P("SC","tc"), P("Free","tc"), P("Govt. — pre-monsoon","tc")],
        [P("August","tcl"),    P("Deworming","tcl"),                    P("Albendazole or Fenbendazole","tcl"), P("750 mg","tc"), P("Oral","tc"), P("15–20","tc"), P("Purchase","tc")],
        [P("August","tcl"),    P("FMD (second dose)","tcl"),            P("FMD Polyvalent","tcl"),           P("2 mL","tc"), P("SC","tc"), P("Free","tc"),  P("Govt. — biannual drive","tc")],
        [P("September","tcl"), P("Anti-tick treatment","tcl"),          P("Deltamethrin / Cypermethrin spray","tcl"),P("Dilute & spray","tc"),P("Topical","tc"),P("10–15","tc"),P("Purchase","tc")],
        [P("October","tcl"),   P("Liver tonic + Vit B-complex","tcl"), P("Livol / Liv-52 Vet oral","tcl"),  P("50 mL/day × 10d","tc"),P("Oral","tc"), P("30–40","tc"), P("Purchase","tc")],
        [P("November","tcl"),  P("Deworming","tcl"),                    P("Ivermectin pour-on or bolus","tcl"), P("200 mcg/kg","tc"), P("Pour-on","tc"),P("40–50","tc"),P("Purchase","tc")],
        [P("December","tcl"),  P("Vitamin AD3E injection","tcl"),       P("AD3E inj (Vit Forte)","tcl"),    P("5 mL","tc"), P("IM","tc"), P("20–25","tc"), P("Purchase","tc")],
    ]
    t = table(vc, [28,68,68,38,22,38,None],[
        ("BACKGROUND",(0,2),(-1,2),LGOLD),
        ("BACKGROUND",(0,8),(-1,8),LGOLD),
        ("BACKGROUND",(0,11),(-1,11),LGOLD),
        ("BACKGROUND",(0,6),(-1,6),LRED),
        ("BACKGROUND",(0,7),(-1,7),LRED),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("SC = Subcutaneous  ·  IM = Intramuscular  ·  Govt. VAS = free through AP Animal Husbandry veterinarian visiting your village", "small"))
    els.append(SP(4))

    els.append(P("5.2  Seasonal Health Interventions", "h2"))
    si = [
        [P("Season","tch"), P("Challenge","tch"), P("Intervention","tch"), P("Product","tch"), P("Cost/month (40 animals)","tch")],
        [P("Summer\n(Mar–Jun)","tcl"),
         P("Heat stress, reduced milk, reduced appetite","tcl"),
         P("• Sodium bicarbonate 100g/day\n• Electrolytes in water\n• Vit E + Selenium injection (Apr)\n• Increase water to 80L/animal/day\n• Shade + fans mandatory","tcl"),
         P("Electral, Bicarb, Vit E-Se","tcl"),
         P("₹3,500–4,500","tc")],
        [P("Monsoon\n(Jul–Sep)","tcl"),
         P("Foot rot, FMD, HS, mastitis, worm load spike","tcl"),
         P("• Pre-monsoon HS + BQ vaccination (June)\n• Deworming in August\n• Foot bath (5% formalin) twice/week\n• Tick control spray monthly\n• Watch for laminitis in wet conditions","tcl"),
         P("Formalin, Cypermethrin, Albendazole","tcl"),
         P("₹2,000–3,000","tc")],
        [P("Winter\n(Oct–Feb)","tcl"),
         P("Reduced water intake, milk fever risk, mastitis in fresh cows","tcl"),
         P("• Liver tonic course (October)\n• Vit AD3E injection (December)\n• Calcium bolus for fresh animals\n• Deworming (November)\n• Watch for cold stress in calves","tcl"),
         P("Livol, Vit Forte, Calcigel bolus","tcl"),
         P("₹1,500–2,500","tc")],
    ]
    t = table(si, [35,65,100,55,None],[
        ("BACKGROUND",(0,1),(-1,1),LGOLD),
        ("BACKGROUND",(0,2),(-1,2),LBLUE),
        ("BACKGROUND",(0,3),(-1,3),LGREEN),
        ("VALIGN",(0,0),(-1,-1),"TOP"),
        ("FONTSIZE",(0,1),(-1,-1),8),
    ], repeat_header=True)
    els.append(t)
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec6_minerals():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 6 — MINERALS, VITAMINS & SUPPLEMENTS PROTOCOL", "h1"))
    els.append(HR())
    els.append(P("Srikakulam district has coastal alluvial soils that are commonly deficient in selenium, zinc, copper and iodine. Murrah buffaloes are especially sensitive to selenium and zinc deficiency (impacts milk fat, reproduction and hoof health).", "body"))
    els.append(SP(3))

    els.append(P("6.1  Daily Mineral Supplementation", "h2"))
    dm = [
        [P("Mineral/Supplement","tch"), P("Daily Dose","tch"), P("Function","tch"), P("Deficiency Signs","tch"), P("Annual Cost/animal (₹)","tch")],
        [P("Mineral mixture (ISI-marked)","tcl"), P("50 g/day","tc"), P("Multi-mineral: Ca, P, Mg, Zn, Cu, Mn, Se, I, Co","tcl"), P("Reduced milk, poor reproduction, weak bones","tcl"), P("900–1,100","tc")],
        [P("Common salt (iodised)","tcl"),         P("30 g/day","tc"), P("Electrolyte balance, iodine supplementation","tcl"), P("Reduced appetite, goitre, poor calving","tcl"), P("90–120","tc")],
        [P("Limestone powder","tcl"),              P("50 g/day","tc"), P("Calcium supplementation (low-cost)","tcl"), P("Milk fever, fragile bones, poor milk yield","tcl"), P("55–70","tc")],
        [P("Bypass fat (Megalac)","tcl"),           P("100 g/day (lactating only)","tc"), P("Energy density, early lactation support","tcl"), P("Excessive weight loss, ketosis","tcl"), P("1,800–2,000","tc")],
        [P("Sodium bicarbonate","tcl"),            P("100 g/day (summer only)","tc"), P("Rumen buffering, heat stress relief","tcl"), P("Acidosis, off-feed in summer","tcl"), P("100–130","tc")],
    ]
    t = table(dm, [70,60,90,80,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("6.2  Targeted Injectable Supplements (Twice Per Year)", "h2"))
    inj = [
        [P("Injection","tch"), P("When","tch"), P("Dose","tch"), P("Why Important for Srikakulam","tch"), P("Cost/dose (₹)","tch")],
        [P("Selenium + Vit E (Selevit / E-Se)","tcl"), P("April & October","tc"), P("5 mL IM","tc"), P("Coastal soils are Se-deficient; prevents white muscle disease, improves conception rate","tcl"), P("40–60","tc")],
        [P("Vitamin AD3E (Vit Forte)","tcl"),           P("December & June","tc"), P("5 mL IM","tc"), P("Supports immunity, fat-soluble vitamin replenishment post-monsoon","tcl"), P("20–30","tc")],
        [P("Vitamin B-complex","tcl"),                  P("After deworming (Aug, Nov)","tc"), P("10 mL IM","tc"), P("Supports recovery, appetite stimulation post-anthelminthic","tcl"), P("15–20","tc")],
        [P("Calcium borogluconate (IV)","tcl"),         P("At calving (as needed)","tc"), P("400 mL IV slow","tc"), P("Milk fever prevention — critical in high-yielding buffaloes","tcl"), P("80–120","tc")],
    ]
    t = table(inj, [80,50,40,120,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("6.3  Annual Medicine & Supplement Cost Per Animal", "h2"))
    ac = [
        [P("Category","tch"), P("Annual Cost/Animal (₹)","tch"), P("Cost for 40 Animals (₹)","tch")],
        [P("Mineral mixture (daily)","tcl"),   P("1,000","tc"),  P("40,000","tc")],
        [P("Salt + Limestone (daily)","tcl"),  P("190","tc"),    P("7,600","tc")],
        [P("Bypass fat (lactating)","tcl"),    P("1,900","tc"),  P("58,900","tc")],
        [P("Vaccines (purchased)","tcl"),      P("220","tc"),    P("8,800","tc")],
        [P("Deworming (3x/year)","tcl"),       P("150","tc"),    P("6,000","tc")],
        [P("Injectable supplements","tcl"),    P("250","tc"),    P("10,000","tc")],
        [P("Tick/ectoparasite control","tcl"), P("120","tc"),    P("4,800","tc")],
        [P("Summer electrolytes + bicarb","tcl"),P("350","tc"),  P("14,000","tc")],
        [P("Emergency/sick animal medicines","tcl"),P("500","tc"),P("20,000","tc")],
        [P("<b>TOTAL ANNUAL</b>","tcb"),        P("<b>₹4,680</b>","tc"), P("<b>₹1,70,100</b>","tc")],
    ]
    t = table(ac, [120,80,None],[
        ("BACKGROUND",(0,10),(-1,10),LGREEN),
        ("FONTNAME",(0,10),(-1,10),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,10),(-1,10),DGREEN),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("📌  Many vaccines (FMD, HS, BQ) are provided FREE by AP Animal Husbandry Dept. Register with your local VAS office in Pydi Bhimavaram to get these services. Also eligible under AP NTR Pashu Seva scheme.", "note"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec7_health_tracking():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 7 — ANIMAL HEALTH TRACKING SYSTEM", "h1"))
    els.append(HR())
    els.append(P("A systematic daily health observation and recording system reduces disease losses by 30–40%. Use the DairyFlow app (already set up on your farm) or this paper-based protocol.", "body"))
    els.append(SP(3))

    els.append(P("7.1  Daily Observation Checklist (Morning Shift)", "h2"))
    chk = [
        [P("Parameter","tch"), P("Normal Range","tch"), P("Action if Abnormal","tch")],
        [P("Body temperature","tcl"),      P("101–103°F (38.3–39.4°C)","tc"),   P(">104°F: isolate, call vet; <100°F: check for ketosis/milk fever","tcl")],
        [P("Rumination","tcl"),            P("6–8 hours/day, ~60 chews/bolus","tc"), P("No rumination for >12h: off-feed, rumen issue — consult vet","tcl")],
        [P("Milk yield","tcl"),            P("Within 10% of previous day","tc"), P(">15% drop: check for mastitis, fever, stress","tcl")],
        [P("Milk appearance","tcl"),       P("Uniform, white, no clots","tc"),   P("Clots/watery/blood: CMT test immediately for mastitis","tcl")],
        [P("Appetite","tcl"),              P("Finishes feed in 30 min","tc"),    P("Leaving feed: fever, acidosis, off-feed — investigate","tcl")],
        [P("Dung consistency","tcl"),      P("Firm cowpat shape","tc"),          P("Watery: dietary change / infection; Hard: dehydration / blockage","tcl")],
        [P("Udder condition","tcl"),       P("Soft, no heat/swelling","tc"),     P("Hard quarter / heat: mastitis — CMT, antibiotic as per vet","tcl")],
        [P("Lameness / gait","tcl"),       P("Normal walking","tc"),             P("Limp: foot rot, laminitis — clean foot, trim, treat","tcl")],
        [P("Nasal / eye discharge","tcl"), P("None or minimal clear","tc"),      P("Thick discharge: respiratory infection — isolate, treat","tcl")],
        [P("Reproductive signs","tcl"),    P("Record heat (restlessness, mucus)","tc"), P("AI within 12–18h of standing heat for best conception","tcl")],
    ]
    t = table(chk, [65,75,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("7.2  Reproductive Events Tracking", "h2"))
    re = [
        [P("Event","tch"), P("What to Record","tch"), P("Target / Ideal","tch")],
        [P("Heat detection","tcl"),          P("Date, time, duration, intensity (standing heat)","tcl"), P("Every 21 days (19–23 day cycle)","tc")],
        [P("Artificial Insemination (AI)","tcl"), P("Date, bull semen (tag no.), technician name, AI no. (1st/2nd/3rd)","tcl"), P("12–18h after standing heat observed","tc")],
        [P("Pregnancy diagnosis","tcl"),     P("Date, result (+ / –), method (rectal/ultrasound)","tcl"), P("60–90 days after AI","tc")],
        [P("Expected calving date","tcl"),   P("AI date + 305 days = expected calving","tcl"), P("Gestation: 300–320 days","tc")],
        [P("Calving record","tcl"),          P("Date, calf sex/weight, ease of calving, colostrum given","tcl"), P("Calving interval target: 13–14 months","tc")],
        [P("Dry-off","tcl"),                 P("Date, milk yield at dry-off, dry cow therapy given","tcl"), P("60 days before expected calving","tc")],
        [P("Lactation record","tcl"),        P("Total milk per lactation, peak yield, lactation days","tcl"), P("Target: >2,000 L per lactation","tc")],
    ]
    t = table(re, [65,120,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("7.3  Common Diseases in Coastal AP Buffaloes — Quick Reference", "h2"))
    cd = [
        [P("Disease","tch"), P("Season","tch"), P("Signs","tch"), P("Treatment","tch"), P("Prevention","tch")],
        [P("Mastitis","tcl"),          P("Year-round\n(peak summer)","tc"), P("Reduced milk, swollen quarter, clots","tcl"), P("Intramammary AB (Amoxyclav); systemic if severe","tcl"), P("Post-milking teat dip (0.5% iodine), dry cow therapy","tcl")],
        [P("FMD","tcl"),               P("Monsoon\n(Jul–Sep)","tc"),       P("Blisters on feet/mouth, high fever, milk drop","tcl"), P("Supportive; wash blisters with KMnO4 solution","tcl"), P("6-monthly FMD vaccine (Govt. free)","tcl")],
        [P("Haemorrhagic Septicaemia","tcl"),P("Pre-monsoon","tc"),        P("High fever (107°F), swollen neck, sudden death","tcl"), P("Oxytetracycline IV immediately; Sulphonamides","tcl"), P("Annual HS vaccine June (Govt. free)","tcl")],
        [P("Milk Fever","tcl"),         P("Post-calving","tc"),            P("Unable to stand, cold extremities, low temp","tcl"), P("Calcium borogluconate 400 mL IV slow — URGENT","tcl"), P("Calcium bolus 48h + 12h before calving","tcl")],
        [P("Ketosis","tcl"),            P("Early lactation","tc"),         P("Off-feed, sweet breath, weight loss","tcl"), P("Glucose IV, propylene glycol oral, Vit B12","tcl"), P("Body condition score (BCS) 3.0–3.5 at calving","tcl")],
        [P("Foot Rot","tcl"),           P("Monsoon","tc"),                 P("Foul smell, swollen foot, lameness","tcl"), P("Oxytetracycline IM; foot bath 5% formalin","tcl"), P("Dry standing areas, zinc supplementation","tcl")],
        [P("Theileriosis","tcl"),       P("Summer\n(vector ticks)","tc"),  P("High fever, anaemia, swollen lymph nodes","tcl"), P("Buparvaquone 2.5 mg/kg IM — URGENT","tcl"), P("Theileria vaccine calves; tick control","tcl")],
    ]
    t = table(cd, [45,32,75,80,None],[("VALIGN",(0,0),(-1,-1),"TOP"),("FONTSIZE",(0,1),(-1,-1),7.5)], repeat_header=True)
    els.append(t)
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec8_water():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 8 — WATER QUALITY TESTING", "h1"))
    els.append(HR())
    els.append(P("Water quality directly impacts buffalo health, milk yield and milk quality. Srikakulam coastal areas can have issues with fluoride, salinity and nitrate contamination in bore wells.", "body"))
    els.append(SP(3))

    els.append(P("8.1  Water Consumption per Buffalo", "h2"))
    wc = [
        [P("Stage","tch"), P("Normal (L/day)","tch"), P("Summer (L/day)","tch"), P("For 40 Animals","tch")],
        [P("Lactating (peak)","tcl"),  P("60–80","tc"), P("90–100","tc"), P("2,400–3,200 L/day","tc")],
        [P("Dry / Pregnant","tcl"),    P("40–50","tc"), P("60–70","tc"),  P("360–450 L/day","tc")],
        [P("Total farm (40 animals)","tcl"), P("~2,500 L","tc"), P("~3,500 L","tc"), P("Need 3,500 L/day peak capacity","tc")],
    ]
    t = table(wc, [70,55,55,None],[("FONTNAME",(0,3),(-1,3),"Helvetica-Bold"),("BACKGROUND",(0,3),(-1,3),LGREEN)], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("8.2  Water Quality Parameters to Test", "h2"))
    wp = [
        [P("Parameter","tch"), P("Acceptable Limit (BIS/BIS-11905)","tch"), P("Buffalo-Specific Limit","tch"), P("Risk if Exceeded","tch")],
        [P("pH","tcl"),                       P("6.5–8.5","tc"),     P("6.5–8.5","tc"),    P("Acidic: rumen upset; Alkaline: mineral binding","tcl")],
        [P("Total Dissolved Solids (TDS)","tcl"),P("<500 mg/L (ideal)","tc"),P("<1,000 mg/L","tc"),P(">3,000 mg/L: reduced intake, diarrhoea","tcl")],
        [P("Hardness (as CaCO3)","tcl"),       P("<300 mg/L","tc"),   P("<500 mg/L","tc"),  P("Excess Ca/Mg: urinary stones","tcl")],
        [P("Nitrates (NO3)","tcl"),             P("<45 mg/L","tc"),    P("<100 mg/L","tc"),  P(">200 mg/L: abortion, methemoglobinemia","tcl")],
        [P("Fluoride (F)","tcl"),               P("<1 mg/L","tc"),     P("<2 mg/L","tc"),    P(">4 mg/L: dental/bone fluorosis — common in Srikakulam bore wells","tcl")],
        [P("Chloride (Cl)","tcl"),              P("<250 mg/L","tc"),   P("<600 mg/L","tc"),  P(">1,000 mg/L: diarrhoea, reduced intake","tcl")],
        [P("Sulphates (SO4)","tcl"),            P("<200 mg/L","tc"),   P("<500 mg/L","tc"),  P("Laxative effect; reduced copper absorption","tcl")],
        [P("Total Coliform (E.coli)","tcl"),    P("Absent","tc"),      P("Absent","tc"),     P("Mastitis, diarrhoea, abortion","tcl")],
        [P("Iron (Fe)","tcl"),                  P("<0.3 mg/L","tc"),   P("<1 mg/L","tc"),    P("Staining; affects palatability","tcl")],
        [P("Arsenic (As)","tcl"),               P("<0.01 mg/L","tc"),  P("<0.05 mg/L","tc"), P("Chronic toxicity, poor production","tcl")],
    ]
    t = table(wp, [70,55,48,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("8.3  Where to Test Water Near Srikakulam", "h2"))
    wt = [
        [P("Lab / Agency","tch"), P("Location","tch"), P("Tests Offered","tch"), P("Approx. Cost","tch")],
        [P("APSPCB (AP Pollution Control Board)","tcl"), P("Srikakulam District Office","tcl"), P("Full chemical + bacteriological","tc"), P("₹500–1,200","tc")],
        [P("AP Water Technology Centre (APTC)","tcl"), P("Vizag (Hyderabad HQ)","tcl"), P("Complete water analysis","tc"), P("₹800–1,500","tc")],
        [P("ANGRAU Soil Testing Lab","tcl"),            P("Anakapalli / Vizianagaram","tcl"), P("Chemical parameters","tc"), P("₹400–800","tc")],
        [P("Primary Health Centre (PHC) Lab","tcl"),    P("Pydi Bhimavaram / Sompeta","tcl"), P("Bacteriological (coliform only)","tc"), P("₹100–200","tc")],
        [P("Private labs (Thyrocare / SRL)","tcl"),     P("Vizag collection centres","tcl"), P("Chemical + bacteriological","tc"), P("₹1,000–2,000","tc")],
    ]
    t = table(wt, [85,65,70,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("📅  <b>Recommended frequency:</b> Test drinking water <b>once per year</b> (preferably post-monsoon, October–November) and after any new bore well is drilled. If fluoride or TDS comes high — install a RO system for animal drinking water.", "note"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec9_soil():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 9 — SOIL TESTING FOR FODDER CULTIVATION", "h1"))
    els.append(HR())
    els.append(P("Srikakulam district has predominantly coastal alluvial soils (sandy loam to clay loam). These soils are generally fertile but can be acidic in upland areas and have micronutrient deficiencies common to the coastal belt.", "body"))
    els.append(SP(3))

    els.append(P("9.1  Soil Tests Required Before Fodder Cultivation", "h2"))
    st = [
        [P("Test","tch"), P("Parameter","tch"), P("Optimal Range (for fodder)","tch"), P("Why It Matters","tch")],
        [P("Soil pH","tcl"),           P("Acidity / Alkalinity","tcl"),       P("6.0–7.5","tc"),         P("Determines nutrient availability; below 5.5 — apply lime","tcl")],
        [P("Organic Carbon (OC)","tcl"),P("Soil organic matter %","tcl"),     P(">0.75%","tc"),           P("Indicates fertility; low OC → apply FYM/compost","tcl")],
        [P("Available Nitrogen (N)","tcl"),P("kg N per hectare","tcl"),       P(">280 kg/ha (medium)","tc"),P("Critical for Napier / maize growth","tcl")],
        [P("Available Phosphorus (P)","tcl"),P("kg P2O5 per hectare","tcl"), P(">25 kg/ha","tc"),        P("Root development; common deficiency in coastal AP","tcl")],
        [P("Available Potassium (K)","tcl"),P("kg K2O per hectare","tcl"),   P(">280 kg/ha","tc"),       P("Disease resistance; coastal soils often K-deficient","tcl")],
        [P("Sulphur (S)","tcl"),        P("Available sulphur ppm","tcl"),     P(">10 ppm","tc"),          P("Protein synthesis in fodder crops","tcl")],
        [P("Zinc (Zn)","tcl"),          P("Available Zn ppm","tcl"),          P(">0.6 ppm","tc"),         P("Very common deficiency in AP — causes poor germination","tcl")],
        [P("Boron (B)","tcl"),          P("Available B ppm","tcl"),           P(">0.5 ppm","tc"),         P("Cell wall formation; deficiency in light soils","tcl")],
        [P("Soil EC","tcl"),            P("Electrical Conductivity (dS/m)","tcl"),P("<1.0 dS/m","tc"),   P("Salinity check — coastal farms may have saline seepage","tcl")],
        [P("Soil texture","tcl"),       P("Sand/Silt/Clay %","tcl"),          P("Sandy loam ideal","tc"), P("Drainage planning; Napier needs well-drained soil","tcl")],
    ]
    t = table(st, [45,65,55,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("9.2  Fertiliser Recommendations for Napier Grass (per acre)", "h2"))
    fr = [
        [P("Application","tch"), P("Fertiliser","tch"), P("Qty/acre","tch"), P("When","tch"), P("Cost/acre (₹)","tch")],
        [P("Basal (planting)","tcl"),    P("FYM (Farm Yard Manure)","tcl"),   P("5 tonnes","tc"), P("15 days before planting","tc"), P("Use own — free","tc")],
        [P("Basal","tcl"),               P("DAP (18:46:0)","tcl"),            P("50 kg","tc"),    P("At planting","tc"),             P("1,300","tc")],
        [P("Basal","tcl"),               P("MOP (Muriate of Potash)","tcl"),  P("25 kg","tc"),    P("At planting","tc"),             P("575","tc")],
        [P("Top dressing (after each cut)","tcl"),P("Urea (46% N)","tcl"),   P("20 kg","tc"),    P("After each harvest (~45 days)","tc"),P("440","tc")],
        [P("Micronutrient (if deficient)","tcl"),P("Zinc Sulphate","tcl"),   P("10 kg","tc"),    P("Once per year","tc"),           P("250","tc")],
        [P("<b>Total annual input cost</b>","tcb"),P("","tc"),P("","tc"),P("","tc"),P("<b>~₹3,500–4,500/acre</b>","tc")],
    ]
    t = table(fr, [70,65,35,65,None],[
        ("BACKGROUND",(0,5),(-1,5),LGREEN),
        ("FONTNAME",(0,5),(-1,5),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,5),(-1,5),DGREEN),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("9.3  Where to Get Soil Tested Near Pydi Bhimavaram", "h2"))
    sl = [
        [P("Lab / Agency","tch"), P("Location","tch"), P("Tests","tch"), P("Cost","tch"), P("Turnaround","tch")],
        [P("ANGRAU Soil Testing Lab","tcl"),       P("Vizianagaram campus\n(60 km from Pydi Bhimavaram)","tcl"), P("N, P, K, pH, OC, EC, micronutrients","tc"), P("₹50–150","tc"), P("7–10 days","tc")],
        [P("Dept. of Agriculture STL","tcl"),       P("Srikakulam District HQ","tcl"), P("N, P, K, pH, OC, EC","tc"), P("Free (govt. scheme)","tc"), P("15–20 days","tc")],
        [P("IFFCO / Coromandel dealer","tcl"),      P("Srikakulam town / Narasannapeta","tcl"), P("Basic NPK + pH","tc"), P("Free with fertiliser purchase","tc"), P("5–7 days","tc")],
        [P("Rythu Seva Kendram (RSK)","tcl"),       P("Pydi Bhimavaram / Narasannapeta","tcl"), P("Soil Health Card — complete","tc"), P("Free (GoAP scheme)","tc"), P("21 days","tc")],
    ]
    t = table(sl, [75,70,65,45,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("📋  <b>Soil Health Card Scheme:</b> Apply at your nearest Rythu Seva Kendram (RSK) in Srikakulam. GoAP provides a free Soil Health Card with N, P, K, pH, OC, S, Zn, Fe, Mn, Cu analysis and crop-specific fertiliser recommendations. Get this done before planting Napier.", "note"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec10_cost_summary():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 10 — ANNUAL COST SUMMARY & REVENUE PROJECTION", "h1"))
    els.append(HR())
    els.append(P("Complete financial picture for 40 Murrah buffaloes (Model B — recommended) in Pydi Bhimavaram, Srikakulam, FY 2026–27.", "body"))
    els.append(SP(3))

    els.append(P("10.1  Annual Operating Cost Summary", "h2"))
    oc = [
        [P("Cost Head","tch"), P("Annual Cost (₹)","tch"), P("Monthly (₹)","tch"), P("Per Litre of Milk (₹)","tch")],
        [P("Feed (Model B — green + concentrate)","tcl"), P("13,56,960","tc"), P("1,13,080","tc"), P("8.74","tc")],
        [P("Medicines, vaccines & minerals","tcl"),        P("1,70,100","tc"),  P("14,175","tc"),   P("1.10","tc")],
        [P("Labour (3 workers @ ₹12,000/mo)","tcl"),       P("4,32,000","tc"),  P("36,000","tc"),   P("2.78","tc")],
        [P("Electricity (motors, fans, lights)","tcl"),    P("72,000","tc"),    P("6,000","tc"),    P("0.46","tc")],
        [P("Water (bore well running cost)","tcl"),         P("24,000","tc"),    P("2,000","tc"),    P("0.15","tc")],
        [P("Artificial Insemination (40 AI/yr)","tcl"),    P("20,000","tc"),    P("1,667","tc"),    P("0.13","tc")],
        [P("Veterinary consultation (12 visits)","tcl"),   P("18,000","tc"),    P("1,500","tc"),    P("0.12","tc")],
        [P("Fodder cultivation inputs (2.5 ac)","tcl"),    P("11,250","tc"),    P("938","tc"),      P("0.07","tc")],
        [P("Milk testing & quality (APDDCF)","tcl"),       P("12,000","tc"),    P("1,000","tc"),    P("0.08","tc")],
        [P("Miscellaneous (repairs, transport)","tcl"),    P("36,000","tc"),    P("3,000","tc"),    P("0.23","tc")],
        [P("<b>TOTAL OPERATING COST</b>","tcb"),           P("<b>21,52,310</b>","tc"), P("<b>1,79,359</b>","tc"), P("<b>₹13.86/litre</b>","tc")],
    ]
    t = table(oc, [105,55,45,None],[
        ("BACKGROUND",(0,11),(-1,11),LGREEN),
        ("FONTNAME",(0,11),(-1,11),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,11),(-1,11),DGREEN),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("10.2  Revenue Projection", "h2"))
    rv = [
        [P("Revenue Source","tch"), P("Quantity","tch"), P("Rate (₹)","tch"), P("Annual Revenue (₹)","tch")],
        [P("Milk sales (31 lactating × 14 L/day × 305 days)","tcl"), P("1,32,370 L","tc"), P("62/L (APDDCF rate)","tc"), P("82,06,940","tc")],
        [P("Calf sales (10 calves/yr average)","tcl"),                P("10 calves","tc"),  P("₹8,000/calf","tc"),        P("80,000","tc")],
        [P("Dry dung / biogas (40 animals)","tcl"),                   P("—","tc"),           P("Captive use","tc"),         P("36,000 (electricity savings)","tc")],
        [P("Spent animal sale (culls, 3–4/yr)","tcl"),                P("3 animals","tc"),   P("₹40,000/animal","tc"),     P("1,20,000","tc")],
        [P("<b>TOTAL ANNUAL REVENUE</b>","tcb"),                      P("","tc"),             P("","tc"),                   P("<b>₹84,42,940</b>","tc")],
    ]
    t = table(rv, [130,40,55,None],[
        ("BACKGROUND",(0,4),(-1,4),LGREEN),
        ("FONTNAME",(0,4),(-1,4),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,4),(-1,4),DGREEN),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("10.3  Profit Summary", "h2"))
    ps = [
        [P("Item","tch"), P("Amount (₹)","tch")],
        [P("Total Annual Revenue","tcl"),   P("84,42,940","tc")],
        [P("Total Annual Operating Cost","tcl"), P("(21,52,310)","tc")],
        [P("<b>NET ANNUAL PROFIT (before loan EMI)</b>","tcb"), P("<b>62,90,630</b>","tc")],
        [P("Loan EMI (₹89.3L @ 7% × 9 years)","tcl"),          P("(11,16,000)","tc")],
        [P("<b>NET PROFIT AFTER EMI</b>","tcb"),                 P("<b>₹51,74,630 (~₹51.7 Lakhs)</b>","tc")],
        [P("Return on Investment (post-subsidy)","tcl"),         P("~58% per annum","tc")],
        [P("Break-even milk price","tcl"),                       P("₹13.86/litre (well below ₹62 sale price)","tc")],
    ]
    t = table(ps, [170,None],[
        ("BACKGROUND",(0,2),(-1,2),LGREEN),
        ("FONTNAME",(0,2),(-1,2),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,2),(-1,2),DGREEN),
        ("BACKGROUND",(0,4),(-1,4),GREEN),
        ("FONTNAME",(0,4),(-1,4),"Helvetica-Bold"),
        ("TEXTCOLOR",(0,4),(-1,4),WHITE),
        ("BACKGROUND",(0,1),(1,1),LRED),
        ("BACKGROUND",(0,3),(1,3),LRED),
    ], repeat_header=True)
    els.append(t)
    els.append(SP(2))
    els.append(P("💰  <b>At ₹62/litre (APDDCF procurement price), your farm generates ₹51.7 lakhs net profit per year after EMI</b> — approximately ₹4.3 lakhs per month take-home. Milk price above ₹14/litre is pure profit.", "note"))
    return els

# ─────────────────────────────────────────────────────────────────────────────
def sec11_recommendation():
    els = []
    els.append(PageBreak())
    els.append(P("SECTION 11 — RECOMMENDED MODEL & 90-DAY ACTION PLAN", "h1"))
    els.append(HR())
    els.append(SP(3))

    els.append(P("11.1  Our Recommendation: Model B with Silage Upgrade in Year 2", "h2"))
    els.append(P(
        "For your farm at Pydi Bhimavaram with 40 Murrah buffaloes and ~2.5 acres of land, <b>Model B (Own Napier + Purchased Concentrate)</b> is the optimal starting point. "
        "In Year 2, after establishing the Napier plantation and cash flow, add a 60-tonne silage pit and transition to Model C for further cost reduction.",
        "body"))
    els.append(SP(3))

    rec = [
        [P("Priority","tch"), P("Recommendation","tch"), P("Benefit","tch"), P("Cost","tch")],
        [P("★★★","tc"), P("Plant 2.5 acres CO-3 Napier immediately","tcl"), P("Saves ₹11,000+/month on green fodder","tcl"), P("₹35,000 one-time","tc")],
        [P("★★★","tc"), P("Start urea-treating paddy straw (free from fields nearby)","tcl"), P("Triples straw protein from 4% to 10%; free input","tcl"), P("₹500/month urea","tc")],
        [P("★★★","tc"), P("Register with SDMPCS (milk co-operative) in Srikakulam","tcl"), P("₹62+/L guaranteed price; subsidised concentrates","tcl"), P("Free registration","tc")],
        [P("★★★","tc"), P("Contact AP Animal Husbandry VAS for free vaccine schedule","tcl"), P("FMD, HS, BQ free; saves ₹8,000/yr","tcl"), P("Free","tc")],
        [P("★★","tc"),  P("Get Soil Health Card (Rythu Seva Kendram, Srikakulam)","tcl"), P("Optimise fertiliser spend on Napier","tcl"), P("Free","tc")],
        [P("★★","tc"),  P("Test bore well water (Oct–Nov)","tcl"), P("Check fluoride/TDS — coastal risk","tcl"), P("₹500–1,200","tc")],
        [P("★★","tc"),  P("Install mineral lick blocks in shed","tcl"), P("Passive mineral intake, improves milk fat","tcl"), P("₹200/block/month","tc")],
        [P("★","tc"),   P("Year 2: Construct 60T silage pit (maize kharif season)","tcl"), P("Transition to Model C; saves ₹15,500/month","tcl"), P("₹80,000","tc")],
        [P("★","tc"),   P("Year 2: Install biogas unit (40 animals = 20 m³/day)","tcl"), P("Free cooking gas + electricity savings ₹3,000/month","tcl"), P("₹1,50,000 (MNRE subsidy available)","tc")],
    ]
    t = table(rec, [20,110,85,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("11.2  90-Day Quick-Start Action Plan", "h2"))
    ap = [
        [P("Week","tch"), P("Action","tch"), P("Who","tch"), P("Cost","tch")],
        [P("Week 1–2","tcl"),  P("Contact local VAS at Pydi Bhimavaram / Sompeta for vaccination schedule registration","tcl"), P("Bhavani / farm manager","tcl"), P("Free","tc")],
        [P("Week 1–2","tcl"),  P("Apply for Soil Health Card at RSK, Narasannapeta","tcl"), P("Bhavani","tcl"), P("Free","tc")],
        [P("Week 2–3","tcl"),  P("Collect paddy straw; start urea treatment (50 kg batches)","tcl"), P("Farm labour","tcl"), P("₹500","tc")],
        [P("Week 3–4","tcl"),  P("Source CO-3 Napier slips from ANGRAU or govt. farm — plant 2.5 acres","tcl"), P("Labour + Bhavani","tcl"), P("₹35,000","tc")],
        [P("Week 4","tcl"),    P("Collect water sample, submit to APSPCB Srikakulam for testing","tcl"), P("Bhavani","tcl"), P("₹800","tc")],
        [P("Week 5–6","tcl"),  P("Register with SDMPCS / APDDCF for guaranteed milk procurement","tcl"), P("Bhavani","tcl"), P("Free","tc")],
        [P("Week 6–8","tcl"),  P("Set up DairyFlow app on mobile — enter all 40 animal records, calving dates, AI history","tcl"), P("Bhavani / Suresh Babu","tcl"), P("Free (system done)","tc")],
        [P("Week 8–10","tcl"), P("Implement daily health checklist using DairyFlow Health module","tcl"), P("Priya Devi (Vet)","tcl"), P("Free","tc")],
        [P("Month 3","tcl"),   P("Review milk yield data on DairyFlow dashboard; optimise ration based on actual performance","tcl"), P("Bhavani + Ravi Kumar","tcl"), P("Feed adjustment only","tc")],
    ]
    t = table(ap, [28,120,55,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    els.append(P("11.3  Government Schemes Available to You in Srikakulam", "h2"))
    gs = [
        [P("Scheme","tch"), P("Benefit","tch"), P("Contact","tch")],
        [P("NABARD DEDS (already approved)","tcl"),         P("₹44.7L subsidy — already utilised for shed construction","tcl"), P("NABARD Regional Office, Vizag","tc")],
        [P("AP NTR Pashu Seva Scheme","tcl"),               P("Free veterinary services, medicines at VSU camps","tcl"),        P("AP Animal Husbandry Dept, Srikakulam","tc")],
        [P("MNRE Biogas Programme","tcl"),                  P("25–50% subsidy on biogas plant (central + state)","tcl"),        P("NEDCAP / DRDA Srikakulam","tc")],
        [P("Rythu Bandhu (if eligible)","tcl"),             P("₹5,000/acre/season for fodder land","tcl"),                     P("Agriculture Dept, Srikakulam","tc")],
        [P("PM Kisan Samman Nidhi","tcl"),                  P("₹6,000/yr direct benefit","tcl"),                               P("CSC / bank — register if not already","tc")],
        [P("APDDCF Milk Procurement","tcl"),                P("₹62/L + bonus, chilling facility, quality premium","tcl"),      P("SDMPCS Srikakulam town","tc")],
        [P("KCC (Kisan Credit Card)","tcl"),                P("Working capital loan at 4% interest for feed purchase","tcl"),   P("SBI / Andhra Bank, Srikakulam","tc")],
    ]
    t = table(gs, [80,110,None],[("VALIGN",(0,0),(-1,-1),"TOP")], repeat_header=True)
    els.append(t)
    els.append(SP(4))

    # Final summary box
    summary = Table([[
        P("<b>SUMMARY:  Your 40-buffalo Murrah farm in Pydi Bhimavaram is extremely viable.</b><br/><br/>"
          "At ₹62/litre milk price:<br/>"
          "• Annual revenue: <b>₹84.4 lakhs</b><br/>"
          "• Annual operating cost (Model B): <b>₹21.5 lakhs</b><br/>"
          "• Net profit after EMI: <b>₹51.7 lakhs/year (~₹4.3 lakhs/month)</b><br/><br/>"
          "Start with Model B (Napier + concentrate). Test your water. Get the free Soil Health Card. "
          "Register with the milk co-operative. Use your DairyFlow system to track every animal daily. "
          "Upgrade to silage in Year 2.", "body")
    ]], colWidths=[PAGE_W - L - R])
    summary.setStyle(TableStyle([
        ("BACKGROUND",(0,0),(-1,-1),LGREEN),
        ("BOX",(0,0),(-1,-1),1.5,GREEN),
        ("TOPPADDING",(0,0),(-1,-1),12),
        ("BOTTOMPADDING",(0,0),(-1,-1),12),
        ("LEFTPADDING",(0,0),(-1,-1),14),
        ("RIGHTPADDING",(0,0),(-1,-1),14),
    ]))
    els.append(summary)
    return els

# ══════════════════════════════════════════════════════════════════════════════
# ASSEMBLE & BUILD
# ══════════════════════════════════════════════════════════════════════════════

def main():
    doc = SimpleDocTemplate(
        OUTPUT,
        pagesize=A4,
        leftMargin=L, rightMargin=R,
        topMargin=T + 12*mm,
        bottomMargin=B + 8*mm,
        title="Bhavani Dairy Farm — Feed & Health Management Guide 2026",
        author="DairyFlow Smart Management System",
    )

    story = []
    story += build_cover()
    story += sec1_ingredients()
    story += sec2_ration()
    story += sec3_models()
    story += sec4_monthly_cost()
    story += sec5_vaccination()
    story += sec6_minerals()
    story += sec7_health_tracking()
    story += sec8_water()
    story += sec9_soil()
    story += sec10_cost_summary()
    story += sec11_recommendation()

    doc.build(story, onFirstPage=on_first_page, onLaterPages=on_page)
    print(f"✅  PDF written to: {OUTPUT}")

if __name__ == "__main__":
    main()
