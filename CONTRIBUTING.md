# Contributing Guidelines

Thanks for your interest in improving the Warranty app! Please follow these conventions to keep the project healthy.

## Workflow
1. Create a feature branch off main.
2. Keep commits focused; prefer the format 	ype(scope): short summary (e.g. eat(db): add warranty migrations).
3. Run ./gradlew ktlintCheck testDebugUnitTest assembleDebug before opening a pull request.
4. Provide context in your PR description: motivation, approach, and any follow-up work.

## Code Style
- Kotlin code follows the official style; ktlint enforces formatting.
- Compose UI should be preview-friendly when possible and use Material 3 components.
- Prefer immutable data models and Flow for streams.
- Add concise comments only where logic is non-obvious.

## Testing
- Cover domain use-cases with unit tests.
- For Room, add DAO/migration tests using the in-memory builder or SQLite helper.
- Include regression tests when fixing bugs.

## Documentation
- Update README and docs for user-visible changes.
- Note any new commands or scripts in AGENTS.md.
