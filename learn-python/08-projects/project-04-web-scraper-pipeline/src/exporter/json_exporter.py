"""Export pipeline results to JSON."""

import json
from typing import Iterable, Optional


class JsonExporter:
    """
    Export a list of dicts to JSON.

    Usage:
        exporter = JsonExporter(indent=2)
        json_string = exporter.to_string(records)
        exporter.to_file("output.json", records)
    """

    def __init__(self, indent: int = 2) -> None:
        self.indent = indent

    def to_string(self, records: Iterable[dict]) -> str:
        """Convert records to a JSON string."""
        return json.dumps(list(records), indent=self.indent, ensure_ascii=False)

    def to_file(self, filepath: str, records: Iterable[dict]) -> int:
        """Write records to a JSON file. Returns number of records."""
        records_list = list(records)
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(records_list, f, indent=self.indent, ensure_ascii=False)
        return len(records_list)
