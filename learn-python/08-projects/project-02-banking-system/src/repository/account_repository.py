from abc import ABC, abstractmethod
from typing import List, Optional

from ..models.account import Account, AccountType


class AccountRepository(ABC):
    """Abstract repository for Account persistence."""

    @abstractmethod
    def save(self, account: Account) -> Account: ...

    @abstractmethod
    def find_by_id(self, account_id: str) -> Optional[Account]: ...

    @abstractmethod
    def find_by_owner(self, owner_id: str) -> List[Account]: ...

    @abstractmethod
    def find_all(self) -> List[Account]: ...

    @abstractmethod
    def delete(self, account_id: str) -> bool: ...
