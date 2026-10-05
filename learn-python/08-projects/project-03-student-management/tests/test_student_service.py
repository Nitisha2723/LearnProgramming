"""Tests for StudentService."""

import pytest
from src.service.student_service import StudentService
from src.models.enrollment import Grade


@pytest.fixture
def svc():
    return StudentService()


@pytest.fixture
def setup(svc):
    math = svc.add_course("Math", "MATH101", credits=3)
    cs = svc.add_course("CS", "CS101", credits=3)
    alice = svc.add_student("Alice", "alice@u.edu", "CS")
    bob = svc.add_student("Bob", "bob@u.edu", "Math")
    return {"svc": svc, "math": math, "cs": cs, "alice": alice, "bob": bob}


class TestStudentManagement:
    def test_add_student(self, svc):
        s = svc.add_student("Alice", "alice@u.edu", "CS")
        assert s.name == "Alice"

    def test_add_student_invalid_email_raises(self, svc):
        with pytest.raises(ValueError):
            svc.add_student("Alice", "not-an-email", "CS")

    def test_get_all_students_sorted_by_name(self, svc):
        svc.add_student("Zara", "z@u.edu", "CS")
        svc.add_student("Alice", "a@u.edu", "CS")
        names = [s.name for s in svc.get_all_students()]
        assert names == sorted(names)


class TestEnrollment:
    def test_enroll_student(self, setup):
        svc, alice, cs = setup["svc"], setup["alice"], setup["cs"]
        e = svc.enroll(alice.id, cs.id)
        assert e.student_id == alice.id

    def test_enroll_duplicate_raises(self, setup):
        svc, alice, cs = setup["svc"], setup["alice"], setup["cs"]
        svc.enroll(alice.id, cs.id)
        with pytest.raises(ValueError, match="already enrolled"):
            svc.enroll(alice.id, cs.id)


class TestGPA:
    def test_gpa_with_single_grade(self, setup):
        svc, alice, cs = setup["svc"], setup["alice"], setup["cs"]
        e = svc.enroll(alice.id, cs.id)
        svc.assign_grade(e.id, Grade.A)
        assert svc.calculate_gpa(alice.id) == pytest.approx(4.0)

    def test_gpa_no_grades_returns_none(self, setup):
        svc, alice = setup["svc"], setup["alice"]
        assert svc.calculate_gpa(alice.id) is None

    def test_gpa_weighted_by_credits(self, setup):
        svc, alice, math, cs = setup["svc"], setup["alice"], setup["math"], setup["cs"]
        e1 = svc.enroll(alice.id, math.id)  # 3 credits
        e2 = svc.enroll(alice.id, cs.id)    # 3 credits
        svc.assign_grade(e1.id, Grade.A)       # 4.0
        svc.assign_grade(e2.id, Grade.B)       # 3.0
        gpa = svc.calculate_gpa(alice.id)
        assert gpa == pytest.approx(3.5)


class TestCSV:
    def test_export_csv_contains_header(self, svc):
        csv = svc.export_students_csv()
        assert "student_number" in csv
        assert "name" in csv

    def test_import_csv_round_trip(self, svc):
        svc.add_student("Alice", "alice@u.edu", "CS")
        csv_data = svc.export_students_csv()

        svc2 = StudentService()
        imported = svc2.import_students_csv(csv_data)
        assert len(imported) == 1
        assert imported[0].name == "Alice"
