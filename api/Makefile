.PHONY: help install run lint format format-check types check

install:
	uv sync --all-groups

run: 
	uv run fastapi dev app/main.py --host 0.0.0.0 --port 8000

lint: 
	uv run ruff check .

format: 
	uv run ruff check . --fix
	uv run ruff format .

format-check:
	uv run ruff format --check .

types: 
	uv run mypy

check: lint format-check types
