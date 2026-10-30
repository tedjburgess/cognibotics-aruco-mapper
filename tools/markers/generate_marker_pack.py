"""Generate exact-size vector markers; print the PDFs at Actual size (100%)."""

import argparse
import json
from pathlib import Path

import cv2
from pypdf import PdfReader, PdfWriter
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas

# These dimensions must agree with the Android ArucoDictionary enum.
FAMILIES = [
    ("4x4", "DICT_4X4_50", 20.0),
    ("5x5", "DICT_5X5_50", 50.0),
    ("6x6", "DICT_6X6_50", 75.0),
    ("7x7", "DICT_7X7_50", 100.0),
    ("original", "DICT_ARUCO_ORIGINAL", 110.0),
]
# Print every ID in the agreed 0-49 sample range, including all directional IDs 0-9.
PAIRS = [(marker_id, marker_id + 1) for marker_id in range(0, 50, 2)]
CENTRES_MM = [(105.0, 80.0), (105.0, 215.0)]  # Positions from the page's top-left.
SPACING_MM = 135.0


def draw_marker(pdf, dictionary, marker_id, size, centre):
    cells = dictionary.markerSize + 2
    # Sample cell centres from OpenCV's encoder, then draw exact vector rectangles.
    image = cv2.aruco.generateImageMarker(dictionary, marker_id, cells * 40, borderBits=1)
    grid = image[20::40, 20::40]
    left = (centre[0] - size / 2) * mm
    bottom = A4[1] - (centre[1] + size / 2) * mm
    cell = size * mm / cells
    pdf.setFillColorRGB(0, 0, 0)
    for row in range(cells):
        for column in range(cells):
            if grid[row, column] == 0:
                pdf.rect(left + column * cell, bottom + (cells - row - 1) * cell,
                         cell, cell, fill=1, stroke=0)


def make_page(pdf, name, size, pair, family_page):
    dictionary = cv2.aruco.getPredefinedDictionary(getattr(cv2.aruco, name))
    # Reserve at least one white cell around each marker, free of other printed content.
    quiet = size / (dictionary.markerSize + 2)
    assert SPACING_MM - size >= quiet
    assert CENTRES_MM[0][1] - size / 2 - quiet > 9
    assert CENTRES_MM[1][1] + size / 2 + quiet < 286
    origin = "CORNER" if pair[0] < 10 else "CENTRE"
    role = "DIRECTIONAL" if pair[0] < 10 else "POSITIONAL"
    assert all((marker_id < 10) == (pair[0] < 10) for marker_id in pair)
    pdf.setFont("Helvetica-Bold", 9)
    pdf.drawString(10 * mm, A4[1] - 8 * mm,
                   f"{name}  |  {size:g} x {size:g} mm  |  IDs {pair[0]} + {pair[1]}  |  Sheet {family_page}/25")
    for marker_id, centre in zip(pair, CENTRES_MM):
        draw_marker(pdf, dictionary, marker_id, size, centre)
        # Labels stay outside the white quiet zone; nothing is drawn over the code.
        pdf.setFont("Helvetica-Bold", 9)
        pdf.drawString(10 * mm, A4[1] - (centre[1] - 3) * mm, f"ID {marker_id}")
        pdf.setFont("Helvetica", 7)
        pdf.drawString(10 * mm, A4[1] - (centre[1] + 2) * mm, role)
        pdf.drawString(10 * mm, A4[1] - (centre[1] + 6) * mm, origin)
        pdf.drawString(10 * mm, A4[1] - (centre[1] + 10) * mm, f"{size:g} mm")
    pdf.setLineWidth(0.5)
    top, bottom = (A4[1] - centre[1] * mm for centre in CENTRES_MM)
    pdf.line(192 * mm, top, 192 * mm, bottom)
    for y in (top, bottom):
        pdf.line(190 * mm, y, 194 * mm, y)
    pdf.saveState()
    pdf.translate(198 * mm, (top + bottom) / 2)
    pdf.rotate(90)
    pdf.setFont("Helvetica", 8)
    pdf.drawCentredString(0, 0, "135 mm centre spacing - same origin type")
    pdf.restoreState()
    # Verify this 50 mm control line and the black square after printing.
    y = A4[1] - 287 * mm
    pdf.line(80 * mm, y, 130 * mm, y)
    for x in (80, 130):
        pdf.line(x * mm, y - mm, x * mm, y + mm)
    pdf.setFont("Helvetica", 8)
    pdf.drawCentredString(105 * mm, A4[1] - 291 * mm,
                          "50 mm scale check | A4 | Actual size 100% | No Fit to page")
    pdf.showPage()
    return {
        "dictionary": name, "size_mm": size, "ids": list(pair), "origin": origin.lower(),
        "role": role.lower(), "family_page": family_page,
        "centre_spacing_mm": SPACING_MM, "centres_from_top_left_mm": CENTRES_MM,
        "quiet_zone_mm": quiet, "border_bits": 1,
    }


def generate(output):
    output.mkdir(parents=True, exist_ok=True)
    families = output / "families"
    families.mkdir(exist_ok=True)
    manifest = {"paper": "A4 portrait", "print_scale_percent": 100,
                "sample_id_range": [0, 49], "directional_id_range": [0, 9],
                "positional_id_range": [10, 49],
                "original_size_basis": "110 mm project test convention, not specified by Mathias",
                "pages": []}
    writer = PdfWriter()
    for label, name, size in FAMILIES:
        family_path = families / f"aruco-{label}-{size:g}mm.pdf"
        pdf = canvas.Canvas(str(family_path), pagesize=A4, pageCompression=1, invariant=1)
        pdf.setTitle(f"{name}: {size:g} mm markers, 135 mm spacing")
        for family_page, pair in enumerate(PAIRS, start=1):
            page = make_page(pdf, name, size, pair, family_page)
            page["family_file"] = family_path.relative_to(output).as_posix()
            page["page"] = len(manifest["pages"]) + 1
            manifest["pages"].append(page)
        pdf.save()
        reader = PdfReader(family_path)
        for page in reader.pages:
            writer.add_page(page)
    writer.add_metadata({"/Title": "ArUco test pack - IDs 0-49 in five dictionaries, 250 markers"})
    with (output / "aruco-test-pack.pdf").open("wb") as stream:
        writer.write(stream)
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(f"Created 125 A4 pages / 250 markers in {output}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path,
                        default=Path(__file__).resolve().parents[2] / "docs/markers")
    generate(parser.parse_args().output)
