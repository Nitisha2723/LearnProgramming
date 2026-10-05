from typing import Dict, List, Optional

from ..models.account import Account
from .account_repository import AccountRepository


class InMemoryAccountRepository(AccountRepository):
    def __init__(self) -> None:
        self._store: Dict[str, Account] = {}

    def save(self, account: Account) -> Account:
        self._store[account.id] = account
        return account

    def find_by_id(self, account_id: str) -> Optional[Account]:
        return self._store.get(account_id)

    def find_by_owner(self, owner_id: str) -> List[Account]:
        return [a for a in self._store.values() if a.owner_id == owner_id]

    def find_all(self) -> List[Account]:
        return list(self._store.values())

    def delete(self, account_id: str) -> bool:
        if account_id in self._store:
            del self._store[account_id]
            return True
        return False
