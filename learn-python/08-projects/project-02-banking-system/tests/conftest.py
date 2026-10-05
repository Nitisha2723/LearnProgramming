"""Pytest fixtures for banking-system tests."""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

import pytest
from decimal import Decimal

from src.models.account import AccountType
from src.repository.in_memory_account_repository import InMemoryAccountRepository
from src.repository.in_memory_customer_repository import InMemoryCustomerRepository
from src.service.account_service import AccountService
from src.service.transfer_service import TransferService


@pytest.fixture
def account_repo():
    return InMemoryAccountRepository()


@pytest.fixture
def customer_repo():
    return InMemoryCustomerRepository()


@pytest.fixture
def service(account_repo, customer_repo):
    return AccountService(account_repo, customer_repo)


@pytest.fixture
def transfer_service(service):
    return TransferService(service)


@pytest.fixture
def alice(service):
    return service.register_customer("Alice", "alice@example.com")


@pytest.fixture
def bob(service):
    return service.register_customer("Bob", "bob@example.com")


@pytest.fixture
def alice_account(service, alice):
    return service.open_account(alice.id, AccountType.CHECKING, Decimal("1000"))


@pytest.fixture
def bob_account(service, bob):
    return service.open_account(bob.id, AccountType.SAVINGS, Decimal("500"))
