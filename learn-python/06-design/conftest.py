"""
conftest.py for 06-design module.
Adds the module root to sys.path so test imports resolve correctly.
"""
import sys
import os

# Add 06-design directory to path
sys.path.insert(0, os.path.dirname(__file__))
# Also add exercises directory
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'exercises'))
