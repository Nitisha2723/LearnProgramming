from abc import ABC, abstractmethod
from typing import List, Optional

from ..models.customer import Customer


class CustomerRepository(ABC):
    """Abstract repository for Customer persistence."""

    @abstractmethod
    def save(self, customer: Customer) -> Customer: ...

    @abstractmethod
    def find_by_id(self, customer_id: str) -> Optional[Customer]: ...

    @abstractmethod
    def find_by_email(self, email: str) -> Optional[Customer]: ...

    @abstractmethod
    def find_all(self) -> List[Customer]: ...

    @abstractmethod
    def delete(self, customer_id: str) -> bool: ...
