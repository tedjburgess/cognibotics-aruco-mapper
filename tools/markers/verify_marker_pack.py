"""Check final PDF geometry and decode every rendered marker with OpenCV."""

import argparse
import json
import math
import shutil
import subprocess
import tempfile
from pathlib import Path

import cv2
import numpy as np
from pypdf import PdfReader
from pypdf.generic import ContentStream
from reportlab.pdfbase import pdfmetrics

POINTS_PER_MM = 72 / 25.4
# Keep expected coverage independent of the generated manifest so an omitted ID cannot pass.
EXPECTED_FAMILIES = {
    "DICT_4X4_50": 20.0, "DICT_5X5_50": 50.0, "DICT_6X6_50": 75.0,
    "DICT_7X7_50": 100.0, "DICT_ARUCO_ORIGINAL": 110.0,
}


def resolve_dictionary_conflicts(image, candidates):
    """Mirror the app's same-square full-pattern check, not its dictionary iteration order."""
    visited = set()
    kept = set(range(len(candidates)))
    conflicts = 0
    def same_square(a, b):
        tolerance = max(2, math.sqrt(max(abs(cv2.contourArea(a[2])), abs(cv2.contourArea(b[2])))) * .03)
        distances = np.linalg.norm(a[2][:, None, :] - b[2][None, :, :], axis=2)
        return np.all(distances.min(axis=0) <= tolerance) and np.all(distances.min(axis=1) <= tolerance)
    def error(candidate):
        name, marker_id, points = candidate
        target = np.array([[0, 0], [143, 0], [143, 143], [0, 143]], dtype=np.float32)
        transform = cv2.getPerspectiveTransform(points, target)
        warped = cv2.warpPerspective(image, transform, (144, 144))
        _, binary = cv2.threshold(warped, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
        dictionary = cv2.aruco.getPredefinedDictionary(getattr(cv2.aruco, name))
        pattern = cv2.aruco.generateImageMarker(dictionary, marker_id, 144)
        return np.count_nonzero(binary != pattern) / pattern.size
    for index in range(len(candidates)):
        if index in visited:
            continue
        visited.add(index)
        group = [index]
        for current in group:
            for other in range(len(candidates)):
                if (other not in visited and candidates[current][0] != candidates[other][0]
                        and same_square(candidates[current], candidates[other])):
                    visited.add(other)
                    group.append(other)
        if len(group) == 1:
            continue
        conflicts += 1
        scored = sorted((error(candidates[i]), i) for i in group)
        kept.difference_update(group)
        if scored[0][0] <= .15 and scored[1][0] - scored[0][0] >= .05:
            kept.add(scored[0][1])
    return [candidate for i, candidate in enumerate(candidates) if i in kept], conflicts


def check_geometry(page, expected):
    assert abs(float(page.mediabox.width) / POINTS_PER_MM - 210) < 0.001
    assert abs(float(page.mediabox.height) / POINTS_PER_MM - 297) < 0.001
    boxes = [[], []]
    segments = []
    point = None
    for args, op in ContentStream(page.get_contents(), page.pdf).operations:
        if op == b"re":
            x, y, width, height = map(float, args)
            middle_from_top = 297 - (y + height / 2) / POINTS_PER_MM
            index = min(range(2), key=lambda i: abs(middle_from_top - expected["centres_from_top_left_mm"][i][1]))
            boxes[index].append((x, y, x + width, y + height))
        elif op == b"m":
            point = tuple(map(float, args))
        elif op == b"l" and point is not None:
            end = tuple(map(float, args))
            segments.append(math.dist(point, end) / POINTS_PER_MM)
            point = end
    centres = []
    for rectangles in boxes:
        left = min(r[0] for r in rectangles)
        bottom = min(r[1] for r in rectangles)
        right = max(r[2] for r in rectangles)
        top = max(r[3] for r in rectangles)
        assert abs((right - left) / POINTS_PER_MM - expected["size_mm"]) < 0.001
        assert abs((top - bottom) / POINTS_PER_MM - expected["size_mm"]) < 0.001
        centres.append(((left + right) / 2, (bottom + top) / 2))
    assert abs(math.dist(*centres) / POINTS_PER_MM - 135) < 0.001
    assert any(abs(length - 135) < 0.001 for length in segments)
    assert any(abs(length - 50) < 0.001 for length in segments)


def check_annotation_clearance(page, expected):
    """Check actual PDF text/line bounds, not just the labels extracted as strings."""
    identity = (1., 0., 0., 1., 0., 0.)
    matrix, text_matrix = identity, identity
    stack = []
    font, size, line_width = "Helvetica", 12., 1.
    point = None
    quiet_boxes = []
    half = expected["size_mm"] / 2 + expected["quiet_zone_mm"]
    for x, y in expected["centres_from_top_left_mm"]:
        quiet_boxes.append(((x - half) * POINTS_PER_MM, (297 - y - half) * POINTS_PER_MM,
                            (x + half) * POINTS_PER_MM, (297 - y + half) * POINTS_PER_MM))
    def transform(m, p):
        a, b, c, d, e, f = m
        return a * p[0] + c * p[1] + e, b * p[0] + d * p[1] + f
    def multiply(a, b):
        return (a[0]*b[0]+a[2]*b[1], a[1]*b[0]+a[3]*b[1],
                a[0]*b[2]+a[2]*b[3], a[1]*b[2]+a[3]*b[3],
                a[0]*b[4]+a[2]*b[5]+a[4], a[1]*b[4]+a[3]*b[5]+a[5])
    def check_box(box, text=False):
        left, bottom, right, top = box
        if text:
            assert left >= 5 * POINTS_PER_MM and right <= 205 * POINTS_PER_MM, box
            assert bottom >= 5 * POINTS_PER_MM and top <= 292 * POINTS_PER_MM, box
        for q in quiet_boxes:
            assert right <= q[0] or left >= q[2] or top <= q[1] or bottom >= q[3], (box, q)
    for args, op in ContentStream(page.get_contents(), page.pdf).operations:
        if op == b"q":
            stack.append((matrix, line_width))
        elif op == b"Q":
            matrix, line_width = stack.pop()
        elif op == b"cm":
            matrix = multiply(matrix, tuple(map(float, args)))
        elif op == b"w":
            line_width = float(args[0])
        elif op == b"Tf":
            font = str(page["/Resources"]["/Font"][args[0]]["/BaseFont"]).lstrip("/")
            size = float(args[1])
        elif op == b"Tm":
            text_matrix = tuple(map(float, args))
        elif op == b"Tj":
            text = str(args[0])
            width = pdfmetrics.stringWidth(text, font, size)
            ascent, descent = pdfmetrics.getAscentDescent(font, size)
            m = multiply(matrix, text_matrix)
            corners = [transform(m, p) for p in [(0, descent), (width, descent), (width, ascent), (0, ascent)]]
            check_box((min(p[0] for p in corners), min(p[1] for p in corners),
                       max(p[0] for p in corners), max(p[1] for p in corners)), text=True)
        elif op == b"m":
            point = transform(matrix, tuple(map(float, args)))
        elif op == b"l" and point is not None:
            end = transform(matrix, tuple(map(float, args)))
            half_line = line_width / 2
            check_box((min(point[0], end[0]) - half_line, min(point[1], end[1]) - half_line,
                       max(point[0], end[0]) + half_line, max(point[1], end[1]) + half_line))
            point = end


def verify(root, renderer, previews=None):
    manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
    reader = PdfReader(root / "aruco-test-pack.pdf")
    assert len(reader.pages) == len(manifest["pages"]) == 125
    assert set(p["dictionary"] for p in manifest["pages"]) == set(EXPECTED_FAMILIES)
    seen = set()
    conflicts_checked = 0
    with tempfile.TemporaryDirectory(prefix="aruco-print-check-") as temp:
        for page_index, expected in enumerate(manifest["pages"]):
            page = reader.pages[page_index]
            assert expected["size_mm"] == EXPECTED_FAMILIES[expected["dictionary"]]
            assert expected["page"] == page_index + 1
            assert expected["family_page"] == page_index % 25 + 1
            assert expected["ids"] == [2 * (page_index % 25), 2 * (page_index % 25) + 1]
            role = "directional" if expected["ids"][0] < 10 else "positional"
            origin = "corner" if role == "directional" else "centre"
            assert expected["role"] == role and expected["origin"] == origin
            text = page.extract_text()
            assert expected["dictionary"] in text
            assert f"IDs {expected['ids'][0]} + {expected['ids'][1]}" in text
            assert f"{expected['size_mm']:g} x {expected['size_mm']:g} mm" in text
            assert text.count(role.upper()) == text.count(origin.upper()) == 2
            assert all(f"ID {marker_id}\n" in text for marker_id in expected["ids"])
            check_geometry(page, expected)
            check_annotation_clearance(page, expected)
            family = PdfReader(root / expected["family_file"])
            assert len(family.pages) == 25
            assert family.pages[expected["family_page"] - 1].get_contents().get_data() == page.get_contents().get_data()
            prefix = Path(temp) / f"page-{page_index + 1}"
            subprocess.run([str(renderer), "-f", str(page_index + 1), "-l", str(page_index + 1),
                            "-r", "300", "-singlefile", "-png", str(root / "aruco-test-pack.pdf"),
                            str(prefix)], check=True, capture_output=True)
            rendered = prefix.with_suffix(".png")
            image = cv2.imread(str(rendered), cv2.IMREAD_GRAYSCALE)
            assert image is not None
            candidates = []
            for name in dict.fromkeys(p["dictionary"] for p in manifest["pages"]):
                dictionary = cv2.aruco.getPredefinedDictionary(getattr(cv2.aruco, name))
                parameters = cv2.aruco.DetectorParameters()
                parameters.cornerRefinementMethod = cv2.aruco.CORNER_REFINE_SUBPIX
                parameters.errorCorrectionRate = 0.0 if name in ("DICT_4X4_50", "DICT_ARUCO_ORIGINAL") else 0.4
                corners, ids, _ = cv2.aruco.ArucoDetector(dictionary, parameters).detectMarkers(image)
                if ids is not None:
                    candidates.extend((name, int(marker_id), points[0]) for points, marker_id in zip(corners, ids.flatten()))
            accepted, conflicts = resolve_dictionary_conflicts(image, candidates)
            conflicts_checked += conflicts
            detected = {(name, marker_id) for name, marker_id, _ in accepted}
            assert detected == {(expected["dictionary"], marker_id) for marker_id in expected["ids"]}, (page_index + 1, detected)
            assert len(accepted) == 2
            for name, marker_id, points in accepted:
                # The rendered border should agree with the exact vector geometry too.
                width_px = math.dist(points[0], points[1])
                assert abs(width_px * 25.4 / 300 - expected["size_mm"]) < 0.3
                # Corner-origin checks need the decoded top-left to match the printed one.
                centre = expected["centres_from_top_left_mm"][expected["ids"].index(int(marker_id))]
                printed_top_left = tuple((v - expected["size_mm"] / 2) * 300 / 25.4 for v in centre)
                assert math.dist(points[0], printed_top_left) < 2
            for marker_id in expected["ids"]:
                key = (expected["dictionary"], marker_id)
                assert key not in seen
                seen.add(key)
            if previews is not None:
                previews.mkdir(parents=True, exist_ok=True)
                shutil.copy2(rendered, previews / rendered.name)
            if (page_index + 1) % 25 == 0:
                print(f"Verified {expected['dictionary']}: all 50 IDs, roles, decoded orientations and geometry.", flush=True)
    assert seen == {(name, marker_id) for name in EXPECTED_FAMILIES for marker_id in range(50)}
    assert sum(p["role"] == "directional" for p in manifest["pages"]) == 25
    assert sum(p["role"] == "positional" for p in manifest["pages"]) == 100
    print(f"PASS: 6 PDFs, 125 A4 pages, all 250 markers (50 directional + 200 positional), sizes and 135/50 mm checks; {conflicts_checked} same-square ambiguities checked with the app's full-pattern filter.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2] / "docs/markers")
    parser.add_argument("--renderer", type=Path, default=shutil.which("pdftoppm"))
    parser.add_argument("--previews", type=Path)
    args = parser.parse_args()
    if args.renderer is None:
        parser.error("Install Poppler or pass --renderer with the path to pdftoppm.")
    verify(args.root, args.renderer, args.previews)
