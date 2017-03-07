#!/usr/bin/env python
from setuptools import find_packages, setup

setup(
    name='example_project',
    description='Amazing thing',
    author='Me',
    packages=find_packages(),

    install_requires=[],
    tests_require=['tox', 'pytest', 'pytest-cov', 'coverage'],
)
