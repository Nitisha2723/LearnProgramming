from typing import Dict, List, Optional

from ..models.customer import Customer
from .customer_repository import CustomerRepository


class InMemoryCustomerRepository(CustomerRepository):
    def __init__(self) -> None:
        self._store: Dict[str, Customer] = {}

    def save(self, customer: Customer) -> Customer:
        self._store[customer.id] = customer
        return customer

    def find_by_id(self, customer_id: str) -> Optional[Customer]:
        return self._store.get(customer_id)

    def find_by_email(self, email: str) -> Optional[Customer]:
        email_lower = email.lower()
        return next(
            (c for c in self._store.values() if c.email == email_lower), None
        )

    def find_all(self) -> List[Customer]:
        return list(self._store.values())

    def delete(self, customer_id: str) -> bool:
        if customer_id in self._store:
            del self._store[customer_id]
            return True
        return False
