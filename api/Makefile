.PHONY: help install run lint format format-check types test check

install:
	uv sync --all-groups

run: 
	uv run fastapi dev app/main.py

lint: 
	uv run ruff check .

format: 
	uv run ruff check . --fix
	uv run ruff format .

format-check:
	uv run ruff format --check .

types: 
	uv run mypy

test:
	uv run python -m unittest discover -s tests

check: lint format-check types test
