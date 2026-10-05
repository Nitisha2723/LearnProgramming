"""Export pipeline results to CSV."""

import csv
import io
from typing import Iterable, List, Optional


class CsvExporter:
    """
    Export a list of dicts to CSV.

    Usage:
        exporter = CsvExporter()
        csv_string = exporter.to_string(records, fields=["url", "title", "word_count"])
        exporter.to_file("output.csv", records)
    """

    def to_string(
        self,
        records: Iterable[dict],
        fields: Optional[List[str]] = None,
    ) -> str:
        """Convert records to a CSV string."""
        records_list = list(records)
        if not records_list:
            return ""
        if fields is None:
            fields = list(records_list[0].keys())
        output = io.StringIO()
        writer = csv.DictWriter(output, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(records_list)
        return output.getvalue()

    def to_file(
        self,
        filepath: str,
        records: Iterable[dict],
        fields: Optional[List[str]] = None,
    ) -> int:
        """Write records to a CSV file. Returns number of rows written."""
        records_list = list(records)
        if not records_list:
            return 0
        if fields is None:
            fields = list(records_list[0].keys())
        with open(filepath, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=fields, extrasaction="ignore")
            writer.writeheader()
            writer.writerows(records_list)
        return len(records_list)
