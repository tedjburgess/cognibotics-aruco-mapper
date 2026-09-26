.PHONY: help install run lint format format-check types check

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

check: lint format-check types
