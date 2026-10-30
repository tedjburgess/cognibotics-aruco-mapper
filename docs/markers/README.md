# Fixed ArUco test sheets (#95)

Print [the complete pack](aruco-test-pack.pdf), or choose one family from [families](families/).
There are **250 markers on 125 A4 pages**, two markers per page. **Every family includes every
ID from 0 through 49 exactly once**: ten directional markers (0-9) and forty positional markers
(10-49). Each sheet pairs consecutive IDs: 0/1, 2/3, 4/5, through 48/49. No IDs are skipped.
No marker image is reused under a different dictionary label.

Each family PDF has 25 sheets: **sheets 1-5 are DIRECTIONAL / CORNER**, and **sheets 6-25 are
POSITIONAL / CENTRE**. The full pack contains the families in the order listed below, with
pages 1-25, 26-50, 51-75, 76-100 and 101-125 respectively. Print only the pages you need.

| Dictionary | Black square including its black border | Family PDF |
|---|---:|---|
| DICT_4X4_50 | 20 mm | [4x4](families/aruco-4x4-20mm.pdf) |
| DICT_5X5_50 | 50 mm | [5x5](families/aruco-5x5-50mm.pdf) |
| DICT_6X6_50 | 75 mm | [6x6](families/aruco-6x6-75mm.pdf) |
| DICT_7X7_50 | 100 mm | [7x7](families/aruco-7x7-100mm.pdf) |
| DICT_ARUCO_ORIGINAL | 110 mm | [Original](families/aruco-original-110mm.pdf) |

**The original dictionary requires the app support in #96.** Its 110 mm size is our project
test convention, not a dimension supplied by Mathias. The four existing sizes are unchanged.
Use the detector ambiguity check in the same app branch for the complete pack: standard
OpenCV detection alone also interprets the 7x7 ID 44 square as 4x4 ID 17. The branch compares
the whole printed pattern before choosing a dictionary/physical size. The PDFs keep the
official codes intact; the verification below uses that same decision rule and checks every ID.
The original dictionary supports IDs 0-1023 in the app; this printable pack deliberately
contains IDs 0-49 only, as agreed for the sample set. It is not the complete original dictionary.

## Print and check

1. Select **A4 portrait, Actual size / 100%**, one PDF page per sheet. Disable Fit, Shrink,
   multiple pages per sheet and booklet printing. There is no printer-specific enlargement.
2. First print one sheet. Measure its **50 mm control line**, the black square width/height,
   and the **135 mm centre spacing**. All must match before printing the rest. If not, check
   printer settings; do not change the app size to compensate for an unverified print.
3. Use flat, matte paper with clear black printing. Do not write in the white space around
   the markers. The PDFs use sharp vector cells with at least one cell of clear white space.

These are fixed sample identities. Do not show two copies of the same dictionary + ID in
the same view, or the marker identity becomes ambiguous. These IDs also occur in older test
sheets; replace the sheet instead of mixing old and new copies.

## Distance and origin

The centres on every page are **135 mm apart**. Both markers are upright and have equal size.
The current app assigns IDs **0-9 to the decoded top-left corner** and IDs **10+ to the centre**.
The sheets label which origin the app uses. On a flat, intact page with both codes equally
oriented, both pairs still have 135 mm between their matching origins. Turning one code
independently, bending the paper or mixing sheets invalidates that same-sheet reference.
There is no assumed distance between markers on different sheets.

All directional IDs 0-9 are included and explicitly labelled DIRECTIONAL, together with their
CORNER reference.
IDs 10-49 are explicitly labelled POSITIONAL with their CENTRE reference. Historical results
in the main README are unchanged and must not be relabelled as measurements from this new pack.

## Regenerate

From the repository root, using Python 3.12 or later:

```powershell
python -m pip install -r tools/markers/requirements.txt
python tools/markers/generate_marker_pack.py
```

To verify sizes and decode the final rendered PDF pages, install Poppler (`pdftoppm` on PATH),
then run `python tools/markers/verify_marker_pack.py`. An optional `--renderer` argument accepts
its executable path. The check also confirms that the separate family PDFs match the full pack.

OpenCV encodes the actual dictionary patterns; ReportLab draws them at exact millimetre
dimensions without raster scaling. The checked-in manifest records IDs, sizes, centres and
origins. PDF dimensions are not a guarantee of printer accuracy; always measure the first print.
