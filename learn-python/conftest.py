"""
Root conftest.py for the learn-python repository.

This file ensures pytest can find all test modules regardless of where
you run pytest from. It adds each module's directory to sys.path.
"""
import sys
import os

# Add all module directories to path so imports work from any location
repo_root = os.path.dirname(__file__)
for module_dir in ['01-foundations', '02-core-language', '03-oop',
                   '04-data-structures', '05-real-world', '06-design', '07-advanced']:
    module_path = os.path.join(repo_root, module_dir)
    if module_path not in sys.path:
        sys.path.insert(0, module_path)
